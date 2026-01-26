<template>
  <div class="tag-page">
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
            <!-- 标签筛选区域 -->
            <div class="tag-filter-section">
              <div class="filter-header">
                <h2 class="section-title">
                  <span class="title-icon">🏷️</span>
                  标签筛选
                </h2>
                <div class="filter-controls">
                  <!-- 搜索框 -->
                  <el-input
                    v-model="searchKeyword"
                    placeholder="搜索标签..."
                    clearable
                    class="search-input"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>

                  <!-- 排序选择器 -->
                  <el-select v-model="sortBy" class="sort-select">
                    <el-option label="按热门排序" value="hot" />
                    <el-option label="按名称排序" value="name" />
                  </el-select>
                </div>
              </div>

              <!-- 加载状态 -->
              <div v-if="loading" class="skeleton-container">
                <el-skeleton v-for="i in 6" :key="i" animated>
                  <template #template>
                    <el-skeleton-item variant="rect" style="width: 100px; height: 36px; margin-right: 12px; border-radius: 18px;" />
                  </template>
                </el-skeleton>
              </div>

              <!-- 空状态 -->
              <div v-else-if="filteredTags.length === 0" class="empty-state">
                <div class="empty-icon">🏷️</div>
                <div class="empty-text">暂无标签</div>
              </div>

              <!-- 标签云 -->
              <div v-else class="tag-cloud">
                <div
                  v-for="tag in filteredTags"
                  :key="tag.id"
                  class="tag-item"
                  :class="{ selected: selectedTagIds.includes(tag.id) }"
                  :style="{ fontSize: getTagFontSize(tag.articleCount || 0) }"
                  @click="handleTagClick(tag)"
                >
                  {{ tag.name }}
                  <span class="tag-count">{{ tag.articleCount }}</span>
                </div>
              </div>

              <!-- 已选标签展示区域 -->
              <div v-if="selectedTagIds.length" class="selected-tags-bar">
                <div class="selected-tags">
                  <span class="label">已选标签：</span>
                  <el-tag
                    v-for="tag in selectedTags"
                    :key="tag.id"
                    closable
                    @close="removeTag(tag.id)"
                    class="selected-tag"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
                <el-button size="small" @click="clearAllTags">清除全部</el-button>
              </div>
            </div>

            <!-- 文章列表区域 -->
            <div v-if="selectedTagIds.length" class="articles-section">
              <div class="section-header">
                <h2 class="section-title">
                  <span class="title-icon">📝</span>
                  文章列表
                  <span class="article-count">({{ total }} 篇)</span>
                </h2>
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
                <div class="empty-text">所选标签下暂无文章</div>
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

            <!-- 提示信息 -->
            <div v-else class="hint-section">
              <el-card class="hint-card">
                <div class="hint-content">
                  <div class="hint-icon">👆</div>
                  <div class="hint-text">点击上方标签查看该标签下的文章，支持多选标签进行筛选</div>
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
                v-model="searchKeyword2"
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
                  @click="handleTagClick(tag)"
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
import { getTagList } from '@/api/tag'
import { getCategoryList } from '@/api/category'
import { getArticlesByTags } from '@/api/article'

interface Tag {
  id: number
  name: string
  articleCount?: number
}

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

// 状态定义
const tags = ref<Tag[]>([])
const categories = ref<Category[]>([])
const selectedTagIds = ref<number[]>([])
const searchKeyword = ref('')
const searchKeyword2 = ref('')
const sortBy = ref<'hot' | 'name'>('hot')
const articles = ref<Article[]>([])
const loading = ref(false)
const articlesLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 用户相关
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : {}
})
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 计算属性：过滤和排序后的标签
const filteredTags = computed(() => {
  let result = [...tags.value]

  // 1. 关键词搜索
  if (searchKeyword.value) {
    result = result.filter(tag =>
      tag.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }

  // 2. 排序
  if (sortBy.value === 'hot') {
    result.sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0))
  } else if (sortBy.value === 'name') {
    result.sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
  }

  return result
})

// 计算属性：已选中的标签对象
const selectedTags = computed(() => {
  return tags.value.filter(tag => selectedTagIds.value.includes(tag.id))
})

// 加载标签列表
const loadTags = async () => {
  try {
    loading.value = true
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    ElMessage.error('加载标签列表失败')
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 标签点击处理
const handleTagClick = (tag: Tag) => {
  const index = selectedTagIds.value.indexOf(tag.id)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)  // 取消选中
  } else {
    selectedTagIds.value.push(tag.id)  // 添加选中
  }
  currentPage.value = 1
  loadArticles()
}

// 移除标签
const removeTag = (tagId: number) => {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
    currentPage.value = 1
    loadArticles()
  }
}

// 清除所有标签
const clearAllTags = () => {
  selectedTagIds.value = []
  articles.value = []
  total.value = 0
}

// 加载文章
const loadArticles = async () => {
  if (selectedTagIds.value.length === 0) {
    articles.value = []
    total.value = 0
    return
  }

  try {
    articlesLoading.value = true
    const res = await getArticlesByTags({
      tagIds: selectedTagIds.value.join(','),
      page: currentPage.value,
      size: pageSize.value
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载文章失败')
  } finally {
    articlesLoading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles()
}

// 计算标签字体大小
const getTagFontSize = (count: number) => {
  if (tags.value.length === 0) return '14px'

  const maxCount = Math.max(...tags.value.map(t => t.articleCount || 0))
  const minCount = Math.min(...tags.value.map(t => t.articleCount || 0))
  const minSize = 14
  const maxSize = 24

  if (maxCount === minCount) return `${minSize}px`

  const ratio = (count - minCount) / (maxCount - minCount)
  return `${minSize + ratio * (maxSize - minSize)}px`
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 搜索处理
const handleSearch = () => {
  if (searchKeyword2.value) {
    router.push({ path: '/search', query: { keyword: searchKeyword2.value } })
  }
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

onMounted(async () => {
  await loadTags()
  loadCategories()

  // 检查 URL 参数中是否有标签，如果有则自动选中
  const tagParam = route.query.tag as string
  if (tagParam && tags.value.length > 0) {
    const tag = tags.value.find(t => t.name === tagParam)
    if (tag) {
      selectedTagIds.value = [tag.id]
      await loadArticles()
    }
  }
})

// 监听路由参数变化
watch(
  () => route.query.tag,
  async (newTag) => {
    if (newTag && tags.value.length > 0) {
      const tag = tags.value.find(t => t.name === newTag)
      if (tag) {
        selectedTagIds.value = [tag.id]
        await loadArticles()
      }
    }
  }
)
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.tag-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.tag-page::before {
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
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
}

.article-count {
  font-size: 16px;
  color: #999;
  font-weight: normal;
}

/* Filter Section */
.tag-filter-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.filter-header {
  margin-bottom: 20px;
}

.filter-controls {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.search-input {
  flex: 1;
}

.sort-select {
  width: 150px;
}

/* Tag Cloud */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.tag-item {
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(44, 62, 80, 0.1) 100%);
  border: 1px solid rgba(74, 85, 104, 0.2);
  color: #4a5568;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  user-select: none;
}

.tag-item:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(74, 85, 104, 0.3);
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  color: white;
  border-color: transparent;
}

.tag-item.selected {
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(74, 85, 104, 0.3);
}

.tag-count {
  display: inline-block;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  padding: 0 6px;
  background: rgba(74, 85, 104, 0.2);
  border-radius: 10px;
  font-size: 12px;
}

.tag-item.selected .tag-count {
  background: rgba(255, 255, 255, 0.2);
}

/* Selected Tags Bar */
.selected-tags-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-top: 16px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-radius: 12px;
  border: 1px solid rgba(74, 85, 104, 0.1);
}

.selected-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  flex: 1;
}

.selected-tags .label {
  font-weight: 600;
  color: #4a5568;
  margin-right: 8px;
}

.selected-tag {
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  color: white;
  border: none;
  font-weight: 500;
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
  flex-wrap: wrap;
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

  .filter-controls {
    flex-direction: column;
  }

  .search-input,
  .sort-select {
    width: 100%;
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
