<template>
  <div class="about-page">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1 class="site-title">
            <router-link to="/">Syh Blog</router-link>
          </h1>
          <nav class="nav-menu">
            <router-link to="/">🏠 首页</router-link>
            <router-link to="/category">📂 分类</router-link>
            <router-link to="/tag">🏷️ 标签</router-link>
            <router-link to="/archive">📦 归档</router-link>
            <router-link to="/about">👤 关于</router-link>
          </nav>
          <div class="right-section">
            <div class="user-section">
              <template v-if="isLoggedIn">
                <el-dropdown>
                  <span class="user-info">
                    <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar" />
                    <span class="username">{{ userInfo.nickname || '管理员' }}</span>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="goToAdmin">
                        🎯 管理后台
                      </el-dropdown-item>
                      <el-dropdown-item divided @click="handleLogout">
                        🚪 退出登录
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <template v-else>
                <el-button type="primary" @click="goToLogin" class="login-btn">
                  🔐 登录
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </el-header>

      <el-main>
        <div class="about-content">
          <el-card class="main-card">
            <div class="about-header">
              <h1 class="about-title">关于我</h1>
              <p class="about-subtitle">很高兴遇见你 ✨</p>
            </div>

            <div class="about-section">
              <h2>👋 你好呀！</h2>
              <p>欢迎来到我的小天地。这里是 Syh Blog，一个承载我思考和记录的角落。</p>
              <p>我会在这里分享技术学习笔记、生活感悟和一些碎碎念。希望这些文字能给你带来一些启发和帮助。</p>
            </div>

            <div class="about-section">
              <h2>🚀 技术栈</h2>

              <div class="tech-stack">
                <div class="tech-category">
                  <h3>前端技术</h3>
                  <div class="tech-items">
                    <div class="tech-item">
                      <div class="tech-icon">🎨</div>
                      <div class="tech-info">
                        <h4>Vue 3</h4>
                        <p>采用 Composition API 和 TypeScript 开发</p>
                      </div>
                    </div>
                    <div class="tech-item">
                      <div class="tech-icon">⚡</div>
                      <div class="tech-info">
                        <h4>Vite</h4>
                        <p>新一代前端构建工具，极速热更新</p>
                      </div>
                    </div>
                    <div class="tech-item">
                      <div class="tech-icon">🎯</div>
                      <div class="tech-info">
                        <h4>Element Plus</h4>
                        <p>基于 Vue 3 的组件库</p>
                      </div>
                    </div>
                    <div class="tech-item">
                      <div class="tech-icon">📦</div>
                      <div class="tech-info">
                        <h4>Pinia</h4>
                        <p>Vue 3 官方推荐的状态管理方案</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="tech-category">
                  <h3>后端技术</h3>
                  <div class="tech-items">
                    <div class="tech-item">
                      <div class="tech-icon">☕</div>
                      <div class="tech-info">
                        <h4>Spring Boot</h4>
                        <p>Java 企业级开发框架</p>
                      </div>
                    </div>
                    <div class="tech-item">
                      <div class="tech-icon">🗄️</div>
                      <div class="tech-info">
                        <h4>PostgreSQL</h4>
                        <p>强大的开源关系型数据库</p>
                      </div>
                    </div>
                    <div class="tech-item">
                      <div class="tech-icon">🔴</div>
                      <div class="tech-info">
                        <h4>Redis</h4>
                        <p>高性能缓存中间件</p>
                      </div>
                    </div>
                    <div class="tech-item">
                      <div class="tech-icon">🔐</div>
                      <div class="tech-info">
                        <h4>Spring Security</h4>
                        <p>安全认证与授权框架</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="about-section">
              <h2>✨ 我能帮你做什么？</h2>
              <ul class="feature-list">
                <li>
                  <span class="feature-icon">📝</span>
                  <div class="feature-content">
                    <h4>陪你阅读</h4>
                    <p>这里收录了我的学习笔记、技术总结和生活感悟，随时欢迎你来逛逛</p>
                  </div>
                </li>
                <li>
                  <span class="feature-icon">💬</span>
                  <div class="feature-content">
                    <h4>倾听声音</h4>
                    <p>每篇文章都期待你的声音，无论是赞同还是质疑，我很乐意和你交流</p>
                  </div>
                </li>
                <li>
                  <span class="feature-icon">🔍</span>
                  <div class="feature-content">
                    <h4>帮你找东西</h4>
                    <p>在海量的文字中迷失了？用搜索功能，帮你快速找到感兴趣的内容</p>
                  </div>
                </li>
                <li>
                  <span class="feature-icon">📂</span>
                  <div class="feature-content">
                    <h4>整理归档</h4>
                    <p>按主题分类，贴好标签，让知识更有序。或者按时间浏览，看看成长轨迹</p>
                  </div>
                </li>
              </ul>
            </div>

            <div class="about-section">
              <h2>💻 关于技术</h2>
              <div class="design-philosophy">
                <p>这个博客是用 <strong>Vue 3</strong> 和 <strong>Spring Boot</strong> 搭建的。选择这些技术，是因为它们足够灵活、高效，让我能专注于内容创作本身。</p>
                <p>如果你对技术细节感兴趣，欢迎去我的 GitHub 看看，代码都是开源的 😊</p>
              </div>
            </div>

            <div class="about-section">
              <h2>📮 找到我</h2>
              <div class="contact-grid">
                <div class="contact-card">
                  <div class="contact-icon">🐙</div>
                  <h4>GitHub</h4>
                  <p>github.com/namesunyahui</p>
                </div>
                <div class="contact-card">
                  <div class="contact-icon">📧</div>
                  <h4>邮箱</h4>
                  <p>sunyahui2026@163.com</p>
                </div>
              </div>
            </div>

            <div class="about-footer">
              <p>感谢你的来访 🙏</p>
              <p>希望这里的某些文字，能在某个时刻，给你带来一丝温暖或启发</p>
              <p class="signature">—— Syh</p>
            </div>
          </el-card>
        </div>
      </el-main>

      <el-footer>
        <p>&copy; 2025 Syh Blog. 用心记录，用爱分享 ✨</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout } from '@/api/auth'

const router = useRouter()
const userInfo = ref<any>({})

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 检查登录状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 跳转到登录页面
const goToLogin = () => {
  router.push('/admin/login')
}

// 跳转到管理后台
const goToAdmin = () => {
  router.push('/admin/dashboard')
}

// 退出登录
const handleLogout = async () => {
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
  loadUserInfo()
})
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.about-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.el-container {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.el-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 0;
  width: 100%;
  flex-shrink: 0;
  height: 47px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 47px;
  padding: 0 40px;
  gap: 60px;
  width: 100%;
}

.site-title a {
  text-decoration: none;
  color: #4a5568;
  font-size: 22px;
  font-weight: bold;
  display: block;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  gap: 30px;
  flex: 1;
  justify-content: center;
  margin: 0;
}

.nav-menu a {
  text-decoration: none;
  color: #333;
  transition: all 0.3s ease;
  font-weight: 500;
  position: relative;
  padding: 5px 0;
}

.nav-menu a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  transition: width 0.3s ease;
}

.nav-menu a:hover {
  color: #4a5568;
}

.nav-menu a:hover::after {
  width: 100%;
}

.right-section {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 15px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(52, 73, 94, 0.1) 100%);
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.login-btn {
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  border: none;
  padding: 8px 24px;
  font-weight: 500;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 85, 104, 0.3);
}

.el-main {
  padding: 87px 20px 40px 20px;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

.about-content {
  max-width: 900px;
  margin: 0 auto;
}

.main-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.main-card :deep(.el-card__body) {
  padding: 50px;
}

.about-header {
  text-align: center;
  margin-bottom: 50px;
  padding-bottom: 30px;
  border-bottom: 2px solid rgba(74, 85, 104, 0.1);
}

.about-title {
  font-size: 42px;
  font-weight: bold;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 15px;
}

.about-subtitle {
  font-size: 18px;
  color: #909399;
  font-weight: 500;
}

.about-section {
  margin-bottom: 40px;
}

.about-section h2 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.about-section p {
  line-height: 1.8;
  color: #606266;
  margin-bottom: 15px;
  font-size: 16px;
}

.tech-stack {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
  margin-top: 20px;
}

.tech-category h3 {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 15px;
}

.tech-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tech-item {
  display: flex;
  align-items: start;
  gap: 15px;
  padding: 15px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.tech-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(74, 85, 104, 0.1);
}

.tech-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.tech-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.tech-info p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-list li {
  display: flex;
  align-items: start;
  gap: 15px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.feature-list li:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(74, 85, 104, 0.15);
}

.feature-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.feature-content h4 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.feature-content p {
  margin: 0;
  font-size: 15px;
  color: #606266;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid rgba(74, 85, 104, 0.1);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(74, 85, 104, 0.2);
  border-color: rgba(74, 85, 104, 0.3);
}

.feature-card-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.feature-card h4 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.feature-card p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.design-philosophy {
  padding: 25px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-radius: 8px;
  border-left: 4px solid #4a5568;
}

.design-philosophy p {
  margin-bottom: 12px;
}

.design-philosophy p:last-child {
  margin-bottom: 0;
}

.design-philosophy strong {
  color: #4a5568;
  font-weight: 600;
}

.about-footer {
  text-align: center;
  margin-top: 50px;
  padding-top: 30px;
  border-top: 2px solid rgba(74, 85, 104, 0.1);
}

.about-footer p {
  color: #909399;
  font-size: 15px;
  margin-bottom: 8px;
}

.about-footer p:last-child {
  margin-bottom: 0;
}

.about-footer .signature {
  font-size: 16px;
  color: #4a5568;
  font-weight: 600;
  margin-top: 8px;
}

/* 联系方式网格 */
.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.contact-card {
  text-align: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid rgba(74, 85, 104, 0.1);
}

.contact-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(74, 85, 104, 0.2);
  border-color: rgba(74, 85, 104, 0.3);
}

.contact-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.contact-card h4 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.contact-card p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
    height: auto;
    padding: 15px 20px;
    gap: 15px;
  }

  .site-title a {
    font-size: 18px;
  }

  .nav-menu {
    gap: 15px;
    flex-wrap: wrap;
    justify-content: center;
    order: 3;
    width: 100%;
  }

  .right-section {
    order: 2;
  }

  .user-info .username {
    display: none;
  }

  .el-header {
    height: auto;
    min-height: 47px;
  }

  .el-main {
    padding: 62px 20px 15px 20px;
  }

  .main-card :deep(.el-card__body) {
    padding: 30px 20px;
  }

  .about-title {
    font-size: 32px;
  }

  .about-subtitle {
    font-size: 16px;
  }

  .about-section h2 {
    font-size: 24px;
  }

  .tech-stack {
    grid-template-columns: 1fr;
  }

  .feature-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }

  .contact-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 15px;
  }

  .el-main {
    padding: 20px 15px;
  }

  .main-card :deep(.el-card__body) {
    padding: 25px 15px;
  }

  .about-title {
    font-size: 28px;
  }

  .about-subtitle {
    font-size: 14px;
  }

  .about-section h2 {
    font-size: 22px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .contact-grid {
    grid-template-columns: 1fr;
  }

  .nav-menu {
    gap: 10px;
  }

  .nav-menu a {
    font-size: 14px;
  }
}

/* Footer */
.el-footer {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  text-align: center;
  color: #909399;
  padding: 20px 40px;
  border-top: 1px solid rgba(74, 85, 104, 0.1);
  flex-shrink: 0;
}
</style>
