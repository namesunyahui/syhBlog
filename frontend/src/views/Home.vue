<template>
  <div class="home-page">
    <!-- Header -->
    <AppHeader />

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-container">
        <!-- 左侧内容区 -->
        <div class="content-left">
          <!-- 英雄区 - 超大标题 -->
          <section class="hero-section scroll-reveal">
            <div class="hero-content">
              <h1 class="hero-title">
                <span class="title-line">探索代码</span>
                <span class="title-line title-accent">与设计</span>
                <span class="title-line">的边界</span>
              </h1>
              <p class="hero-subtitle">
                在这里，我分享关于前端开发、用户体验设计和技术思考的见解
              </p>
            </div>
          </section>

          <!-- 筛选和排序 -->
          <div class="content-filter scroll-reveal">
            <div class="filter-group">
              <button
                v-for="filter in filters"
                :key="filter.key"
                :class="['filter-btn', { 'is-active': currentFilter === filter.key }]"
                @click="handleFilterChange(filter.key)"
              >
                {{ filter.label }}
              </button>
            </div>
          </div>

          <!-- 文章列表 -->
          <section class="articles-section">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <div v-for="i in 3" :key="i" class="skeleton-card">
                <div class="skeleton-image skeleton"></div>
                <div class="skeleton-content">
                  <div class="skeleton-meta skeleton"></div>
                  <div class="skeleton-title skeleton"></div>
                  <div class="skeleton-text skeleton"></div>
                  <div class="skeleton-text skeleton"></div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="articles.length === 0" class="empty-state scroll-reveal">
              <div class="empty-icon">📭</div>
              <h3 class="empty-title">暂无文章</h3>
              <p class="empty-text">敬请期待更多精彩内容</p>
            </div>

            <!-- 文章网格 -->
            <div v-else class="articles-grid">
              <ArticleCard
                v-for="(article, index) in articles"
                :key="article.id"
                :article="article"
                :is-featured="index === 0 && currentPage === 1"
                class="article-item scroll-reveal"
                :style="{ animationDelay: `${index * 100}ms` }"
              />
            </div>

            <!-- 分页 -->
            <div v-if="total > pageSize" class="pagination-wrapper">
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                :background="false"
                @current-change="handlePageChange"
              />
            </div>
          </section>
        </div>

        <!-- 右侧边栏 -->
        <aside class="sidebar">
          <!-- 搜索卡片 -->
          <div class="sidebar-card scroll-reveal">
            <div class="card-header">
              <h3 class="card-title">搜索</h3>
            </div>
            <div class="card-body">
              <div class="search-box">
                <input
                  v-model="searchKeyword"
                  type="text"
                  placeholder="搜索文章..."
                  class="search-input"
                  @keyup.enter="handleSearch"
                />
                <button class="search-btn cursor-interactive" data-cursor-label="搜索" @click="handleSearch">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"/>
                    <path d="M21 21l-4.35-4.35"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- 分类卡片 -->
          <div class="sidebar-card scroll-reveal">
            <div class="card-header">
              <h3 class="card-title">分类</h3>
            </div>
            <div class="card-body">
              <ul v-if="categories.length" class="category-list">
                <li v-for="category in categories" :key="category.id">
                  <router-link
                    :to="{ path: '/category', query: { categoryId: category.id } }"
                    class="category-link cursor-interactive"
                    data-cursor-label="查看"
                  >
                    <span class="category-name">{{ category.name }}</span>
                    <span class="category-count">{{ category.articleCount }}</span>
                  </router-link>
                </li>
              </ul>
              <div v-else class="empty-mini">
                <span>暂无分类</span>
              </div>
            </div>
          </div>

          <!-- 标签卡片 -->
          <div class="sidebar-card scroll-reveal">
            <div class="card-header">
              <h3 class="card-title">标签</h3>
            </div>
            <div class="card-body">
              <div v-if="tags.length" class="tag-cloud">
                <router-link
                  v-for="tag in tags"
                  :key="tag.id"
                  :to="{ path: '/tag', query: { tag: tag.name } }"
                  class="tag-cloud-item cursor-interactive"
                  data-cursor-label="查看"
                >
                  {{ tag.name }}
                </router-link>
              </div>
              <div v-else class="empty-mini">
                <span>暂无标签</span>
              </div>
            </div>
          </div>

          <!-- 站点信息 -->
          <div class="sidebar-card sidebar-about scroll-reveal">
            <div class="card-body">
              <div class="about-content">
                <div class="about-avatar">
                  <span class="avatar-symbol">§</span>
                </div>
                <h4 class="about-title">Syh Blog</h4>
                <p class="about-text">
                  用心记录，用爱分享。探索技术，分享见解。
                </p>
                <div class="about-stats">
                  <div class="stat">
                    <span class="stat-value">{{ total }}</span>
                    <span class="stat-label">文章</span>
                  </div>
                  <div class="stat">
                    <span class="stat-value">{{ categories.length }}</span>
                    <span class="stat-label">分类</span>
                  </div>
                  <div class="stat">
                    <span class="stat-value">{{ tags.length }}</span>
                    <span class="stat-label">标签</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="footer-content">
        <p class="footer-text">
          &copy; {{ new Date().getFullYear() }} Syh Blog. Made with
          <span class="footer-heart">♥</span> by SunYaHui
        </p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getArticleList } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'

const route = useRoute()

const articles = ref([])
const categories = ref([])
const tags = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const loading = ref(true)
const currentFilter = ref('latest')

const filters = [
  { key: 'latest', label: '最新发布' },
  { key: 'popular', label: '最受欢迎' },
  { key: 'trending', label: '热门趋势' }
]

// 加载文章列表
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

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
    categories.value = []
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败', error)
    tags.value = []
  }
}

// 分页切换
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 筛选切换
const handleFilterChange = (filter: string) => {
  currentFilter.value = filter
  // 这里可以根据不同的筛选值调用不同的API
  loadArticles()
}

// 搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    // 跳转到搜索页面
    window.location.href = `/search?keyword=${encodeURIComponent(searchKeyword.value)}`
  }
}

// 滚动触发动画
const handleScrollReveal = () => {
  const elements = document.querySelectorAll('.scroll-reveal')
  const windowHeight = window.innerHeight

  elements.forEach((element) => {
    const elementTop = (element as HTMLElement).offsetTop
    const elementVisible = 150

    if (elementTop < windowHeight - elementVisible) {
      element.classList.add('is-visible')
    }
  })
}

onMounted(() => {
  loadArticles()
  loadCategories()
  loadTags()

  // 添加滚动监听
  window.addEventListener('scroll', handleScrollReveal)
  // 初始触发一次
  handleScrollReveal()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScrollReveal)
})

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/') {
      loadArticles()
      handleScrollReveal()
    }
  }
)
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-top: 72px; /* Header 高度 */
}

/* ----- 主内容区 ----- */
.main-content {
  width: 100%;
  padding: var(--space-12) 0;
}

.content-container {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: var(--space-12);
  max-width: var(--container-2xl);
  margin: 0 auto;
  padding: 0 var(--space-8);
}

/* ----- 左侧内容区 ----- */
.content-left {
  display: flex;
  flex-direction: column;
  gap: var(--space-12);
}

/* ----- 英雄区 ----- */
.hero-section {
  padding: var(--space-16) 0;
  border-bottom: 1px solid var(--border-subtle);
}

.hero-content {
  max-width: 800px;
}

.hero-title {
  font-family: var(--font-display);
  font-size: var(--text-8xl);
  font-weight: var(--font-black);
  line-height: 0.95;
  letter-spacing: -0.04em;
  margin: 0 0 var(--space-8);
}

.title-line {
  display: block;
  color: var(--text-primary);
  opacity: 0;
  animation: slideInUp 0.8s var(--ease-out) forwards;
}

.title-line:nth-child(1) {
  animation-delay: 0.1s;
}

.title-line:nth-child(2) {
  animation-delay: 0.2s;
}

.title-line:nth-child(3) {
  animation-delay: 0.3s;
}

.title-accent {
  color: var(--accent-gold);
  position: relative;
}

.title-accent::after {
  content: '';
  position: absolute;
  bottom: 0.05em;
  left: 0;
  width: 100%;
  height: 0.08em;
  background: var(--accent-gold);
  opacity: 0.5;
}

.hero-subtitle {
  font-family: var(--font-body);
  font-size: var(--text-xl);
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  opacity: 0;
  animation: fadeIn 0.8s var(--ease-out) 0.4s forwards;
}

/* ----- 筛选区 ----- */
.content-filter {
  padding: var(--space-4) 0;
}

.filter-group {
  display: flex;
  gap: var(--space-3);
}

.filter-btn {
  padding: var(--space-2) var(--space-5);
  background: transparent;
  border: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  cursor: pointer;
  cursor: none; /* 使用自定义光标 */
  transition: all 0.3s var(--ease-out);
}

.filter-btn:hover {
  border-color: var(--border-accent);
  color: var(--text-primary);
}

.filter-btn.is-active {
  background: var(--accent-gold);
  border-color: var(--accent-gold);
  color: var(--bg-primary);
}

/* ----- 文章区 ----- */
.articles-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-8);
}

.article-item {
  animation: cascadeIn 0.6s var(--ease-out) forwards;
  opacity: 0;
}

/* ----- 加载状态 ----- */
.loading-state {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-8);
}

.skeleton-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  padding-top: 56.25%;
}

.skeleton-content {
  padding: var(--space-6);
}

.skeleton-meta {
  width: 120px;
  height: 16px;
  margin-bottom: var(--space-4);
  border-radius: var(--radius-sm);
}

.skeleton-title {
  width: 80%;
  height: 28px;
  margin-bottom: var(--space-4);
  border-radius: var(--radius-sm);
}

.skeleton-text {
  width: 100%;
  height: 16px;
  margin-bottom: var(--space-2);
  border-radius: var(--radius-sm);
}

.skeleton-text:last-child {
  width: 60%;
}

/* ----- 空状态 ----- */
.empty-state {
  text-align: center;
  padding: var(--space-20) var(--space-8);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--space-6);
  opacity: 0.5;
}

.empty-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-3);
}

.empty-text {
  font-family: var(--font-body);
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
}

/* ----- 分页 ----- */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--space-8) 0;
}

:deep(.el-pagination) {
  display: flex;
  gap: var(--space-2);
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next),
:deep(.el-pagination .el-pager li) {
  background: transparent;
  border: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-weight: var(--font-medium);
  border-radius: var(--radius-md);
  transition: all 0.3s var(--ease-out);
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover),
:deep(.el-pagination .el-pager li:hover) {
  border-color: var(--border-accent);
  color: var(--text-primary);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--accent-gold);
  border-color: var(--accent-gold);
  color: var(--bg-primary);
}

/* ----- 边栏 ----- */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  position: sticky;
  top: 100px;
  align-self: start;
}

.sidebar-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s var(--ease-out);
}

.sidebar-card:hover {
  border-color: var(--border-accent);
  box-shadow: var(--shadow-lg);
}

.card-header {
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(212, 163, 115, 0.03);
}

.card-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.01em;
}

.card-body {
  padding: var(--space-5);
}

/* ----- 搜索 ----- */
.search-box {
  display: flex;
  gap: var(--space-2);
}

.search-input {
  flex: 1;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  color: var(--text-primary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  transition: all 0.3s var(--ease-out);
}

.search-input:focus {
  outline: none;
  border-color: var(--accent-gold);
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  padding: var(--space-3);
  background: var(--accent-gold);
  border: 1px solid var(--accent-gold);
  border-radius: var(--radius-lg);
  color: var(--bg-primary);
  cursor: pointer;
  cursor: none;
  transition: all 0.3s var(--ease-out);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background: transparent;
  color: var(--accent-gold);
}

.search-btn svg {
  width: 18px;
  height: 18px;
}

/* ----- 分类列表 ----- */
.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.category-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  text-decoration: none;
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  border-radius: var(--radius-md);
  transition: all 0.3s var(--ease-out);
}

.category-link:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
  transform: translateX(4px);
}

.category-name {
  flex: 1;
}

.category-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 var(--space-2);
  background: rgba(212, 163, 115, 0.1);
  color: var(--accent-gold);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-full);
}

/* ----- 标签云 ----- */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.tag-cloud-item {
  display: inline-block;
  padding: var(--space-2) var(--space-3);
  background: rgba(212, 163, 115, 0.06);
  border: 1px solid var(--border-accent);
  color: var(--accent-gold);
  font-family: var(--font-body);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all 0.3s var(--ease-out);
}

.tag-cloud-item:hover {
  background: var(--accent-gold);
  color: var(--bg-primary);
  transform: translateY(-2px);
}

/* ----- 空状态（迷你） ----- */
.empty-mini {
  text-align: center;
  padding: var(--space-6);
  color: var(--text-muted);
  font-size: var(--text-sm);
}

/* ----- 站点信息 ----- */
.sidebar-about {
  background: linear-gradient(135deg, rgba(212, 163, 115, 0.05) 0%, transparent 100%);
}

.about-content {
  text-align: center;
}

.about-avatar {
  width: 64px;
  height: 64px;
  margin: 0 auto var(--space-4);
  background: var(--accent-gold);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-symbol {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--bg-primary);
}

.about-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.about-text {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 var(--space-6);
}

.about-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-subtle);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--accent-gold);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-weight: var(--font-medium);
}

/* ----- 页脚 ----- */
.site-footer {
  padding: var(--space-12) 0;
  border-top: 1px solid var(--border-subtle);
  margin-top: var(--space-16);
}

.footer-content {
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: 0 var(--space-8);
  text-align: center;
}

.footer-text {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin: 0;
}

.footer-heart {
  color: var(--accent-red);
  animation: heartbeat 1.5s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

/* ----- 响应式 ----- */
@media (max-width: 1280px) {
  .content-container {
    grid-template-columns: 1fr 320px;
    gap: var(--space-8);
  }

  .hero-title {
    font-size: var(--text-7xl);
  }
}

@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
  }

  .articles-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: var(--text-6xl);
  }
}

@media (max-width: 768px) {
  .home-page {
    padding-top: 60px;
  }

  .content-container {
    padding: 0 var(--space-5);
  }

  .main-content {
    padding: var(--space-8) 0;
  }

  .hero-section {
    padding: var(--space-10) 0;
  }

  .hero-title {
    font-size: var(--text-4xl);
  }

  .hero-subtitle {
    font-size: var(--text-base);
  }

  .filter-group {
    flex-wrap: wrap;
  }

  .loading-state {
    grid-template-columns: 1fr;
  }
}
</style>
