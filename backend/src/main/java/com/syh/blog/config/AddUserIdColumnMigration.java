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
 * 数据库迁移：添加 user_id 字段到 comments 表
 *
 * @author sunyahui
 * @since 2024-01-20
 */
@Component
@Order(1) // 确保在 DataInitializer 之前执行
public class AddUserIdColumnMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AddUserIdColumnMigration.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 检查 user_id 字段是否已存在
            Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'comments' AND column_name = 'user_id'",
                Integer.class
            );

            if (columnExists != null && columnExists > 0) {
                logger.info("user_id 字段已存在于 comments 表，跳过迁移");
                return;
            }

            logger.info("开始迁移：添加 user_id 字段到 comments 表...");

            // 添加 user_id 字段
            jdbcTemplate.execute("ALTER TABLE comments ADD COLUMN user_id BIGINT REFERENCES users(id) ON DELETE SET NULL");

            // 添加索引以提高查询性能
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id)");

            logger.info("========================================");
            logger.info("数据库迁移完成！");
            logger.info("已成功添加 user_id 字段到 comments 表");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("数据库迁移失败", e);
            // 不抛出异常，允许应用继续启动
        }
    }
}
