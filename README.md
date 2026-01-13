# Syh Blog

一个基于 Vue 3 + Spring Boot + PostgreSQL 的全栈个人博客系统，支持文章管理、分类标签、评论、全文搜索等功能。

## 项目概述

本项目采用前后端分离架构，前端使用 Vue 3 + TypeScript + Vite 构建，后端使用 Spring Boot 3.1 + Spring Security + MyBatis Plus，数据存储采用 PostgreSQL，缓存使用 Redis。

## 技术栈

### 前端技术
- **框架**：Vue 3.4 (Composition API)
- **语言**：TypeScript 5.3
- **构建工具**：Vite 5.0
- **路由**：Vue Router 4.2
- **状态管理**：Pinia 2.1
- **UI组件库**：Element Plus 2.5
- **HTTP客户端**：Axios 1.6
- **Markdown渲染**：markdown-it 14.0
- **代码高亮**：highlight.js 11.9
- **工具库**：@vueuse/core 10.7

### 后端技术
- **框架**：Spring Boot 3.1.6
- **Java版本**：JDK 17
- **安全框架**：Spring Security + JWT (jjwt 0.12.3)
- **持久层**：MyBatis Plus 3.5.5
- **数据库**：PostgreSQL
- **缓存**：Redis (Spring Data Redis)
- **连接池**：Druid 1.2.20
- **API文档**：Knife4j 4.4.0 (OpenAPI 3)
- **工具库**：Hutool 5.8.23, Apache Commons Lang3 3.14.0
- **日志**：SLF4J + Logback

## 功能特性

### 前台功能
- 文章列表（分页、分类筛选、标签筛选）
- 文章详情（Markdown渲染、代码高亮）
- 评论系统（支持昵称、邮箱）
- 全文搜索
- 文章归档（按时间归档）
- 分类/标签浏览
- 访问统计

### 后台管理
- 用户登录（JWT认证）
- 文章管理（增删改查、发布/草稿、富文本编辑）
- 分类管理
- 标签管理
- 评论管理（审核、删除）
- 统计分析（文章数、浏览量、评论数等）
- 数据可视化仪表板

### 技术特性
- JWT令牌认证
- CORS跨域支持
- MyBatis Plus自动填充（创建/更新时间）
- 逻辑删除
- Redis缓存
- 请求日志记录
- 访问统计
- 全局异常处理
- 参数校验（Validation）

## 项目结构

```
syhBlog/
├── frontend/                    # Vue 3 前端项目
│   ├── src/
│   │   ├── api/                # API接口模块
│   │   │   ├── index.ts        # API入口
│   │   │   ├── article.ts      # 文章API
│   │   │   ├── auth.ts         # 认证API
│   │   │   ├── category.ts     # 分类API
│   │   │   ├── tag.ts          # 标签API
│   │   │   └── comment.ts      # 评论API
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   │   └── index.ts        # 路由定义
│   │   ├── stores/             # Pinia状态管理
│   │   ├── utils/              # 工具类
│   │   │   └── request.ts      # Axios封装
│   │   ├── views/              # 页面组件
│   │   │   ├── Home.vue        # 首页
│   │   │   ├── admin/          # 后台管理页面
│   │   │   │   ├── Login.vue   # 登录页
│   │   │   │   ├── Dashboard.vue
│   │   │   │   ├── ArticleList.vue
│   │   │   │   └── ...
│   │   ├── App.vue             # 根组件
│   │   └── main.ts             # 入口文件
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts          # Vite配置（含代理）
│
├── backend/                     # Spring Boot 后端项目
│   ├── src/main/java/com/syh/blog/
│   │   ├── config/             # 配置类
│   │   │   ├── SecurityConfig.java        # 安全配置
│   │   │   ├── DataInitializer.java       # 数据初始化
│   │   │   ├── MyBatisPlusConfig.java     # MyBatis Plus配置
│   │   │   ├── RedisConfig.java           # Redis配置
│   │   │   └── Knife4jConfig.java         # API文档配置
│   │   ├── controller/         # 控制器层
│   │   │   ├── AuthController.java        # 认证接口
│   │   │   ├── ArticleController.java     # 文章接口
│   │   │   ├── CategoryController.java    # 分类接口
│   │   │   ├── TagController.java         # 标签接口
│   │   │   └── CommentController.java     # 评论接口
│   │   ├── entity/             # 实体类
│   │   │   ├── User.java
│   │   │   ├── Article.java
│   │   │   ├── Category.java
│   │   │   ├── Tag.java
│   │   │   └── Comment.java
│   │   ├── service/            # 服务层接口
│   │   ├── service/impl/       # 服务层实现
│   │   ├── mapper/             # MyBatis Mapper接口
│   │   ├── security/           # 安全相关
│   │   │   ├── CustomUserDetailsService.java
│   │   │   └── CustomUserDetails.java
│   │   ├── common/             # 公共类
│   │   │   └── Result.java     # 统一响应封装
│   │   ├── handler/            # 处理器
│   │   │   └── MyMetaObjectHandler.java  # 自动填充处理器
│   │   └── BlogApplication.java           # 启动类
│   ├── src/main/resources/
│   │   ├── mapper/             # MyBatis XML映射文件
│   │   ├── application.yml     # 应用配置
│   │   └── schema.sql          # 数据库表结构
│   ├── src/test/java/          # 测试代码
│   ├── init-db.bat             # Windows数据库初始化脚本
│   ├── init-db.sh              # Linux/Mac数据库初始化脚本
│   ├── start.bat               # Windows启动脚本
│   ├── stop.bat                # Windows停止脚本
│   └── pom.xml                 # Maven配置
│
├── database/                    # 数据库相关文档
├── CLAUDE.md                    # Claude Code 项目指南
└── README.md                    # 项目说明文档
```

## 数据库设计

### 主要数据表

| 表名 | 说明 |
|------|------|
| users | 用户表（管理员） |
| articles | 文章表 |
| categories | 分类表 |
| tags | 标签表 |
| article_tags | 文章-标签关联表 |
| comments | 评论表 |
| visit_logs | 访问日志表 |

### 关键字段

- **用户表**：id, username, password（BCrypt加密）, nickname, avatar, email
- **文章表**：id, title, content（Markdown）, summary, cover_image, category_id, view_count, is_published
- **分类表**：id, name, description
- **标签表**：id, name
- **评论表**：id, article_id, nickname, email, content, ip_address, user_agent

## 环境配置

项目使用环境变量来管理敏感配置（数据库密码、JWT密钥等）。

1. 复制环境变量模板文件：
   ```bash
   cp backend/.env.example backend/.env
   ```

2. 根据实际情况修改 `backend/.env` 文件中的配置：
   ```properties
   # Database Configuration
   DB_URL=jdbc:postgresql://localhost:5432/syh_blog
   DB_USERNAME=syh
   DB_PASSWORD=your_password

   # JWT Configuration
   JWT_SECRET=your_jwt_secret_key

   # Druid Monitor
   DRUID_USERNAME=admin
   DRUID_PASSWORD=your_druid_password

   # Redis (optional)
   REDIS_HOST=localhost
   REDIS_PORT=6379
   REDIS_PASSWORD=
   ```

3. **重要**：不要将 `.env` 文件提交到 Git 仓库（已添加到 `.gitignore`）

## 快速开始

### 环境要求

- **Node.js**: 16.0+
- **JDK**: 17+
- **PostgreSQL**: 13+
- **Redis**: 6.0+（可选，用于缓存）
- **Maven**: 3.6+

### 1. 数据库准备

**创建数据库：**

```sql
CREATE DATABASE syh_blog;
CREATE USER syh WITH PASSWORD 'qwer1234';
GRANT ALL PRIVILEGES ON DATABASE syh_blog TO syh;
```

**初始化表结构：**

- **Windows**:
  ```bash
  cd backend
  init-db.bat
  ```

- **Linux/Mac**:
  ```bash
  cd backend
  chmod +x init-db.sh
  ./init-db.sh
  ```

或手动执行：
```bash
psql -h localhost -p 5432 -U syh -d syh_blog -f backend/src/main/resources/schema.sql
```

### 2. 启动后端

**方式一：使用Maven命令**

```bash
cd backend
mvn spring-boot:run
```

**方式二：使用启动脚本（Windows）**

```bash
cd backend
start.bat
```

**方式三：IDE运行**

运行 `BlogApplication.java` 的 `main` 方法

后端服务启动在：**http://localhost:8088/api**

API文档：**http://localhost:8088/api/doc.html**

### 3. 启动前端

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端服务启动在：**http://localhost:3000**

### 4. 访问系统

- **前台首页**：http://localhost:3000
- **后台登录**：http://localhost:3000/admin/login
- **默认账户**：
  - 用户名：`admin`
  - 密码：`admin123`

> 注意：首次启动后端时，会自动创建默认管理员账户

## 开发指南

### 后端开发

```bash
cd backend

# 编译项目
mvn clean compile

# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=AuthControllerTest

# 打包项目
mvn clean package

# 运行打包后的jar
java -jar target/blog-backend-1.0.0.jar
```

### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 开发模式（热重载）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 配置说明

### 后端配置 (backend/src/main/resources/application.yml)

```yaml
server:
  port: 8088                    # 服务端口
  servlet:
    context-path: /api          # 上下文路径

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/syh_blog
    username: syh
    password: qwer1234

  data:
    redis:
      host: localhost
      port: 6379
      password:                 # Redis密码（可选）

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL日志

jwt:
  secret: syh-blog-secret-key-2024-very-long-secret
  expiration: 604800000         # 7天（毫秒）
```

### 前端配置 (frontend/vite.config.ts)

```typescript
export default defineConfig({
  server: {
    port: 3000,                 // 开发服务器端口
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  }
})
```

## API接口

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 用户登录 |
| POST | /auth/logout | 用户登出 |

### 文章接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /articles | 获取文章列表 |
| GET | /articles/{id} | 获取文章详情 |
| POST | /articles | 创建文章 |
| PUT | /articles/{id} | 更新文章 |
| DELETE | /articles/{id} | 删除文章 |

更多API文档请访问：http://localhost:8088/api/doc.html

## 部署

### 前端部署

```bash
cd frontend
npm run build
```

将 `dist` 目录部署到 Nginx/CDN：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 后端部署

```bash
cd backend
mvn clean package
java -jar target/blog-backend-1.0.0.jar
```

生产环境建议：
- 使用 `nohup` 或 `systemd` 管理进程
- 配置 HTTPS
- 使用 Nginx 反向代理
- 定期备份数据库

## 常见问题

### 1. 数据库连接失败

检查：
- PostgreSQL 服务是否启动
- 数据库、用户名、密码是否正确
- 防火墙是否允许 5432 端口

### 2. Redis 连接失败

如果未安装 Redis：
- 安装并启动 Redis 服务
- 或在 `application.yml` 中禁用 Redis 相关功能

### 3. 前端跨域问题

后端已配置 CORS，如仍有问题：
- 检查 `vite.config.ts` 中的 proxy 配置
- 确保前端代理指向正确的后端地址

### 4. 后端启动失败

检查：
- JDK 版本是否为 17+
- 端口 8088 是否被占用
- Maven 依赖是否正确下载

## 许可证

MIT License

## 作者

sunyahui

---

**注意**：本项目仅供学习交流使用。
