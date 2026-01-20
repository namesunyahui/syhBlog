package com.syh.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

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
     * 是否删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
