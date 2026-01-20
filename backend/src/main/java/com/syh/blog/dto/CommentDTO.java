package com.syh.blog.dto;

import com.syh.blog.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论DTO（用于返回评论列表，包含用户信息）
 *
 * @author sunyahui
 * @since 2024-01-20
 */
@Data
public class CommentDTO {

    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private String nickname;
    private String email;
    private String ipAddress;
    private LocalDateTime createdAt;

    // 用户信息（登录用户的评论）
    private String username;
    private String userNickname;
    private String userAvatar;

    /**
     * 获取显示名称（优先使用登录用户的昵称，其次用户名，最后匿名昵称）
     */
    private String displayName;

    /**
     * 从 Comment 实体转换为 DTO
     */
    public static CommentDTO fromComment(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setArticleId(comment.getArticleId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setNickname(comment.getNickname());
        dto.setEmail(comment.getEmail());
        dto.setIpAddress(comment.getIpAddress());
        dto.setCreatedAt(comment.getCreatedAt());

        // 初始化 displayName
        if (comment.getNickname() != null && !comment.getNickname().isEmpty()) {
            dto.setDisplayName(comment.getNickname());
        } else {
            dto.setDisplayName("匿名用户");
        }

        return dto;
    }

    /**
     * 设置用户信息后，更新 displayName
     */
    public void setUserDisplayName(String username, String userNickname) {
        this.username = username;
        this.userNickname = userNickname;

        // 更新 displayName：优先使用登录用户的昵称，其次用户名，最后匿名昵称
        if (userNickname != null && !userNickname.isEmpty()) {
            this.displayName = userNickname;
        } else if (username != null && !username.isEmpty()) {
            this.displayName = username;
        }
    }

    /**
     * 获取显示名称（已废弃，直接使用 displayName 字段）
     */
    @Deprecated
    public String getDisplayNameValue() {
        return displayName;
    }
}
