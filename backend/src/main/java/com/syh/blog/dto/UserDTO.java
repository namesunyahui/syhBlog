package com.syh.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应 DTO
 *
 * @author sunyahui
 * @since 2025-01-12
 */
@Data
public class UserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色：SUPER_ADMIN（超级管理员）、ADMIN（管理员）
     */
    private String role;

    /**
     * 状态：ACTIVE（激活）、BANNED（封禁）
     */
    private String status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 角色描述
     */
    public String getRoleDescription() {
        if ("SUPER_ADMIN".equals(role)) {
            return "超级管理员";
        } else if ("ADMIN".equals(role)) {
            return "管理员";
        }
        return "未知";
    }

    /**
     * 状态描述
     */
    public String getStatusDescription() {
        if ("ACTIVE".equals(status)) {
            return "激活";
        } else if ("BANNED".equals(status)) {
            return "封禁";
        }
        return "未知";
    }
}
