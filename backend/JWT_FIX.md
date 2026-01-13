# 403 错误修复说明

## 问题描述

访问文章管理界面时出现 403 错误，这是因为：
1. `/admin/**` 路径需要认证
2. Spring Security 没有正确验证前端传递的 token

## 解决方案

### 1. 添加 JWT 认证过滤器

**文件**: `backend/src/main/java/com/syh/blog/security/JwtAuthenticationFilter.java`

这个过滤器会：
- 从请求头 `Authorization` 中提取 token
- 验证 token 格式（简化版本，支持 mock-token-{id} 格式）
- 将认证信息设置到 Spring Security 上下文

### 2. 更新 SecurityConfig

**主要修改**:
```java
// 添加无状态会话管理
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)

// 添加JWT过滤器到过滤器链
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

// 所有需要认证的接口
.anyRequest().authenticated()
```

### 3. 认证流程

#### 登录流程
1. 前端发送 `POST /auth/login`
2. 后端验证用户名密码
3. 返回 token: `mock-token-{userId}`
4. 前端保存到 localStorage

#### 访问受保护接口
1. 前端从 localStorage 获取 token
2. 在请求头中添加: `Authorization: Bearer {token}`
3. JwtAuthenticationFilter 拦截请求
4. 验证 token 并设置认证信息
5. 允许访问受保护的接口

## 修复的文件

1. ✅ `JwtAuthenticationFilter.java` - JWT 认证过滤器
2. ✅ `SecurityConfig.java` - 更新安全配置
3. ✅ 编译成功

## 重启后端

**重要**: 需要重启后端服务以应用修改。

```bash
cd backend
stop.bat
start.bat
```

## 测试步骤

1. 重启后端服务
2. 访问登录页面：http://localhost:3000/admin/login
3. 输入账号密码：admin / admin123
4. 登录成功后进入管理后台
5. 点击"文章管理" - 应该能看到文章列表
6. 点击"新增文章" - 应该能正常创建文章

## 支持的接口

### 公开接口（无需认证）
- `GET /articles/**` - 文章浏览
- `GET /categories/**` - 分类浏览
- `GET /tags/**` - 标签浏览
- `GET /comments/**` - 评论查看
- `POST /auth/login` - 登录

### 需要认证的接口
- `GET /admin/articles` - 文章列表（管理后台）
- `POST /admin/articles` - 创建文章
- `PUT /admin/articles/{id}` - 更新文章
- `DELETE /admin/articles/{id}` - 删除文章
- `PUT /admin/articles/{id}/publish` - 发布/撤回文章

## Token 格式

当前使用简化的 mock token 格式：
```
Bearer mock-token-{userId}
```

例如：用户 ID 为 1 的 token 是：
```
Bearer mock-token-1
```

## 注意事项

1. **Token 存储位置**: 前端使用 `localStorage.setItem('token', token)` 存储

2. **Token 发送**: Axios 请求拦截器会自动添加到请求头

3. **Token 验证**: JWT 过滤器会验证每个需要认证的请求

4. **Session 策略**: 使用 `STATELESS` 无状态会话，适合前后端分离

## 后续优化

当前实现是简化版本，生产环境建议：
1. 使用真实的 JWT（jsonwebtoken 库）
2. 添加 token 过期时间
3. 实现 token 刷新机制
4. 添加更完善的权限控制（角色、权限）
5. 添加请求日志和异常处理
