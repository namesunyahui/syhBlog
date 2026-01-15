package com.syh.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

        wrapper.orderByDesc(Article::getCreatedAt);

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
    public Article getArticleDetail(Long id) {
        Article article = getById(id);
        if (article != null) {
            fillRelatedInfo(article);
        }
        return article;
    }

    @Override
    public boolean incrementViewCount(Long id) {
        if (isRedisAvailable()) {
            try {
                // 使用Redis计数器
                String key = ARTICLE_VIEW_COUNT_KEY + id;
                redisTemplate.opsForValue().increment(key);

                // 同步到数据库的标志
                String syncKey = ARTICLE_VIEW_COUNT_SYNC_KEY + id;
                redisTemplate.opsForValue().set(syncKey, "1");
            } catch (Exception e) {
                // Redis写入失败，直接更新数据库
                System.err.println("Redis写入失败，直接更新数据库: " + e.getMessage());
                lambdaUpdate()
                        .eq(Article::getId, id)
                        .setSql("view_count = view_count + 1")
                        .update();
            }
        } else {
            // Redis不可用，直接更新数据库
            lambdaUpdate()
                    .eq(Article::getId, id)
                    .setSql("view_count = view_count + 1")
                    .update();
        }
        return true;
    }

    @Override
    public IPage<Article> searchArticles(Page<Article> page, String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true)
               .and(w -> w.like(Article::getTitle, keyword)
                          .or()
                          .like(Article::getContent, keyword)
                          .or()
                          .like(Article::getSummary, keyword))
               // 排除content字段，在数据库查询时就过滤掉（虽然搜索时需要用content，但返回结果不需要）
               .select(Article.class, info -> !info.getProperty().equals("content"))
               .orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = page(page, wrapper);
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
    public IPage<Article> getAllArticles(Page<Article> page) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 排除content字段，在数据库查询时就过滤掉（管理后台列表也不需要完整内容）
        wrapper.select(Article.class, info -> !info.getColumn().equals("content"))
               .orderByDesc(Article::getCreatedAt);

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

        return getById(article.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteArticle(Long id) {
        return removeById(id);
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
        if (isRedisAvailable()) {
            try {
                // 清除所有文章列表缓存（因为缓存键包含 page:size:categoryId:tagId）
                Set<String> keys = redisTemplate.keys(ARTICLE_LIST_CACHE_KEY + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                System.err.println("清除缓存失败: " + e.getMessage());
            }
        }
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
}
