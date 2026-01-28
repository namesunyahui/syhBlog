<template>
  <div class="user-manage-page">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-item">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名、昵称、邮箱..."
              clearable
              class="filter-input"
              @clear="loadUsers"
              @keyup.enter="loadUsers"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="filter-actions">
            <el-button class="action-btn primary" @click="loadUsers">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <div class="table-wrapper" v-loading="loading">
        <el-table :data="users" class="luxury-table">
          <el-table-column prop="id" label="ID" width="80" align="center">
            <template #default="{ row }">
              <span class="table-id">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" width="150" align="center">
            <template #default="{ row }">
              <span class="table-value">{{ row.username }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="昵称" width="150" align="center">
            <template #default="{ row }">
              <span class="table-name">{{ row.nickname }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="200">
            <template #default="{ row }">
              <span class="table-value">{{ row.email }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="150" align="center">
            <template #default="{ row }">
              <span class="role-badge" :class="row.role === 'SUPER_ADMIN' ? 'super-admin' : 'admin'">
                {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="{ row }">
              <span class="status-badge" :class="row.status === 'ACTIVE' ? 'active' : 'banned'">
                {{ row.status === 'ACTIVE' ? '激活' : '封禁' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录" width="180" align="center">
            <template #default="{ row }">
              <span class="table-date">{{ row.lastLoginTime ? formatDate(row.lastLoginTime) : '未登录' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
            <template #default="{ row }">
              <span class="table-date">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  v-if="row.role !== 'SUPER_ADMIN'"
                  size="small"
                  class="table-btn edit"
                  @click="handleChangeRole(row)">
                  角色
                </el-button>
                <el-button
                  size="small"
                  class="table-btn"
                  :class="row.status === 'ACTIVE' ? 'ban' : 'activate'"
                  @click="handleChangeStatus(row)">
                  {{ row.status === 'ACTIVE' ? '封禁' : '激活' }}
                </el-button>
                <el-button
                  v-if="row.role !== 'SUPER_ADMIN'"
                  size="small"
                  class="table-btn delete"
                  @click="handleDelete(row)">
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
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </div>

    <!-- 修改角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="修改用户角色"
      width="500px"
      :close-on-click-modal="false"
      class="form-dialog">
      <el-form label-width="100px" class="luxury-form">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled class="luxury-input" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="currentUser.nickname" disabled class="luxury-input" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newRole" placeholder="请选择角色" class="luxury-select">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="dialog-btn secondary" @click="roleDialogVisible = false">取消</el-button>
          <el-button class="dialog-btn primary" @click="confirmChangeRole">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserList, updateUserRole, updateUserStatus, deleteUser, type User } from '@/api/user'

const users = ref<User[]>([])
const loading = ref(false)
const keyword = ref('')

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const roleDialogVisible = ref(false)
const currentUser = ref<Partial<User>>({})
const newRole = ref('')

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const loadUsers = async () => {
  try {
    loading.value = true
    const res = await getUserList({
      current: pagination.current,
      size: pagination.size,
      keyword: keyword.value || undefined
    })

    users.value = res.data.list
    pagination.total = res.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleChangeRole = (user: User) => {
  currentUser.value = user
  newRole.value = user.role
  roleDialogVisible.value = true
}

const confirmChangeRole = async () => {
  try {
    await updateUserRole(currentUser.value.id!, newRole.value)
    ElMessage.success('修改角色成功')
    roleDialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    ElMessage.error(error.message || '修改角色失败')
  }
}

const handleChangeStatus = async (user: User) => {
  const newStatus = user.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? '激活' : '封禁'

  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 ${user.nickname}(${user.username}) 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${action}失败`)
    }
  }
}

const handleDelete = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.nickname}(${user.username}) 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await deleteUser(user.id)
    ElMessage.success('删除成功')
    if (users.value.length === 1 && pagination.current > 1) {
      pagination.current--
    }
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
/* ==================== 页面布局 ==================== */
.user-manage-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ==================== 搜索区域 ==================== */
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

.table-name {
  color: #f4e4bc;
  font-weight: 500;
}

.table-value {
  color: rgba(244, 228, 188, 0.8);
  font-weight: 500;
}

.table-date {
  color: rgba(244, 228, 188, 0.5);
  font-size: 13px;
}

/* 角色标签 */
.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-badge.super-admin {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.25) 0%, rgba(244, 228, 188, 0.15) 100%);
  color: #f4e4bc;
  border: 1px solid rgba(212, 175, 55, 0.4);
}

.role-badge.admin {
  background: rgba(212, 175, 55, 0.1);
  color: rgba(244, 228, 188, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.3);
}

/* 状态标签 */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.active {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.status-badge.banned {
  background: rgba(244, 67, 54, 0.15);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

/* 操作按钮 */
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

.table-btn.edit {
  background: transparent;
  color: #d4af37;
  border-color: rgba(212, 175, 55, 0.3);
}

.table-btn.edit:hover {
  background: rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.5);
}

.table-btn.ban {
  background: rgba(255, 152, 0, 0.15);
  color: #ffb74d;
  border-color: rgba(255, 152, 0, 0.3);
}

.table-btn.ban:hover {
  background: rgba(255, 152, 0, 0.2);
  border-color: rgba(255, 152, 0, 0.5);
}

.table-btn.activate {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border-color: rgba(76, 175, 80, 0.3);
}

.table-btn.activate:hover {
  background: rgba(76, 175, 80, 0.2);
  border-color: rgba(76, 175, 80, 0.5);
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

/* ==================== 对话框 ==================== */
.form-dialog :deep(.el-dialog) {
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

.form-dialog :deep(.el-dialog)::before {
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
  z-index: 0;
}

.form-dialog :deep(.el-dialog__header) {
  border-bottom: none;
  padding: 30px 30px 20px 30px;
  margin: 0;
  position: relative;
  background: transparent;
}

.form-dialog :deep(.el-dialog__header)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 30px;
  right: 30px;
  height: 2px;
  background: linear-gradient(90deg, transparent 0%, #d4af37 50%, transparent 100%);
  opacity: 0.5;
}

.form-dialog :deep(.el-dialog__title) {
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

.form-dialog :deep(.el-dialog__title)::before {
  content: '✦';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  color: #d4af37;
  font-size: 16px;
}

.form-dialog :deep(.el-dialog__headerbtn) {
  top: 25px;
  right: 25px;
  width: 32px;
  height: 32px;
  background: rgba(212, 175, 55, 0.1);
  border-radius: 50%;
  transition: all 0.3s ease;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.form-dialog :deep(.el-dialog__headerbtn:hover) {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.4);
}

.form-dialog :deep(.el-dialog__close) {
  color: rgba(244, 228, 188, 0.7);
  font-size: 18px;
}

.form-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #f4e4bc;
}

.form-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.form-dialog :deep(.el-dialog__footer) {
  border-top: none;
  padding: 0 30px 30px 30px;
  display: flex;
  justify-content: center;
}

.luxury-form :deep(.el-form-item__label) {
  color: rgba(244, 228, 188, 0.85);
  font-weight: 500;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.luxury-form :deep(.el-form-item) {
  margin-bottom: 26px;
}

.luxury-input :deep(.el-input__wrapper),
.luxury-textarea :deep(.el-textarea__inner),
.luxury-select :deep(.el-select__wrapper) {
  background: rgba(26, 26, 26, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.25);
  box-shadow: none;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.luxury-input :deep(.el-input__wrapper):hover,
.luxury-input :deep(.el-input__wrapper.is-focus),
.luxury-textarea :deep(.el-textarea__inner):hover,
.luxury-textarea :deep(.el-textarea__inner:focus),
.luxury-select :deep(.el-select__wrapper):hover,
.luxury-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(212, 175, 55, 0.5);
  background: rgba(26, 26, 26, 0.9);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.08);
}

.luxury-input :deep(.el-input__inner),
.luxury-textarea :deep(.el-textarea__inner) {
  color: rgba(244, 228, 188, 0.95);
  font-size: 14px;
}

.luxury-input :deep(.el-input__inner):disabled {
  color: rgba(244, 228, 188, 0.5);
  -webkit-text-fill-color: rgba(244, 228, 188, 0.5);
}

.luxury-input :deep(.el-input__inner::placeholder),
.luxury-textarea :deep(.el-textarea__inner::placeholder) {
  color: rgba(244, 228, 188, 0.35);
}

.luxury-select {
  width: 100%;
}

.luxury-select :deep(.el-select__selected-item) {
  color: rgba(244, 228, 188, 0.95);
}

.luxury-select :deep(.el-select__placeholder) {
  color: rgba(244, 228, 188, 0.35);
}

/* 下拉框图标 */
.luxury-select :deep(.el-select__caret),
.filter-select :deep(.el-select__caret) {
  color: rgba(212, 175, 55, 0.7);
}

/* 下拉弹出菜单 */
:deep(.el-select-dropdown) {
  background: linear-gradient(145deg, rgba(18, 18, 18, 0.98) 0%, rgba(10, 10, 10, 0.96)) !important;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  box-shadow:
    0 10px 40px rgba(0, 0, 0, 0.6),
    0 0 0 1px rgba(212, 175, 55, 0.1),
    0 0 20px rgba(212, 175, 55, 0.05) !important;
  border-radius: 12px !important;
  backdrop-filter: blur(20px) !important;
  padding: 8px !important;
}

/* 下拉选项 */
:deep(.el-select-dropdown__item) {
  color: rgba(244, 228, 188, 0.85) !important;
  background: transparent !important;
  border-radius: 8px !important;
  padding: 10px 16px !important;
  margin: 2px 0 !important;
  transition: all 0.3s ease !important;
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(212, 175, 55, 0.15) !important;
  color: #f4e4bc !important;
}

:deep(.el-select-dropdown__item.is-selected) {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.25) 0%, rgba(212, 175, 55, 0.15) 100%) !important;
  color: #f4e4bc !important;
  font-weight: 500 !important;
}

:deep(.el-select-dropdown__item.is-selected:hover) {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.35) 0%, rgba(212, 175, 55, 0.25) 100%) !important;
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

.filter-select :deep(.el-select__wrapper) {
  background: rgba(26, 26, 26, 0.6);
  border: 1px solid rgba(212, 175, 55, 0.2);
  box-shadow: none;
}

.filter-select :deep(.el-select__wrapper:hover),
.filter-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(212, 175, 55, 0.4);
  background: rgba(26, 26, 26, 0.8);
}

.filter-select :deep(.el-select__placeholder) {
  color: rgba(244, 228, 188, 0.35);
}

.dialog-footer {
  display: flex;
  justify-content: center;
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

  .action-btn {
    flex: 1;
  }
}
</style>

<style>
/* 全局遮罩层样式 - 确保暗色背景 */
.form-dialog + .el-overlay,
.form-dialog ~ .el-overlay {
  background-color: rgba(0, 0, 0, 0.75) !important;
  backdrop-filter: blur(4px);
}
</style>
