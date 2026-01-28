<template>
  <div class="archive-page">
    <AppHeader />

    <main class="main-content">
      <div class="content-container">
        <!-- 左侧归档内容 -->
        <div class="content-left">
          <!-- 英雄区 -->
          <section class="hero-section scroll-reveal">
            <h1 class="hero-title">
              <span class="title-line">文章</span>
              <span class="title-line title-accent">归档</span>
            </h1>
            <p class="hero-subtitle">
              共 {{ totalCount }} 篇文章，分布在 {{ statistics.totalYears }} 个年份的 {{ statistics.totalMonths }} 个月份中
            </p>
          </section>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container scroll-reveal">
            <div class="skeleton-year skeleton"></div>
            <div class="skeleton-months">
              <div v-for="i in 3" :key="i" class="skeleton-month skeleton"></div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="archives.length === 0" class="empty-state scroll-reveal">
            <div class="empty-icon">📭</div>
            <h3 class="empty-title">暂无归档文章</h3>
          </div>

          <!-- 归档时间线 -->
          <div v-else class="archive-timeline scroll-reveal">
            <div
              v-for="yearArchive in archives"
              :key="yearArchive.year"
              class="year-group"
            >
              <!-- 年份头部 -->
              <div
                class="year-header cursor-interactive"
                :class="{ 'is-expanded': isYearExpanded(yearArchive.year) }"
                data-cursor-label="展开/收起"
                @click="toggleYear(yearArchive.year)"
              >
                <div class="year-title">
                  <span class="year-text">{{ yearArchive.year }}年</span>
                  <span class="year-count">{{ getYearTotalCount(yearArchive) }} 篇</span>
                </div>
                <svg class="toggle-icon" :class="{ 'is-rotated': isYearExpanded(yearArchive.year) }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 9l6 6 6-6" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>

              <!-- 月份列表 -->
              <div v-show="isYearExpanded(yearArchive.year)" class="months-container">
                <div
                  v-for="monthArchive in yearArchive.months"
                  :key="monthArchive.month"
                  class="month-group"
                >
                  <!-- 月份头部 -->
                  <div
                    class="month-header cursor-interactive"
                    :class="{ 'is-expanded': isMonthExpanded(yearArchive.year, monthArchive.month) }"
                    data-cursor-label="展开/收起"
                    @click="toggleMonth(yearArchive.year, monthArchive.month)"
                  >
                    <span class="month-text">{{ monthArchive.month }}月</span>
                    <span class="month-count">{{ monthArchive.count }} 篇</span>
                    <svg class="toggle-icon" :class="{ 'is-rotated': isMonthExpanded(yearArchive.year, monthArchive.month) }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M6 9l6 6 6-6" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>

                  <!-- 文章列表 -->
                  <div v-show="isMonthExpanded(yearArchive.year, monthArchive.month)" class="articles-list">
                    <router-link
                      v-for="article in monthArchive.articles"
                      :key="article.id"
                      :to="`/article/${article.id}`"
                      class="article-item cursor-interactive"
                      data-cursor-label="阅读"
                    >
                      <div class="article-title">{{ article.title }}</div>
                      <div class="article-meta">
                        <span class="meta-date">{{ formatFullDate(article.createdAt) }}</span>
                        <span v-if="article.category" class="meta-category">
                          {{ article.category.name }}
                        </span>
                        <span class="meta-views">{{ article.viewCount }} 阅读</span>
                      </div>
                    </router-link>
                  </div>
                </div>
              </div>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import { getGroupedArchive } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'

interface Article {
  id: number
  title: string
  createdAt: string
  viewCount: number
  category?: { id: number; name: string }
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

const router = useRouter()

const archives = ref<ArchiveDTO[]>([])
const totalCount = ref(0)
const statistics = ref<Statistics>({ totalYears: 0, totalMonths: 0 })
const loading = ref(true)
const categories = ref([])
const tags = ref([])
const searchKeyword = ref('')

// 展开状态管理
const expandedYears = ref<Set<number>>(new Set())
const expandedMonths = ref<Map<string, Set<number>>>(new Map())

const getYearTotalCount = (yearArchive: ArchiveDTO) => {
  return yearArchive.months.reduce((sum, month) => sum + month.count, 0)
}

const isYearExpanded = (year: number) => {
  return expandedYears.value.has(year)
}

const toggleYear = (year: number) => {
  if (expandedYears.value.has(year)) {
    expandedYears.value.delete(year)
  } else {
    expandedYears.value.add(year)
  }
}

const isMonthExpanded = (year: number, month: number) => {
  const yearKey = year.toString()
  const months = expandedMonths.value.get(yearKey)
  return months ? months.has(month) : false
}

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

const formatFullDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const loadArchive = async () => {
  loading.value = true
  try {
    const res = await getGroupedArchive()
    const data: ArchiveVO = res.data
    archives.value = data.archives || []
    totalCount.value = data.totalCount || 0
    statistics.value = data.statistics || { totalYears: 0, totalMonths: 0 }

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
  } finally {
    loading.value = false
    // 数据加载完成后触发滚动动画
    setTimeout(handleScrollReveal, 50)
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

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

onMounted(() => {
  loadArchive()
  loadCategories()
  loadTags()

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
</script>

<style scoped>
.archive-page {
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

/* ----- 骨架屏 ----- */
.loading-container {
  padding: var(--space-8);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}

.skeleton-year {
  height: 60px;
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-6);
}

.skeleton-months {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding-left: var(--space-8);
}

.skeleton-month {
  height: 40px;
  border-radius: var(--radius-md);
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
  margin: 0;
}

/* ----- 归档时间线 ----- */
.archive-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.year-group {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s var(--ease-out);
}

.year-group:hover {
  border-color: var(--border-accent);
  box-shadow: var(--shadow-lg);
}

.year-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6);
  background: rgba(212, 163, 115, 0.03);
  border-bottom: 1px solid var(--border-subtle);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
}

.year-header:hover {
  background: rgba(212, 163, 115, 0.06);
}

.year-title {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.year-text {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

.year-count {
  padding: var(--space-1) var(--space-3);
  background: rgba(212, 163, 115, 0.1);
  color: var(--accent-gold);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-full);
}

.toggle-icon {
  width: 20px;
  height: 20px;
  color: var(--text-tertiary);
  transition: transform 0.3s var(--ease-out);
}

.toggle-icon.is-rotated {
  transform: rotate(180deg);
}

.months-container {
  padding: var(--space-4);
}

.month-group {
  margin-bottom: var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.month-group:last-child {
  margin-bottom: 0;
}

.month-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
}

.month-header:hover {
  background: rgba(212, 163, 115, 0.05);
}

.month-text {
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.month-count {
  margin-left: auto;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.articles-list {
  padding: var(--space-3) var(--space-4) var(--space-4) var(--space-5);
}

.article-item {
  display: block;
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-2);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all 0.3s var(--ease-out);
}

.article-item:hover {
  border-color: var(--border-accent);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
}

.article-item:last-child {
  margin-bottom: 0;
}

.article-title {
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.meta-date,
.meta-category,
.meta-views {
  display: flex;
  align-items: center;
}

.meta-category {
  color: var(--accent-gold);
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
  .archive-page {
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

  .article-meta {
    flex-wrap: wrap;
    gap: var(--space-2);
  }
}
</style>
