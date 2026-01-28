<template>
  <div class="category-manage-page">
    <!-- 表格区域 -->
    <div class="table-section">
      <div class="table-wrapper" v-loading="loading">
        <el-table :data="categories" class="luxury-table">
          <el-table-column prop="id" label="ID" width="80" align="center">
            <template #default="{ row }">
              <span class="table-id">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="分类名称" min-width="200">
            <template #default="{ row }">
              <span class="table-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="300">
            <template #default="{ row }">
              <span class="table-value">{{ row.description || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="100" align="center">
            <template #default="{ row }">
              <span class="table-value">{{ row.sortOrder }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
            <template #default="{ row }">
              <span class="table-date">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" class="table-btn edit" @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button size="small" class="table-btn delete" @click="handleDelete(row)">
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增按钮区域 -->
    <div class="create-section">
      <el-button class="create-btn-main" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="600px"
      :close-on-click-modal="false"
      class="form-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="luxury-form">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
            :max="9999"
            placeholder="数值越小排序越靠前"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="dialog-btn secondary" @click="dialogVisible = false">取消</el-button>
          <el-button class="dialog-btn primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getCategoryList,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'

interface Category {
  id?: number
  name: string
  description: string
  sortOrder: number
  createdAt?: string
}

const categories = ref<Category[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const form = ref<Category>({
  name: '',
  description: '',
  sortOrder: 0
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '分类名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述长度不能超过 200 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

const loadCategories = async () => {
  try {
    loading.value = true
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    name: '',
    description: '',
    sortOrder: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row: Category) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name,
    description: row.description,
    sortOrder: row.sortOrder
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await updateCategory(form.value.id!, form.value)
      ElMessage.success('更新成功')
    } else {
      await createCategory(form.value)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadCategories()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: Category) => {
  try {
    await ElMessageBox.confirm('确定删除该分类吗?', '提示', {
      type: 'warning'
    })

    await deleteCategory(row.id!)
    ElMessage.success('删除成功')
    loadCategories()
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
  loadCategories()
})
</script>

<style scoped>
/* ==================== 页面布局 ==================== */
.category-manage-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ==================== 表格区域 ==================== */
.table-section {
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 16px;
  overflow: hidden;
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

/* ==================== 对话框 ==================== */
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

.table-btn.delete {
  background: rgba(244, 67, 54, 0.15);
  color: #ef5350;
  border-color: rgba(244, 67, 54, 0.3);
}

.table-btn.delete:hover {
  background: rgba(244, 67, 54, 0.2);
  border-color: rgba(244, 67, 54, 0.5);
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

.luxury-form :deep(.el-input__wrapper),
.luxury-form :deep(.el-textarea__inner) {
  background: rgba(26, 26, 26, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.25);
  box-shadow: none;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.luxury-form :deep(.el-input__wrapper):hover,
.luxury-form :deep(.el-textarea__inner):hover {
  border-color: rgba(212, 175, 55, 0.4);
  background: rgba(26, 26, 26, 0.85);
}

.luxury-form :deep(.el-input__wrapper.is-focus),
.luxury-form :deep(.el-textarea__inner:focus) {
  border-color: rgba(212, 175, 55, 0.5);
  background: rgba(26, 26, 26, 0.9);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.08);
}

.luxury-form :deep(.el-input__inner),
.luxury-form :deep(.el-textarea__inner) {
  color: rgba(244, 228, 188, 0.95);
  font-size: 14px;
}

.luxury-form :deep(.el-input__inner::placeholder),
.luxury-form :deep(.el-textarea__inner::placeholder) {
  color: rgba(244, 228, 188, 0.35);
}

.luxury-form :deep(.el-input-number) {
  background: rgba(26, 26, 26, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.25);
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.luxury-form :deep(.el-input-number):hover {
  border-color: rgba(212, 175, 55, 0.4);
  background: rgba(26, 26, 26, 0.85);
}

.luxury-form :deep(.el-input-number):focus,
.luxury-form :deep(.el-input-number.is-focus) {
  border-color: rgba(212, 175, 55, 0.5);
  background: rgba(26, 26, 26, 0.9);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.08);
}

.luxury-form :deep(.el-input-number .el-input__wrapper) {
  background: transparent;
  box-shadow: none;
  border: none;
}

.luxury-form :deep(.el-input-number__decrease),
.luxury-form :deep(.el-input-number__increase) {
  background: rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.2);
  color: rgba(244, 228, 188, 0.7);
  transition: all 0.3s ease;
}

.luxury-form :deep(.el-input-number__decrease:hover),
.luxury-form :deep(.el-input-number__increase:hover) {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.4);
  color: #f4e4bc;
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
</style>
