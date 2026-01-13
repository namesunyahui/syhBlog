# 数据库初始化说明

## 问题说明

如果启动后端时遇到以下错误：
```
ERROR: relation "users" does not exist
```

说明数据库表尚未创建。

## 解决方案

### 方式一：自动初始化（推荐）

Spring Boot 已配置为在启动时自动执行 `schema.sql` 创建表结构。

配置文件：`src/main/resources/application.yml`
```yaml
spring:
  sql:
    init:
      schema-locations: classpath:schema.sql
      mode: always
```

**直接启动后端即可，会自动创建所有表。**

### 方式二：手动执行 SQL 脚本

如果自动初始化失败，可以手动执行 SQL 脚本：

#### Windows 系统
```bash
cd backend
init-db.bat
```

#### Linux/Mac 系统
```bash
cd backend
chmod +x init-db.sh
./init-db.sh
```

#### 使用 psql 命令行
```bash
psql -h localhost -p 5432 -U syh -d syh_blog -f src/main/resources/schema.sql
```

#### 使用数据库管理工具
1. 打开 pgAdmin、DBeaver 或其他数据库管理工具
2. 连接到 `syh_blog` 数据库
3. 执行 `backend/src/main/resources/schema.sql` 文件中的 SQL

## 数据库连接信息

- **主机**: localhost
- **端口**: 5432
- **数据库**: syh_blog
- **用户名**: syh
- **密码**: qwer1234

## 数据库表结构

初始化后会创建以下表：

1. **users** - 用户表
2. **categories** - 文章分类表
3. **tags** - 文章标签表
4. **articles** - 文章表
5. **comments** - 评论表
6. **article_tags** - 文章标签关联表
7. **visit_logs** - 访问日志表

## 验证表是否创建成功

连接到数据库后执行：
```sql
\dt
```

应该能看到上述所有表。

## 默认管理员账户

后端启动后，`DataInitializer` 会自动创建默认管理员账户：

- **用户名**: admin
- **密码**: admin123
- **邮箱**: admin@syhblog.com

⚠️ **重要**: 首次登录后请立即修改默认密码！

## 常见问题

### 1. PostgreSQL 连接失败
- 检查 PostgreSQL 服务是否正在运行
- 检查用户名和密码是否正确
- 检查数据库 `syh_blog` 是否已创建

### 2. 表已存在错误
如果表已存在，`schema.sql` 会先删除旧表再创建，数据会丢失。如需保留数据，请修改 SQL 脚本。

### 3. 权限问题
确保数据库用户 `syh` 有创建表的权限：
```sql
GRANT ALL PRIVILEGES ON DATABASE syh_blog TO syh;
```
