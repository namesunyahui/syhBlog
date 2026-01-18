package com.syh.blog.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syh.blog.entity.User;
import com.syh.blog.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 用于在应用启动时初始化默认数据
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        // 初始化默认管理员账户
        initDefaultAdmin();
    }

    /**
     * 初始化默认管理员账户
     */
    private void initDefaultAdmin() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, "admin");
        User existingUser = userService.getOne(queryWrapper);

        if (existingUser == null) {
            logger.info("开始初始化默认管理员账户...");

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(BCrypt.hashpw("admin123"));
            admin.setNickname("超级管理员");
            admin.setEmail("admin@syhblog.com");
            admin.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            admin.setRole("SUPER_ADMIN");  // 设置为超级管理员
            admin.setStatus("ACTIVE");      // 激活状态

            userService.save(admin);

            logger.info("========================================");
            logger.info("默认管理员账户创建成功！");
            logger.info("用户名: admin");
            logger.info("密码: admin123");
            logger.info("角色: 超级管理员");
            logger.info("请及时修改默认密码！");
            logger.info("========================================");
        } else {
            logger.info("默认管理员账户已存在，检查角色配置...");

            // 如果角色不是 SUPER_ADMIN，自动更新
            if (!"SUPER_ADMIN".equals(existingUser.getRole())) {
                logger.warn("检测到 admin 用户角色不是超级管理员，正在更新...");
                existingUser.setRole("SUPER_ADMIN");
                userService.updateById(existingUser);
                logger.info("admin 用户角色已更新为 SUPER_ADMIN");
            } else {
                logger.info("admin 用户角色配置正确（SUPER_ADMIN）");
            }
        }
    }
}
