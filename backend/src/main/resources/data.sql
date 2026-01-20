-- =============================================
-- Syh Blog 初始数据
-- PostgreSQL 数据库
-- =============================================

-- 插入默认管理员账户
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO users (username, password, nickname, avatar, email, role, status)
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '超级管理员',
    'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    'admin@syhblog.com',
    'SUPER_ADMIN',
    'ACTIVE'
)
ON CONFLICT (username) DO NOTHING;

-- 插入示例分类
INSERT INTO categories (name, description, sort_order) VALUES
    ('技术分享', '分享编程技术和开发经验', 1),
    ('生活随笔', '记录生活点滴和感悟', 2),
    ('学习笔记', '学习过程中的笔记总结', 3)
ON CONFLICT (name) DO NOTHING;

-- 插入示例标签
INSERT INTO tags (name) VALUES
    ('Java'),
    ('Spring Boot'),
    ('Vue.js'),
    ('PostgreSQL'),
    ('前端开发'),
    ('后端开发'),
    ('数据库'),
    ('算法')
ON CONFLICT (name) DO NOTHING;

-- 获取分类ID和标签ID用于文章插入
-- 注意：这里使用硬编码的ID值（假设按插入顺序为1,2,3）
-- 实际使用时可能需要根据实际情况调整

-- 插入示例文章
INSERT INTO articles (title, content, summary, cover_image, category_id, view_count, is_published) VALUES
    (
        '欢迎来到Syh Blog',
        '# 欢迎来到Syh Blog

这是基于 **Spring Boot** + **Vue 3** + **PostgreSQL** 构建的个人博客系统。

## 主要功能

- 📝 文章管理：支持Markdown编辑
- 🏷️ 分类标签：灵活的内容组织
- 💬 评论系统：与读者互动交流
- 📊 访问统计：了解文章受欢迎程度
- 🎨 现代界面：简洁美观的用户体验

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security + JWT
- MyBatis Plus
- PostgreSQL
- Redis (可选)

### 前端
- Vue 3 + Composition API
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router

## 开始使用

1. 使用默认账户登录后台管理系统
2. 创建分类和标签
3. 开始发布你的第一篇文章！

祝您使用愉快！',
        '欢迎来到Syh Blog，这是一个基于Spring Boot和Vue 3构建的现代化个人博客系统。支持Markdown编辑、分类标签、评论系统等功能。',
        'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
        1,
        0,
        true
    ),
    (
        'Spring Boot入门教程',
        '# Spring Boot入门教程

## 什么是Spring Boot？

Spring Boot是由Pivotal团队提供的全新框架，其设计目的是用来简化新Spring应用的初始搭建以及开发过程。

## 核心特性

- 独立运行的Spring项目
- 自动配置Spring
- 提供生产级的监控、健康检查和外部化配置
- 绝对没有代码生成和XML配置

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 创建第一个Controller

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring Boot!";
    }
}
```

## 总结

Spring Boot大大简化了Spring应用的开发，让我们可以更专注于业务逻辑的实现。',
        'Spring Boot是由Pivotal团队提供的全新框架，其设计目的是用来简化新Spring应用的初始搭建以及开发过程。本文介绍Spring Boot的核心特性和快速开始方法。',
        'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
        1,
        0,
        true
    ),
    (
        'Vue 3 Composition API详解',
        '# Vue 3 Composition API详解

## 为什么需要Composition API？

Vue 2的Options API在处理复杂组件时存在一些问题：
- 代码逻辑分散在不同选项中
- 难以复用逻辑
- TypeScript支持不友好

## Composition API基础

### setup函数

```javascript
import { ref, computed } from ''vue''

export default {
  setup() {
    const count = ref(0)
    const doubled = computed(() => count.value * 2)

    function increment() {
      count.value++
    }

    return { count, doubled, increment }
  }
}
```

### ref和reactive

- `ref`: 用于基本类型和对象
- `reactive`: 只用于对象

## 优势

1. 更好的逻辑组织
2. 更强的类型推断
3. 更灵活的代码复用

Composition API是Vue 3最重要的新特性之一。',
        'Vue 3的Composition API是全新的组件逻辑组织方式，解决了Options API在处理复杂组件时的问题。本文详细介绍Composition API的使用方法和优势。',
        'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
        1,
        0,
        true
    )
ON CONFLICT DO NOTHING;

-- 插入文章标签关联（文章ID对应上面插入的文章）
-- 第一篇文章：Java, Spring Boot
INSERT INTO article_tags (article_id, tag_id)
SELECT 1, id FROM tags WHERE name IN ('Java', 'Spring Boot')
ON CONFLICT DO NOTHING;

-- 第二篇文章：Java, Spring Boot, 后端开发
INSERT INTO article_tags (article_id, tag_id)
SELECT 2, id FROM tags WHERE name IN ('Java', 'Spring Boot', '后端开发')
ON CONFLICT DO NOTHING;

-- 第三篇文章：Vue.js, 前端开发
INSERT INTO article_tags (article_id, tag_id)
SELECT 3, id FROM tags WHERE name IN ('Vue.js', '前端开发')
ON CONFLICT DO NOTHING;

-- 插入示例评论
INSERT INTO comments (article_id, nickname, email, content, ip_address) VALUES
    (
        1,
        '访客用户',
        'visitor@example.com',
        '博客系统做得真不错！界面简洁美观，功能也很完善。',
        '127.0.0.1'
    ),
    (
        2,
        'Spring爱好者',
        'spring@example.com',
        '教程写得很详细，对我学习Spring Boot很有帮助，感谢分享！',
        '127.0.0.1'
    ),
    (
        3,
        '前端开发者',
        'frontend@example.com',
        'Composition API确实比Options API好用很多，代码组织更清晰了。',
        '127.0.0.1'
    )
ON CONFLICT DO NOTHING;

-- =============================================
-- 数据插入完成
-- =============================================

-- 统计信息
-- DO $$
-- BEGIN
--     RAISE NOTICE '========================================';
--     RAISE NOTICE '初始数据插入完成！';
--     RAISE NOTICE '管理员账户: admin / admin123';
--     RAISE NOTICE '示例文章: 3篇';
--     RAISE NOTICE '示例分类: 3个';
--     RAISE NOTICE '示例标签: 8个';
--     RAISE NOTICE '示例评论: 3条';
--     RAISE NOTICE '========================================';
-- END $$;
