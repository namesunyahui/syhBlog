<template>
  <div class="article-list-page">
    <!-- 搜索过滤区域 -->
    <div class="filter-section">
      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-item">
            <el-input
              v-model="filterForm.title"
              placeholder="搜索文章标题..."
              clearable
              class="filter-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="filter-item">
            <el-select
              v-model="filterForm.categoryId"
              placeholder="选择分类"
              clearable
              class="filter-select"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </div>
          <div class="filter-actions">
            <el-button class="action-btn primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button class="action-btn secondary" @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <div class="table-wrapper" v-loading="loading">
        <el-table :data="articles" class="luxury-table">
          <el-table-column prop="id" label="ID" width="80" align="center">
            <template #default="{ row }">
              <span class="table-id">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <div class="table-title">{{ row.title || '(无标题)' }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.category" type="info" class="category-tag">
                {{ row.category.name }}
              </el-tag>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="status-badge" :class="row.isPublished ? 'published' : 'draft'">
                {{ row.isPublished ? '已发布' : '草稿' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="100" align="center">
            <template #default="{ row }">
              <span class="view-count">{{ row.viewCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
            <template #default="{ row }">
              <span class="table-date">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="320" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" class="table-btn preview" @click="handlePreview(row)">
                  <el-icon><View /></el-icon>
                  预览
                </el-button>
                <el-button size="small" class="table-btn edit" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  size="small"
                  class="table-btn"
                  :class="row.isPublished ? 'unpublish' : 'publish'"
                  @click="handlePublish(row)">
                  {{ row.isPublished ? '撤回' : '发布' }}
                </el-button>
                <el-button size="small" class="table-btn delete" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
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
    </div>

    <!-- 新增按钮 -->
    <div class="create-section">
      <el-button class="create-btn-main" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增文章
      </el-button>
    </div>

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
          <span class="meta-badge" :class="currentArticle.isPublished ? 'published' : 'draft'">
            {{ currentArticle.isPublished ? '已发布' : '草稿' }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            {{ currentArticle.viewCount || 0 }}
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
        <div class="dialog-footer">
          <el-button class="dialog-btn secondary" @click="previewVisible = false">关闭</el-button>
          <el-button class="dialog-btn primary" @click="handleEditFromPreview">编辑文章</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Clock, Plus, Search, RefreshLeft, Edit, Delete } from '@element-plus/icons-vue'
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
/* ==================== 页面布局 ==================== */
.article-list-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ==================== 过滤区域 ==================== */
.filter-section {
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 16px;
  padding: 20px 24px;
}

.filter-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  flex-wrap: wrap;
}

.filter-item {
  flex: 1;
  min-width: 200px;
}

.filter-input,
.filter-select {
  width: 100%;
}

.filter-input :deep(.el-input__wrapper),
.filter-select :deep(.el-select__wrapper) {
  background: rgba(26, 26, 26, 0.6);
  border: 1px solid rgba(212, 175, 55, 0.2);
  box-shadow: none;
  transition: all 0.3s ease;
}

.filter-input :deep(.el-input__wrapper):hover,
.filter-select :deep(.el-select__wrapper):hover,
.filter-input :deep(.el-input__wrapper.is-focus),
.filter-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(212, 175, 55, 0.4);
}

.filter-input :deep(.el-input__inner),
.filter-select :deep(.el-select__selected-item) {
  color: rgba(244, 228, 188, 0.9);
}

.filter-input :deep(.el-input__inner::placeholder) {
  color: rgba(244, 228, 188, 0.4);
}

.filter-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
}

.action-btn.primary {
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%);
  color: #0a0a0a;
}

.action-btn.primary:hover {
  box-shadow: 0 4px 20px rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
}

.action-btn.secondary {
  background: rgba(212, 175, 55, 0.1);
  color: #f4e4bc;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

.action-btn.secondary:hover {
  background: rgba(212, 175, 55, 0.15);
  border-color: rgba(212, 175, 55, 0.4);
}

/* ==================== 表格区域 ==================== */
.table-section {
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.table-wrapper {
  padding: 0;
  flex: 1;
  overflow: auto;
}

/* 表格样式 */
.luxury-table {
  background: transparent;
  width: 100%;
  color: rgba(244, 228, 188, 0.9) !important;
}

.luxury-table :deep(.el-table__header-wrapper) {
  background: #1a1a1a;
  border-bottom: 2px solid #d4af37;
}

.luxury-table :deep(.el-table__header-wrapper .el-table__header) {
  background: #1a1a1a;
}

.luxury-table :deep(.el-table__header th) {
  background: #1a1a1a !important;
  border-bottom: 2px solid #d4af37 !important;
  color: #f4e4bc !important;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 18px 12px;
}

.luxury-table :deep(.el-table__header th .cell) {
  color: #f4e4bc !important;
}

.luxury-table :deep(.el-table__body-wrapper) {
  background: transparent;
}

.luxury-table :deep(.el-table__body tr) {
  background: transparent !important;
  transition: all 0.3s ease;
  color: rgba(244, 228, 188, 0.9) !important;
}

.luxury-table :deep(.el-table__body tr:hover) {
  background: rgba(212, 175, 55, 0.08) !important;
}

.luxury-table :deep(.el-table__body tr:hover td) {
  background: transparent !important;
  color: #f4e4bc !important;
}

.luxury-table :deep(.el-table__body td) {
  background: transparent !important;
  border-bottom: 1px solid rgba(212, 175, 55, 0.08);
  padding: 16px 12px;
  color: rgba(244, 228, 188, 0.9) !important;
}

.luxury-table :deep(.el-table__body td .cell) {
  color: rgba(244, 228, 188, 0.9) !important;
}

.luxury-table :deep(.el-table__body tr:last-child td) {
  border-bottom: none;
}

.luxury-table :deep(.el-table__empty-block) {
  background: transparent;
  color: rgba(244, 228, 188, 0.5);
}

.luxury-table :deep(.el-table__empty-text) {
  color: rgba(244, 228, 188, 0.5) !important;
}

/* 表格内容样式 */
.table-id {
  color: rgba(212, 175, 55, 0.6);
  font-weight: 600;
  font-size: 13px;
}

.table-title {
  color: #f4e4bc;
  font-weight: 500;
}

.category-tag {
  background: rgba(212, 175, 55, 0.15) !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  color: #f4e4bc !important;
}

.empty-text {
  color: rgba(244, 228, 188, 0.3);
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.published {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.status-badge.draft {
  background: rgba(244, 228, 188, 0.1);
  color: rgba(244, 228, 188, 0.6);
  border: 1px solid rgba(244, 228, 188, 0.2);
}

.view-count {
  color: rgba(212, 175, 55, 0.8);
  font-weight: 600;
}

.table-date {
  color: rgba(244, 228, 188, 0.5);
  font-size: 13px;
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.table-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  border: 1px solid;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
  background: transparent !important;
}

/* 预览按钮 - 暗色奢华风格 */
.table-btn.preview {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15) 0%, rgba(244, 228, 188, 0.08) 100%) !important;
  color: #f4e4bc !important;
  border-color: rgba(212, 175, 55, 0.3) !important;
  font-weight: 500;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  position: relative;
  overflow: hidden;
}

.table-btn.preview::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s ease;
}

.table-btn.preview:hover::before {
  left: 100%;
}

.table-btn.preview:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.25) 0%, rgba(244, 228, 188, 0.15) 100%) !important;
  border-color: rgba(212, 175, 55, 0.5) !important;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.25) !important;
  transform: translateY(-2px);
}

.table-btn.preview :deep(.el-icon) {
  color: #f4e4bc !important;
}

.table-btn.edit {
  background: transparent;
  color: #d4af37;
  border-color: rgba(212, 175, 55, 0.3);
}

.table-btn.edit:hover {
  background: rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.5);
}

.table-btn.publish {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border-color: rgba(76, 175, 80, 0.3);
}

.table-btn.publish:hover {
  background: rgba(76, 175, 80, 0.2);
  border-color: rgba(76, 175, 80, 0.5);
}

.table-btn.unpublish {
  background: rgba(255, 152, 0, 0.15);
  color: #ffb74d;
  border-color: rgba(255, 152, 0, 0.3);
}

.table-btn.unpublish:hover {
  background: rgba(255, 152, 0, 0.2);
  border-color: rgba(255, 152, 0, 0.5);
}

.table-btn.delete {
  background: rgba(244, 67, 54, 0.15);
  color: #ef5350;
  border-color: rgba(244, 67, 54, 0.3);
}

.table-btn.delete:hover {
  background: rgba(244, 67, 54, 0.2);
  border-color: rgba(244, 67, 54, 0.5);
}

/* ==================== 分页 ==================== */
.pagination-wrapper {
  padding: 20px 24px;
  border-top: 1px solid rgba(212, 175, 55, 0.1);
  display: flex;
  justify-content: center;
}

.pagination-wrapper :deep(.el-pagination) {
  color: rgba(244, 228, 188, 0.7);
}

.pagination-wrapper :deep(.el-pagination button),
.pagination-wrapper :deep(.el-pagination span) {
  background: transparent;
  color: rgba(244, 228, 188, 0.7);
}

.pagination-wrapper :deep(.el-pagination button:hover) {
  background: rgba(212, 175, 55, 0.1);
}

.pagination-wrapper :deep(.el-pagination .is-active) {
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%) !important;
  color: #0a0a0a !important;
}

.pagination-wrapper :deep(.el-input__wrapper) {
  background: rgba(26, 26, 26, 0.6);
  border: 1px solid rgba(212, 175, 55, 0.2);
}

/* ==================== 新增按钮区域 ==================== */
.create-section {
  display: flex;
  justify-content: center;
  padding: 0;
}

.create-btn-main {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%);
  color: #0a0a0a;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(212, 175, 55, 0.3);
}

.create-btn-main:hover {
  box-shadow: 0 6px 30px rgba(212, 175, 55, 0.5);
  transform: translateY(-3px);
}

/* ==================== 预览对话框 ==================== */
.preview-dialog :deep(.el-dialog) {
  background: linear-gradient(145deg, rgba(20, 20, 20, 0.98) 0%, rgba(15, 15, 15, 0.95) 100%);
  backdrop-filter: blur(30px);
  border: 2px solid transparent;
  background-clip: padding-box;
  position: relative;
  border-radius: 20px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(212, 175, 55, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.preview-dialog :deep(.el-dialog)::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 20px;
  padding: 2px;
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 50%, #d4af37 100%);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.3;
  pointer-events: none;
}

.preview-dialog :deep(.el-dialog__header) {
  border-bottom: none;
  padding: 30px 30px 20px 30px;
  margin: 0;
  position: relative;
}

.preview-dialog :deep(.el-dialog__header)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 30px;
  right: 30px;
  height: 2px;
  background: linear-gradient(90deg, transparent 0%, #d4af37 50%, transparent 100%);
  opacity: 0.5;
}

.preview-dialog :deep(.el-dialog__title) {
  color: #f4e4bc;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 1px;
  text-align: center;
  display: block;
  width: 100%;
  margin: 0;
  padding-bottom: 15px;
  position: relative;
}

.preview-dialog :deep(.el-dialog__title)::before {
  content: '✦';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  color: #d4af37;
  font-size: 16px;
}

.preview-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.preview-dialog :deep(.el-dialog__footer) {
  border-top: none;
  padding: 0 30px 30px 30px;
}

.preview-container {
  max-height: calc(90vh - 200px);
  overflow-y: auto;
  padding: 20px 0;
}

.preview-title {
  font-size: 36px;
  font-weight: 600;
  color: #f4e4bc;
  margin-bottom: 24px;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  text-align: center;
  letter-spacing: 0.5px;
  line-height: 1.3;
}

.preview-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.15);
  flex-wrap: wrap;
}

.meta-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.meta-badge.published {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.meta-badge.draft {
  background: rgba(244, 228, 188, 0.1);
  color: rgba(244, 228, 188, 0.6);
  border: 1px solid rgba(244, 228, 188, 0.2);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(244, 228, 188, 0.6);
  font-size: 14px;
}

.preview-summary {
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1) 0%, rgba(212, 175, 55, 0.05) 100%);
  border-left: 4px solid #d4af37;
  border-radius: 12px;
  color: rgba(244, 228, 188, 0.85);
  margin-bottom: 28px;
  line-height: 1.7;
  font-size: 15px;
}

.preview-summary strong {
  color: #f4e4bc;
  font-weight: 600;
}

.preview-content {
  line-height: 1.8;
  color: rgba(244, 228, 188, 0.9);
}

.preview-content :deep(h1),
.preview-content :deep(h2),
.preview-content :deep(h3) {
  color: #f4e4bc;
  font-family: 'Playfair Display', 'Times New Roman', serif;
}

.preview-content :deep(code) {
  background: rgba(212, 175, 55, 0.1);
  color: #d4af37;
  padding: 2px 8px;
  border-radius: 4px;
}

.preview-content :deep(pre) {
  background: rgba(26, 26, 26, 0.8);
  padding: 16px;
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.15);
  overflow-x: auto;
}

.preview-content :deep(blockquote) {
  border-left: 3px solid #d4af37;
  padding-left: 16px;
  color: rgba(244, 228, 188, 0.6);
}

.preview-content :deep(a) {
  color: #d4af37;
  text-decoration: none;
}

.preview-content :deep(a:hover) {
  text-decoration: underline;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.dialog-btn {
  padding: 12px 32px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
}

.dialog-btn.primary {
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%);
  color: #0a0a0a;
  border: none;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
}

.dialog-btn.primary:hover {
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
}

.dialog-btn.secondary {
  background: rgba(212, 175, 55, 0.1);
  color: #f4e4bc;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

.dialog-btn.secondary:hover {
  background: rgba(212, 175, 55, 0.15);
  border-color: rgba(212, 175, 55, 0.5);
  transform: translateY(-2px);
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    min-width: 100%;
  }

  .filter-actions {
    width: 100%;
    justify-content: stretch;
  }

  .action-btn {
    flex: 1;
  }

  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .action-buttons {
    flex-wrap: wrap;
  }
}
</style>
