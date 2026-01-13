-- 创建数据库
-- CREATE DATABASE syh_blog;
-- \c syh_blog;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文章表
CREATE TABLE IF NOT EXISTS articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    summary VARCHAR(500),
    cover_image VARCHAR(255),
    category_id BIGINT REFERENCES categories(id),
    author_id BIGINT REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, PUBLISHED
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    is_top BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    publish_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文章标签关联表
CREATE TABLE IF NOT EXISTS article_tags (
    article_id BIGINT REFERENCES articles(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT REFERENCES articles(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    ip_address VARCHAR(50),
    is_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 访问日志表
CREATE TABLE IF NOT EXISTS visit_logs (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT REFERENCES articles(id) ON DELETE CASCADE,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    visit_date VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建全文搜索索引
CREATE INDEX IF NOT EXISTS idx_articles_search ON articles USING gin(
    to_tsvector('chinese', COALESCE(title, '') || ' ' || COALESCE(content, ''))
);

-- 创建其他索引
CREATE INDEX IF NOT EXISTS idx_articles_status ON articles(status);
CREATE INDEX IF NOT EXISTS idx_articles_category ON articles(category_id);
CREATE INDEX IF NOT EXISTS idx_articles_author ON articles(author_id);
CREATE INDEX IF NOT EXISTS idx_articles_created ON articles(created_at);
CREATE INDEX IF NOT EXISTS idx_comments_article ON comments(article_id);
CREATE INDEX IF NOT EXISTS idx_comments_approved ON comments(is_approved);
CREATE INDEX IF NOT EXISTS idx_visit_logs_article ON visit_logs(article_id);
CREATE INDEX IF NOT EXISTS idx_visit_logs_date ON visit_logs(visit_date);

-- 插入默认管理员用户（密码：admin123，需要在应用启动后通过BCrypt加密）
INSERT INTO users (username, password, nickname, email)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@example.com')
ON CONFLICT (username) DO NOTHING;

-- 插入示例分类
INSERT INTO categories (name, description, sort_order) VALUES
('技术', '技术相关文章', 1),
('生活', '生活感悟', 2),
('读书', '读书笔记', 3)
ON CONFLICT (name) DO NOTHING;

-- 插入示例标签
INSERT INTO tags (name) VALUES
('Java'),
('Spring Boot'),
('Vue'),
('PostgreSQL'),
('Redis')
ON CONFLICT (name) DO NOTHING;

-- 插入示例文章
INSERT INTO articles (title, content, summary, category_id, author_id, status, publish_time) VALUES
('欢迎来到我的博客', '# 欢迎来到我的博客

这是第一篇文章，介绍了博客的基本功能。

## 功能特性

- 文章管理
- 分类标签
- 评论功能
- 全文搜索

希望你能喜欢！', '这是博客的第一篇文章', 1, 1, 'PUBLISHED', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

COMMENT ON TABLE users IS '用户表';
COMMENT ON TABLE articles IS '文章表';
COMMENT ON TABLE categories IS '分类表';
COMMENT ON TABLE tags IS '标签表';
COMMENT ON TABLE article_tags IS '文章标签关联表';
COMMENT ON TABLE comments IS '评论表';
COMMENT ON TABLE visit_logs IS '访问日志表';

COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.password IS '密码';
COMMENT ON COLUMN users.nickname IS '昵称';
COMMENT ON COLUMN users.avatar IS '头像';
COMMENT ON COLUMN users.email IS '邮箱';

COMMENT ON COLUMN articles.title IS '标题';
COMMENT ON COLUMN articles.content IS '内容';
COMMENT ON COLUMN articles.summary IS '摘要';
COMMENT ON COLUMN articles.status IS '状态';
COMMENT ON COLUMN articles.view_count IS '浏览量';
COMMENT ON COLUMN articles.is_top IS '是否置顶';
