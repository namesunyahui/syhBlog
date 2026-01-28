<template>
  <el-container class="admin-layout">
    <!-- 左侧侧边栏 -->
    <el-aside width="260px" class="sidebar">
      <div class="logo-section">
        <div class="logo-icon">
          <span class="logo-text">S</span>
        </div>
        <div class="logo-info">
          <h1 class="logo-title">SYH BLOG</h1>
          <p class="logo-subtitle">Editorial Dashboard</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <span class="nav-number">{{ String(menuItems.indexOf(item) + 1).padStart(2, '0') }}</span>
          <span class="nav-label">{{ item.label }}</span>
          <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-mini">
          <div class="user-avatar">
            <el-icon><User /></el-icon>
          </div>
          <div class="user-info">
            <p class="user-name">{{ currentUser.nickname || '管理员' }}</p>
            <p class="user-role">Editor in Chief</p>
          </div>
        </div>
      </div>
    </el-aside>

    <!-- 右侧主内容区 -->
    <el-container class="main-container">
      <!-- 顶部工具栏 -->
      <el-header class="top-header">
        <div class="header-left">
          <h2 class="page-title">{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <el-button class="header-btn" @click="handleLogout" text>
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="content-main">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  SwitchButton
} from '@element-plus/icons-vue'
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const currentUser = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : {}
})

const menuItems = [
  { path: '/admin/dashboard', label: '仪表盘' },
  { path: '/admin/articles', label: '文章管理' },
  { path: '/admin/categories', label: '分类管理' },
  { path: '/admin/tags', label: '标签管理' },
  { path: '/admin/comments', label: '评论管理', badge: '3' },
  { path: '/admin/users', label: '用户管理' },
  { path: '/', label: '返回首页' }
]

const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/dashboard': '仪表盘',
    '/admin/articles': '文章管理',
    '/admin/categories': '分类管理',
    '/admin/tags': '标签管理',
    '/admin/comments': '评论管理',
    '/admin/users': '用户管理',
    '/admin/article/edit': '文章编辑'
  }
  return titles[route.path] || '管理后台'
})

const handleLogout = async () => {
  try {
    await logout()
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/admin/login')
  } catch (error) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/admin/login')
  }
}
</script>

<style scoped>
/* ==================== 全局字体导入 ==================== */
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Inter:wght@300;400;500;600&display=swap');

/* ==================== 基础布局 ==================== */
.admin-layout {
  height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  overflow: hidden;
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  background: linear-gradient(180deg, #0d0d0d 0%, #1a1a1a 50%, #0d0d0d 100%);
  border-right: 1px solid rgba(212, 175, 55, 0.15);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 0%, rgba(212, 175, 55, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 100%, rgba(212, 175, 55, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

/* Logo 区域 */
.logo-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 28px 24px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
  position: relative;
  z-index: 1;
}

.logo-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 50%, #d4af37 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4px 20px rgba(212, 175, 55, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.logo-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.3) 50%,
    transparent 70%
  );
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}

.logo-text {
  font-size: 28px;
  font-weight: 700;
  color: #0a0a0a;
  font-family: 'Times New Roman', serif;
  position: relative;
  z-index: 1;
}

.logo-info {
  flex: 1;
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
  color: #f4e4bc;
  letter-spacing: 2px;
  margin: 0;
  font-family: 'Playfair Display', 'Times New Roman', serif;
}

.logo-subtitle {
  font-size: 11px;
  color: rgba(244, 228, 188, 0.5);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin: 4px 0 0 0;
}

/* 导航菜单 */
.sidebar-nav {
  flex: 1;
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
  z-index: 1;
  overflow-y: auto;
}

.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: rgba(212, 175, 55, 0.3);
  border-radius: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  color: rgba(244, 228, 188, 0.6);
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  font-size: 14px;
  border: 1px solid transparent;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15) 0%, rgba(244, 228, 188, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 12px;
}

.nav-item:hover {
  color: #f4e4bc;
  background: rgba(212, 175, 55, 0.08);
  transform: translateX(6px);
  border-color: rgba(212, 175, 55, 0.2);
}

.nav-item:hover::before {
  opacity: 1;
}

.nav-item.active {
  color: #0a0a0a;
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%);
  box-shadow:
    0 4px 15px rgba(212, 175, 55, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  border-color: transparent;
  transform: translateX(0);
}

.nav-item.active::before {
  opacity: 0;
}

.nav-number {
  font-size: 16px;
  font-weight: 600;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  color: rgba(212, 175, 55, 0.4);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  transition: color 0.3s ease;
}

.nav-item:hover .nav-number {
  color: rgba(212, 175, 55, 0.8);
}

.nav-item.active .nav-number {
  color: rgba(10, 10, 10, 0.5);
}

.nav-label {
  flex: 1;
  font-weight: 500;
  position: relative;
  z-index: 1;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-size: 13px;
}

.nav-badge {
  background: rgba(212, 175, 55, 0.2);
  color: #f4e4bc;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
  position: relative;
  z-index: 1;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(212, 175, 55, 0.1);
  position: relative;
  z-index: 1;
}

.user-mini {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(212, 175, 55, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(212, 175, 55, 0.1);
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3), rgba(244, 228, 188, 0.2));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f4e4bc;
  font-size: 20px;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #f4e4bc;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 11px;
  color: rgba(244, 228, 188, 0.5);
  margin: 2px 0 0 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* ==================== 主容器 ==================== */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a1a 100%);
  position: relative;
}

.main-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(212, 175, 55, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(212, 175, 55, 0.02) 0%, transparent 50%);
  pointer-events: none;
}

/* 顶部工具栏 */
.top-header {
  height: 70px;
  background: rgba(13, 13, 13, 0.6);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: relative;
  z-index: 10;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #f4e4bc;
  margin: 0;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-btn {
  color: rgba(244, 228, 188, 0.7);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.header-btn:hover {
  color: #f4e4bc;
  background: rgba(212, 175, 55, 0.1);
}

/* ==================== 主内容区 ==================== */
.content-main {
  flex: 1;
  padding: 0;
  overflow: auto;
  position: relative;
  z-index: 1;
}

.content-main::-webkit-scrollbar {
  width: 6px;
}

.content-main::-webkit-scrollbar-track {
  background: rgba(26, 26, 26, 0.5);
}

.content-main::-webkit-scrollbar-thumb {
  background: rgba(212, 175, 55, 0.3);
  border-radius: 3px;
}

.content-main::-webkit-scrollbar-thumb:hover {
  background: rgba(212, 175, 55, 0.5);
}

.content-wrapper {
  min-height: 100%;
  padding: 24px 32px;
}
</style>
