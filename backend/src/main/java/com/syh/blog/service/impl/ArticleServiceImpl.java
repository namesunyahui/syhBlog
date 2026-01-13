package com.syh.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syh.blog.entity.Article;
import com.syh.blog.entity.Category;
import com.syh.blog.entity.Tag;
import com.syh.blog.mapper.ArticleMapper;
import com.syh.blog.service.ArticleService;
import com.syh.blog.service.CategoryService;
import com.syh.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_VIEW_COUNT_KEY = "article:view:";
    private static final String ARTICLE_VIEW_COUNT_SYNC_KEY = "article:view:sync:";

    // 缓存key
    private static final String ARTICLE_LIST_CACHE_KEY = "article:list:";
    private static final String CATEGORY_CACHE_KEY = "category:all";
    private static final String TAG_CACHE_KEY = "tag:all";

    @Override
    public IPage<Article> getPublishedArticles(Page<Article> page, Long categoryId, Long tagId) {
        // 尝试从缓存获取
        String cacheKey = ARTICLE_LIST_CACHE_KEY + page.getCurrent() + ":" + page.getSize() + ":" + (categoryId != null ? categoryId : "null") + ":" + (tagId != null ? tagId : "null");
        IPage<Article> cachedPage = (IPage<Article>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPage != null) {
            return cachedPage;
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true);

        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = page(page, wrapper);

        // 批量填充分类和标签信息（避免N+1查询）
        batchFillRelatedInfo(articlePage.getRecords());

        // 缓存结果（5分钟）
        redisTemplate.opsForValue().set(cacheKey, articlePage, java.time.Duration.ofMinutes(5));

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
        // 使用Redis计数器
        String key = ARTICLE_VIEW_COUNT_KEY + id;
        redisTemplate.opsForValue().increment(key);

        // 同步到数据库的标志
        String syncKey = ARTICLE_VIEW_COUNT_SYNC_KEY + id;
        redisTemplate.opsForValue().set(syncKey, "1");

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
               .orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = page(page, wrapper);
        batchFillRelatedInfo(articlePage.getRecords());
        return articlePage;
    }

    @Override
    public IPage<Article> getArchiveList(Page<Article> page) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, true)
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

        save(article);
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Article updateArticle(Article article) {
        Article existing = getById(article.getId());
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }

        updateById(article);
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
        lambdaUpdate()
            .eq(Article::getId, id)
            .set(Article::getIsPublished, publish)
            .update();
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

        // TODO: 批量填充标签信息（需要article_tags关联表查询）
    }

    /**
     * 填充分类和标签信息（单个文章，保留用于详情页）
     */
    private void fillRelatedInfo(Article article) {
        if (article.getCategoryId() != null) {
            Category category = categoryService.getById(article.getCategoryId());
            article.setCategory(category);
        }

        // TODO: 填充标签信息（需要关联查询）
    }
}
