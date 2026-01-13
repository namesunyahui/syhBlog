<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名、昵称、邮箱"
              clearable
              style="width: 250px; margin-right: 10px"
              @clear="loadUsers"
              @keyup.enter="loadUsers"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="loadUsers">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="users" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="150">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'SUPER_ADMIN' ? 'danger' : 'primary'">
              {{ scope.row.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '激活' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="scope">
            {{ scope.row.lastLoginTime ? formatDate(scope.row.lastLoginTime) : '未登录' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.role !== 'SUPER_ADMIN'"
              size="small"
              @click="handleChangeRole(scope.row)">
              修改角色
            </el-button>
            <el-button
              size="small"
              :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="handleChangeStatus(scope.row)">
              {{ scope.row.status === 'ACTIVE' ? '封禁' : '激活' }}
            </el-button>
            <el-button
              v-if="scope.row.role !== 'SUPER_ADMIN'"
              size="small"
              type="danger"
              @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadUsers"
        @current-change="loadUsers"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 修改角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="currentUser.nickname" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newRole" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeRole">确定</el-button>
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
    // 如果当前页只有一条数据且不是第一页，则跳转到上一页
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
.user-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

:deep(.el-pagination) {
  display: flex;
}
</style>
