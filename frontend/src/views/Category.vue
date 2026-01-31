<template>
  <div class="category-page">
    <AppHeader />

    <main class="main-content">
      <div class="content-container">
        <!-- 左侧内容区 -->
        <div class="content-left">
          <!-- 英雄区 -->
          <section class="hero-section scroll-reveal">
            <h1 class="hero-title">
              <span class="title-line">文章</span>
              <span class="title-line title-accent">分类</span>
            </h1>
            <p class="hero-subtitle">
              浏览 {{ totalCount }} 篇文章，分布在 {{ categories.length }} 个分类中
            </p>
          </section>

          <!-- 分类网格 -->
          <section class="categories-section scroll-reveal">
            <!-- 加载状态 -->
            <div v-if="loading" class="skeleton-grid">
              <div v-for="i in 6" :key="i" class="skeleton-card skeleton"></div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="categories.length === 0" class="empty-state">
              <div class="empty-icon">📂</div>
              <h3 class="empty-title">暂无分类</h3>
              <p class="empty-text">还没有创建任何文章分类</p>
            </div>

            <!-- 分类卡片 -->
            <div v-else class="category-grid">
              <div
                v-for="category in categories"
                :key="category.id"
                class="category-card cursor-interactive"
                :class="{ 'is-active': selectedCategoryId === category.id }"
                data-cursor-label="查看文章"
                @click="handleCategoryClick(category)"
              >
                <div class="category-icon">{{ getCategoryIcon(category.name) }}</div>
                <h3 class="category-name">{{ category.name }}</h3>
                <p v-if="category.description" class="category-description">{{ category.description }}</p>
                <div class="category-count">{{ category.articleCount || 0 }} 篇文章</div>
              </div>
            </div>
          </section>

          <!-- 文章列表区域 -->
          <section v-if="selectedCategoryId" class="articles-section">
            <div class="section-header">
              <h2 class="section-title">
                <span class="title-icon">📝</span>
                {{ currentCategory?.name }} - 文章列表
              </h2>
              <button class="clear-btn" @click="handleClearCategory">
                清除筛选
              </button>
            </div>

            <!-- 文章加载状态 -->
            <div v-if="articlesLoading" class="loading-state">
              <div v-for="i in 3" :key="i" class="skeleton-card skeleton"></div>
            </div>

            <!-- 文章空状态 -->
            <div v-else-if="articles.length === 0" class="empty-state">
              <div class="empty-icon">📝</div>
              <h3 class="empty-title">该分类下暂无文章</h3>
            </div>

            <!-- 文章列表 -->
            <template v-else>
              <ArticleCard
                v-for="article in articles"
                :key="article.id"
                :article="article"
                class="article-item"
              />

              <!-- 分页 -->
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="currentPage"
                  :page-size="pageSize"
                  :total="total"
                  layout="prev, pager, next"
                  :background="false"
                  @current-change="handlePageChange"
                />
              </div>
            </template>
          </section>

          <!-- 提示信息 -->
          <div v-else class="hint-section scroll-reveal">
            <div class="hint-card">
              <div class="hint-icon">👆</div>
              <p class="hint-text">点击上方分类卡片查看该分类下的文章</p>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <AppSidebar
          :categories="categories"
          :tags="tags"
          :modelValue="{ searchKeyword }"
          @update:searchKeyword="searchKeyword = $event"
          @search="handleSearch"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getCategoryList } from '@/api/category'
import { getArticleList } from '@/api/article'
import { getTagList } from '@/api/tag'

interface Category {
  id: number
  name: string
  description?: string
  articleCount?: number
}

const router = useRouter()
const route = useRoute()
const categories = ref<Category[]>([])
const articles = ref<any[]>([])
const tags = ref<any[]>([])
const loading = ref(false)
const articlesLoading = ref(false)
const selectedCategoryId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const currentCategory = computed(() => {
  return categories.value.find(c => c.id === selectedCategoryId.value)
})

const totalCount = computed(() => {
  return categories.value.reduce((sum, cat) => sum + (cat.articleCount || 0), 0)
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
    // 数据加载完成后触发滚动动画
    setTimeout(handleScrollReveal, 50)
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

    // 等待 DOM 更新后直接显示文章区域
    await nextTick()
    const articleSection = document.querySelector('.articles-section')
    if (articleSection) {
      articleSection.classList.add('is-visible')
    }
    // 同时触发滚动动画以确保其他元素可见
    setTimeout(handleScrollReveal, 50)
  } catch (error) {
    ElMessage.error('加载文章列表失败')
    } finally {
    articlesLoading.value = false
  }
}

// 点击分类
const handleCategoryClick = (category: Category) => {
  if (selectedCategoryId.value === category.id) {
    selectedCategoryId.value = null
    articles.value = []
    router.replace({ path: '/category', query: {} })
  } else {
    selectedCategoryId.value = category.id
    currentPage.value = 1
    loadArticles(1)
    router.replace({ path: '/category', query: { categoryId: category.id } })
  }
}

// 清除分类筛选
const handleClearCategory = () => {
  selectedCategoryId.value = null
  articles.value = []
  router.replace({ path: '/category', query: {} })
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // 分页切换后，滚动到顶部并触发动画
  setTimeout(handleScrollReveal, 300)
  setTimeout(handleScrollReveal, 500)
}

// 获取分类图标
const getCategoryIcon = (name: string) => {
  const iconMap: Record<string, string> = {
    '技术': '💻',
    '生活': '🌱',
    '学习': '📚',
    '随笔': '✍️',
    '教程': '📖',
    '分享': '🎁'
  }
  return iconMap[name] || '📁'
}

// 加载标签
const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

// 搜索
const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
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

onMounted(async () => {
  await loadCategories()
  loadTags()

  const categoryIdFromQuery = route.query.categoryId
  if (categoryIdFromQuery) {
    const categoryId = Number(categoryIdFromQuery)
    setTimeout(() => {
      const category = categories.value.find(c => c.id === categoryId)
      if (category) {
        selectedCategoryId.value = categoryId
        loadArticles(1)
      }
    }, 100)
  }

  // 添加滚动监听
  window.addEventListener('scroll', handleScrollReveal)

  // 初始触发多次，确保 DOM 渲染完成
  setTimeout(handleScrollReveal, 100)
  setTimeout(handleScrollReveal, 300)
  setTimeout(handleScrollReveal, 500)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScrollReveal)
})

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
    selectedCategoryId.value = null
    articles.value = []
  }
})
</script>

<style scoped>
.category-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-top: 72px;
}

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

.hero-title {
  font-family: var(--font-display);
  font-size: var(--text-7xl);
  font-weight: var(--font-black);
  line-height: 0.95;
  letter-spacing: -0.04em;
  margin: 0 0 var(--space-6);
}

.title-line {
  display: block;
  color: var(--text-primary);
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
  margin: 0;
}

/* ----- 分类网格 ----- */
.categories-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-6);
}

.category-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  text-align: center;
  transition: all 0.3s var(--ease-out);
  position: relative;
  overflow: hidden;
}

.category-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--accent-gold) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s var(--ease-out);
  z-index: 0;
}

.category-card:hover::before {
  opacity: 0.05;
}

.category-card:hover {
  border-color: var(--border-accent);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.category-card.is-active {
  border-color: var(--accent-gold);
  background: rgba(212, 163, 115, 0.03);
}

.category-icon {
  font-size: var(--text-5xl);
  margin-bottom: var(--space-4);
  position: relative;
  z-index: 1;
}

.category-name {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  position: relative;
  z-index: 1;
}

.category-description {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  line-height: 1.6;
  margin: 0 0 var(--space-4);
  position: relative;
  z-index: 1;
}

.category-count {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--accent-gold);
  font-weight: var(--font-semibold);
  position: relative;
  z-index: 1;
}

/* ----- 文章区域 ----- */
.articles-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.title-icon {
  font-size: var(--text-2xl);
}

.clear-btn {
  padding: var(--space-2) var(--space-5);
  background: transparent;
  border: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
}

.clear-btn:hover {
  border-color: var(--border-accent);
  color: var(--text-primary);
}

.article-item {
  animation: cascadeIn 0.6s var(--ease-out) forwards;
  opacity: 0;
}

/* ----- 骨架屏 ----- */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-6);
}

.skeleton-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  height: 200px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
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
  font-size: var(--text-2xl);
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

/* ----- 提示区域 ----- */
.hint-section {
  display: flex;
  justify-content: center;
}

.hint-card {
  background: var(--bg-card);
  border: 2px dashed var(--border-accent);
  border-radius: var(--radius-xl);
  padding: var(--space-12) var(--space-16);
  text-align: center;
}

.hint-icon {
  font-size: 48px;
  margin-bottom: var(--space-4);
}

.hint-text {
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

/* ----- 响应式 ----- */
@media (max-width: 1280px) {
  .content-container {
    grid-template-columns: 1fr 320px;
  }

  .hero-title {
    font-size: var(--text-6xl);
  }
}

@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .category-page {
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

  .category-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }
}
</style>
