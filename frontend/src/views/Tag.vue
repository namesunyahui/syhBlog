<template>
  <div class="tag-page">
    <AppHeader />

    <main class="main-content">
      <div class="content-container">
        <!-- 左侧内容区 -->
        <div class="content-left">
          <!-- 英雄区 -->
          <section class="hero-section scroll-reveal">
            <h1 class="hero-title">
              <span class="title-line">文章</span>
              <span class="title-line title-accent">标签</span>
            </h1>
            <p class="hero-subtitle">
              探索 {{ tags.length }} 个标签，发现感兴趣的内容
            </p>
          </section>

          <!-- 标签筛选区域 -->
          <section class="tags-filter-section scroll-reveal">
            <!-- 搜索和排序 -->
            <div class="filter-controls">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索标签..."
                class="search-input"
              />
              <select v-model="sortBy" class="sort-select">
                <option value="hot">按热门排序</option>
                <option value="name">按名称排序</option>
              </select>
            </div>

            <!-- 标签云 -->
            <div v-if="loading" class="skeleton-cloud">
              <div v-for="i in 12" :key="i" class="skeleton-tag skeleton"></div>
            </div>

            <div v-else-if="filteredTags.length === 0" class="empty-state">
              <div class="empty-icon">🏷️</div>
              <h3 class="empty-title">暂无标签</h3>
            </div>

            <div v-else class="tag-cloud">
              <div
                v-for="tag in filteredTags"
                :key="tag.id"
                class="tag-item cursor-interactive"
                :class="{ 'is-selected': selectedTagIds.includes(tag.id) }"
                :style="{ fontSize: getTagFontSize(tag.articleCount || 0) }"
                @click="handleTagClick(tag)"
                data-cursor-label="选择"
              >
                {{ tag.name }}
                <span class="tag-count">{{ tag.articleCount }}</span>
              </div>
            </div>

            <!-- 已选标签栏 -->
            <div v-if="selectedTagIds.length" class="selected-tags-bar">
              <div class="selected-tags">
                <span class="label">已选：</span>
                <div
                  v-for="tag in selectedTags"
                  :key="tag.id"
                  class="selected-tag"
                  @click="removeTag(tag.id)"
                >
                  {{ tag.name }}
                  <span class="remove-icon">×</span>
                </div>
              </div>
              <button class="clear-all-btn" @click="clearAllTags">清除全部</button>
            </div>
          </section>

          <!-- 文章列表区域 -->
          <section v-if="selectedTagIds.length" class="articles-section">
            <div class="section-header">
              <h2 class="section-title">
                <span class="title-icon">📝</span>
                文章列表
                <span class="article-count">({{ total }} 篇)</span>
              </h2>
            </div>

            <!-- 文章加载状态 -->
            <div v-if="articlesLoading" class="loading-state">
              <div v-for="i in 3" :key="i" class="skeleton-card skeleton"></div>
            </div>

            <!-- 文章空状态 -->
            <div v-else-if="articles.length === 0" class="empty-state">
              <div class="empty-icon">📝</div>
              <h3 class="empty-title">所选标签下暂无文章</h3>
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
              <p class="hint-text">点击上方标签查看该标签下的文章，支持多选标签进行筛选</p>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <AppSidebar
          :categories="categories"
          :tags="tags"
          :modelValue="{ searchKeyword: searchKeyword2 }"
          @update:searchKeyword="searchKeyword2 = $event"
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
import { getTagList } from '@/api/tag'
import { getCategoryList } from '@/api/category'
import { getArticlesByTags } from '@/api/article'

interface Tag {
  id: number
  name: string
  articleCount?: number
}

const router = useRouter()
const route = useRoute()

const tags = ref<Tag[]>([])
const categories = ref([])
const selectedTagIds = ref<number[]>([])
const searchKeyword = ref('')
const searchKeyword2 = ref('')
const sortBy = ref<'hot' | 'name'>('hot')
const articles = ref<any[]>([])
const loading = ref(false)
const articlesLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filteredTags = computed(() => {
  let result = [...tags.value]

  if (searchKeyword.value) {
    result = result.filter(tag =>
      tag.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }

  if (sortBy.value === 'hot') {
    result.sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0))
  } else if (sortBy.value === 'name') {
    result.sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
  }

  return result
})

const selectedTags = computed(() => {
  return tags.value.filter(tag => selectedTagIds.value.includes(tag.id))
})

const loadTags = async () => {
  try {
    loading.value = true
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    ElMessage.error('加载标签列表失败')
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

const handleTagClick = (tag: Tag) => {
  const index = selectedTagIds.value.indexOf(tag.id)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
  } else {
    selectedTagIds.value.push(tag.id)
  }
  currentPage.value = 1
  loadArticles()
}

const removeTag = (tagId: number) => {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
    currentPage.value = 1
    loadArticles()
  }
}

const clearAllTags = () => {
  selectedTagIds.value = []
  articles.value = []
  total.value = 0
}

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

    // 等待 DOM 更新后直接显示文章区域
    await nextTick()
    const articleSection = document.querySelector('.articles-section')
    if (articleSection) {
      articleSection.classList.add('is-visible')
    }
    // 同时触发滚动动画以确保其他元素可见
    setTimeout(handleScrollReveal, 50)
  } catch (error) {
    ElMessage.error('加载文章失败')
  } finally {
    articlesLoading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // 分页切换后，滚动到顶部并触发动画
  setTimeout(handleScrollReveal, 300)
  setTimeout(handleScrollReveal, 500)
}

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

const handleSearch = () => {
  if (searchKeyword2.value) {
    router.push({ path: '/search', query: { keyword: searchKeyword2.value } })
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
  await loadTags()
  loadCategories()

  const tagParam = route.query.tag as string
  if (tagParam && tags.value.length > 0) {
    const tag = tags.value.find(t => t.name === tagParam)
    if (tag) {
      selectedTagIds.value = [tag.id]
      await loadArticles()
    }
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

watch(() => route.query.tag, async (newTag) => {
  if (newTag && tags.value.length > 0) {
    const tag = tags.value.find(t => t.name === newTag)
    if (tag) {
      selectedTagIds.value = [tag.id]
      await loadArticles()
    }
  }
})
</script>

<style scoped>
.tag-page {
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

/* ----- 标签筛选区 ----- */
.tags-filter-section {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.filter-controls {
  display: flex;
  gap: var(--space-4);
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

.sort-select {
  padding: var(--space-3) var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  color: var(--text-primary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  cursor: pointer;
}

/* ----- 标签云 ----- */
.skeleton-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.skeleton-tag {
  width: 100px;
  height: 40px;
  border-radius: var(--radius-full);
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  align-items: center;
}

.tag-item {
  padding: var(--space-2) var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  transition: all 0.3s var(--ease-out);
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

.tag-item:hover {
  transform: translateY(-2px);
  border-color: var(--border-accent);
  color: var(--text-primary);
}

.tag-item.is-selected {
  background: var(--accent-gold);
  border-color: var(--accent-gold);
  color: var(--bg-primary);
}

.tag-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 var(--space-1);
  background: rgba(0, 0, 0, 0.1);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
}

.tag-item.is-selected .tag-count {
  background: rgba(255, 255, 255, 0.2);
}

/* ----- 已选标签栏 ----- */
.selected-tags-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: rgba(212, 163, 115, 0.05);
  border: 1px solid var(--border-accent);
  border-radius: var(--radius-lg);
}

.selected-tags {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  flex: 1;
}

.selected-tags .label {
  font-weight: var(--font-semibold);
  color: var(--accent-gold);
  margin-right: var(--space-2);
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3);
  background: var(--accent-gold);
  color: var(--bg-primary);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  cursor: none;
}

.remove-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.1);
  font-size: 14px;
  line-height: 1;
}

.clear-all-btn {
  padding: var(--space-2) var(--space-4);
  background: transparent;
  border: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  border-radius: var(--radius-md);
  cursor: pointer;
  cursor: none;
  transition: all 0.3s var(--ease-out);
}

.clear-all-btn:hover {
  border-color: var(--border-accent);
  color: var(--text-primary);
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

.article-count {
  font-size: var(--text-base);
  color: var(--text-tertiary);
  font-weight: var(--font-normal);
}

.article-item {
  animation: cascadeIn 0.6s var(--ease-out) forwards;
  opacity: 0;
}

/* ----- 骨架/加载/空状态 ----- */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.skeleton-card {
  height: 200px;
  border-radius: var(--radius-xl);
}

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
  .tag-page {
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

  .filter-controls {
    flex-direction: column;
  }

  .selected-tags-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }
}
</style>
