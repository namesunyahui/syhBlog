<template>
  <div class="article-list">
    <!-- 搜索过滤区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="文章标题">
          <el-input
            v-model="filterForm.title"
            placeholder="请输入文章标题"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="filterForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增文章
          </el-button>
        </div>
      </template>

      <el-table :data="articles" border v-loading="loading" class="article-table">
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

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="文章预览"
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
      class="preview-dialog"
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
import { View, Clock, Plus } from '@element-plus/icons-vue'
import { getAdminArticles, deleteArticle, getArticleDetail, publishArticle } from '@/api/article'
import { getAllCategories } from '@/api/category'

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
const categories = ref<any[]>([])

// 过滤表单
const filterForm = ref({
  title: '',
  categoryId: undefined as number | undefined
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const previewHtml = computed(() => {
  if (!currentArticle.value?.content) {
    return '<p class="placeholder">暂无内容</p>'
  }
  if (window.marked) {
    return window.marked.parse(currentArticle.value.content)
  }
  return '<pre>' + currentArticle.value.content + '</pre>'
})

const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  }
}

const loadArticles = async () => {
  try {
    loading.value = true
    const params: any = {
      page: pagination.value.page,
      size: pagination.value.size
    }

    // 添加过滤参数
    if (filterForm.value.title) {
      params.title = filterForm.value.title
    }
    if (filterForm.value.categoryId) {
      params.categoryId = filterForm.value.categoryId
    }

    const res = await getAdminArticles(params)
    articles.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadArticles()
}

const handleReset = () => {
  filterForm.value = {
    title: '',
    categoryId: undefined
  }
  pagination.value.page = 1
  loadArticles()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadArticles()
}

const handleSizeChange = (size: number) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadArticles()
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

    // 如果当前页只有一条数据且不是第一页，则返回上一页
    if (articles.value.length === 1 && pagination.value.page > 1) {
      pagination.value.page--
    }

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
  loadCategories()
  loadArticles()
})
</script>

<style scoped>
.article-list {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

/* 搜索过滤卡片 */
.filter-card {
  flex-shrink: 0;
}

.filter-card :deep(.el-card__body) {
  padding: 12px;
}

.filter-form {
  margin-bottom: 0;
}

.filter-form .el-form-item {
  margin-bottom: 0;
}

/* 表格卡片 */
.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-card :deep(.el-card__header) {
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  overflow: auto;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.table-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.article-table {
  flex: 1;
}

.article-table :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 10;
}

/* 分页容器 */
.pagination-container {
  padding: 8px 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #ebeef5;
  background-color: #fff;
  flex-shrink: 0;
}

/* 预览容器 */
.preview-container {
  max-height: calc(90vh - 150px);
  overflow-y: auto;
  padding: 10px 0;
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

/* 预览弹窗样式 */
.preview-dialog :deep(.el-dialog) {
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  margin-top: 5vh !important;
  margin-bottom: 5vh !important;
}

.preview-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow: hidden;
  padding: 0 20px;
}

.preview-dialog :deep(.el-dialog__header) {
  flex-shrink: 0;
}

.preview-dialog :deep(.el-dialog__footer) {
  flex-shrink: 0;
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
