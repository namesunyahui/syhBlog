<template>
  <header class="app-header" :class="{ 'is-scrolled': isScrolled }">
    <div class="header-container">
      <!-- Logo - 不对称布局左侧 -->
      <div class="header-left">
        <router-link to="/" class="logo cursor-interactive" data-cursor-label="首页">
          <span class="logo-symbol">§</span>
          <span class="logo-text">Syh<span class="logo-accent">.</span>Blog</span>
        </router-link>
      </div>

      <!-- 导航菜单 - 偏右布局 -->
      <nav class="header-nav">
        <router-link
          to="/"
          class="nav-link cursor-interactive"
          data-cursor-label="首页"
        >
          <span class="nav-text">首页</span>
          <span class="nav-indicator"></span>
        </router-link>
        <router-link
          to="/category"
          class="nav-link cursor-interactive"
          data-cursor-label="分类"
        >
          <span class="nav-text">分类</span>
          <span class="nav-indicator"></span>
        </router-link>
        <router-link
          to="/tag"
          class="nav-link cursor-interactive"
          data-cursor-label="标签"
        >
          <span class="nav-text">标签</span>
          <span class="nav-indicator"></span>
        </router-link>
        <router-link
          to="/archive"
          class="nav-link cursor-interactive"
          data-cursor-label="归档"
        >
          <span class="nav-text">归档</span>
          <span class="nav-indicator"></span>
        </router-link>
        <router-link
          to="/about"
          class="nav-link cursor-interactive"
          data-cursor-label="关于"
        >
          <span class="nav-text">关于</span>
          <span class="nav-indicator"></span>
        </router-link>
      </nav>

      <!-- 右侧用户区域 -->
      <div class="header-right">
        <div v-if="isLoggedIn" class="user-section">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info cursor-interactive" data-cursor-label="菜单">
              <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar" />
              <span class="username">{{ userInfo.nickname || '管理员' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <el-button
          v-else
          class="login-btn cursor-interactive"
          data-cursor-label="登录"
          @click="goToLogin"
        >
          登录
        </el-button>
      </div>
    </div>

    <!-- 底部边框装饰 -->
    <div class="header-border"></div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { logout } from '@/api/auth'

const router = useRouter()
const isScrolled = ref(false)
const userInfo = ref<any>({})
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 检查登录状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 处理滚动
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  if (command === 'admin') {
    router.push('/admin/dashboard')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await logout()
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      userInfo.value = {}
      ElMessage.success('退出成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('退出失败', error)
      }
    }
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/admin/login')
}

// 加载用户信息
const loadUserInfo = () => {
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo) {
    try {
      userInfo.value = JSON.parse(savedUserInfo)
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  loadUserInfo()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-fixed);
  background: rgba(10, 10, 10, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid transparent;
  transition: all 0.3s var(--ease-out);
}

.app-header.is-scrolled {
  background: rgba(10, 10, 10, 0.95);
  border-bottom-color: var(--border-subtle);
}

.header-container {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: var(--space-12);
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: var(--space-4) var(--space-6);
  height: 72px;
}

/* ----- Logo ----- */
.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
  transition: all 0.3s var(--ease-out);
}

.logo-symbol {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--accent-gold);
  line-height: 1;
}

.logo-text {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  letter-spacing: -0.02em;
  transition: color 0.3s var(--ease-out);
}

.logo-accent {
  color: var(--accent-gold);
}

.logo:hover {
  transform: translateY(-2px);
}

.logo:hover .logo-text {
  color: var(--accent-gold);
}

/* ----- 导航菜单 ----- */
.header-nav {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  justify-content: flex-end;
  margin-right: var(--space-8);
}

.nav-link {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-2) var(--space-3);
  text-decoration: none;
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  letter-spacing: 0.02em;
  transition: color 0.3s var(--ease-out);
}

.nav-text {
  position: relative;
  z-index: 1;
}

.nav-indicator {
  position: absolute;
  bottom: var(--space-2);
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--accent-gold);
  transform: translateX(-50%);
  transition: width 0.3s var(--ease-out);
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--text-primary);
}

.nav-link:hover .nav-indicator,
.nav-link.router-link-active .nav-indicator {
  width: 100%;
}

/* ----- 右侧区域 ----- */
.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
  border: 1px solid transparent;
}

.user-info:hover {
  background: var(--bg-secondary);
  border-color: var(--border-accent);
}

.username {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.login-btn {
  padding: var(--space-2) var(--space-6);
  height: 36px;
  background: var(--accent-gold);
  border: 1px solid var(--accent-gold);
  color: var(--bg-primary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-full);
  transition: all 0.3s var(--ease-out);
}

.login-btn:hover {
  background: transparent;
  color: var(--accent-gold);
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

/* ----- 底部边框装饰 ----- */
.header-border {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    var(--border-accent) 50%,
    transparent 100%
  );
  opacity: 0.5;
}

/* ----- Element Plus 下拉菜单样式覆盖 ----- */
:deep(.el-dropdown-menu) {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-xl);
  padding: var(--space-2);
}

:deep(.el-dropdown-menu__item) {
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  transition: all 0.2s var(--ease-out);
}

:deep(.el-dropdown-menu__item:hover) {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-dropdown-menu__item.is-divided) {
  border-top: 1px solid var(--border-subtle);
  margin-top: var(--space-2);
  padding-top: var(--space-3);
}

/* ----- 响应式 ----- */
@media (max-width: 1024px) {
  .header-container {
    gap: var(--space-6);
  }

  .header-nav {
    gap: 0;
    margin-right: var(--space-4);
  }

  .nav-link {
    padding: var(--space-2);
    font-size: var(--text-xs);
  }
}

@media (max-width: 768px) {
  .app-header {
    background: rgba(10, 10, 10, 0.95);
  }

  .header-container {
    grid-template-columns: auto 1fr;
    grid-template-rows: auto auto;
    gap: var(--space-3);
    height: auto;
    padding: var(--space-3) var(--space-4);
  }

  .header-left {
    grid-column: 1;
  }

  .header-nav {
    grid-column: 1 / -1;
    justify-content: center;
    flex-wrap: wrap;
    margin-right: 0;
  }

  .header-right {
    grid-column: 2;
    justify-self: end;
  }

  .logo-text {
    font-size: var(--text-base);
  }

  .logo-symbol {
    font-size: var(--text-2xl);
  }

  .username {
    display: none;
  }

  .login-btn {
    padding: var(--space-2) var(--space-4);
    font-size: var(--text-xs);
  }
}
</style>
