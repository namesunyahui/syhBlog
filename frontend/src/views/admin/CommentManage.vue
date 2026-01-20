<template>
  <div class="comment-manage">
    <!-- 搜索过滤区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="昵称">
          <el-input
            v-model="filterForm.nickname"
            placeholder="请输入评论者昵称"
            clearable
            style="width: 200px"
          />
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
          <el-button
            type="danger"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </template>

      <el-table
        :data="comments"
        border
        v-loading="loading"
        class="comment-table"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip min-width="300" />
        <el-table-column prop="nickname" label="评论者" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
        <el-table-column prop="articleId" label="文章ID" width="100" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="createdAt" label="评论时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              删除
            </el-button>
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="评论详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="comment-detail" v-if="currentComment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="评论ID">
            {{ currentComment.id }}
          </el-descriptions-item>
          <el-descriptions-item label="文章ID">
            {{ currentComment.articleId }}
          </el-descriptions-item>
          <el-descriptions-item label="评论者昵称">
            {{ currentComment.nickname }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ currentComment.email }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ currentComment.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="评论时间">
            {{ formatDate(currentComment.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="评论内容">
            <div class="comment-content">{{ currentComment.content }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleDeleteFromDetail">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
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

    // 添加过滤参数
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

    // 如果当前页只有一条数据且不是第一页，则返回上一页
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

    // 如果当前页数据全部被删除且不是第一页，则返回上一页
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

    // 如果当前页只有一条数据且不是第一页，则返回上一页
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
.comment-manage {
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

.comment-table {
  flex: 1;
}

.comment-table :deep(.el-table__header-wrapper) {
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

/* 评论详情 */
.comment-detail {
  padding: 10px 0;
}

.comment-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  max-height: 300px;
  overflow-y: auto;
}
</style>
