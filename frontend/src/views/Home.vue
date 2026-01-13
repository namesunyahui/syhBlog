<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1 class="site-title">Syh Blog</h1>
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
        <div class="main-content">
          <div class="article-list">
            <!-- 加载状态 -->
            <div v-if="loading" class="skeleton-container">
              <el-skeleton v-for="i in 3" :key="i" animated>
                <template #template>
                  <el-skeleton-item variant="rect" style="width: 100%; height: 200px; margin-bottom: 20px; border-radius: 16px;" />
                </template>
              </el-skeleton>
            </div>

            <!-- 空状态 -->
            <div v-else-if="articles.length === 0" class="empty-state">
              <div class="empty-icon">📝</div>
              <div class="empty-text">暂无文章</div>
              <div class="empty-hint">敬请期待更多精彩内容</div>
            </div>

            <!-- 文章列表 -->
            <template v-else>
              <el-card v-for="article in articles" :key="article.id" class="article-card">
                <h2 class="article-title">
                  <router-link :to="`/article/${article.id}`">
                    {{ article.title }}
                  </router-link>
                </h2>
                <div class="article-meta">
                  <span>{{ formatDate(article.createdAt) }}</span>
                  <span v-if="article.category">{{ article.category.name }}</span>
                  <span>{{ article.viewCount }} 阅读</span>
                </div>
                <p class="article-summary">{{ article.summary }}</p>
                <div class="article-tags" v-if="article.tags && article.tags.length">
                  <el-tag v-for="tag in article.tags" :key="tag.id" size="small">
                    {{ tag.name }}
                  </el-tag>
                </div>
              </el-card>

              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                @current-change="handlePageChange"
              />
            </template>
          </div>

          <div class="sidebar">
            <el-card class="sidebar-card">
              <template #header>
                <h3>🔍 搜索</h3>
              </template>
              <el-input
                v-model="searchKeyword"
                placeholder="输入关键词搜索文章..."
                @keyup.enter="handleSearch"
                clearable
              >
                <template #append>
                  <el-button :icon="Search" @click="handleSearch">搜索</el-button>
                </template>
              </el-input>
            </el-card>

            <el-card class="sidebar-card">
              <template #header>
                <h3>📁 分类</h3>
              </template>
              <ul class="category-list" v-if="categories.length">
                <li v-for="category in categories" :key="category.id">
                  <router-link :to="`/category/${category.id}`">
                    <span>{{ category.name }}</span>
                    <span class="count">{{ category.articleCount }}</span>
                  </router-link>
                </li>
              </ul>
              <div v-else class="empty-state" style="padding: 30px 10px;">
                <div class="empty-text" style="font-size: 14px;">暂无分类</div>
              </div>
            </el-card>

            <el-card class="sidebar-card">
              <template #header>
                <h3>🏷️ 标签</h3>
              </template>
              <div class="tag-cloud" v-if="tags.length">
                <el-tag
                  v-for="tag in tags"
                  :key="tag.id"
                  class="tag-item"
                  @click="handleTagClick(tag.name)"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              <div v-else class="empty-state" style="padding: 30px 10px;">
                <div class="empty-text" style="font-size: 14px;">暂无标签</div>
              </div>
            </el-card>
          </div>
        </div>
      </el-main>

      <el-footer>
        <p>&copy; 2025 Syh Blog. 用心记录，用爱分享 ✨</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArticleList } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { logout } from '@/api/auth'

const router = useRouter()

const articles = ref([])
const categories = ref([])
const tags = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const userInfo = ref<any>({})
const loading = ref(true)

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 检查登录状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      page: currentPage.value,
      size: pageSize.value
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载文章失败', error)
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
    categories.value = []
  }
}

const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败', error)
    tags.value = []
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles()
}

const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

const handleTagClick = (tagName: string) => {
  router.push({ path: '/search', query: { tag: tagName } })
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

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
  loadArticles()
  loadCategories()
  loadTags()
  loadUserInfo()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.home-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.home-container::before {
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
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.el-main {
  padding: 20px 40px;
  flex: 1;
  width: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
  min-height: 0;
}

.el-main::-webkit-scrollbar {
  width: 8px;
}

.el-main::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.el-main::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 4px;
}

.el-main::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.5);
}

.el-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 0;
  width: 100%;
  flex-shrink: 0;
  height: 70px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
  padding: 0 40px;
  gap: 60px;
  width: 100%;
}

.site-title {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  flex-shrink: 0;
}

.nav-menu {
  display: flex;
  gap: 30px;
  flex: 1;
  justify-content: center;
  margin: 0;
}

.right-section {
  display: flex;
  align-items: center;
  flex-shrink: 0;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.nav-menu a:hover {
  color: #667eea;
}

.nav-menu a:hover::after {
  width: 100%;
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
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 8px 24px;
  font-weight: 500;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.el-main {
  padding: 30px 40px;
  flex: 1;
  width: 100%;
  overflow-y: auto;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 40px;
  align-items: start;
  width: 100%;
  max-width: 100%;
}

.article-list {
  min-height: 0;
  max-width: 100%;
}

.article-card {
  margin-bottom: 20px;
  border-radius: 16px;
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.2);
}

.article-card :deep(.el-card__body) {
  padding: 24px;
}

.article-title {
  margin-bottom: 12px;
}

.article-title a {
  text-decoration: none;
  color: #333;
  font-size: 22px;
  font-weight: 600;
  transition: color 0.3s ease;
}

.article-title a:hover {
  color: #667eea;
}

.article-meta {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.article-meta span {
  margin-right: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.article-meta span::before {
  content: '';
  width: 4px;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
}

.article-summary {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 15px;
  font-size: 15px;
}

.article-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.article-tags .el-tag {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-color: transparent;
  color: #667eea;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 18px;
  color: #909399;
  margin-bottom: 10px;
}

.empty-hint {
  font-size: 14px;
  color: #c0c4cc;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  margin-bottom: 0;
  border-radius: 16px;
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.sidebar-card:hover {
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.15);
}

.sidebar-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  padding: 18px 20px;
}

.sidebar-card h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-card :deep(.el-card__body) {
  padding: 20px;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li {
  padding: 0;
}

.category-list a {
  text-decoration: none;
  color: #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.category-list a:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  transform: translateX(5px);
}

.category-list .count {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: pointer;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-color: transparent;
  color: #667eea;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tag-item:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.05);
}

.el-footer {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  text-align: center;
  color: #909399;
  padding: 20px 40px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  flex-shrink: 0;
}

.el-pagination {
  margin-top: 30px;
  justify-content: center;
}

.el-pagination :deep(.el-pager li) {
  border-radius: 8px;
  font-weight: 500;
}

.el-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.el-pagination :deep(.btn-prev),
.el-pagination :deep(.btn-next) {
  border-radius: 8px;
}

@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr 300px;
    gap: 30px;
  }
}

@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .header-content {
    flex-wrap: wrap;
    height: auto;
    padding: 15px 20px;
    gap: 15px;
  }

  .site-title {
    font-size: 22px;
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

  .el-main {
    padding: 15px 20px;
  }

  .el-header {
    height: auto;
    min-height: 60px;
  }
}
</style>
