<template>
  <div class="article-detail-container">
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
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="rect" style="width: 100%; height: 400px; margin-bottom: 20px; border-radius: 16px;" />
              </template>
            </el-skeleton>
          </div>

          <!-- 加载失败提示 -->
          <el-alert
            v-else-if="error"
            title="加载失败"
            :description="error"
            type="error"
            show-icon
            :closable="false"
            class="error-alert"
          />

          <!-- 左侧文章内容 -->
          <div class="article-list" v-else-if="article">
            <el-card>
              <h1 class="article-title">{{ article.title }}</h1>

              <div class="article-meta">
                <span>{{ formatDate(article.createdAt) }}</span>
                <span v-if="article.category">{{ article.category.name }}</span>
                <span>{{ article.viewCount }} 阅读</span>
              </div>

              <div class="markdown-body" v-html="renderedContent"></div>

              <div class="article-tags" v-if="article.tags && article.tags.length">
                <el-tag v-for="tag in article.tags" :key="tag.id">
                  {{ tag.name }}
                </el-tag>
              </div>
            </el-card>

            <!-- 评论区域 -->
            <el-card class="comments-section">
              <template #header>
                <h3>评论 ({{ comments.length }})</h3>
              </template>

              <!-- 未登录用户：显示昵称输入框 -->
              <el-form v-if="!isLoggedIn" :model="commentForm" label-width="80px">
                <el-form-item label="昵称">
                  <el-input v-model="commentForm.nickname" placeholder="点击按钮生成随机昵称或自定义">
                    <template #append>
                      <el-button @click="generateNickname" type="primary">🎲 随机生成</el-button>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="评论内容">
                  <el-input
                    v-model="commentForm.content"
                    type="textarea"
                    :rows="4"
                    placeholder="说点什么吧..."
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitComment">提交评论</el-button>
                </el-form-item>
              </el-form>

              <!-- 登录用户：直接显示用户信息 -->
              <el-form v-else :model="commentForm" label-width="80px">
                <el-form-item label="当前用户">
                  <div class="user-info-display">
                    <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar" />
                    <span>{{ userInfo.nickname || userInfo.username || '管理员' }}</span>
                  </div>
                </el-form-item>
                <el-form-item label="评论内容">
                  <el-input
                    v-model="commentForm.content"
                    type="textarea"
                    :rows="4"
                    placeholder="说点什么吧..."
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitComment">提交评论</el-button>
                </el-form-item>
              </el-form>

              <div class="comment-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div class="comment-header">
                    <div class="comment-user">
                      <el-avatar
                        v-if="comment.userAvatar"
                        :size="32"
                        :src="comment.userAvatar"
                        class="comment-avatar"
                      />
                      <strong>{{ comment.displayName || comment.nickname || '匿名用户' }}</strong>
                    </div>
                    <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>
                <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧！" />
              </div>
            </el-card>
          </div>

          <!-- 右侧边栏 -->
          <div class="sidebar" v-if="article">
            <!-- 热门文章 -->
            <el-card class="sidebar-card">
              <template #header>
                <h3 class="sidebar-title">🔥 热门文章</h3>
              </template>
              <div class="sidebar-article-list">
                <div
                  v-for="item in hotArticles"
                  :key="item.id"
                  class="article-item"
                  @click="goToArticle(item.id)"
                >
                  <div class="article-item-title">{{ item.title }}</div>
                  <div class="article-item-meta">
                    <span>{{ item.viewCount }} 阅读</span>
                  </div>
                </div>
                <el-empty v-if="hotArticles.length === 0" description="暂无数据" :image-size="80" />
              </div>
            </el-card>

            <!-- 相关文章 -->
            <el-card class="sidebar-card">
              <template #header>
                <h3 class="sidebar-title">📚 相关文章</h3>
              </template>
              <div class="sidebar-article-list">
                <div
                  v-for="item in relatedArticles"
                  :key="item.id"
                  class="article-item"
                  @click="goToArticle(item.id)"
                >
                  <div class="article-item-title">{{ item.title }}</div>
                  <div class="article-item-meta">
                    <span v-if="item.category">{{ item.category.name }}</span>
                    <span>{{ item.viewCount }} 阅读</span>
                  </div>
                </div>
                <el-empty v-if="relatedArticles.length === 0" description="暂无相关文章" :image-size="80" />
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MarkdownIt from 'markdown-it'
import { getArticleDetail, addViewCount, getHotArticles, getRelatedArticles } from '@/api/article'
import { getCommentList, submitComment as postComment } from '@/api/comment'
import { logout } from '@/api/auth'
import { generateRandomNickname } from '@/utils/nicknameGenerator'

const route = useRoute()
const router = useRouter()
const article = ref<any>(null)
const comments = ref([])
const loading = ref(false)
const error = ref('')
const commentForm = ref({
  nickname: '',
  content: ''
})
const userInfo = ref<any>({})
const hotArticles = ref<any[]>([])
const relatedArticles = ref<any[]>([])

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 检查登录状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

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
    console.log('加载文章，ID:', id)

    const res = await getArticleDetail(id)
    console.log('文章数据:', res)

    if (res.code === 200 && res.data) {
      article.value = res.data
      // 增加浏览量
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

      // 加载热门文章和相关文章
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
    // 加载热门文章
    const hotRes = await getHotArticles(5)
    if (hotRes.code === 200) {
      hotArticles.value = (hotRes.data || []).filter((item: any) => item.id !== articleId)
    }

    // 加载相关文章
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
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // 重新加载数据
  setTimeout(() => {
    loadArticle()
  }, 100)
}

const submitComment = async () => {
  // 未登录用户必须填写昵称
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

    // 只有未登录用户才传 nickname
    if (!isLoggedIn.value) {
      data.nickname = commentForm.value.nickname
    }

    const res = await postComment(data)

    if (res.code === 200) {
      ElMessage.success('评论提交成功')
      commentForm.value = {
        nickname: '',
        content: ''
      }
      // 重新加载评论
      await loadArticle()
    } else {
      ElMessage.error(res.message || '评论提交失败')
    }
  } catch (err: any) {
    console.error('提交评论失败:', err)
    ElMessage.error(err.response?.data?.message || err.message || '评论提交失败')
  }
}

// 生成随机昵称
const generateNickname = () => {
  commentForm.value.nickname = generateRandomNickname()
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
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
  loadUserInfo()
  loadArticle()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.article-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.article-detail-container::before {
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
  margin: 0;
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

.main-content {
  display: grid;
  grid-template-columns: 1fr 28%;
  gap: 3%;
  align-items: start;
  width: 90%;
  max-width: none;
  margin: 0 auto;
}

.article-list {
  min-height: 0;
  max-width: 100%;
  overflow: hidden;
}

.article-list .el-card {
  margin-bottom: 20px;
  border-radius: 16px;
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.article-list .el-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(74, 85, 104, 0.2);
}

.article-list :deep(.el-card__body) {
  padding: 24px;
}

.error-alert {
  margin-bottom: 20px;
}

.loading-container {
  width: 100%;
}

.article-title {
  font-size: 32px;
  margin-bottom: 20px;
  line-height: 1.4;
  color: #303133;
}

.article-meta {
  color: #909399;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
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
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  border-radius: 50%;
}

.markdown-body {
  line-height: 1.8;
  font-size: 16px;
  color: #606266;
  min-height: 300px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
}

.markdown-body :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.article-tags {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
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

.comments-section {
  margin-top: 0;
  border-radius: 16px;
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.comments-section :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-bottom: 1px solid rgba(74, 85, 104, 0.1);
  padding: 18px 20px;
}

.comments-section h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-time {
  color: #909399;
  font-size: 14px;
}

.comment-content {
  line-height: 1.6;
  color: #606266;
}

.user-info-display {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.05) 0%, rgba(44, 62, 80, 0.05) 100%);
  border-radius: 8px;
}

.user-info-display span {
  font-weight: 500;
  color: #303133;
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

.sidebar-title {
  font-size: 18px;
  margin: 0;
  font-weight: 600;
  background: linear-gradient(135deg, #4a5568 0%, #2c3e50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-card :deep(.el-card__body) {
  padding: 20px;
}

.sidebar-article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-item {
  padding: 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
  background: #fff;
}

.article-item:hover {
  background: linear-gradient(135deg, rgba(74, 85, 104, 0.1) 0%, rgba(44, 62, 80, 0.1) 100%);
  border-color: #4a5568;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(74, 85, 104, 0.15);
}

.article-item-title {
  font-size: 15px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-weight: 500;
}

.article-item-meta {
  font-size: 13px;
  color: #909399;
  display: flex;
  gap: 12px;
  align-items: center;
}

.article-item-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr 30%;
    gap: 4%;
    width: 92%;
  }

  .el-main {
    padding: 20px 20px;
  }
}

@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 20px;
    width: 94%;
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

  .el-header {
    height: auto;
    min-height: 60px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-meta {
    font-size: 13px;
  }

  .markdown-body {
    font-size: 15px;
  }
}
</style>
