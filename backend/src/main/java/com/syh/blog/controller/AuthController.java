package com.syh.blog.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.syh.blog.common.Result;
import com.syh.blog.dto.RegisterRequestDTO;
import com.syh.blog.entity.User;
import com.syh.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证Controller
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 验证密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 隐藏密码字段
        user.setPassword(null);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", "mock-token-" + user.getId()); // TODO: 实际项目中应该使用JWT

        return Result.success(data);
    }

    @Operation(summary = "获取管理员信息")
    @GetMapping("/info")
    public Result<User> info() {
        // TODO: 从JWT中获取用户信息
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    // 注册功能已禁用，用户只能由管理员在后台创建
    // @Operation(summary = "用户注册")
    // @PostMapping("/register")
    // public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequestDTO registerDTO) {
    //     try {
    //         // 调用 Service 层注册方法
    //         User user = userService.register(registerDTO);
    //
    //         // 构建返回数据
    //         Map<String, Object> data = new HashMap<>();
    //         data.put("user", user);
    //         data.put("token", "mock-token-" + user.getId()); // TODO: 实际项目中应该使用JWT
    //
    //         return Result.success("注册成功", data);
    //     } catch (RuntimeException e) {
    //         return Result.error(e.getMessage());
    //     }
    // }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
