<template>
  <div class="comment-manage-page">
    <!-- 搜索过滤区域 -->
    <div class="filter-section">
      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-item">
            <el-input
              v-model="filterForm.nickname"
              placeholder="搜索评论者昵称..."
              clearable
              class="filter-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
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
        <el-table
          :data="comments"
          class="luxury-table"
          @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="id" label="ID" width="80" align="center">
            <template #default="{ row }">
              <span class="table-id">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评论内容" min-width="300">
            <template #default="{ row }">
              <div class="comment-text">{{ row.content }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="评论者" width="150" align="center">
            <template #default="{ row }">
              <span class="table-value">{{ row.nickname }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip align="center">
            <template #default="{ row }">
              <span class="table-value subtle">{{ row.email || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="articleId" label="文章ID" width="100" align="center">
            <template #default="{ row }">
              <span class="table-id">#{{ row.articleId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="ipAddress" label="IP地址" width="150" align="center">
            <template #default="{ row }">
              <span class="table-value subtle">{{ row.ipAddress || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="评论时间" width="180" align="center">
            <template #default="{ row }">
              <span class="table-date">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" class="table-btn view" @click="handleViewDetail(row)">
                  查看
                </el-button>
                <el-button size="small" class="table-btn delete" @click="handleDelete(row)">
                  删除
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

    <!-- 操作按钮区域 -->
    <div class="action-buttons-section">
      <el-button
        class="batch-delete-btn"
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>
        批量删除 ({{ selectedIds.length }})
      </el-button>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="评论详情"
      width="700px"
      :close-on-click-modal="false"
      class="detail-dialog">
      <div class="comment-detail" v-if="currentComment">
        <div class="detail-item">
          <span class="detail-label">评论 ID</span>
          <span class="detail-value">{{ currentComment.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">文章 ID</span>
          <span class="detail-value">#{{ currentComment.articleId }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">评论者昵称</span>
          <span class="detail-value">{{ currentComment.nickname }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邮箱</span>
          <span class="detail-value">{{ currentComment.email || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">IP 地址</span>
          <span class="detail-value">{{ currentComment.ipAddress || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">评论时间</span>
          <span class="detail-value">{{ formatDate(currentComment.createdAt) }}</span>
        </div>
        <div class="detail-item full">
          <span class="detail-label">评论内容</span>
          <div class="comment-content">{{ currentComment.content }}</div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="dialog-btn secondary" @click="detailVisible = false">关闭</el-button>
          <el-button class="dialog-btn danger" @click="handleDeleteFromDetail">删除评论</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Delete } from '@element-plus/icons-vue'
import { getAdminComments, deleteComment, batchDeleteComments } from '@/api/comment'

interface Comment {
  id: number
  articleId: number
  content: string
  nickname: string
  email: string
  ipAddress: string
  createdAt: string
}

const comments = ref<Comment[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentComment = ref<Comment | null>(null)
const selectedIds = ref<number[]>([])

// 过滤表单
const filterForm = ref({
  nickname: ''
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const loadComments = async () => {
  try {
    loading.value = true
    const params: any = {
      page: pagination.value.page,
      size: pagination.value.size
    }

    if (filterForm.value.nickname) {
      params.nickname = filterForm.value.nickname
    }

    const res = await getAdminComments(params)
    comments.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadComments()
}

const handleReset = () => {
  filterForm.value = {
    nickname: ''
  }
  pagination.value.page = 1
  loadComments()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadComments()
}

const handleSizeChange = (size: number) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadComments()
}

const handleSelectionChange = (selection: Comment[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleViewDetail = (row: Comment) => {
  currentComment.value = row
  detailVisible.value = true
}

const handleDelete = async (row: Comment) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗?', '提示', {
      type: 'warning'
    })

    await deleteComment(row.id)
    ElMessage.success('删除成功')

    if (comments.value.length === 1 && pagination.value.page > 1) {
      pagination.value.page--
    }

    loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条评论吗?`, '提示', {
      type: 'warning'
    })

    await batchDeleteComments(selectedIds.value)
    ElMessage.success('批量删除成功')

    if (selectedIds.value.length === comments.value.length && pagination.value.page > 1) {
      pagination.value.page--
    }

    loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleDeleteFromDetail = async () => {
  if (!currentComment.value) return

  try {
    await ElMessageBox.confirm('确定删除该评论吗?', '提示', {
      type: 'warning'
    })

    await deleteComment(currentComment.value.id)
    ElMessage.success('删除成功')
    detailVisible.value = false

    if (comments.value.length === 1 && pagination.value.page > 1) {
      pagination.value.page--
    }

    loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
/* ==================== 页面布局 ==================== */
.comment-manage-page {
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

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  flex: 1;
  min-width: 200px;
}

.filter-input {
  width: 100%;
}

.filter-input :deep(.el-input__wrapper) {
  background: rgba(26, 26, 26, 0.6);
  border: 1px solid rgba(212, 175, 55, 0.2);
  box-shadow: none;
  transition: all 0.3s ease;
}

.filter-input :deep(.el-input__wrapper):hover,
.filter-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(212, 175, 55, 0.4);
}

.filter-input :deep(.el-input__inner) {
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

/* ==================== 操作按钮区域 ==================== */
.action-buttons-section {
  display: flex;
  justify-content: center;
  padding: 0;
}

.batch-delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 32px;
  background: rgba(244, 67, 54, 0.15);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
  border-radius: 10px;
  font-weight: 500;
  font-size: 15px;
  transition: all 0.3s ease;
}

.batch-delete-btn:hover:not(:disabled) {
  background: rgba(244, 67, 54, 0.2);
  border-color: rgba(244, 67, 54, 0.5);
  transform: translateY(-3px);
  box-shadow: 0 4px 20px rgba(244, 67, 54, 0.3);
}

.batch-delete-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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

/* 多选框样式 */
.luxury-table :deep(.el-checkbox) {
  color: rgba(244, 228, 188, 0.9);
}

.luxury-table :deep(.el-checkbox__inner) {
  background: rgba(26, 26, 26, 0.6);
  border-color: rgba(212, 175, 55, 0.4);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.luxury-table :deep(.el-checkbox__inner:hover) {
  border-color: rgba(212, 175, 55, 0.8);
}

.luxury-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%);
  border-color: #d4af37;
}

.luxury-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: #0a0a0a;
  border-width: 2px;
}

.luxury-table :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%);
  border-color: #d4af37;
}

.luxury-table :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner::before) {
  background-color: #0a0a0a;
}

.luxury-table :deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: rgba(212, 175, 55, 0.8);
}

/* 表格内容样式 */
.table-id {
  color: rgba(212, 175, 55, 0.6);
  font-weight: 600;
  font-size: 13px;
}

.table-value {
  color: rgba(244, 228, 188, 0.8);
  font-weight: 500;
}

.table-value.subtle {
  color: rgba(244, 228, 188, 0.5);
}

.comment-text {
  color: rgba(244, 228, 188, 0.8);
  line-height: 1.5;
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
}

.table-btn.view {
  background: transparent;
  color: #f4e4bc;
  border-color: rgba(244, 228, 188, 0.3);
}

.table-btn.view:hover {
  background: rgba(244, 228, 188, 0.1);
  border-color: rgba(244, 228, 188, 0.5);
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

/* ==================== 详情对话框 ==================== */
.detail-dialog :deep(.el-dialog) {
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

.detail-dialog :deep(.el-dialog)::before {
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

.detail-dialog :deep(.el-dialog__header) {
  border-bottom: none;
  padding: 30px 30px 20px 30px;
  margin: 0;
  position: relative;
}

.detail-dialog :deep(.el-dialog__header)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 30px;
  right: 30px;
  height: 2px;
  background: linear-gradient(90deg, transparent 0%, #d4af37 50%, transparent 100%);
  opacity: 0.5;
}

.detail-dialog :deep(.el-dialog__title) {
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

.detail-dialog :deep(.el-dialog__title)::before {
  content: '✦';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  color: #d4af37;
  font-size: 16px;
}

.detail-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.detail-dialog :deep(.el-dialog__footer) {
  border-top: none;
  padding: 0 30px 30px 30px;
}

.comment-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(26, 26, 26, 0.6) 0%, rgba(26, 26, 26, 0.4) 100%);
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.15);
  transition: all 0.3s ease;
}

.detail-item:hover {
  border-color: rgba(212, 175, 55, 0.25);
  background: linear-gradient(135deg, rgba(26, 26, 26, 0.7) 0%, rgba(26, 26, 26, 0.5) 100%);
}

.detail-item.full {
  flex-direction: column;
  align-items: flex-start;
}

.detail-label {
  min-width: 100px;
  color: rgba(244, 228, 188, 0.6);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 500;
}

.detail-value {
  color: rgba(244, 228, 188, 0.95);
  font-weight: 500;
  font-size: 14px;
}

.comment-content {
  width: 100%;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1) 0%, rgba(212, 175, 55, 0.05) 100%);
  border-left: 4px solid #d4af37;
  border-radius: 12px;
  color: rgba(244, 228, 188, 0.9);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  font-size: 15px;
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

.dialog-btn.danger {
  background: rgba(244, 67, 54, 0.15);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

.dialog-btn.danger:hover {
  background: rgba(244, 67, 54, 0.2);
  border-color: rgba(244, 67, 54, 0.5);
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

  .action-btn {
    flex: 1;
  }
}
</style>

<style>
/* 全局遮罩层样式 - 确保暗色背景 */
.detail-dialog + .el-overlay,
.detail-dialog ~ .el-overlay {
  background-color: rgba(0, 0, 0, 0.75) !important;
  backdrop-filter: blur(4px);
}
</style>
