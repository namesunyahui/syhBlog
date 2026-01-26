<template>
  <div class="category-page">
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
          <div class="content-wrapper">
            <!-- 分类列表 -->
            <div class="category-section">
              <!-- 加载状态 -->
              <div v-if="loading" class="skeleton-container">
                <el-skeleton v-for="i in 6" :key="i" animated>
                  <template #template>
                    <el-skeleton-item variant="rect" style="width: 100%; height: 60px; margin-bottom: 12px; border-radius: 8px;" />
                  </template>
                </el-skeleton>
              </div>

              <!-- 空状态 -->
              <div v-else-if="categories.length === 0" class="empty-state">
                <div class="empty-icon">📂</div>
                <div class="empty-text">暂无分类</div>
              </div>

              <!-- 分类卡片列表 -->
              <div v-else class="category-grid">
                <el-card
                  v-for="category in categories"
                  :key="category.id"
                  class="category-card"
                  :class="{ active: selectedCategoryId === category.id }"
                  shadow="hover"
                  @click="handleCategoryClick(category)"
                >
                  <div class="category-info">
                    <div class="category-name">
                      <span class="category-icon">{{ getCategoryIcon(category.name) }}</span>
                      {{ category.name }}
                    </div>
                    <div class="category-count">{{ category.articleCount || 0 }} 篇文章</div>
                  </div>
                  <div v-if="category.description" class="category-description">
                    {{ category.description }}
                  </div>
                </el-card>
              </div>
            </div>

            <!-- 分类文章列表 -->
            <div v-if="selectedCategoryId" class="articles-section">
              <div class="section-header">
                <h2 class="section-title">
                  <span class="title-icon">📝</span>
                  {{ currentCategory?.name }} - 文章列表
                </h2>
                <el-button size="small" @click="handleClearCategory" v-if="selectedCategoryId">
                  清除筛选
                </el-button>
              </div>

              <!-- 文章加载状态 -->
              <div v-if="articlesLoading" class="skeleton-container">
                <el-skeleton v-for="i in 3" :key="i" animated>
                  <template #template>
                    <el-skeleton-item variant="rect" style="width: 100%; height: 150px; margin-bottom: 16px; border-radius: 12px;" />
                  </template>
                </el-skeleton>
              </div>

              <!-- 文章空状态 -->
              <div v-else-if="articles.length === 0" class="empty-state">
                <div class="empty-icon">📝</div>
                <div class="empty-text">该分类下暂无文章</div>
              </div>

              <!-- 文章列表 -->
              <template v-else>
                <el-card v-for="article in articles" :key="article.id" class="article-card">
                  <h3 class="article-title">
                    <router-link :to="`/article/${article.id}`">
                      {{ article.title }}
                    </router-link>
                  </h3>
                  <div class="article-meta">
                    <span class="meta-item">
                      {{ formatDate(article.createdAt) }}
                    </span>
                    <span class="meta-item">
                      {{ article.viewCount }} 阅读
                    </span>
                  </div>
                  <p v-if="article.summary" class="article-summary">{{ article.summary }}</p>
                  <div class="article-tags" v-if="article.tags && article.tags.length">
                    <el-tag v-for="tag in article.tags" :key="tag.id" size="small">
                      {{ tag.name }}
                    </el-tag>
                  </div>
                </el-card>

                <!-- 分页 -->
                <el-pagination
                  v-model:current-page="currentPage"
                  :page-size="pageSize"
                  :total="total"
                  layout="prev, pager, next"
                  @current-change="handlePageChange"
                />
              </template>
            </div>

            <!-- 全部文章列表（未选择分类时） -->
            <div v-else class="hint-section">
              <el-card class="hint-card">
                <div class="hint-content">
                  <div class="hint-icon">👆</div>
                  <div class="hint-text">点击上方分类卡片查看该分类下的文章</div>
                </div>
              </el-card>
            </div>
          </div>

          <!-- 侧边栏 -->
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
                  <router-link :to="{ path: '/category', query: { categoryId: category.id } }">
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { getArticleList } from '@/api/article'
import { getTagList } from '@/api/tag'

interface Category {
  id: number
  name: string
  description?: string
  articleCount?: number
}

interface Article {
  id: number
  title: string
  summary?: string
  viewCount: number
  createdAt: string
  tags?: any[]
}

const router = useRouter()
const route = useRoute()
const categories = ref<Category[]>([])
const articles = ref<Article[]>([])
const tags = ref<any[]>([])
const loading = ref(false)
const articlesLoading = ref(false)
const selectedCategoryId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// 用户相关
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : {}
})
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const currentCategory = computed(() => {
  return categories.value.find(c => c.id === selectedCategoryId.value)
})

// 加载分类列表
const loadCategories = async () => {
  try {
    loading.value = true
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

// 加载文章列表
const loadArticles = async (page: number = 1) => {
  if (!selectedCategoryId.value) return

  try {
    articlesLoading.value = true
    const res = await getArticleList({
      page,
      size: pageSize.value,
      categoryId: selectedCategoryId.value
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    articlesLoading.value = false
  }
}

// 点击分类
const handleCategoryClick = (category: Category) => {
  if (selectedCategoryId.value === category.id) {
    // 如果点击的是已选中的分类，则取消选中
    selectedCategoryId.value = null
    articles.value = []
    // 清除 URL 查询参数
    router.replace({ path: '/category', query: {} })
  } else {
    selectedCategoryId.value = category.id
    currentPage.value = 1
    loadArticles(1)
    // 更新 URL 查询参数
    router.replace({ path: '/category', query: { categoryId: category.id } })
  }
}

// 清除分类筛选
const handleClearCategory = () => {
  selectedCategoryId.value = null
  articles.value = []
  // 清除 URL 查询参数
  router.replace({ path: '/category', query: {} })
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles(page)
}

// 获取分类图标
const getCategoryIcon = (name: string) => {
  const iconMap: Record<string, string> = {
    '技术': '💻',
    '生活': '🌱',
    '学习': '📚',
    '随笔': '✍️',
    '教程': '📖',
    '分享': '🎁',
    '默认': '📁'
  }
  return iconMap[name] || '📁'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 路由跳转
const goToLogin = () => {
  router.push('/admin/login')
}

const goToAdmin = () => {
  router.push('/admin/dashboard')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('退出登录成功')
  router.push('/')
}

// 加载标签列表
const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

// 搜索处理
const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

// 标签点击处理
const handleTagClick = (tagName: string) => {
  router.push({ path: '/tag', query: { tag: tagName } })
}

onMounted(() => {
  loadCategories()
  loadTags()
  // 检查 URL 查询参数，如果有 categoryId 则自动选中该分类
  const categoryIdFromQuery = route.query.categoryId
  if (categoryIdFromQuery) {
    const categoryId = Number(categoryIdFromQuery)
    // 延迟执行，确保分类列表已加载
    setTimeout(() => {
      const category = categories.value.find(c => c.id === categoryId)
      if (category) {
        selectedCategoryId.value = categoryId
        loadArticles(1)
      }
    }, 100)
  }
})

// 监听路由查询参数变化
watch(() => route.query.categoryId, (newCategoryId) => {
  if (newCategoryId) {
    const categoryId = Number(newCategoryId)
    const category = categories.value.find(c => c.id === categoryId)
    if (category && selectedCategoryId.value !== categoryId) {
      selectedCategoryId.value = categoryId
      currentPage.value = 1
      loadArticles(1)
    }
  } else {
    // 查询参数被清除，重置状态
    selectedCategoryId.value = null
    articles.value = []
  }
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.category-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.category-page::before {
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

/* Header */
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

.site-title {
  font-size: 22px;
  font-weight: bold;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
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

/* Main Content */
.el-main {
  padding: 67px 40px 20px 40px;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 40px;
  align-items: start;
  width: 100%;
  max-width: 100%;
}

.content-wrapper {
  min-height: 0;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Section */
.section-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
}

/* Category Grid */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.category-card {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.category-card.active {
  border-color: #4a5568;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
}

.category-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.category-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  font-size: 24px;
}

.category-count {
  font-size: 14px;
  color: #999;
}

.category-description {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
  line-height: 1.5;
}

/* Articles Section */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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
  box-shadow: 0 8px 30px rgba(74, 85, 104, 0.2);
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
  color: #4a5568;
}

.article-meta {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.article-meta span::before {
  content: '';
  width: 4px;
  height: 4px;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
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
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(44, 62, 80, 0.1) 100%);
  border-color: transparent;
  color: #4a5568;
  font-weight: 500;
}

/* Pagination */
.el-pagination {
  margin-top: 30px;
  justify-content: center;
}

.el-pagination :deep(.el-pager li) {
  border-radius: 8px;
  font-weight: 500;
}

.el-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
}

.el-pagination :deep(.btn-prev),
.el-pagination :deep(.btn-next) {
  border-radius: 8px;
}

/* Empty State */
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
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  color: #999;
}

/* Hint Section */
.hint-card {
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(44, 62, 80, 0.1) 100%);
  border: 2px dashed #4a5568;
}

.hint-content {
  text-align: center;
  padding: 40px 20px;
}

.hint-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.hint-text {
  font-size: 16px;
  color: #4a5568;
  font-weight: 500;
}

/* Skeleton */
.skeleton-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Sidebar */
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
  box-shadow: 0 8px 30px rgba(74, 85, 104, 0.15);
}

.sidebar-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-bottom: 1px solid rgba(74, 85, 104, 0.1);
  padding: 18px 20px;
}

.sidebar-card h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
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
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(44, 62, 80, 0.1) 100%);
  color: #4a5568;
  transform: translateX(5px);
}

.category-list .count {
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
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
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(44, 62, 80, 0.1) 100%);
  border-color: transparent;
  color: #4a5568;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tag-item:hover {
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  color: white;
  transform: scale(1.05);
}

/* Responsive */
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

  .el-main {
    padding: 62px 20px 15px 20px;
  }

  .el-header {
    height: auto;
    min-height: 47px;
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
