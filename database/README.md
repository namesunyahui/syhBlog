# 数据库初始化说明

## 前置条件

确保已安装PostgreSQL数据库（推荐版本 13+）

## 初始化步骤

### 1. 创建数据库

```bash
# 方式一：使用psql命令
psql -U postgres
CREATE DATABASE syh_blog;
\q

# 方式二：使用createdb命令
createdb -U postgres syh_blog
```

### 2. 执行初始化脚本

```bash
psql -U postgres -d syh_blog -f init.sql
```

### 3. 修改数据库配置

编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/syh_blog
    username: postgres
    password: your_password  # 修改为你的密码
```

### 4. 默认账户

系统初始化后会创建一个默认管理员账户：

- 用户名：`admin`
- 密码：`admin123`

**注意**：首次登录后请立即修改密码！

## 数据库表说明

- `users` - 用户表（管理员）
- `articles` - 文章表
- `categories` - 分类表
- `tags` - 标签表
- `article_tags` - 文章标签关联表
- `comments` - 评论表
- `visit_logs` - 访问日志表

## 备份与恢复

### 备份数据库

```bash
pg_dump -U postgres syh_blog > backup.sql
```

### 恢复数据库

```bash
psql -U postgres syh_blog < backup.sql
```

## 注意事项

1. 确保PostgreSQL服务已启动
2. 根据实际情况修改数据库用户名和密码
3. 生产环境建议修改默认管理员密码
4. 建议定期备份数据库
