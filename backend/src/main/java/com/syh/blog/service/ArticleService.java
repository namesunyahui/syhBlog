package com.syh.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.syh.blog.dto.ArchiveVO;
import com.syh.blog.entity.Article;

import java.util.List;

/**
 * 文章Service
 *
 * @author sunyahui
 * @since 2024-01-01
 */
public interface ArticleService extends IService<Article> {

    /**
     * 分页查询已发布的文章
     */
    IPage<Article> getPublishedArticles(Page<Article> page, Long categoryId, Long tagId);

    /**
     * 根据多个标签查询文章（文章必须同时包含所有指定标签）
     */
    IPage<Article> getArticlesByTags(Page<Article> page, List<Long> tagIds);

    /**
     * 获取文章详情（包含分类和标签）
     */
    Article getArticleDetail(Long id);

    /**
     * 增加浏览量
     */
    boolean incrementViewCount(Long id);

    /**
     * 搜索文章
     */
    IPage<Article> searchArticles(Page<Article> page, String keyword);

    /**
     * 获取归档文章
     */
    IPage<Article> getArchiveList(Page<Article> page);

    /**
     * 发布文章
     */
    boolean publishArticle(Long id);

    /**
     * 撤回文章
     */
    boolean withdrawArticle(Long id);

    /**
     * 设置置顶
     */
    boolean setTop(Long id, boolean isTop);

    // ============ 管理后台方法 ============

    /**
     * 获取所有文章（包括草稿）
     */
    IPage<Article> getAllArticles(Page<Article> page, String title, Long categoryId);

    /**
     * 创建文章
     */
    Article createArticle(Article article);

    /**
     * 更新文章
     */
    Article updateArticle(Article article);

    /**
     * 删除文章
     */
    boolean deleteArticle(Long id);

    /**
     * 发布或撤回文章
     */
    void publishArticle(Long id, Boolean publish);

    /**
     * 获取热门文章（按浏览量排序）
     */
    List<Article> getHotArticles(Integer limit);

    /**
     * 获取相关文章（同分类或同标签的文章，排除当前文章）
     */
    List<Article> getRelatedArticles(Long articleId, Integer limit);

    /**
     * 获取按年月分组的归档数据
     */
    ArchiveVO getGroupedArchive();

    /**
     * 获取总浏览量
     */
    Long getTotalViewCount();
}

