# Syh Blog

<div align="center">

一个功能完善的全栈个人博客系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.0-42b883.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.3.0-blue.svg)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-336791.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 项目简介

Syh Blog 是一个现代化的全栈个人博客系统，采用前后端分离架构设计。前端基于 Vue 3 + TypeScript + Vite 构建，后端使用 Spring Boot 3 + Spring Security + MyBatis Plus，数据存储采用 PostgreSQL，缓存使用 Redis。

本项目具备完善的博客功能，包括文章管理、分类标签、评论系统、全文搜索、访问统计等，同时提供了友好的后台管理界面。

---

## 功能特性

### 前台功能

| 功能 | 描述 |
|------|------|
| 文章列表 | 支持分页、分类筛选、标签筛选 |
| 文章详情 | Markdown 渲染、代码高亮显示 |
| 评论系统 | 支持昵称、邮箱的游客评论 |
| 全文搜索 | 基于文章标题和内容的搜索 |
| 文章归档 | 按时间归档展示所有文章 |
| 分类浏览 | 按分类查看相关文章 |
| 标签浏览 | 按标签查看相关文章 |
| 访问统计 | 文章浏览量统计 |

### 后台管理

| 模块 | 功能 |
|------|------|
| 登录认证 | JWT 令牌认证、自动续期 |
| 文章管理 | 增删改查、发布/草稿、富文本编辑 |
| 分类管理 | 分类的创建、编辑、删除 |
| 标签管理 | 标签的创建、编辑、删除 |
| 评论管理 | 评论审核、删除、回复 |
| 数据统计 | 文章数、浏览量、评论数等统计 |
| 仪表板 | 数据可视化展示 |

### 技术特性

- JWT 令牌认证与授权
- CORS 跨域支持
- MyBatis Plus 自动填充（创建/更新时间）
- 逻辑删除机制
- Redis 缓存支持
- 请求日志记录
- 全局异常处理
- 参数校验（Validation）
- API 接口文档（Knife4j）

---

## 技术栈

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 渐进式 JavaScript 框架 |
| TypeScript | 5.3.0 | JavaScript 的超集 |
| Vite | 5.0.0 | 下一代前端构建工具 |
| Vue Router | 4.2.5 | Vue.js 官方路由管理器 |
| Pinia | 2.1.7 | Vue 3 状态管理库 |
| Element Plus | 2.5.0 | Vue 3 组件库 |
| Axios | 1.6.2 | HTTP 客户端 |
| Markdown-it | 14.0.0 | Markdown 解析器 |
| Highlight.js | 11.9.0 | 代码语法高亮 |
| @vueuse/core | 10.7.0 | Vue Composition API 工具集 |

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.1.6 | Java 应用框架 |
| JDK | 17 | Java 开发工具包 |
| Spring Security | - | 安全框架 |
| JWT | 0.12.3 | JSON Web Token |
| MyBatis Plus | 3.5.5 | MyBatis 增强工具 |
| PostgreSQL | 13+ | 关系型数据库 |
| Redis | 6.0+ | 内存数据库（缓存） |
| Druid | 1.2.20 | 数据库连接池 |
| Knife4j | 4.4.0 | API 文档生成工具 |
| Hutool | 5.8.23 | Java 工具类库 |
| Lombok | 1.18.30 | 简化 Java 代码 |

---

## 项目结构

```
syhBlog/
├── frontend/                          # Vue 3 前端项目
│   ├── src/
│   │   ├── api/                       # API 接口模块
│   │   │   ├── index.ts               # API 入口
│   │   │   ├── article.ts             # 文章 API
│   │   │   ├── auth.ts                # 认证 API
│   │   │   ├── category.ts            # 分类 API
│   │   │   ├── tag.ts                 # 标签 API
│   │   │   └── comment.ts             # 评论 API
│   │   ├── assets/                    # 静态资源
│   │   │   └── styles/                # 全局样式
│   │   ├── components/                # 公共组件
│   │   ├── router/                    # 路由配置
│   │   │   └── index.ts               # 路由定义
│   │   ├── stores/                    # Pinia 状态管理
│   │   ├── utils/                     # 工具类
│   │   │   └── request.ts             # Axios 封装
│   │   ├── views/                     # 页面组件
│   │   │   ├── Home.vue               # 首页
│   │   │   ├── ArticleDetail.vue      # 文章详情
│   │   │   ├── Category.vue           # 分类页
│   │   │   ├── Tag.vue                # 标签页
│   │   │   ├── Archive.vue            # 归档页
│   │   │   ├── Search.vue             # 搜索页
│   │   │   └── admin/                 # 后台管理页面
│   │   │       ├── Login.vue          # 登录页
│   │   │       ├── Dashboard.vue      # 仪表板
│   │   │       ├── ArticleList.vue    # 文章列表
│   │   │       ├── ArticleEdit.vue    # 文章编辑
│   │   │       └── ...
│   │   ├── App.vue                    # 根组件
│   │   └── main.ts                    # 入口文件
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts                 # Vite 配置（含代理）
│
├── backend/                           # Spring Boot 后端项目
│   ├── src/main/java/com/syh/blog/
│   │   ├── config/                    # 配置类
│   │   │   ├── SecurityConfig.java    # 安全配置
│   │   │   ├── DataInitializer.java   # 数据初始化
│   │   │   ├── MyBatisPlusConfig.java # MyBatis Plus 配置
│   │   │   ├── RedisConfig.java       # Redis 配置
│   │   │   └── Knife4jConfig.java     # API 文档配置
│   │   ├── controller/                # 控制器层
│   │   │   ├── AuthController.java    # 认证接口
│   │   │   ├── ArticleController.java # 文章接口
│   │   │   ├── CategoryController.java# 分类接口
│   │   │   ├── TagController.java     # 标签接口
│   │   │   └── CommentController.java # 评论接口
│   │   ├── entity/                    # 实体类
│   │   │   ├── User.java
│   │   │   ├── Article.java
│   │   │   ├── Category.java
│   │   │   ├── Tag.java
│   │   │   └── Comment.java
│   │   ├── service/                   # 服务层接口
│   │   ├── service/impl/              # 服务层实现
│   │   ├── mapper/                    # MyBatis Mapper 接口
│   │   ├── security/                  # 安全相关
│   │   │   ├── CustomUserDetailsService.java
│   │   │   └── CustomUserDetails.java
│   │   ├── common/                    # 公共类
│   │   │   └── Result.java            # 统一响应封装
│   │   ├── handler/                   # 处理器
│   │   │   └── MyMetaObjectHandler.java # 自动填充处理器
│   │   └── BlogApplication.java       # 启动类
│   ├── src/main/resources/
│   │   ├── mapper/                    # MyBatis XML 映射文件
│   │   ├── application.yml            # 应用配置
│   │   └── schema.sql                 # 数据库表结构
│   ├── src/test/java/                 # 测试代码
│   ├── init-db.bat                    # Windows 数据库初始化脚本
│   ├── init-db.sh                     # Linux/Mac 数据库初始化脚本
│   ├── start.bat                      # Windows 启动脚本
│   ├── stop.bat                       # Windows 停止脚本
│   └── pom.xml                        # Maven 配置
│
├── CLAUDE.md                          # Claude Code 项目指南
└── README.md                          # 项目说明文档
```

---

## 数据库设计

### 核心数据表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `users` | 用户表（管理员） | id, username, password, nickname, avatar, email, role, status |
| `articles` | 文章表 | id, title, content, summary, cover_image, category_id, view_count, is_published |
| `categories` | 分类表 | id, name, description, sort_order |
| `tags` | 标签表 | id, name |
| `article_tags` | 文章-标签关联表 | article_id, tag_id |
| `comments` | 评论表 | id, article_id, nickname, email, content, ip_address |
| `visit_logs` | 访问日志表 | id, article_id, ip_address, user_agent, visit_date |

### ER 关系

```
users (1) ────────< (N) articles
categories (1) ──< (N) articles
articles (1) ─────< (N) comments
articles (N) >──< (N) tags (通过 article_tags)
articles (1) ─────< (N) visit_logs
```

---

## 快速开始

### 环境要求

| 环境 | 版本要求 |
|------|----------|
| Node.js | 16.0+ |
| JDK | 17+ |
| PostgreSQL | 13+ |
| Redis | 6.0+（可选） |
| Maven | 3.6+ |

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/syhBlog.git
cd syhBlog
```

### 2. 数据库准备

**创建数据库：**

```sql
CREATE DATABASE syh_blog;
CREATE USER syh WITH PASSWORD 'qwer1234';
GRANT ALL PRIVILEGES ON DATABASE syh_blog TO syh;
```

**初始化表结构：**

选择以下任一方式：

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

- **手动执行**:
  ```bash
  psql -h localhost -p 5432 -U syh -d syh_blog -f backend/src/main/resources/schema.sql
  ```

### 3. 环境配置

复制环境变量模板文件并配置：

```bash
cp backend/.env.example backend/.env
```

编辑 `backend/.env` 文件：

```properties
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/syh_blog
DB_USERNAME=syh
DB_PASSWORD=qwer1234

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_change_this

# Druid Monitor (可选)
DRUID_USERNAME=admin
DRUID_PASSWORD=your_druid_password

# Redis (可选)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

> ⚠️ **重要**：生产环境请修改默认密码和密钥，不要将 `.env` 文件提交到 Git 仓库。

### 4. 启动后端

**方式一：Maven 命令**

```bash
cd backend
mvn spring-boot:run
```

**方式二：启动脚本（Windows）**

```bash
cd backend
start.bat
```

**方式三：IDE 运行**

运行 `BlogApplication.java` 的 `main` 方法

后端服务启动在：**http://localhost:8088/api**

API 文档：**http://localhost:8088/api/doc.html**

### 5. 启动前端

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端服务启动在：**http://localhost:3000**

### 6. 访问系统

| 页面 | 地址 |
|------|------|
| 前台首页 | http://localhost:3000 |
| 后台登录 | http://localhost:3000/admin/login |
| API 文档 | http://localhost:8088/api/doc.html |

**默认管理员账户：**

- 用户名：`admin`
- 密码：`admin123`

> 注意：首次启动后端时，会自动创建默认管理员账户

---

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

# 运行打包后的 jar
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

---

## 配置说明

### 后端配置文件

位置：`backend/src/main/resources/application.yml`

```yaml
server:
  port: 8088                    # 服务端口
  servlet:
    context-path: /api          # 上下文路径

spring:
  application:
    name: syh-blog

  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/syh_blog
    username: syh
    password: qwer1234

  data:
    redis:
      host: localhost
      port: 6379
      password:                 # Redis 密码（可选）
      database: 0

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志
  global-config:
    db-config:
      logic-delete-field: isDeleted      # 逻辑删除字段
      logic-delete-value: 1              # 删除值
      logic-not-delete-value: 0          # 未删除值

jwt:
  secret: syh-blog-secret-key-2024-very-long-secret  # JWT 密钥
  expiration: 604800000                            # 7天（毫秒）

file:
  upload-path: /uploads                            # 文件上传路径
  max-size: 10MB                                   # 最大文件大小
```

### 前端配置文件

位置：`frontend/vite.config.ts`

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

---

## API 接口

### 认证接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /auth/login | 用户登录 | 否 |
| POST | /auth/logout | 用户登出 | 是 |

### 文章接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /articles | 获取文章列表 | 否 |
| GET | /articles/{id} | 获取文章详情 | 否 |
| POST | /articles | 创建文章 | 是 |
| PUT | /articles/{id} | 更新文章 | 是 |
| DELETE | /articles/{id} | 删除文章 | 是 |

### 分类接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /categories | 获取分类列表 | 否 |
| POST | /categories | 创建分类 | 是 |
| PUT | /categories/{id} | 更新分类 | 是 |
| DELETE | /categories/{id} | 删除分类 | 是 |

### 标签接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /tags | 获取标签列表 | 否 |
| POST | /tags | 创建标签 | 是 |
| PUT | /tags/{id} | 更新标签 | 是 |
| DELETE | /tags/{id} | 删除标签 | 是 |

### 评论接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /comments | 获取评论列表 | 否 |
| POST | /comments | 发表评论 | 否 |
| DELETE | /comments/{id} | 删除评论 | 是 |

更多详细 API 文档请访问：**http://localhost:8088/api/doc.html**

---

## 部署

### 前端部署

```bash
cd frontend
npm run build
```

将 `dist` 目录部署到 Nginx：

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
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
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
- 配置 HTTPS 证书
- 使用 Nginx 反向代理
- 定期备份数据库
- 配置防火墙规则
- 设置日志轮转

---

## 常见问题

### 1. 数据库连接失败

**检查项：**
- PostgreSQL 服务是否启动
- 数据库、用户名、密码是否正确
- 防火墙是否允许 5432 端口
- `.env` 文件配置是否正确

### 2. Redis 连接失败

**解决方案：**
- 安装并启动 Redis 服务
- 或在 `application.yml` 中禁用 Redis 相关功能
- 检查 Redis 密码配置

### 3. 前端跨域问题

**解决方案：**
- 检查 `vite.config.ts` 中的 proxy 配置
- 确保前端代理指向正确的后端地址
- 后端已配置 CORS，一般不需要额外处理

### 4. 后端启动失败

**检查项：**
- JDK 版本是否为 17+
- 端口 8088 是否被占用
- Maven 依赖是否正确下载
- 数据库连接是否正常

### 5. JWT 令牌过期

**解决方案：**
- 重新登录获取新令牌
- 调整 `application.yml` 中的 `jwt.expiration` 配置

---

## 项目截图

### 前台首页

```
[截图占位]
```

### 后台管理

```
[截图占位]
```

---

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 更新日志

### v1.0.0 (2024-01-01)

- 初始版本发布
- 完成基础博客功能
- 前后台分离架构
- JWT 认证系统
- 文章、分类、标签管理
- 评论系统
- 访问统计

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 作者

**sunyahui**

- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your@email.com

---

## 致谢

感谢以下开源项目：

- [Vue.js](https://vuejs.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [Element Plus](https://element-plus.org/)
- [Knife4j](https://doc.xiaominfo.com/)

---

<div align="center">

如果这个项目对你有帮助，请给个 Star ⭐

</div>
