package com.syh.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.syh.blog.dto.RegisterRequestDTO;
import com.syh.blog.dto.UserDTO;
import com.syh.blog.entity.User;

/**
 * 用户Service
 *
 * @author sunyahui
 * @since 2024-01-01
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 更新密码
     */
    boolean updatePassword(Long userId, String newPassword);

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求DTO
     * @return 注册成功的用户
     */
    User register(RegisterRequestDTO registerDTO);

    /**
     * 分页查询用户列表
     *
     * @param page    分页参数
     * @param keyword 搜索关键词（用户名、昵称、邮箱）
     * @return 用户分页数据
     */
    IPage<UserDTO> getUserList(Page<User> page, String keyword);

    /**
     * 根据ID查询用户DTO
     *
     * @param userId 用户ID
     * @return 用户DTO
     */
    UserDTO getUserDTOById(Long userId);

    /**
     * 更新用户角色
     *
     * @param userId 用户ID
     * @param role   新角色
     * @return 是否成功
     */
    boolean updateUserRole(Long userId, String role);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, String status);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUserById(Long userId);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 创建用户（管理员功能）
     *
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @param email    邮箱
     * @param role     角色
     * @return 创建的用户
     */
    User createUser(String username, String password, String nickname, String email, String role);
}
