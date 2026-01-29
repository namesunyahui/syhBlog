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
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.9) 0%, rgba(10, 10, 10, 0.85) 100%);
  border: 1px solid rgba(212, 175, 55, 0.3);
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.filter-input :deep(.el-input__wrapper):hover,
.filter-select :deep(.el-select__wrapper):hover,
.filter-input :deep(.el-input__wrapper.is-focus),
.filter-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(212, 175, 55, 0.6);
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.95) 0%, rgba(10, 10, 10, 0.9) 100%);
  box-shadow:
    0 0 0 3px rgba(212, 175, 55, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.filter-input :deep(.el-input__inner),
.filter-select :deep(.el-select__selected-item) {
  color: rgba(244, 228, 188, 0.9);
}

.filter-input :deep(.el-input__inner::placeholder) {
  color: rgba(244, 228, 188, 0.4);
}

/* ==================== 暗色奢华风格下拉框 ==================== */
.filter-select :deep(.el-select__placeholder) {
  color: rgba(244, 228, 188, 0.35);
}

/* 下拉框图标 */
.filter-select :deep(.el-select__caret) {
  color: rgba(212, 175, 55, 0.7);
}

/* 下拉弹出菜单 */
:deep(.el-select-dropdown) {
  background: linear-gradient(145deg, rgba(15, 15, 15, 0.98) 0%, rgba(5, 5, 5, 0.96)) !important;
  border: 1px solid rgba(212, 175, 55, 0.4) !important;
  box-shadow:
    0 15px 50px rgba(0, 0, 0, 0.8),
    0 0 0 1px rgba(212, 175, 55, 0.15),
    0 0 30px rgba(212, 175, 55, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
  border-radius: 12px !important;
  backdrop-filter: blur(30px) !important;
  padding: 8px !important;
}

/* 下拉选项 */
:deep(.el-select-dropdown__item) {
  color: rgba(244, 228, 188, 0.9) !important;
  background: transparent !important;
  border-radius: 8px !important;
  padding: 11px 16px !important;
  margin: 2px 0 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  font-weight: 400 !important;
  letter-spacing: 0.3px !important;
}

:deep(.el-select-dropdown__item:hover) {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2) 0%, rgba(212, 175, 55, 0.12) 100%) !important;
  color: #f4e4bc !important;
  transform: translateX(2px);
}

:deep(.el-select-dropdown__item.is-selected) {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3) 0%, rgba(212, 175, 55, 0.2) 100%) !important;
  color: #f4e4bc !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.08) !important;
}

:deep(.el-select-dropdown__item.is-selected:hover) {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.4) 0%, rgba(212, 175, 55, 0.3) 100%) !important;
  border-color: rgba(212, 175, 55, 0.5) !important;
}

/* 多选标签 */
:deep(.el-tag) {
  background: rgba(212, 175, 55, 0.15) !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  color: #f4e4bc !important;
}

:deep(.el-tag__close) {
  color: rgba(244, 228, 188, 0.7) !important;
}

:deep(.el-tag__close:hover) {
  color: #f4e4bc !important;
  background: rgba(212, 175, 55, 0.2) !important;
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

/* ==================== 滚动条暗色奢华样式 ==================== */
.table-wrapper::-webkit-scrollbar,
.el-table__body-wrapper::-webkit-scrollbar,
.el-table__header-wrapper::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.table-wrapper::-webkit-scrollbar-track,
.el-table__body-wrapper::-webkit-scrollbar-track,
.el-table__header-wrapper::-webkit-scrollbar-track {
  background: rgba(10, 10, 10, 0.3);
  border-radius: 10px;
}

.table-wrapper::-webkit-scrollbar-thumb,
.el-table__body-wrapper::-webkit-scrollbar-thumb,
.el-table__header-wrapper::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3) 0%, rgba(212, 175, 55, 0.2) 100%);
  border-radius: 10px;
  border: 2px solid rgba(10, 10, 10, 0.3);
  transition: all 0.3s ease;
}

.table-wrapper::-webkit-scrollbar-thumb:hover,
.el-table__body-wrapper::-webkit-scrollbar-thumb:hover,
.el-table__header-wrapper::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.5) 0%, rgba(212, 175, 55, 0.4) 100%);
  border-color: rgba(10, 10, 10, 0.5);
}

.table-wrapper::-webkit-scrollbar-corner,
.el-table__body-wrapper::-webkit-scrollbar-corner,
.el-table__header-wrapper::-webkit-scrollbar-corner {
  background: rgba(10, 10, 10, 0.3);
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

/* 分页器输入框 - 使用奢华渐变背景 */
.pagination-wrapper :deep(.el-input__wrapper) {
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.9) 0%, rgba(10, 10, 10, 0.85) 100%) !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
  transition: all 0.3s ease !important;
}

.pagination-wrapper :deep(.el-input__wrapper:hover),
.pagination-wrapper :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(212, 175, 55, 0.6) !important;
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.95) 0%, rgba(10, 10, 10, 0.9) 100%) !important;
  box-shadow:
    0 0 0 3px rgba(212, 175, 55, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.08) !important;
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
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.98) 0%, rgba(10, 10, 10, 0.96) 100%);
  backdrop-filter: blur(40px);
  border: 2px solid transparent;
  background-clip: padding-box;
  position: relative;
  border-radius: 24px;
  box-shadow:
    0 25px 80px rgba(0, 0, 0, 0.7),
    0 0 0 1px rgba(212, 175, 55, 0.15),
    0 0 40px rgba(212, 175, 55, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
  overflow: visible;
  z-index: 9999 !important;
}

/* 确保遮罩层也在最高层级 */
.preview-dialog :deep(.el-overlay) {
  z-index: 9998 !important;
}

/* 对话框金色流光边框 */
.preview-dialog :deep(.el-dialog)::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 24px;
  padding: 2px;
  background: linear-gradient(135deg,
    #d4af37 0%,
    #f4e4bc 25%,
    #d4af37 50%,
    #b8962e 75%,
    #d4af37 100%);
  background-size: 300% 300%;
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.4;
  pointer-events: none;
  z-index: -1;
  animation: borderFlow 6s ease infinite;
}

@keyframes borderFlow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.preview-dialog :deep(.el-dialog__header) {
  border-bottom: none;
  padding: 32px 32px 24px 32px;
  margin: 0;
  position: relative;
  background: transparent;
}

/* 标题底部分隔线 */
.preview-dialog :deep(.el-dialog__header)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 32px;
  right: 32px;
  height: 1px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(212, 175, 55, 0.6) 20%,
    #d4af37 50%,
    rgba(212, 175, 55, 0.6) 80%,
    transparent 100%);
}

.preview-dialog :deep(.el-dialog__title) {
  color: #f4e4bc;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 1.5px;
  text-align: center;
  display: block;
  width: 100%;
  margin: 0;
  padding-bottom: 18px;
  position: relative;
  text-shadow: 0 2px 10px rgba(212, 175, 55, 0.3);
}

/* 标题装饰星标 */
.preview-dialog :deep(.el-dialog__title)::before {
  content: '✦';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  color: #d4af37;
  font-size: 18px;
  filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.6));
}

.preview-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
  width: 36px;
  height: 36px;
  background: rgba(212, 175, 55, 0.12);
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(212, 175, 55, 0.25);
}

.preview-dialog :deep(.el-dialog__headerbtn:hover) {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.5);
  transform: rotate(90deg);
}

.preview-dialog :deep(.el-dialog__close) {
  color: rgba(244, 228, 188, 0.75);
  font-size: 20px;
  transition: all 0.3s ease;
}

.preview-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #f4e4bc;
  text-shadow: 0 0 10px rgba(244, 228, 188, 0.5);
}

.preview-dialog :deep(.el-dialog__body) {
  padding: 32px 32px 28px 32px;
}

.preview-dialog :deep(.el-dialog__footer) {
  border-top: none;
  padding: 0 32px 32px 32px;
  display: flex;
  justify-content: center;
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

/* ==================== 预览对话框按钮 ==================== */
.preview-dialog .dialog-footer {
  display: flex;
  justify-content: center;
  gap: 18px;
  padding-top: 8px;
}

.preview-dialog .dialog-btn {
  padding: 13px 36px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.8px;
  min-width: 120px;
  position: relative;
  overflow: hidden;
}

.preview-dialog .dialog-btn.primary {
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 50%, #d4af37 100%);
  background-size: 200% 200%;
  color: #0a0a0a;
  border: none;
  box-shadow:
    0 4px 20px rgba(212, 175, 55, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.preview-dialog .dialog-btn.primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent);
  transition: left 0.6s ease;
}

.preview-dialog .dialog-btn.primary:hover {
  box-shadow:
    0 6px 25px rgba(212, 175, 55, 0.45),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transform: translateY(-3px);
  background-position: 100% 50%;
}

.preview-dialog .dialog-btn.primary:hover::before {
  left: 100%;
}

.preview-dialog .dialog-btn.primary:active {
  transform: translateY(-1px);
}

.preview-dialog .dialog-btn.secondary {
  background: rgba(212, 175, 55, 0.12);
  color: #f4e4bc;
  border: 1px solid rgba(212, 175, 55, 0.32);
  box-shadow:
    0 2px 10px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.preview-dialog .dialog-btn.secondary:hover {
  background: rgba(212, 175, 55, 0.18);
  border-color: rgba(212, 175, 55, 0.55);
  box-shadow:
    0 4px 15px rgba(212, 175, 55, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  transform: translateY(-3px);
  text-shadow: 0 0 8px rgba(244, 228, 188, 0.3);
}

.preview-dialog .dialog-btn.secondary:active {
  transform: translateY(-1px);
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

<style>
/* ==================== 全局下拉菜单暗色奢华样式 ==================== */
/* 覆盖 Element Plus 下拉菜单的默认白色样式 */

.el-select-dropdown {
  background: linear-gradient(145deg, rgba(15, 15, 15, 0.98) 0%, rgba(5, 5, 5, 0.96)) !important;
  border: 1px solid rgba(212, 175, 55, 0.4) !important;
  box-shadow:
    0 15px 50px rgba(0, 0, 0, 0.8),
    0 0 0 1px rgba(212, 175, 55, 0.15),
    0 0 30px rgba(212, 175, 55, 0.1) !important;
  border-radius: 12px !important;
  backdrop-filter: blur(30px) !important;
  padding: 8px !important;
}

/* 下拉菜单外层包装 - 移除白色边框 */
.el-select-dropdown__list,
.el-select-dropdown__wrap {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

/* 下拉菜单边框 - 移除默认白色边框 */
.el-select-dropdown .el-scrollbar__wrap,
.el-select-dropdown .el-scrollbar__view {
  background: transparent !important;
  border: none !important;
}

.el-select-dropdown .el-select-dropdown__list {
  background: transparent !important;
  border: none !important;
}

/* 下拉选项 */
.el-select-dropdown__item {
  color: rgba(244, 228, 188, 0.9) !important;
  background: transparent !important;
  border-radius: 8px !important;
  padding: 10px 16px !important;
  margin: 2px 0 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  font-weight: 400 !important;
  letter-spacing: 0.3px !important;
  line-height: 1.5 !important;
  height: auto !important;
  display: flex !important;
  align-items: center !important;
}

.el-select-dropdown__item:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2) 0%, rgba(212, 175, 55, 0.12) 100%) !important;
  color: #f4e4bc !important;
  transform: translateX(2px);
}

.el-select-dropdown__item.is-selected {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3) 0%, rgba(212, 175, 55, 0.2) 100%) !important;
  color: #f4e4bc !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.08) !important;
}

.el-select-dropdown__item.is-selected:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.4) 0%, rgba(212, 175, 55, 0.3) 100%) !important;
  border-color: rgba(212, 175, 55, 0.5) !important;
}

/* 移除下拉面板的所有白色边框 */
.el-popper.is-light,
.el-select-dropdown.is-light {
  background: transparent !important;
  border: none !important;
}

/* 下拉箭头样式 */
.el-select-dropdown__list .el-select-dropdown__item span {
  line-height: 1.5 !important;
}

/* ==================== 分页组件暗色奢华样式 ==================== */

/* 分页器中的选择器 */
.el-pagination .el-select .el-select__wrapper {
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.9) 0%, rgba(10, 10, 10, 0.85) 100%) !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
  transition: all 0.3s ease !important;
}

.el-pagination .el-select .el-select__wrapper:hover,
.el-pagination .el-select .el-select__wrapper.is-focused {
  border-color: rgba(212, 175, 55, 0.6) !important;
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.95) 0%, rgba(10, 10, 10, 0.9) 100%) !important;
  box-shadow:
    0 0 0 3px rgba(212, 175, 55, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.08) !important;
}

/* 分页器选择器文字颜色 */
.el-pagination .el-select .el-select__selected-item {
  color: rgba(244, 228, 188, 0.9) !important;
}

.el-pagination .el-select .el-select__placeholder {
  color: rgba(244, 228, 188, 0.35) !important;
}

/* 分页器选择器图标 */
.el-pagination .el-select .el-select__caret {
  color: rgba(212, 175, 55, 0.7) !important;
}

/* 分页器输入框（jumper 跳转输入框） */
.el-pagination .el-input .el-input__wrapper {
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.9) 0%, rgba(10, 10, 10, 0.85) 100%) !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
  transition: all 0.3s ease !important;
}

.el-pagination .el-input .el-input__wrapper:hover,
.el-pagination .el-input .el-input__wrapper.is-focus {
  border-color: rgba(212, 175, 55, 0.6) !important;
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.95) 0%, rgba(10, 10, 10, 0.9) 100%) !important;
  box-shadow:
    0 0 0 3px rgba(212, 175, 55, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.08) !important;
}

.el-pagination .el-input .el-input__inner {
  color: rgba(244, 228, 188, 0.9) !important;
}

.el-pagination .el-input .el-input__inner::placeholder {
  color: rgba(244, 228, 188, 0.35) !important;
}

/* ==================== 全局滚动条暗色奢华样式 ==================== */

/* 预览容器滚动条 */
.preview-container::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.preview-container::-webkit-scrollbar-track {
  background: rgba(10, 10, 10, 0.3);
  border-radius: 10px;
}

.preview-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3) 0%, rgba(212, 175, 55, 0.2) 100%);
  border-radius: 10px;
  border: 2px solid rgba(10, 10, 10, 0.3);
  transition: all 0.3s ease;
}

.preview-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.5) 0%, rgba(212, 175, 55, 0.4) 100%);
  border-color: rgba(10, 10, 10, 0.5);
}

.preview-container::-webkit-scrollbar-corner {
  background: rgba(10, 10, 10, 0.3);
}

/* 所有滚动区域的通用滚动条样式 */
*::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

*::-webkit-scrollbar-track {
  background: rgba(10, 10, 10, 0.3);
  border-radius: 10px;
}

*::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3) 0%, rgba(212, 175, 55, 0.2) 100%);
  border-radius: 10px;
  border: 2px solid rgba(10, 10, 10, 0.3);
  transition: all 0.3s ease;
}

*::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.5) 0%, rgba(212, 175, 55, 0.4) 100%);
  border-color: rgba(10, 10, 10, 0.5);
}

*::-webkit-scrollbar-corner {
  background: rgba(10, 10, 10, 0.3);
}

/* ==================== 全局对话框样式覆盖 ==================== */

/* 强制覆盖 Element Plus 对话框默认白色背景 */
.el-dialog {
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.98) 0%, rgba(10, 10, 10, 0.96) 100%) !important;
  backdrop-filter: blur(40px) !important;
}

/* 确保对话框头部、主体、底部都是透明或暗色背景 */
.el-dialog__header,
.el-dialog__body,
.el-dialog__footer {
  background: transparent !important;
}

/* ==================== 全局遮罩层样式 ==================== */
/* 确保暗色背景，增强视觉层次 */
.preview-dialog + .el-overlay,
.preview-dialog ~ .el-overlay,
.form-dialog + .el-overlay,
.form-dialog ~ .el-overlay,
.detail-dialog + .el-overlay,
.detail-dialog ~ .el-overlay,
.el-overlay {
  background-color: rgba(0, 0, 0, 0.82) !important;
  backdrop-filter: blur(8px) !important;
  z-index: 9998 !important;
}

/* 确保对话框在最上层 */
.el-dialog__wrapper {
  z-index: 9999 !important;
}

/* 遮罩层动画效果 */
.preview-dialog + .el-overlay,
.preview-dialog ~ .el-overlay {
  animation: overlayFadeIn 0.3s ease-out;
}

@keyframes overlayFadeIn {
  from {
    opacity: 0;
    backdrop-filter: blur(0px);
  }
  to {
    opacity: 1;
    backdrop-filter: blur(8px);
  }
}
</style>
