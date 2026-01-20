package com.syh.blog.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syh.blog.entity.User;
import com.syh.blog.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 重置管理员密码
 *
 * @author sunyahui
 * @since 2024-01-20
 */
@Component
@Order(2) // 在 AddIsDeletedColumnMigration 之后，DataInitializer 之前执行
public class ResetAdminPasswordMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ResetAdminPasswordMigration.class);

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, "admin");
            User admin = userService.getOne(queryWrapper);

            if (admin != null) {
                logger.info("正在重置 admin 用户密码...");

                // 重置密码为 admin123
                String newPassword = BCrypt.hashpw("admin123");
                admin.setPassword(newPassword);
                userService.updateById(admin);

                logger.info("========================================");
                logger.info("admin 密码已重置为: admin123");
                logger.info("========================================");
            } else {
                logger.warn("未找到 admin 用户，跳过密码重置");
            }
        } catch (Exception e) {
            logger.error("重置密码失败", e);
        }
    }
}
