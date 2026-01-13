<template>
  <div class="article-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文章列表</span>
          <el-button type="primary" @click="handleAdd">新增文章</el-button>
        </div>
      </template>

      <el-table :data="articles" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="scope">
            {{ scope.row.category?.name }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isPublished ? 'success' : 'info'">
              {{ scope.row.isPublished ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350">
          <template #default="scope">
            <el-button size="small" @click="handlePreview(scope.row)">预览</el-button>
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              :type="scope.row.isPublished ? 'warning' : 'success'"
              @click="handlePublish(scope.row)">
              {{ scope.row.isPublished ? '撤回' : '发布' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="文章预览"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="preview-container" v-if="currentArticle">
        <h1 class="preview-title">{{ currentArticle.title || '(无标题)' }}</h1>

        <div class="preview-meta">
          <el-tag :type="currentArticle.isPublished ? 'success' : 'info'" size="small">
            {{ currentArticle.isPublished ? '已发布' : '草稿' }}
          </el-tag>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            浏览: {{ currentArticle.viewCount || 0 }}
          </span>
          <span class="meta-item">
            <el-icon><Clock /></el-icon>
            {{ formatDate(currentArticle.createdAt) }}
          </span>
        </div>

        <div class="preview-summary" v-if="currentArticle.summary">
          <strong>摘要：</strong>{{ currentArticle.summary }}
        </div>

        <el-divider />

        <div class="preview-content markdown-body" v-html="previewHtml"></div>
      </div>

      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEditFromPreview">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Clock } from '@element-plus/icons-vue'
import { getAdminArticles, deleteArticle, getArticleDetail, publishArticle } from '@/api/article'

declare global {
  interface Window {
    marked: any
  }
}

const router = useRouter()
const articles = ref([])
const loading = ref(false)
const previewVisible = ref(false)
const currentArticle = ref<any>(null)

const previewHtml = computed(() => {
  if (!currentArticle.value?.content) {
    return '<p class="placeholder">暂无内容</p>'
  }
  if (window.marked) {
    return window.marked.parse(currentArticle.value.content)
  }
  return '<pre>' + currentArticle.value.content + '</pre>'
})

const loadArticles = async () => {
  try {
    loading.value = true
    const res = await getAdminArticles({ page: 1, size: 100 })
    articles.value = res.data?.records || []
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  router.push('/admin/article/edit')
}

const handleEdit = (row: any) => {
  router.push(`/admin/article/edit?id=${row.id}`)
}

const handlePreview = async (row: any) => {
  try {
    loading.value = true
    const res = await getArticleDetail(row.id)
    currentArticle.value = res.data
    previewVisible.value = true
  } catch (error) {
    ElMessage.error('加载文章详情失败')
  } finally {
    loading.value = false
  }
}

const handleEditFromPreview = () => {
  previewVisible.value = false
  if (currentArticle.value) {
    router.push(`/admin/article/edit?id=${currentArticle.value.id}`)
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该文章吗?', '提示', {
      type: 'warning'
    })

    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handlePublish = async (row: any) => {
  try {
    const publish = !row.isPublished
    const action = publish ? '发布' : '撤回'
    await ElMessageBox.confirm(`确定${action}该文章吗?`, '提示', {
      type: 'warning'
    })

    // 注意：后端接口需要传递 boolean 值
    await publishArticle(row.id, publish)
    ElMessage.success(`${action}成功`)
    loadArticles()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-container {
  max-height: 60vh;
  overflow-y: auto;
}

.preview-title {
  font-size: 28px;
  margin-bottom: 16px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 12px;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  color: #606266;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.preview-summary {
  padding: 12px;
  background-color: #f5f7fa;
  border-left: 4px solid #409eff;
  margin-bottom: 16px;
  color: #606266;
}

.preview-content {
  line-height: 1.8;
  color: #333;
}

.placeholder {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 40px;
}

/* Markdown 样式 */
.markdown-body :deep(h1) {
  font-size: 2em;
  margin-top: 24px;
  margin-bottom: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.markdown-body :deep(h2) {
  font-size: 1.5em;
  margin-top: 24px;
  margin-bottom: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.markdown-body :deep(h3) {
  font-size: 1.25em;
  margin-top: 20px;
  margin-bottom: 12px;
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(code) {
  background-color: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #e83e8c;
}

.markdown-body :deep(pre) {
  background-color: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: #24292e;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #dfe2e5;
  padding-left: 16px;
  color: #6a737d;
  margin-bottom: 16px;
}

.markdown-body :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  border: 1px solid #dfe2e5;
  padding: 8px 12px;
}

.markdown-body :deep(table th) {
  background-color: #f6f8fa;
  font-weight: 600;
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 16px 0;
}
</style>
