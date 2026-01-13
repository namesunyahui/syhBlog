# 后端重启说明

## 403 错误已修复

刚刚修改了 Spring Security 配置，允许公共接口访问，现在需要重启后端服务。

## 修复内容

### SecurityConfig.java 修改：

**修改前：**
- 只有 `/auth/**` 等少数端点允许公开访问
- 所有其他请求都需要认证（`.anyRequest().authenticated()`）

**修改后：**
```java
.requestMatchers(
    "/auth/**",           // 认证相关
    "/articles/**",       // 文章列表和详情（公开访问）
    "/categories/**",     // 分类列表（公开访问）
    "/tags/**",           // 标签列表（公开访问）
    "/comments/**",       // 评论（公开访问）
    "/doc.html",          // API 文档
    ...
).permitAll()
```

## 重启后端服务

### 方式 1：使用停止脚本（推荐）

**Windows:**
```bash
cd backend
stop.bat
start.bat
```

**Linux/Mac:**
```bash
cd backend
# 查找并停止进程
lsof -ti:8088 | xargs kill -9
# 启动服务
mvn spring-boot:run
```

### 方式 2：手动停止并重启

1. **找到后端进程**
   ```bash
   netstat -ano | findstr :8088
   ```

2. **停止进程**（需要管理员权限）
   ```bash
   taskkill //F //PID <进程ID>
   ```

3. **重新启动**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### 方式 3：使用 Maven

```bash
cd backend
mvn spring-boot:run
```

## 验证修复

重启后，访问以下 URL 应该不再出现 403 错误：

- 前端首页：http://localhost:3000
- 文章列表 API：http://localhost:8088/api/articles
- 分类列表 API：http://localhost:8088/api/categories
- 标签列表 API：http://localhost:8088/api/tags

## 修改说明

### 现在的权限规则：

1. **公开访问（无需登录）：**
   - ✅ 文章列表
   - ✅ 文章详情
   - ✅ 分类列表
   - ✅ 标签列表
   - ✅ 评论查看
   - ✅ 登录接口

2. **需要认证：**
   - 🔒 管理后台 (`/admin/**`)
   - 🔒 文章创建/编辑/删除
   - 🔒 分类管理
   - 🔒 标签管理
   - 🔒 评论管理

3. **其他请求：**
   - ✅ 默认允许访问 (`.anyRequest().permitAll()`)

这样的配置符合博客系统的需求：前台内容公开，后台管理需要认证。
