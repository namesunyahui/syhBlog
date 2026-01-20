package com.syh.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syh.blog.dto.ArchiveDTO;
import com.syh.blog.dto.ArchiveVO;
import com.syh.blog.dto.MonthArchive;
import com.syh.blog.entity.Article;
import com.syh.blog.entity.ArticleTag;
import com.syh.blog.entity.Category;
import com.syh.blog.entity.Tag;
import com.syh.blog.mapper.ArticleMapper;
import com.syh.blog.mapper.ArticleTagMapper;
import com.syh.blog.service.ArticleService;
import com.syh.blog.service.CategoryService;
import com.syh.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章Service实现
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired(required = false)
    private CategoryService categoryService;

    @Autowired(required = false)
    private TagService tagService;

    @Autowired(required = false)
    private ArticleTagMapper articleTagMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_VIEW_COUNT_KEY = "article:view:";
    private static final String ARTICLE_VIEW_COUNT_SYNC_KEY = "article:view:sync:";

    // 缓存key
    private static final String ARTICLE_LIST_CACHE_KEY = "article:list:";
    private static final String CATEGORY_CACHE_KEY = "category:all";
    private static final String TAG_CACHE_KEY = "tag:all";

    /**
     * 检查Redis是否可用
     */
    private boolean isRedisAvailable() {
        return redisTemplate != null;
    }

    @Override
    public IPage<Article> getPublishedArticles(Page<Article> page, Long categoryId, Long tagId) {
        // 尝试从缓存获取
        if (isRedisAvailable()) {
            try {
                String cacheKey = ARTICLE_LIST_CACHE_KEY + page.getCurrent() + ":" + page.getSize() + ":" + (categoryId != null ? categoryId : "null") + ":" + (tagId != null ? tagId : "null");
                IPage<Article> cachedPage = (IPage<Article>) redisTemplate.opsForValue().get(cacheKey);
                if (cachedPage != null) {
                    // 缓存中的数据已经排除了content字段
                    return cachedPage;
                }
            } catch (Exception e) {
                // Redis访问失败，降级到数据库查询
                System.err.println("Redis缓存读取失败，降级到数据库查询: " + e.getMessage());
            }
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true);

        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        // 排除content字段，在数据库查询时就过滤掉，减少内存占用和网络传输
        wrapper.select(Article.class, info -> !info.getProperty().equals("content"));

        // 按创建时间降序排序（最新的文章在前）
        wrapper.orderByDesc(true, Article::getCreatedAt);

        IPage<Article> articlePage = page(page, wrapper);

        // 批量填充分类和标签信息（避免N+1查询）
        batchFillRelatedInfo(articlePage.getRecords());

        // 缓存结果（5分钟）
        if (isRedisAvailable()) {
            try {
                String cacheKey = ARTICLE_LIST_CACHE_KEY + page.getCurrent() + ":" + page.getSize() + ":" + (categoryId != null ? categoryId : "null") + ":" + (tagId != null ? tagId : "null");
                redisTemplate.opsForValue().set(cacheKey, articlePage, java.time.Duration.ofMinutes(5));
            } catch (Exception e) {
                // 缓存写入失败，不影响主流程
                System.err.println("Redis缓存写入失败: " + e.getMessage());
            }
        }

        return articlePage;
    }

    @Override
    public IPage<Article> getArticlesByTags(Page<Article> page, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new Page<>();
        }

        // 使用自定义查询方法
        IPage<Article> articlePage = this.baseMapper.selectArticlesByTags(page, tagIds, tagIds.size());

        // 批量填充分类和标签信息
        batchFillRelatedInfo(articlePage.getRecords());

        return articlePage;
    }

    @Override
    public Article getArticleDetail(Long id) {
        Article article = getById(id);
        if (article != null) {
            fillRelatedInfo(article);
        }
        return article;
    }

    @Override
    public boolean incrementViewCount(Long id) {
        // 直接更新数据库，简单可靠
        boolean success = lambdaUpdate()
                .eq(Article::getId, id)
                .setSql("view_count = view_count + 1")
                .update();

        if (!success) {
            System.err.println("增加浏览量失败: 文章ID " + id + " 不存在");
        } else {
            // 清除文章列表缓存，确保首页能显示最新的阅读量
            clearArticleListCache();
        }

        return success;
    }

    /**
     * 清除所有文章列表缓存
     */
    private void clearArticleListCache() {
        if (isRedisAvailable()) {
            try {
                Set<String> keys = redisTemplate.keys(ARTICLE_LIST_CACHE_KEY + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    System.out.println("已清除 " + keys.size() + " 个文章列表缓存");
                }
            } catch (Exception e) {
                System.err.println("清除缓存失败: " + e.getMessage());
            }
        }
    }

    @Override
    public IPage<Article> searchArticles(Page<Article> page, String keyword) {
        return searchArticles(page, keyword, null, null);
    }

    @Override
    public IPage<Article> searchArticles(Page<Article> page, String keyword, Long categoryId, Long tagId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true);

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                          .or()
                          .like(Article::getContent, keyword)
                          .or()
                          .like(Article::getSummary, keyword));
        }

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        // 标签筛选（需要通过 article_tags 关联表查询）
        // 如果指定了标签，需要通过子查询或自定义 SQL 实现
        // 这里使用简化的方式：先查询文章，再通过标签关联过滤
        // 注意：这种方式在大数据量时效率较低，建议使用自定义 SQL 或使用 MyBatis-Plus 的关联查询

        // 排除content字段，在数据库查询时就过滤掉（虽然搜索时需要用content，但返回结果不需要）
        wrapper.select(Article.class, info -> !info.getProperty().equals("content"))
               .orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = page(page, wrapper);

        // 如果指定了标签，需要过滤结果
        if (tagId != null && articleTagMapper != null) {
            // 查询包含该标签的所有文章ID
            LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(ArticleTag::getTagId, tagId);
            List<ArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            Set<Long> articleIdsWithTag = articleTags.stream()
                    .map(ArticleTag::getArticleId)
                    .collect(Collectors.toSet());

            // 过滤出包含该标签的文章
            List<Article> filteredRecords = articlePage.getRecords().stream()
                    .filter(article -> articleIdsWithTag.contains(article.getId()))
                    .collect(Collectors.toList());

            // 更新分页结果
            articlePage.setRecords(filteredRecords);
            articlePage.setTotal(filteredRecords.size());
        }

        batchFillRelatedInfo(articlePage.getRecords());
        return articlePage;
    }

    @Override
    public IPage<Article> getArchiveList(Page<Article> page) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true)
               // 排除content字段，在数据库查询时就过滤掉
               .select(Article.class, info -> !info.getColumn().equals("content"))
               .orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = page(page, wrapper);
        batchFillRelatedInfo(articlePage.getRecords());
        return articlePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishArticle(Long id) {
        return lambdaUpdate()
                .eq(Article::getId, id)
                .set(Article::getIsPublished, true)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawArticle(Long id) {
        return lambdaUpdate()
                .eq(Article::getId, id)
                .set(Article::getIsPublished, false)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setTop(Long id, boolean isTop) {
        // 数据库表中没有 is_top 字段，此方法暂时不做任何操作
        // 如需置顶功能，需要修改数据库表添加 is_top 字段
        return true;
    }

    // ============ 管理后台方法实现 ============

    @Override
    public IPage<Article> getAllArticles(Page<Article> page, String title, Long categoryId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 排除content字段，在数据库查询时就过滤掉（管理后台列表也不需要完整内容）
        wrapper.select(Article.class, info -> !info.getColumn().equals("content"));

        // 按标题过滤
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Article::getTitle, title.trim());
        }

        // 按分类过滤
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = page(page, wrapper);
        batchFillRelatedInfo(articlePage.getRecords());

        return articlePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Article createArticle(Article article) {
        // 设置默认值
        if (article.getIsPublished() == null) {
            article.setIsPublished(false);
        }
        if (article.getViewCount() == null) {
            article.setViewCount(0L);
        }

        // 保存文章
        save(article);

        // 保存标签关联
        saveArticleTags(article.getId(), article.getTags());

        // 清除文章列表缓存
        clearArticleListCache();

        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Article updateArticle(Article article) {
        Article existing = getById(article.getId());
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }

        // 更新文章
        updateById(article);

        // 更新标签关联（先删除旧的，再添加新的）
        if (articleTagMapper != null) {
            // 删除旧的标签关联
            LambdaQueryWrapper<ArticleTag> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ArticleTag::getArticleId, article.getId());
            articleTagMapper.delete(deleteWrapper);

            // 保存新的标签关联
            saveArticleTags(article.getId(), article.getTags());
        }

        // 清除文章列表缓存
        clearArticleListCache();

        return getById(article.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteArticle(Long id) {
        boolean result = removeById(id);
        if (result) {
            // 清除文章列表缓存
            clearArticleListCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long id, Boolean publish) {
        // 先更新数据库
        boolean updated = lambdaUpdate()
            .eq(Article::getId, id)
            .set(Article::getIsPublished, publish)
            .update();

        if (!updated) {
            throw new RuntimeException("文章不存在或更新失败");
        }

        // 清除缓存
        clearArticleListCache();
    }

    /**
     * 批量填充分类和标签信息（避免N+1查询）
     */
    private void batchFillRelatedInfo(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        // 收集所有分类ID
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 批量查询分类（一次查询代替N次）
        Map<Long, Category> categoryMap;
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryService.listByIds(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
        } else {
            categoryMap = Map.of();
        }

        // 填充分类信息
        articles.forEach(article -> {
            if (article.getCategoryId() != null) {
                article.setCategory(categoryMap.get(article.getCategoryId()));
            }
        });

        // 批量填充标签信息
        if (articleTagMapper != null && tagService != null) {
            // 收集所有文章ID
            List<Long> articleIds = articles.stream()
                    .map(Article::getId)
                    .collect(Collectors.toList());

            if (!articleIds.isEmpty()) {
                // 批量查询文章标签关联
                LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(ArticleTag::getArticleId, articleIds);
                List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);

                // 收集所有标签ID
                Set<Long> tagIds = articleTags.stream()
                        .map(ArticleTag::getTagId)
                        .collect(Collectors.toSet());

                // 批量查询标签
                final Map<Long, Tag> tagMap;
                if (!tagIds.isEmpty()) {
                    List<Tag> tags = tagService.listByIds(tagIds);
                    tagMap = tags.stream()
                            .collect(Collectors.toMap(Tag::getId, t -> t));
                } else {
                    tagMap = Map.of();
                }

                // 按文章ID分组标签
                Map<Long, List<Tag>> articleTagMap = articleTags.stream()
                        .collect(Collectors.groupingBy(
                                ArticleTag::getArticleId,
                                Collectors.mapping(at -> tagMap.get(at.getTagId()), Collectors.toList())
                        ));

                // 填充分类信息
                articles.forEach(article -> {
                    List<Tag> tags = articleTagMap.get(article.getId());
                    if (tags != null && !tags.isEmpty()) {
                        article.setTags(tags);
                    }
                });
            }
        }
    }

    /**
     * 填充分类和标签信息（单个文章，保留用于详情页）
     */
    private void fillRelatedInfo(Article article) {
        if (article.getCategoryId() != null && categoryService != null) {
            Category category = categoryService.getById(article.getCategoryId());
            article.setCategory(category);
        }

        // 填充标签信息
        if (articleTagMapper != null && tagService != null) {
            LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ArticleTag::getArticleId, article.getId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);
            
            if (!CollectionUtils.isEmpty(articleTags)) {
                List<Long> tagIds = articleTags.stream()
                        .map(ArticleTag::getTagId)
                        .collect(Collectors.toList());
                List<Tag> tags = tagService.listByIds(tagIds);
                article.setTags(tags);
            }
        }
    }

    @Override
    public List<Article> getHotArticles(Integer limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true)
               .select(Article.class, info -> !info.getProperty().equals("content"))
               .orderByDesc(Article::getViewCount)
               .last("LIMIT " + (limit != null && limit > 0 ? limit : 5));

        List<Article> articles = list(wrapper);
        batchFillRelatedInfo(articles);
        return articles;
    }

    @Override
    public List<Article> getRelatedArticles(Long articleId, Integer limit) {
        // 获取当前文章信息
        Article currentArticle = getById(articleId);
        if (currentArticle == null) {
            return List.of();
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true)
               .ne(Article::getId, articleId)  // 排除当前文章
               .select(Article.class, info -> !info.getProperty().equals("content"));

        // 优先找同分类的文章
        if (currentArticle.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, currentArticle.getCategoryId());
        }

        wrapper.orderByDesc(Article::getViewCount)
               .last("LIMIT " + (limit != null && limit > 0 ? limit : 5));

        List<Article> articles = list(wrapper);
        batchFillRelatedInfo(articles);

        // 如果同分类文章不足，尝试获取同标签的文章
        if (articles.size() < (limit != null ? limit : 5)) {
            articles.clear();
            LambdaQueryWrapper<Article> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(Article::getIsPublished, true)
                      .ne(Article::getId, articleId)
                      .select(Article.class, info -> !info.getProperty().equals("content"));

            // 如果有分类，排除同分类的文章（避免重复）
            if (currentArticle.getCategoryId() != null) {
                tagWrapper.ne(Article::getCategoryId, currentArticle.getCategoryId());
            }

            tagWrapper.orderByDesc(Article::getViewCount)
                      .last("LIMIT " + (limit != null && limit > 0 ? limit : 5));

            articles = list(tagWrapper);
            batchFillRelatedInfo(articles);
        }

        return articles;
    }

    /**
     * 保存文章标签关联
     */
    private void saveArticleTags(Long articleId, List<Tag> tags) {
        if (articleTagMapper == null || CollectionUtils.isEmpty(tags)) {
            return;
        }

        for (Tag tag : tags) {
            if (tag != null && tag.getId() != null) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tag.getId());
                articleTagMapper.insert(articleTag);
            }
        }
    }

    @Override
    public ArchiveVO getGroupedArchive() {
        // 1. 查询所有已发布文章（排除content字段）
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true)
               .select(Article.class, info -> !info.getColumn().equals("content"))
               .orderByDesc(Article::getCreatedAt);

        List<Article> allArticles = list(wrapper);

        // 2. 批量填充分类和标签信息
        batchFillRelatedInfo(allArticles);

        // 3. 按年月分组
        Map<Integer, Map<Integer, List<Article>>> grouped = allArticles.stream()
            .collect(Collectors.groupingBy(
                article -> article.getCreatedAt().getYear(),
                LinkedHashMap::new,
                Collectors.groupingBy(
                    article -> article.getCreatedAt().getMonthValue(),
                    LinkedHashMap::new,
                    Collectors.toList()
                )
            ));

        // 4. 构建归档VO
        ArchiveVO archiveVO = new ArchiveVO();
        archiveVO.setTotalCount(allArticles.size());

        List<ArchiveDTO> archives = grouped.entrySet().stream()
            .map(yearEntry -> {
                ArchiveDTO archiveDTO = new ArchiveDTO();
                archiveDTO.setYear(yearEntry.getKey());

                List<MonthArchive> months = yearEntry.getValue().entrySet().stream()
                    .map(monthEntry -> {
                        MonthArchive monthArchive = new MonthArchive();
                        monthArchive.setMonth(monthEntry.getKey());
                        monthArchive.setCount(monthEntry.getValue().size());
                        monthArchive.setArticles(monthEntry.getValue());
                        return monthArchive;
                    })
                    .collect(Collectors.toList());

                archiveDTO.setMonths(months);
                return archiveDTO;
            })
            .collect(Collectors.toList());

        archiveVO.setArchives(archives);

        // 5. 添加统计信息
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalYears", archives.size());
        stats.put("totalMonths", archives.stream()
            .mapToInt(a -> a.getMonths().size())
            .sum());
        archiveVO.setStatistics(stats);

        return archiveVO;
    }

    @Override
    public Long getTotalViewCount() {
        // 查询所有文章的浏览量总和
        List<Article> articles = list();
        return articles.stream()
            .mapToLong(Article::getViewCount)
            .sum();
    }
}
