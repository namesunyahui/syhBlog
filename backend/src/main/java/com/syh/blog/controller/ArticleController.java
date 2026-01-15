package com.syh.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syh.blog.common.Result;
import com.syh.blog.entity.Article;
import com.syh.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文章Controller
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Tag(name = "文章管理")
@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Operation(summary = "分页查询文章")
    @GetMapping
    public Result<IPage<Article>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> articlePage = articleService.getPublishedArticles(pageParam, categoryId, tagId);
        return Result.success(articlePage);
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        Article article = articleService.getArticleDetail(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        return Result.success(article);
    }

    @Operation(summary = "增加浏览量")
    @PostMapping("/{id}/view")
    public Result<Void> addView(@PathVariable Long id) {
        articleService.incrementViewCount(id);
        return Result.success();
    }

    @Operation(summary = "搜索文章")
    @GetMapping("/search")
    public Result<IPage<Article>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> articlePage = articleService.searchArticles(pageParam, keyword);
        return Result.success(articlePage);
    }

    @Operation(summary = "获取归档文章")
    @GetMapping("/archive")
    public Result<IPage<Article>> archive(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> articlePage = articleService.getArchiveList(pageParam);
        return Result.success(articlePage);
    }

    // ==================== 管理后台接口 ====================

    @Operation(summary = "获取所有文章（包括草稿）")
    @GetMapping("/all")
    public Result<IPage<Article>> getAllArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> articlePage = articleService.getAllArticles(pageParam);
        return Result.success(articlePage);
    }

    @Operation(summary = "创建文章")
    @PostMapping
    public Result<Article> createArticle(@RequestBody Article article) {
        Article saved = articleService.createArticle(article);
        return Result.success(saved);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        Article updated = articleService.updateArticle(article);
        return Result.success(updated);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "发布/撤回文章")
    @PutMapping("/{id}/publish")
    public Result<Void> publishArticle(@PathVariable Long id, @RequestBody PublishRequest request) {
        articleService.publishArticle(id, request.getPublish());
        return Result.success();
    }

    /**
     * 发布请求DTO
     */
    public static class PublishRequest {
        private Boolean publish;

        public Boolean getPublish() {
            return publish;
        }

        public void setPublish(Boolean publish) {
            this.publish = publish;
        }
    }
}
