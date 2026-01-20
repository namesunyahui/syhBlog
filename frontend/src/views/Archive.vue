<template>
  <div class="archive-container">
    <el-container>
      <!-- 导航栏 -->
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

      <!-- 主体内容 -->
      <el-main>
        <div class="main-content">
          <!-- 左侧归档内容 -->
          <div class="archive-content">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>

            <!-- 空状态 -->
            <div v-else-if="archives.length === 0" class="empty-state">
              <div class="empty-icon">📭</div>
              <div class="empty-text">暂无归档文章</div>
            </div>

            <!-- 归档时间线 -->
            <template v-else>
              <div class="archive-header">
                <h2>📦 文章归档</h2>
                <p class="archive-stats">
                  共 {{ totalCount }} 篇文章，
                  分布在 {{ statistics.totalYears }} 个年份的
                  {{ statistics.totalMonths }} 个月份中
                </p>
              </div>

              <!-- 年份分组 -->
              <div v-for="yearArchive in archives" :key="yearArchive.year" class="year-group">
                <!-- 年份头部 -->
                <div class="year-header" @click="toggleYear(yearArchive.year)">
                  <div class="year-title">
                    <span class="year-icon">{{ isYearExpanded(yearArchive.year) ? '📂' : '📁' }}</span>
                    <span class="year-text">{{ yearArchive.year }}年</span>
                    <span class="year-count">{{ getYearTotalCount(yearArchive) }} 篇</span>
                  </div>
                  <el-icon class="toggle-icon" :class="{ 'is-expanded': isYearExpanded(yearArchive.year) }">
                    <ArrowDown />
                  </el-icon>
                </div>

                <!-- 月份列表 -->
                <el-collapse-transition>
                  <div v-show="isYearExpanded(yearArchive.year)" class="months-container">
                    <!-- 月份分组 -->
                    <div v-for="monthArchive in yearArchive.months" :key="monthArchive.month" class="month-group">
                      <!-- 月份头部 -->
                      <div class="month-header" @click="toggleMonth(yearArchive.year, monthArchive.month)">
                        <span class="month-icon">{{ isMonthExpanded(yearArchive.year, monthArchive.month) ? '📖' : '📕' }}</span>
                        <span class="month-text">{{ monthArchive.month }}月</span>
                        <span class="month-count">{{ monthArchive.count }} 篇</span>
                        <el-icon class="toggle-icon" :class="{ 'is-expanded': isMonthExpanded(yearArchive.year, monthArchive.month) }">
                          <ArrowDown />
                        </el-icon>
                      </div>

                      <!-- 文章列表 -->
                      <el-collapse-transition>
                        <div v-show="isMonthExpanded(yearArchive.year, monthArchive.month)" class="articles-list">
                          <div v-for="article in monthArchive.articles" :key="article.id" class="archive-article">
                            <router-link :to="`/article/${article.id}`" class="article-link">
                              <div class="article-title">{{ article.title }}</div>
                              <div class="article-meta">
                                <span class="meta-date">📅 {{ formatFullDate(article.createdAt) }}</span>
                                <span v-if="article.category" class="meta-category">📁 {{ article.category.name }}</span>
                                <span class="meta-views">👁️ {{ article.viewCount }}</span>
                                <span v-if="article.tags && article.tags.length" class="meta-tags">
                                  🏷️
                                  <span v-for="(tag, index) in article.tags" :key="tag.id">
                                    {{ tag.name }}{{ index < article.tags.length - 1 ? ', ' : '' }}
                                  </span>
                                </span>
                              </div>
                            </router-link>
                          </div>
                        </div>
                      </el-collapse-transition>
                    </div>
                  </div>
                </el-collapse-transition>
              </div>
            </template>
          </div>

          <!-- 右侧边栏 -->
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

      <!-- 页脚 -->
      <el-footer>
        <p>&copy; 2025 Syh Blog. 用心记录，用爱分享 ✨</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGroupedArchive } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { logout } from '@/api/auth'

const router = useRouter()

// 类型定义
interface Article {
  id: number
  title: string
  createdAt: string
  viewCount: number
  category?: { id: number; name: string }
  tags?: Array<{ id: number; name: string }>
}

interface MonthArchive {
  month: number
  count: number
  articles: Article[]
}

interface ArchiveDTO {
  year: number
  months: MonthArchive[]
}

interface Statistics {
  totalYears: number
  totalMonths: number
}

interface ArchiveVO {
  archives: ArchiveDTO[]
  totalCount: number
  statistics: Statistics
}

// 响应式数据
const archives = ref<ArchiveDTO[]>([])
const totalCount = ref(0)
const statistics = ref<Statistics>({ totalYears: 0, totalMonths: 0 })
const loading = ref(true)

// 侧边栏数据
const categories = ref([])
const tags = ref([])
const searchKeyword = ref('')
const userInfo = ref<any>({})

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 检查登录状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 展开状态管理
const expandedYears = ref<Set<number>>(new Set())
const expandedMonths = ref<Map<string, Set<number>>>(new Map())

// 计算某年文章总数
const getYearTotalCount = (yearArchive: ArchiveDTO) => {
  return yearArchive.months.reduce((sum, month) => sum + month.count, 0)
}

// 判断年份是否展开
const isYearExpanded = (year: number) => {
  return expandedYears.value.has(year)
}

// 切换年份展开状态
const toggleYear = (year: number) => {
  if (expandedYears.value.has(year)) {
    expandedYears.value.delete(year)
  } else {
    expandedYears.value.add(year)
  }
}

// 判断月份是否展开
const isMonthExpanded = (year: number, month: number) => {
  const yearKey = year.toString()
  const months = expandedMonths.value.get(yearKey)
  return months ? months.has(month) : false
}

// 切换月份展开状态
const toggleMonth = (year: number, month: number) => {
  const yearKey = year.toString()
  if (!expandedMonths.value.has(yearKey)) {
    expandedMonths.value.set(yearKey, new Set())
  }
  const months = expandedMonths.value.get(yearKey)!
  if (months.has(month)) {
    months.delete(month)
  } else {
    months.add(month)
  }
}

// 格式化完整日期
const formatFullDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 加载归档数据
const loadArchive = async () => {
  loading.value = true
  try {
    const res = await getGroupedArchive()
    const data: ArchiveVO = res.data
    archives.value = data.archives || []
    totalCount.value = data.totalCount || 0
    statistics.value = data.statistics || { totalYears: 0, totalMonths: 0 }

    // 默认展开最近年份的第一个月
    if (archives.value.length > 0) {
      const latestYear = archives.value[0].year
      expandedYears.value.add(latestYear)
      if (archives.value[0].months.length > 0) {
        const firstMonth = archives.value[0].months[0].month
        expandedMonths.value.set(latestYear.toString(), new Set([firstMonth]))
      }
    }
  } catch (error) {
    console.error('加载归档失败', error)
    ElMessage.error('加载归档失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
    categories.value = []
  }
}

// 加载标签
const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败', error)
    tags.value = []
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
  router.push({ path: '/search', query: { tag: tagName } })
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
  loadArchive()
  loadCategories()
  loadTags()
  loadUserInfo()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.archive-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.archive-container::before {
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

.el-main {
  padding: 20px 40px;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

/* 导航栏样式 */
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
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  transition: width 0.3s ease;
}

.nav-menu a:hover {
  color: #4a5568;
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

/* 主体内容布局 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 40px;
  align-items: start;
  width: 100%;
  max-width: 100%;
}

.archive-content {
  min-height: 0;
  max-width: 100%;
}

/* 归档头部 */
.archive-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.archive-header h2 {
  margin: 0 0 15px 0;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.archive-stats {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

/* 年份分组 */
.year-group {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.year-group:hover {
  box-shadow: 0 8px 30px rgba(74, 85, 104, 0.2);
}

/* 年份头部 */
.year-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  cursor: pointer;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-bottom: 1px solid rgba(74, 85, 104, 0.1);
  transition: all 0.3s ease;
}

.year-header:hover {
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(44, 62, 80, 0.1) 100%);
}

.year-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.year-icon {
  font-size: 24px;
}

.year-text {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.year-count {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
  background: rgba(74, 85, 104, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
}

.toggle-icon {
  transition: transform 0.3s ease;
  color: #909399;
}

.toggle-icon.is-expanded {
  transform: rotate(180deg);
}

/* 月份容器 */
.months-container {
  padding: 15px;
}

/* 月份分组 */
.month-group {
  margin-bottom: 10px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(74, 85, 104, 0.03);
}

.month-group:last-child {
  margin-bottom: 0;
}

/* 月份头部 */
.month-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.month-header:hover {
  background: rgba(74, 85, 104, 0.08);
}

.month-icon {
  font-size: 18px;
}

.month-text {
  font-size: 18px;
  font-weight: 600;
  color: #4a5568;
}

.month-count {
  font-size: 13px;
  color: #909399;
  margin-left: auto;
}

/* 文章列表 */
.articles-list {
  padding: 10px 20px 15px 48px;
}

/* 单篇文章 */
.archive-article {
  margin-bottom: 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.archive-article:last-child {
  margin-bottom: 0;
}

.article-link {
  display: block;
  text-decoration: none;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: white;
  border: 1px solid rgba(74, 85, 104, 0.1);
}

.article-link:hover {
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-color: rgba(74, 85, 104, 0.2);
  transform: translateX(5px);
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.5;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}

.meta-date,
.meta-category,
.meta-views,
.meta-tags {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-category {
  color: #4a5568;
}

.meta-views {
  color: #67c23a;
}

/* 侧边栏样式 */
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

/* 加载和空状态 */
.loading-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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
  font-size: 72px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 18px;
  color: #909399;
}

/* 页脚 */
.el-footer {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  text-align: center;
  color: #909399;
  padding: 20px 40px;
  border-top: 1px solid rgba(74, 85, 104, 0.1);
  flex-shrink: 0;
}

/* 响应式设计 */
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

  .archive-header h2 {
    font-size: 24px;
  }

  .year-header {
    padding: 15px 20px;
  }

  .year-text {
    font-size: 20px;
  }

  .articles-list {
    padding-left: 20px;
  }

  .article-meta {
    font-size: 12px;
    gap: 10px;
  }
}
</style>
