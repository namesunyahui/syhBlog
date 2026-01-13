package com.syh.blog.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syh.blog.dto.RegisterRequestDTO;
import com.syh.blog.dto.UserDTO;
import com.syh.blog.entity.User;
import com.syh.blog.mapper.UserMapper;
import com.syh.blog.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }

    @Override
    public boolean updatePassword(Long userId, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword);
        return lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getPassword, hashedPassword)
                .update();
    }

    @Override
    public User register(RegisterRequestDTO registerDTO) {
        // 检查用户名是否已存在
        if (existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setEmail(registerDTO.getEmail());

        // 默认为 ADMIN 角色，激活状态
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");

        save(user);

        // 清除密码后返回
        user.setPassword(null);
        return user;
    }

    @Override
    public IPage<UserDTO> getUserList(Page<User> page, String keyword) {
        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = keyword.trim();
            String finalKeyword = keyword;
            userPage = lambdaQuery()
                    .and(wrapper -> wrapper
                            .like(User::getUsername, finalKeyword)
                            .or()
                            .like(User::getNickname, finalKeyword)
                            .or()
                            .like(User::getEmail, finalKeyword)
                    )
                    .orderByDesc(User::getCreatedAt)
                    .page(page);
        } else {
            userPage = lambdaQuery()
                    .orderByDesc(User::getCreatedAt)
                    .page(page);
        }

        // 转换为 UserDTO
        return userPage.convert(this::convertToDTO);
    }

    @Override
    public UserDTO getUserDTOById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToDTO(user);
    }

    @Override
    public boolean updateUserRole(Long userId, String role) {
        if (!"SUPER_ADMIN".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("无效的角色");
        }
        return lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getRole, role)
                .update();
    }

    @Override
    public boolean updateUserStatus(Long userId, String status) {
        if (!"ACTIVE".equals(status) && !"BANNED".equals(status)) {
            throw new RuntimeException("无效的状态");
        }
        return lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getStatus, status)
                .update();
    }

    @Override
    public boolean deleteUserById(Long userId) {
        return removeById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .count() > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return lambdaQuery()
                .eq(User::getEmail, email)
                .count() > 0;
    }

    /**
     * 将 User 实体转换为 UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        // UserDTO 不包含 password 字段，自动忽略
        return dto;
    }
}
