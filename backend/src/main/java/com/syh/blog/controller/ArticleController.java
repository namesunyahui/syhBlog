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
}
