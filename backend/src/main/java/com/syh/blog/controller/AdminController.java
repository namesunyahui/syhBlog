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
 * 管理后台Controller
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Tag(name = "管理后台")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ArticleService articleService;

    @Operation(summary = "获取所有文章（包括草稿）")
    @GetMapping("/articles")
    public Result<IPage<Article>> getAllArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> articlePage = articleService.getAllArticles(pageParam);
        return Result.success(articlePage);
    }

    @Operation(summary = "创建文章")
    @PostMapping("/articles")
    public Result<Article> createArticle(@RequestBody Article article) {
        Article saved = articleService.createArticle(article);
        return Result.success(saved);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/articles/{id}")
    public Result<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        Article updated = articleService.updateArticle(article);
        return Result.success(updated);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "发布/撤回文章")
    @PutMapping("/articles/{id}/publish")
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
