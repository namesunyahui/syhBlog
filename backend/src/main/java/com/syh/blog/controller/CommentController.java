package com.syh.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syh.blog.common.Result;
import com.syh.blog.dto.CommentDTO;
import com.syh.blog.entity.Comment;
import com.syh.blog.entity.User;
import com.syh.blog.service.CommentService;
import com.syh.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private UserService userService;

    @Operation(summary = "获取评论列表")
    @GetMapping
    public Result<Page<CommentDTO>> list(
            @RequestParam(required = false) Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }

        wrapper.orderByDesc(Comment::getCreatedAt);

        Page<Comment> commentPage = commentService.page(pageParam, wrapper);

        // 转换为 DTO 并填充用户信息
        Page<CommentDTO> dtoPage = new Page<>(page, size, commentPage.getTotal());
        List<CommentDTO> dtoList = new ArrayList<>();

        for (Comment comment : commentPage.getRecords()) {
            CommentDTO dto = CommentDTO.fromComment(comment);

            // 如果是登录用户的评论，查询用户信息
            if (comment.getUserId() != null) {
                User user = userService.getById(comment.getUserId());
                if (user != null) {
                    dto.setUsername(user.getUsername());
                    dto.setUserNickname(user.getNickname());
                    dto.setUserAvatar(user.getAvatar());
                    // 更新 displayName
                    dto.setUserDisplayName(user.getUsername(), user.getNickname());
                }
            }
            dtoList.add(dto);
        }

        dtoPage.setRecords(dtoList);
        return Result.success(dtoPage);
    }

    @Operation(summary = "提交评论")
    @PostMapping
    public Result<String> submit(@RequestBody Map<String, Object> params, Authentication authentication) {
        Comment comment = new Comment();

        // 设置文章ID
        Object articleIdObj = params.get("articleId");
        if (articleIdObj instanceof Number) {
            comment.setArticleId(((Number) articleIdObj).longValue());
        }

        // 设置评论内容
        comment.setContent((String) params.get("content"));

        // 判断用户是否登录
        if (authentication != null && authentication.isAuthenticated()) {
            // 登录用户：设置用户ID
            String username = authentication.getName();
            User user = userService.lambdaQuery()
                    .eq(User::getUsername, username)
                    .one();
            if (user != null) {
                comment.setUserId(user.getId());
            }
        } else {
            // 未登录用户：设置昵称
            comment.setNickname((String) params.get("nickname"));
        }

        // 保存评论
        boolean success = commentService.save(comment);
        return success ? Result.success("评论提交成功") : Result.error("评论提交失败");
    }
}
