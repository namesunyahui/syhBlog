package com.syh.blog.security;

import com.syh.blog.entity.User;
import com.syh.blog.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取token
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀

            // 简化的token验证：检查token格式
            if (token.startsWith("mock-token-")) {
                try {
                    // 从token中提取用户ID
                    String userIdStr = token.substring("mock-token-".length());
                    Long userId = Long.parseLong(userIdStr);

                    // 从数据库查询用户信息，获取实际角色
                    User user = userService.getById(userId);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        // 根据用户的实际角色设置权限
                        String role = user.getRole(); // SUPER_ADMIN 或 ADMIN
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(authority)
                        );

                        // 设置到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (NumberFormatException e) {
                    // token格式不正确，忽略
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
