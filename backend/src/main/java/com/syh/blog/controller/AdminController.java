package com.syh.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syh.blog.common.Result;
import com.syh.blog.entity.Comment;
import com.syh.blog.service.ArticleService;
import com.syh.blog.service.CommentService;
import com.syh.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private CommentService commentService;

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 文章总数
        long articleCount = articleService.count();
        stats.put("articleCount", articleCount);

        // 总浏览量
        Long viewCount = articleService.getTotalViewCount();
        stats.put("viewCount", viewCount != null ? viewCount : 0);

        // 评论总数
        long commentCount = commentService.count();
        stats.put("commentCount", commentCount);

        // 分类数量
        long categoryCount = categoryService.count();
        stats.put("categoryCount", categoryCount);

        return Result.success(stats);
    }

    // ==================== 评论管理接口 ====================

    @Operation(summary = "获取评论列表（管理员）")
    @GetMapping("/comments")
    public Result<Page<Comment>> getComments(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String nickname,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            wrapper.like(Comment::getNickname, nickname);
        }

        wrapper.orderByDesc(Comment::getCreatedAt);

        Page<Comment> commentPage = commentService.page(pageParam, wrapper);
        return Result.success(commentPage);
    }

    @Operation(summary = "删除评论（管理员）")
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        boolean success = commentService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @Operation(summary = "批量删除评论（管理员）")
    @DeleteMapping("/comments/batch")
    public Result<Void> batchDeleteComments(@RequestBody java.util.List<Long> ids) {
        boolean success = commentService.removeByIds(ids);
        return success ? Result.success() : Result.error("批量删除失败");
    }
}
