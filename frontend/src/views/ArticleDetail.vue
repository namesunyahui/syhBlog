<template>
  <div class="article-detail-page">
    <AppHeader />

    <main class="main-content">
      <div class="content-container">
        <!-- 左侧文章内容 -->
        <div class="content-left">
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container">
            <div class="skeleton-title skeleton"></div>
            <div class="skeleton-meta skeleton"></div>
            <div class="skeleton-content skeleton"></div>
          </div>

          <!-- 加载失败 -->
          <div v-else-if="error" class="error-state">
            <div class="error-icon">⚠️</div>
            <h3 class="error-title">{{ error }}</h3>
          </div>

          <!-- 文章内容 -->
          <article v-else-if="article" class="article-content">
            <!-- 文章头部 -->
            <header class="article-header">
              <!-- 分类 -->
              <div v-if="article.category" class="article-category">
                <router-link
                  :to="{ path: '/category', query: { categoryId: article.category.id } }"
                  class="category-link cursor-interactive"
                  data-cursor-label="分类"
                >
                  {{ article.category.name }}
                </router-link>
              </div>

              <!-- 标题 -->
              <h1 class="article-title">{{ article.title }}</h1>

              <!-- 元信息 -->
              <div class="article-meta">
                <span class="meta-item">
                  <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <rect x="3" y="4" width="18" height="18" rx="2" stroke-width="2"/>
                    <path d="M16 2v4M8 2v4M3 10h18" stroke-width="2"/>
                  </svg>
                  {{ formatDate(article.createdAt) }}
                </span>
                <span class="meta-item">
                  <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke-width="2"/>
                  </svg>
                  {{ article.viewCount }} 阅读
                </span>
              </div>
            </header>

            <!-- 文章正文 -->
            <div class="markdown-body" v-html="renderedContent"></div>

            <!-- 文章标签 -->
            <div v-if="article.tags && article.tags.length" class="article-tags">
              <span class="tags-label">标签：</span>
              <router-link
                v-for="tag in article.tags"
                :key="tag.id"
                :to="{ path: '/tag', query: { tag: tag.name } }"
                class="tag-item cursor-interactive"
                data-cursor-label="标签"
              >
                {{ tag.name }}
              </router-link>
            </div>

            <!-- 文章分割线 -->
            <div class="article-divider"></div>
          </article>

          <!-- 评论区 -->
          <section v-if="article" class="comments-section">
            <div class="section-header">
              <h2 class="section-title">
                <span class="title-icon">💬</span>
                评论 ({{ comments.length }})
              </h2>
            </div>

            <!-- 评论表单 -->
            <div class="comment-form-card">
              <template v-if="!isLoggedIn">
                <div class="form-group">
                  <label class="form-label">昵称</label>
                  <div class="input-with-action">
                    <input
                      v-model="commentForm.nickname"
                      type="text"
                      class="form-input"
                      placeholder="点击按钮生成随机昵称或自定义"
                    />
                    <button @click="generateNickname" class="input-action-btn cursor-interactive" data-cursor-label="生成">
                      🎲 随机
                    </button>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="user-display">
                  <div class="user-avatar-display">
                    <img v-if="userInfo.avatar" :src="userInfo.avatar" :alt="userInfo.nickname" />
                    <span v-else class="avatar-placeholder">{{ (userInfo.nickname || '管理员')[0] }}</span>
                  </div>
                  <span class="user-name">{{ userInfo.nickname || userInfo.username || '管理员' }}</span>
                </div>
              </template>

              <div class="form-group">
                <label class="form-label">评论内容</label>
                <textarea
                  v-model="commentForm.content"
                  class="form-textarea"
                  rows="4"
                  placeholder="说点什么吧..."
                ></textarea>
              </div>

              <button @click="submitComment" class="submit-btn cursor-interactive" data-cursor-label="提交">
                提交评论
              </button>
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-avatar">
                  <img v-if="comment.userAvatar" :src="comment.userAvatar" :alt="comment.nickname" />
                  <span v-else class="avatar-placeholder">{{ (comment.displayName || comment.nickname || '匿')[0] }}</span>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.displayName || comment.nickname || '匿名用户' }}</span>
                    <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>
              </div>
              <div v-if="comments.length === 0" class="empty-comments">
                <div class="empty-icon">💭</div>
                <p>暂无评论，快来抢沙发吧！</p>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧边栏 -->
        <aside v-if="article" class="sidebar">
          <!-- 热门文章 -->
          <div class="sidebar-card">
            <div class="sidebar-card-header">
              <h3 class="sidebar-card-title">
                <span class="title-icon">🔥</span>
                热门文章
              </h3>
            </div>
            <div class="sidebar-card-body">
              <div v-if="hotArticles.length > 0" class="article-list">
                <div
                  v-for="item in hotArticles"
                  :key="item.id"
                  class="sidebar-article-item cursor-interactive"
                  data-cursor-label="阅读"
                  @click="goToArticle(item.id)"
                >
                  <h4 class="sidebar-article-title">{{ item.title }}</h4>
                  <div class="sidebar-article-meta">
                    <span>{{ item.viewCount }} 阅读</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty-mini">
                <span>暂无数据</span>
              </div>
            </div>
          </div>

          <!-- 相关文章 -->
          <div class="sidebar-card">
            <div class="sidebar-card-header">
              <h3 class="sidebar-card-title">
                <span class="title-icon">📚</span>
                相关文章
              </h3>
            </div>
            <div class="sidebar-card-body">
              <div v-if="relatedArticles.length > 0" class="article-list">
                <div
                  v-for="item in relatedArticles"
                  :key="item.id"
                  class="sidebar-article-item cursor-interactive"
                  data-cursor-label="阅读"
                  @click="goToArticle(item.id)"
                >
                  <h4 class="sidebar-article-title">{{ item.title }}</h4>
                  <div class="sidebar-article-meta">
                    <span v-if="item.category">{{ item.category.name }}</span>
                    <span>{{ item.viewCount }} 阅读</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty-mini">
                <span>暂无相关文章</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import AppHeader from '@/components/AppHeader.vue'
import { getArticleDetail, addViewCount, getHotArticles, getRelatedArticles } from '@/api/article'
import { getCommentList, submitComment as postComment } from '@/api/comment'
import { generateRandomNickname } from '@/utils/nicknameGenerator'

const route = useRoute()
const router = useRouter()

const article = ref<any>(null)
const comments = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const commentForm = ref({
  nickname: '',
  content: ''
})
const userInfo = ref<any>({})
const hotArticles = ref<any[]>([])
const relatedArticles = ref<any[]>([])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

const renderedContent = computed(() => {
  if (!article.value) return ''
  return md.render(article.value.content || '')
})

const loadArticle = async () => {
  try {
    loading.value = true
    error.value = ''

    const id = Number(route.params.id)
    const res = await getArticleDetail(id)

    if (res.code === 200 && res.data) {
      article.value = res.data
      await addViewCount(id)

      // 加载评论
      try {
        const commentRes = await getCommentList(id)
        if (commentRes.code === 200) {
          comments.value = commentRes.data?.records || []
        }
      } catch (commentError) {
        console.warn('加载评论失败:', commentError)
        comments.value = []
      }

      loadSidebarArticles(id)
    } else {
      error.value = res.message || '文章不存在'
      ElMessage.error(error.value)
    }
  } catch (err: any) {
    console.error('加载文章失败:', err)
    error.value = err.response?.data?.message || err.message || '加载失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

const loadSidebarArticles = async (articleId: number) => {
  try {
    const hotRes = await getHotArticles(5)
    if (hotRes.code === 200) {
      hotArticles.value = (hotRes.data || []).filter((item: any) => item.id !== articleId)
    }

    const relatedRes = await getRelatedArticles(articleId, 5)
    if (relatedRes.code === 200) {
      relatedArticles.value = relatedRes.data || []
    }
  } catch (err) {
    console.warn('加载侧边栏文章失败:', err)
  }
}

const goToArticle = (id: number) => {
  router.push(`/article/${id}`)
  window.scrollTo({ top: 0, behavior: 'smooth' })
  setTimeout(() => loadArticle(), 100)
}

const submitComment = async () => {
  if (!isLoggedIn.value && !commentForm.value.nickname) {
    ElMessage.warning('请填写昵称')
    return
  }

  if (!commentForm.value.content) {
    ElMessage.warning('请填写评论内容')
    return
  }

  try {
    const data: any = {
      articleId: Number(route.params.id),
      content: commentForm.value.content
    }

    if (!isLoggedIn.value) {
      data.nickname = commentForm.value.nickname
    }

    const res = await postComment(data)

    if (res.code === 200) {
      ElMessage.success('评论提交成功')
      commentForm.value = { nickname: '', content: '' }
      await loadArticle()
    } else {
      ElMessage.error(res.message || '评论提交失败')
    }
  } catch (err: any) {
    console.error('提交评论失败:', err)
    ElMessage.error(err.response?.data?.message || err.message || '评论提交失败')
  }
}

const generateNickname = () => {
  commentForm.value.nickname = generateRandomNickname()
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

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
  loadArticle()
})
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-top: 72px;
}

.main-content {
  width: 100%;
  padding: var(--space-8) 0;
}

.content-container {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: var(--space-8);
  max-width: var(--container-2xl);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.content-left {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
  min-width: 0;
  overflow: hidden;
}

/* ----- 加载状态 ----- */
.loading-container {
  padding: var(--space-16);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}

.skeleton-title {
  height: 48px;
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-6);
}

.skeleton-meta {
  height: 24px;
  width: 300px;
  border-radius: var(--radius-md);
  margin-bottom: var(--space-8);
}

.skeleton-content {
  height: 400px;
  border-radius: var(--radius-lg);
}

/* ----- 错误状态 ----- */
.error-state {
  text-align: center;
  padding: var(--space-20) var(--space-8);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}

.error-icon {
  font-size: 64px;
  margin-bottom: var(--space-6);
  opacity: 0.5;
}

.error-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
}

/* ----- 文章内容 ----- */
.article-content {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  max-width: 100%;
  overflow-x: hidden;
}

.article-header {
  padding: var(--space-6) var(--space-8) var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
}

.article-category {
  margin-bottom: var(--space-4);
}

.category-link {
  display: inline-block;
  padding: var(--space-2) var(--space-4);
  background: rgba(212, 163, 115, 0.1);
  color: var(--accent-gold);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  text-decoration: none;
  border-radius: var(--radius-full);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: all 0.3s var(--ease-out);
}

.category-link:hover {
  background: var(--accent-gold);
  color: var(--bg-primary);
  transform: translateY(-2px);
}

.article-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  line-height: 1.3;
  letter-spacing: -0.02em;
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-tertiary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

.meta-icon {
  width: 16px;
  height: 16px;
}

/* ----- Markdown 正文 ----- */
.markdown-body {
  padding: var(--space-6) var(--space-8);
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-base);
  line-height: 1.7;
  overflow-wrap: break-word;
  word-wrap: break-word;
  max-width: 100%;
  overflow-x: hidden;
}

/* 确保 Markdown 内所有元素不溢出 */
.markdown-body :deep(*) {
  max-width: 100%;
  box-sizing: border-box;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  font-family: var(--font-display);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-top: var(--space-6);
  margin-bottom: var(--space-3);
  line-height: 1.3;
}

.markdown-body :deep(h1) { font-size: var(--text-3xl); }
.markdown-body :deep(h2) { font-size: var(--text-2xl); }
.markdown-body :deep(h3) { font-size: var(--text-xl); }
.markdown-body :deep(h4) { font-size: var(--text-lg); }

.markdown-body :deep(p) {
  margin-bottom: var(--space-3);
  max-width: 100%;
  overflow-x: auto;
}

.markdown-body :deep(a) {
  color: var(--accent-gold);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s var(--ease-out);
  max-width: 100%;
  overflow-wrap: break-word;
}

.markdown-body :deep(a:hover) {
  border-color: var(--accent-gold);
}

.markdown-body :deep(code) {
  padding: var(--space-1) var(--space-2);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  font-family: var(--font-code);
  font-size: var(--text-sm);
  color: var(--accent-gold);
  max-width: 100%;
  overflow-wrap: break-word;
}

.markdown-body :deep(pre) {
  padding: var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow-x: auto;
  margin-bottom: var(--space-4);
  max-width: 100%;
}

.markdown-body :deep(pre code) {
  background: transparent;
  border: none;
  padding: 0;
  max-width: 100%;
  white-space: pre;
  word-wrap: normal;
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius-lg);
  margin: var(--space-4) 0;
}

.markdown-body :deep(blockquote) {
  padding-left: var(--space-4);
  border-left: 3px solid var(--accent-gold);
  color: var(--text-tertiary);
  font-style: italic;
  margin: var(--space-4) 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: var(--space-6);
  margin-bottom: var(--space-4);
}

.markdown-body :deep(li) {
  margin-bottom: var(--space-2);
  max-width: 100%;
  overflow-wrap: break-word;
}

.markdown-body :deep(table) {
  width: 100%;
  max-width: 100%;
  border-collapse: collapse;
  margin: var(--space-4) 0;
  overflow-x: auto;
  display: block;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: var(--space-3);
  border: 1px solid var(--border-subtle);
  text-align: left;
  min-width: 80px;
}

.markdown-body :deep(th) {
  background: var(--bg-secondary);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

/* ----- 文章标签 ----- */
.article-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-8);
  border-top: 1px solid var(--border-subtle);
}

.tags-label {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

.tag-item {
  display: inline-block;
  padding: var(--space-1) var(--space-3);
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

.tag-item:hover {
  background: var(--accent-gold);
  color: var(--bg-primary);
  transform: translateY(-2px);
}

.article-divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 0;
}

/* ----- 评论区 ----- */
.comments-section {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.section-header {
  padding: var(--space-6) var(--space-8);
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(212, 163, 115, 0.03);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.title-icon {
  font-size: var(--text-xl);
}

.comment-form-card {
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
}

.user-display {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-6);
}

.user-avatar-display {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  overflow: hidden;
  background: var(--accent-gold);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar-display img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.form-group {
  margin-bottom: var(--space-5);
}

.form-label {
  display: block;
  margin-bottom: var(--space-2);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

.form-input,
.form-textarea {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  color: var(--text-primary);
  font-family: var(--font-body);
  font-size: var(--text-base);
  transition: all 0.3s var(--ease-out);
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--accent-gold);
  background: var(--bg-elevated);
}

.input-with-action {
  display: flex;
  gap: var(--space-2);
}

.form-input {
  flex: 1;
}

.input-action-btn {
  padding: var(--space-3) var(--space-5);
  background: var(--accent-gold);
  border: 1px solid var(--accent-gold);
  border-radius: var(--radius-lg);
  color: var(--bg-primary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
  white-space: nowrap;
}

.input-action-btn:hover {
  background: transparent;
  color: var(--accent-gold);
}

.submit-btn {
  width: 100%;
  padding: var(--space-4);
  background: var(--accent-gold);
  border: 1px solid var(--accent-gold);
  border-radius: var(--radius-lg);
  color: var(--bg-primary);
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
}

.submit-btn:hover {
  background: transparent;
  color: var(--accent-gold);
}

/* ----- 评论列表 ----- */
.comment-list {
  padding: var(--space-6);
}

.comment-item {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-5) 0;
  border-bottom: 1px solid var(--border-subtle);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  overflow: hidden;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.comment-author {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.comment-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.comment-content {
  color: var(--text-secondary);
  line-height: 1.6;
  word-break: break-word;
}

.empty-comments {
  text-align: center;
  padding: var(--space-12) var(--space-8);
}

.empty-comments .empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.empty-comments p {
  color: var(--text-tertiary);
  margin: 0;
}

/* ----- 侧边栏 ----- */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
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

.sidebar-card-header {
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(212, 163, 115, 0.03);
}

.sidebar-card-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.sidebar-card-body {
  padding: var(--space-5);
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.sidebar-article-item {
  padding: var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
}

.sidebar-article-item:hover {
  border-color: var(--border-accent);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
}

.sidebar-article-title {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sidebar-article-meta {
  display: flex;
  gap: var(--space-3);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.empty-mini {
  text-align: center;
  padding: var(--space-6);
  color: var(--text-muted);
  font-size: var(--text-sm);
}

/* ----- 响应式 ----- */
@media (max-width: 1280px) {
  .content-container {
    grid-template-columns: 1fr 320px;
  }

  .article-title {
    font-size: var(--text-2xl);
  }
}

@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .article-detail-page {
    padding-top: 60px;
  }

  .content-container {
    padding: 0 var(--space-4);
  }

  .main-content {
    padding: var(--space-6) 0;
  }

  .article-header {
    padding: var(--space-5) var(--space-6) var(--space-3);
  }

  .article-title {
    font-size: var(--text-2xl);
  }

  .markdown-body {
    padding: var(--space-5) var(--space-6);
    font-size: var(--text-sm);
  }

  .comment-form-card {
    padding: var(--space-5);
  }

  .comment-list {
    padding: var(--space-5);
  }
}
</style>
