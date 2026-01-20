package com.syh.blog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 数据库迁移：添加 is_deleted 字段
 *
 * @author sunyahui
 * @since 2024-01-20
 */
@Component
@Order(1) // 确保在 DataInitializer 之前执行
public class AddIsDeletedColumnMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AddIsDeletedColumnMigration.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 检查 is_deleted 字段是否已存在
            Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'is_deleted'",
                Integer.class
            );

            if (columnExists != null && columnExists > 0) {
                logger.info("is_deleted 字段已存在，跳过迁移");
                return;
            }

            logger.info("开始迁移：添加 is_deleted 字段到 users 表...");

            // 添加 is_deleted 字段
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN is_deleted INTEGER DEFAULT 0");

            // 更新现有数据
            jdbcTemplate.execute("UPDATE users SET is_deleted = 0 WHERE is_deleted IS NULL");

            logger.info("========================================");
            logger.info("数据库迁移完成！");
            logger.info("已成功添加 is_deleted 字段到 users 表");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("数据库迁移失败", e);
            // 不抛出异常，允许应用继续启动
        }
    }
}
