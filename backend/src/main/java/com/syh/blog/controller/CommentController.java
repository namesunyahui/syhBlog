package com.syh.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syh.blog.common.Result;
import com.syh.blog.entity.Comment;
import com.syh.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论Controller
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "评论管理")
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "获取评论列表")
    @GetMapping
    public Result<Page<Comment>> list(
            @RequestParam(required = false) Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }

        wrapper.eq(Comment::getIsApproved, true)
               .orderByDesc(Comment::getCreatedAt);

        Page<Comment> commentPage = commentService.page(pageParam, wrapper);
        return Result.success(commentPage);
    }

    @Operation(summary = "提交评论")
    @PostMapping
    public Result<String> submit(@RequestBody Comment comment) {
        comment.setIsApproved(false); // 默认需要审核
        boolean success = commentService.save(comment);
        return success ? Result.success("评论提交成功，等待审核") : Result.error("评论提交失败");
    }
}
