<template>
  <div class="article-detail">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1 class="site-title">
            <router-link to="/">Syh Blog</router-link>
          </h1>
        </div>
      </el-header>

      <el-main>
        <div class="article-content">
          <el-card v-if="article">
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

          <el-card class="comments-section">
            <template #header>
              <h3>评论 ({{ comments.length }})</h3>
            </template>

            <el-form :model="commentForm" label-width="80px">
              <el-form-item label="昵称">
                <el-input v-model="commentForm.nickname" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="commentForm.email" />
              </el-form-item>
              <el-form-item label="评论内容">
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="4"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitComment">提交评论</el-button>
              </el-form-item>
            </el-form>

            <div class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <strong>{{ comment.nickname }}</strong>
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>
            </div>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import MarkdownIt from 'markdown-it'
import { getArticleDetail, addViewCount } from '@/api/article'
import { getCommentList, submitComment as postComment } from '@/api/comment'

const route = useRoute()
const article = ref<any>(null)
const comments = ref([])
const commentForm = ref({
  nickname: '',
  email: '',
  content: ''
})

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

const renderedContent = computed(() => {
  if (!article.value) return ''
  return md.render(article.value.content)
})

const loadArticle = async () => {
  try {
    const id = Number(route.params.id)
    const res = await getArticleDetail(id)
    article.value = res.data
    await addViewCount(id)

    // 加载评论
    const commentRes = await getCommentList(id)
    comments.value = commentRes.data
  } catch (error) {
    console.error('加载文章失败', error)
  }
}

const submitComment = async () => {
  if (!commentForm.value.nickname || !commentForm.value.content) {
    return
  }

  try {
    await postComment({
      articleId: route.params.id,
      ...commentForm.value
    })
    commentForm.value = {
      nickname: '',
      email: '',
      content: ''
    }
    await loadArticle()
  } catch (error) {
    console.error('提交评论失败', error)
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail {
  min-height: 100vh;
  background: #f5f7fa;
}

.el-header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-content {
  max-width: 900px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
}

.site-title a {
  text-decoration: none;
  color: #409eff;
  font-size: 24px;
  font-weight: bold;
}

.el-main {
  max-width: 900px;
  margin: 20px auto;
}

.article-title {
  font-size: 32px;
  margin-bottom: 20px;
}

.article-meta {
  color: #909399;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.article-meta span {
  margin-right: 20px;
}

.markdown-body {
  line-height: 1.8;
  font-size: 16px;
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
}

.comments-section {
  margin-top: 20px;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-time {
  color: #909399;
  font-size: 14px;
}

.comment-content {
  line-height: 1.6;
  color: #606266;
}
</style>
