-- =============================================
-- Syh Blog 数据库表结构
-- PostgreSQL 数据库
-- 根据实体类生成的表结构
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_deleted INTEGER DEFAULT 0,
    last_login_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID';
COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.password IS '密码（BCrypt加密）';
COMMENT ON COLUMN users.nickname IS '昵称';
COMMENT ON COLUMN users.avatar IS '头像URL';
COMMENT ON COLUMN users.email IS '邮箱';
COMMENT ON COLUMN users.role IS '角色：SUPER_ADMIN（超级管理员）、ADMIN（管理员）';
COMMENT ON COLUMN users.status IS '状态：ACTIVE（激活）、BANNED（封禁）';
COMMENT ON COLUMN users.last_login_time IS '最后登录时间';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE categories IS '文章分类表';
COMMENT ON COLUMN categories.id IS '分类ID';
COMMENT ON COLUMN categories.name IS '分类名称';
COMMENT ON COLUMN categories.description IS '分类描述';
COMMENT ON COLUMN categories.sort_order IS '排序顺序';
COMMENT ON COLUMN categories.created_at IS '创建时间';

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tags IS '文章标签表';
COMMENT ON COLUMN tags.id IS '标签ID';
COMMENT ON COLUMN tags.name IS '标签名称';
COMMENT ON COLUMN tags.created_at IS '创建时间';

-- 文章表
CREATE TABLE IF NOT EXISTS articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    summary VARCHAR(500),
    cover_image VARCHAR(255),
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    view_count BIGINT DEFAULT 0,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE articles IS '文章表';
COMMENT ON COLUMN articles.id IS '文章ID';
COMMENT ON COLUMN articles.title IS '文章标题';
COMMENT ON COLUMN articles.content IS '文章内容（Markdown格式）';
COMMENT ON COLUMN articles.summary IS '文章摘要';
COMMENT ON COLUMN articles.cover_image IS '封面图片URL';
COMMENT ON COLUMN articles.category_id IS '所属分类ID';
COMMENT ON COLUMN articles.view_count IS '浏览次数';
COMMENT ON COLUMN articles.is_published IS '是否已发布';
COMMENT ON COLUMN articles.created_at IS '创建时间';
COMMENT ON COLUMN articles.updated_at IS '更新时间';

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    content TEXT NOT NULL,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE comments IS '评论表';
COMMENT ON COLUMN comments.id IS '评论ID';
COMMENT ON COLUMN comments.article_id IS '文章ID';
COMMENT ON COLUMN comments.user_id IS '用户ID（登录用户）';
COMMENT ON COLUMN comments.nickname IS '评论者昵称（未登录用户）';
COMMENT ON COLUMN comments.email IS '评论者邮箱';
COMMENT ON COLUMN comments.content IS '评论内容';
COMMENT ON COLUMN comments.ip_address IS 'IP地址';
COMMENT ON COLUMN comments.created_at IS '创建时间';

-- 文章标签关联表
CREATE TABLE IF NOT EXISTS article_tags (
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

COMMENT ON TABLE article_tags IS '文章标签关联表（多对多）';
COMMENT ON COLUMN article_tags.article_id IS '文章ID';
COMMENT ON COLUMN article_tags.tag_id IS '标签ID';

-- 访问日志表
CREATE TABLE IF NOT EXISTS visit_logs (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    visit_date VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE visit_logs IS '访问日志表';
COMMENT ON COLUMN visit_logs.id IS '日志ID';
COMMENT ON COLUMN visit_logs.article_id IS '文章ID';
COMMENT ON COLUMN visit_logs.ip_address IS '访问IP地址';
COMMENT ON COLUMN visit_logs.user_agent IS '用户代理（浏览器信息）';
COMMENT ON COLUMN visit_logs.visit_date IS '访问日期';
COMMENT ON COLUMN visit_logs.created_at IS '创建时间';

-- 创建索引
-- 文章表索引
CREATE INDEX IF NOT EXISTS idx_articles_category_id ON articles(category_id);
CREATE INDEX IF NOT EXISTS idx_articles_created_at ON articles(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_articles_is_published ON articles(is_published);

-- 评论表索引
CREATE INDEX IF NOT EXISTS idx_comments_article_id ON comments(article_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);

-- 访问日志表索引
CREATE INDEX IF NOT EXISTS idx_visit_logs_article_id ON visit_logs(article_id);
CREATE INDEX IF NOT EXISTS idx_visit_logs_visit_date ON visit_logs(visit_date);
CREATE INDEX IF NOT EXISTS idx_visit_logs_created_at ON visit_logs(created_at DESC);

-- 文章标签关联表索引
CREATE INDEX IF NOT EXISTS idx_article_tags_tag_id ON article_tags(tag_id);
