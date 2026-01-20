<template>
  <div class="search-page">
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
        <div class="search-container">
          <!-- 搜索输入区域 -->
          <el-card class="search-input-card">
            <el-input
              v-model="searchKeyword"
              placeholder="输入关键词搜索文章..."
              size="large"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>

            <!-- 筛选条件 -->
            <div class="filter-section">
              <div class="filter-item">
                <span class="filter-label">分类：</span>
                <el-select
                  v-model="selectedCategoryId"
                  placeholder="全部分类"
                  clearable
                  @change="handleFilterChange"
                  style="width: 150px"
                >
                  <el-option label="全部分类" :value="null" />
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </div>
              <div class="filter-item">
                <span class="filter-label">标签：</span>
                <el-select
                  v-model="selectedTagId"
                  placeholder="全部标签"
                  clearable
                  @change="handleFilterChange"
                  style="width: 150px"
                >
                  <el-option label="全部标签" :value="null" />
                  <el-option
                    v-for="tag in tags"
                    :key="tag.id"
                    :label="tag.name"
                    :value="tag.id"
                  />
                </el-select>
              </div>
              <div class="filter-item" v-if="hasActiveFilters">
                <el-button type="info" text @click="clearFilters">
                  清除筛选
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 搜索结果信息 -->
          <div v-if="searched" class="search-info">
            <div class="search-summary">
              <span v-if="searchKeyword" class="search-keyword">
                关键词：<strong>"{{ searchKeyword }}"</strong>
              </span>
              <span v-if="selectedCategoryId" class="search-filter">
                分类：<strong>{{ getCategoryName(selectedCategoryId) }}</strong>
              </span>
              <span v-if="selectedTagId" class="search-filter">
                标签：<strong>{{ getTagName(selectedTagId) }}</strong>
              </span>
              <span class="search-count">
                找到 <strong>{{ total }}</strong> 篇文章
              </span>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="skeleton-container">
            <el-skeleton v-for="i in 3" :key="i" animated>
              <template #template>
                <el-skeleton-item variant="rect" style="width: 100%; height: 180px; margin-bottom: 20px; border-radius: 12px;" />
              </template>
            </el-skeleton>
          </div>

          <!-- 空状态 -->
          <div v-else-if="searched && articles.length === 0" class="empty-state">
            <div class="empty-icon">🔍</div>
            <div class="empty-text">未找到相关文章</div>
            <div class="empty-hint">
              <span v-if="searchKeyword">试试调整关键词或筛选条件</span>
              <span v-else>请输入搜索关键词</span>
            </div>
          </div>

          <!-- 搜索结果列表 -->
          <div v-else-if="articles.length > 0" class="search-results">
            <el-card v-for="article in articles" :key="article.id" class="article-card">
              <h2 class="article-title">
                <router-link :to="`/article/${article.id}`" v-html="highlightText(article.title, searchKeyword)">
                </router-link>
              </h2>
              <div class="article-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(article.createdAt) }}
                </span>
                <span v-if="article.category" class="meta-item">
                  <el-icon><Folder /></el-icon>
                  {{ article.category.name }}
                </span>
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ article.viewCount }} 阅读
                </span>
              </div>
              <p class="article-summary" v-html="highlightText(article.summary, searchKeyword)"></p>
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
          </div>

          <!-- 初始状态 -->
          <div v-else class="empty-state">
            <div class="empty-icon">🔍</div>
            <div class="empty-text">搜索文章</div>
            <div class="empty-hint">输入关键词开始搜索</div>
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
import { useRouter, useRoute } from 'vue-router'
import { Search, Calendar, Folder, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { searchArticles } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const articles = ref([])
const categories = ref([])
const tags = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const selectedCategoryId = ref<number | null>(null)
const selectedTagId = ref<number | null>(null)
const userInfo = ref<any>({})
const loading = ref(false)
const searched = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 检查登录状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return selectedCategoryId.value !== null || selectedTagId.value !== null
})

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
  }
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

// 执行搜索
const performSearch = async () => {
  loading.value = true
  try {
    const res = await searchArticles({
      keyword: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value,
      categoryId: selectedCategoryId.value || undefined,
      tagId: selectedTagId.value || undefined
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
    searched.value = true
  } catch (error) {
    console.error('搜索失败', error)
    ElMessage.error('搜索失败，请稍后重试')
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索按钮点击
const handleSearch = () => {
  currentPage.value = 1
  performSearch()
}

// 处理筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1
  if (searched.value) {
    performSearch()
  }
}

// 清除筛选条件
const clearFilters = () => {
  selectedCategoryId.value = null
  selectedTagId.value = null
  currentPage.value = 1
  if (searched.value) {
    performSearch()
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  performSearch()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 高亮关键词
const highlightText = (text: string, keyword: string) => {
  if (!text || !keyword) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || ''
}

// 获取标签名称
const getTagName = (tagId: number) => {
  const tag = tags.value.find(t => t.id === tagId)
  return tag?.name || ''
}

// 格式化日期
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
  loadCategories()
  loadTags()
  loadUserInfo()

  // 从 URL 参数中获取搜索条件
  const { keyword, tag } = route.query
  if (keyword) {
    searchKeyword.value = keyword as string
  }
  if (tag) {
    // 通过标签名称搜索
    const foundTag = tags.value.find(t => t.name === tag)
    if (foundTag) {
      selectedTagId.value = foundTag.id
    }
  }

  // 如果有搜索条件，自动执行搜索
  if (keyword || tag) {
    performSearch()
  }
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.search-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.search-page::before {
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

.el-main {
  padding: 30px 40px;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

.search-container {
  max-width: 900px;
  margin: 0 auto;
}

.search-input-card {
  border-radius: 16px;
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  margin-bottom: 20px;
}

.search-input-card :deep(.el-card__body) {
  padding: 24px;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.search-info {
  margin-bottom: 20px;
}

.search-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.search-keyword,
.search-filter {
  color: #606266;
  font-size: 14px;
}

.search-count {
  margin-left: auto;
  color: #409eff;
  font-size: 14px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-card {
  border-radius: 16px;
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.article-card:hover {
  transform: translateY(-3px);
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

/* 关键词高亮样式 */
:deep(mark) {
  background: linear-gradient(135deg, rgba(255, 235, 59, 0.5) 0%, rgba(255, 193, 7, 0.5) 100%);
  color: #333;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 500;
}

.article-meta {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
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

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 20px;
  color: #909399;
  margin-bottom: 10px;
  font-weight: 500;
}

.empty-hint {
  font-size: 14px;
  color: #c0c4cc;
}

.el-footer {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  text-align: center;
  color: #909399;
  padding: 20px 40px;
  border-top: 1px solid rgba(74, 85, 104, 0.1);
  flex-shrink: 0;
}

.el-pagination {
  margin-top: 20px;
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

.skeleton-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@media (max-width: 768px) {
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

  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .search-count {
    margin-left: 0;
  }
}
</style>
