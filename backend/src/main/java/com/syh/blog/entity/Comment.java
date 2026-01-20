package com.syh.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Data
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID（登录用户）
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 昵称（未登录用户）
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
