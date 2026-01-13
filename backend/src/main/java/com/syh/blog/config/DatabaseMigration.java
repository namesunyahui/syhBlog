package com.syh.blog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库自动迁移
 * 在应用启动时自动添加缺失的字段
 *
 * @author sunyahui
 * @since 2025-01-12
 */
@Component
public class DatabaseMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        migrateUsersTable();
    }

    /**
     * 迁移 users 表，添加新字段
     */
    private void migrateUsersTable() {
        try {
            logger.info("开始检查 users 表结构...");

            // 获取数据库连接
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "users", null);

            // 获取已存在的列名
            List<String> existingColumns = new ArrayList<>();
            while (columns.next()) {
                existingColumns.add(columns.getString("COLUMN_NAME").toLowerCase());
            }
            columns.close();
            connection.close();

            logger.info("users 表当前列: {}", existingColumns);

            // 检查并添加 role 字段
            if (!existingColumns.contains("role")) {
                logger.info("添加 role 字段...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ADMIN'");
                jdbcTemplate.execute("COMMENT ON COLUMN users.role IS '角色：SUPER_ADMIN（超级管理员）、ADMIN（管理员）'");
                logger.info("role 字段添加成功");
            } else {
                logger.info("role 字段已存在，跳过");
            }

            // 检查并添加 status 字段
            if (!existingColumns.contains("status")) {
                logger.info("添加 status 字段...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
                jdbcTemplate.execute("COMMENT ON COLUMN users.status IS '状态：ACTIVE（激活）、BANNED（封禁）'");
                logger.info("status 字段添加成功");
            } else {
                logger.info("status 字段已存在，跳过");
            }

            // 检查并添加 last_login_time 字段
            if (!existingColumns.contains("last_login_time")) {
                logger.info("添加 last_login_time 字段...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN last_login_time TIMESTAMP");
                jdbcTemplate.execute("COMMENT ON COLUMN users.last_login_time IS '最后登录时间'");
                logger.info("last_login_time 字段添加成功");
            } else {
                logger.info("last_login_time 字段已存在，跳过");
            }

            // 将现有的 admin 用户设置为超级管理员
            int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = 'admin' AND role IS NULL", Integer.class);

            if (count > 0) {
                logger.info("将 admin 用户设置为超级管理员...");
                jdbcTemplate.execute("UPDATE users SET role = 'SUPER_ADMIN' WHERE username = 'admin'");
                logger.info("admin 用户角色更新成功");
            }

            // 确保所有用户都有 role 和 status 值
            jdbcTemplate.execute("UPDATE users SET role = 'ADMIN' WHERE role IS NULL");
            jdbcTemplate.execute("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL");

            logger.info("========================================");
            logger.info("users 表迁移完成！");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("数据库迁移失败: {}", e.getMessage(), e);
            // 不抛出异常，允许应用继续启动
        }
    }
}
