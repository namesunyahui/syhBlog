package com.syh.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syh.blog.common.Result;
import com.syh.blog.dto.UserDTO;
import com.syh.blog.entity.User;
import com.syh.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理Controller
 *
 * @author sunyahui
 * @since 2025-01-12
 */
@RestController
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页获取用户列表（仅超级管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        IPage<UserDTO> userPage = userService.getUserList(page, keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("list", userPage.getRecords());
        data.put("total", userPage.getTotal());
        data.put("current", userPage.getCurrent());
        data.put("size", userPage.getSize());
        data.put("pages", userPage.getPages());

        return Result.success(data);
    }

    /**
     * 获取用户详情（仅超级管理员）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserDTOById(id);
        return Result.success(userDTO);
    }

    /**
     * 更新用户角色（仅超级管理员）
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        userService.updateUserRole(id, role);
        return Result.success();
    }

    /**
     * 更新用户状态（仅超级管理员）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    /**
     * 删除用户（仅超级管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return Result.success();
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(exists);
    }
}
