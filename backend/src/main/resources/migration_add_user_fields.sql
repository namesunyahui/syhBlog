-- 用户表字段迁移脚本
-- 添加角色、状态、最后登录时间字段

-- 添加 role 字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'ADMIN';

-- 添加 status 字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- 添加 last_login_time 字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_time TIMESTAMP;

-- 添加字段注释
COMMENT ON COLUMN users.role IS '角色：SUPER_ADMIN（超级管理员）、ADMIN（管理员）';
COMMENT ON COLUMN users.status IS '状态：ACTIVE（激活）、BANNED（封禁）';
COMMENT ON COLUMN users.last_login_time IS '最后登录时间';

-- 将现有的 admin 用户设置为超级管理员（如果存在）
UPDATE users SET role = 'SUPER_ADMIN' WHERE username = 'admin';

-- 验证更新
SELECT
    id,
    username,
    nickname,
    role,
    status,
    last_login_time,
    created_at
FROM users;
