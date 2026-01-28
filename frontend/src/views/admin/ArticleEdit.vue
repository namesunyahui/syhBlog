<template>
  <div class="article-edit-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ form.id ? '编辑文章' : '新增文章' }}</h1>
      <div class="header-decoration"></div>
    </div>

    <!-- 编辑卡片 -->
    <div class="edit-card">
      <el-tabs v-model="activeTab" type="card" class="luxury-tabs">
        <el-tab-pane label="编辑" name="edit">
          <el-form :model="form" label-width="100px" class="luxury-form">
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" class="luxury-input" />
            </el-form-item>

            <el-form-item label="文章摘要" prop="summary">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="3"
                placeholder="请输入文章摘要"
                class="luxury-textarea"
              />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="分类" prop="categoryId">
                  <el-select
                    v-model="form.categoryId"
                    placeholder="请选择分类"
                    clearable
                    class="luxury-select"
                  >
                    <el-option
                      v-for="category in categories"
                      :key="category.id"
                      :label="category.name"
                      :value="category.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="标签" prop="tagIds">
                  <el-select
                    v-model="form.tagIds"
                    placeholder="请选择标签"
                    multiple
                    clearable
                    class="luxury-select"
                  >
                    <el-option
                      v-for="tag in tags"
                      :key="tag.id"
                      :label="tag.name"
                      :value="tag.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="文章内容" prop="content" class="content-item">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="18"
                placeholder="请输入Markdown格式的文章内容"
                class="luxury-textarea content-textarea"
                @input="updatePreview"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="预览" name="preview">
          <div class="preview-container">
            <h1 class="preview-title">{{ form.title || '(无标题)' }}</h1>
            <div class="preview-summary" v-if="form.summary">
              <strong>摘要：</strong>{{ form.summary }}
            </div>
            <div class="preview-content markdown-body" v-html="previewHtml"></div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 固定在底部的操作按钮 -->
      <div class="action-bar">
        <el-button class="action-btn draft" @click="handleSave" :loading="loading">
          保存草稿
        </el-button>
        <el-button class="action-btn publish" @click="handlePublish" :loading="loading">
          发布文章
        </el-button>
        <el-button class="action-btn back" @click="handleBack">
          返回
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createArticle, updateArticle, getArticleDetail, publishArticle } from '@/api/article'
import { getAllCategories } from '@/api/category'
import { getAllTags } from '@/api/tag'

declare global {
  interface Window {
    marked: any
  }
}

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const activeTab = ref('edit')

const form = ref({
  id: undefined,
  title: '',
  summary: '',
  content: '',
  categoryId: undefined,
  tagIds: [] as number[]
})

const categories = ref<any[]>([])
const tags = ref<any[]>([])

const previewHtml = computed(() => {
  if (!form.value.content) {
    return '<p class="placeholder">在编辑标签页输入内容后即可预览...</p>'
  }
  if (window.marked) {
    return window.marked.parse(form.value.content)
  }
  return '<pre>' + form.value.content + '</pre>'
})

const updatePreview = () => {
  // 预览会自动通过 computed 更新
}

const loadArticleDetail = async (id: number) => {
  try {
    const res = await getArticleDetail(id)
    const article = res.data

    form.value = {
      id: article.id,
      title: article.title || '',
      summary: article.summary || '',
      content: article.content || '',
      categoryId: article.categoryId,
      tagIds: article.tags ? article.tags.map((tag: any) => tag.id) : []
    }
  } catch (error) {
    ElMessage.error('加载文章详情失败')
  }
}

const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  }
}

const loadTags = async () => {
  try {
    const res = await getAllTags()
    tags.value = res.data || []
  } catch (error) {
    ElMessage.error('加载标签列表失败')
  }
}

const handleSave = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入文章标题')
    return
  }

  try {
    loading.value = true

    const data = {
      title: form.value.title,
      summary: form.value.summary,
      content: form.value.content,
      categoryId: form.value.categoryId,
      tags: form.value.tagIds.map(tagId => ({ id: tagId })),
      isPublished: false
    }

    if (form.value.id) {
      await updateArticle(form.value.id, data)
    } else {
      await createArticle(data)
    }

    ElMessage.success('保存草稿成功')
    router.push('/admin/articles')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

const handlePublish = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入文章标题')
    return
  }

  try {
    loading.value = true

    const data = {
      title: form.value.title,
      summary: form.value.summary,
      content: form.value.content,
      categoryId: form.value.categoryId,
      tags: form.value.tagIds.map(tagId => ({ id: tagId })),
      isPublished: true
    }

    if (form.value.id) {
      await updateArticle(form.value.id, data)
      await publishArticle(form.value.id, true)
    } else {
      const res = await createArticle(data)
      await publishArticle(res.data.id, true)
    }

    ElMessage.success('发布成功')
    router.push('/admin/articles')
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadCategories()
  loadTags()
  const id = route.query.id as number
  if (id) {
    loadArticleDetail(id)
  }
})
</script>

<style scoped>
/* ==================== 页面布局 ==================== */
.article-edit-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

/* ==================== 页面头部 ==================== */
.page-header {
  text-align: center;
  position: relative;
  padding: 10px 0;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #f4e4bc;
  margin: 0 0 12px 0;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  letter-spacing: 1px;
}

.header-decoration {
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, transparent 0%, #d4af37 50%, transparent 100%);
  margin: 0 auto;
}

/* ==================== 编辑卡片 ==================== */
.edit-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

/* ==================== 标签页 ==================== */
.luxury-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.luxury-tabs :deep(.el-tabs__header) {
  background: rgba(26, 26, 26, 0.6);
  border-bottom: 2px solid rgba(212, 175, 55, 0.2);
  margin: 0;
  padding: 0 20px;
}

.luxury-tabs :deep(.el-tabs__nav) {
  border: none;
}

.luxury-tabs :deep(.el-tabs__item) {
  color: rgba(244, 228, 188, 0.6);
  border: none;
  background: transparent;
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  position: relative;
}

.luxury-tabs :deep(.el-tabs__item::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: #d4af37;
  transition: width 0.3s ease;
}

.luxury-tabs :deep(.el-tabs__item:hover) {
  color: #f4e4bc;
}

.luxury-tabs :deep(.el-tabs__item.is-active) {
  color: #f4e4bc;
}

.luxury-tabs :deep(.el-tabs__item.is-active::after) {
  width: 100%;
}

.luxury-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: auto;
  padding: 30px;
  background: transparent;
}

.luxury-tabs :deep(.el-tab-pane) {
  height: 100%;
}

/* ==================== 表单 ==================== */
.luxury-form {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.luxury-form :deep(.el-form-item__label) {
  color: rgba(244, 228, 188, 0.85);
  font-weight: 500;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.luxury-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

/* 输入框样式 */
.luxury-input :deep(.el-input__wrapper),
.luxury-textarea :deep(.el-textarea__inner),
.luxury-select :deep(.el-select__wrapper) {
  background: rgba(26, 26, 26, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.25);
  box-shadow: none;
  border-radius: 10px;
  transition: all 0.3s ease;
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

.luxury-input :deep(.el-input__inner::placeholder),
.luxury-textarea :deep(.el-textarea__inner::placeholder) {
  color: rgba(244, 228, 188, 0.35);
}

/* 下拉选择器 */
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
.luxury-select :deep(.el-select__caret) {
  color: rgba(212, 175, 55, 0.7);
}

.luxury-select :deep(.el-tag) {
  background: rgba(212, 175, 55, 0.15) !important;
  border-color: rgba(212, 175, 55, 0.3) !important;
  color: #f4e4bc !important;
}

.luxury-select :deep(.el-tag__close) {
  color: rgba(244, 228, 188, 0.7) !important;
}

.luxury-select :deep(.el-tag__close:hover) {
  color: #f4e4bc !important;
  background: rgba(212, 175, 55, 0.2) !important;
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

/* 内容编辑区 */
.content-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-item :deep(.el-form-item__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-textarea :deep(.el-textarea__inner) {
  flex: 1;
  font-family: 'Courier New', 'Monaco', monospace;
  line-height: 1.7;
  resize: none;
  font-size: 14px;
}

/* ==================== 预览区域 ==================== */
.preview-container {
  height: 100%;
  overflow: auto;
  padding: 10px;
}

.preview-title {
  font-size: 36px;
  font-weight: 600;
  color: #f4e4bc;
  margin-bottom: 24px;
  text-align: center;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  letter-spacing: 0.5px;
  line-height: 1.3;
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

.placeholder {
  color: rgba(244, 228, 188, 0.35);
  font-style: italic;
  text-align: center;
  padding: 60px 20px;
  font-size: 15px;
}

/* ==================== 操作按钮 ==================== */
.action-bar {
  padding: 20px 30px;
  border-top: 1px solid rgba(212, 175, 55, 0.15);
  background: rgba(26, 26, 26, 0.6);
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-shrink: 0;
}

.action-btn {
  padding: 12px 32px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
  border: none;
}

.action-btn.draft {
  background: rgba(212, 175, 55, 0.15);
  color: #f4e4bc;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

.action-btn.draft:hover {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.5);
  transform: translateY(-2px);
}

.action-btn.publish {
  background: linear-gradient(135deg, #d4af37 0%, #f4e4bc 100%);
  color: #0a0a0a;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
}

.action-btn.publish:hover {
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.5);
  transform: translateY(-2px);
}

.action-btn.back {
  background: rgba(244, 228, 188, 0.08);
  color: #f4e4bc;
  border: 1px solid rgba(244, 228, 188, 0.2);
}

.action-btn.back:hover {
  background: rgba(244, 228, 188, 0.12);
  border-color: rgba(244, 228, 188, 0.3);
  transform: translateY(-2px);
}

/* ==================== Markdown 样式 ==================== */
.markdown-body :deep(h1) {
  font-size: 2em;
  margin-top: 24px;
  margin-bottom: 16px;
  color: #f4e4bc;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
  padding-bottom: 8px;
}

.markdown-body :deep(h2) {
  font-size: 1.6em;
  margin-top: 24px;
  margin-bottom: 16px;
  color: #f4e4bc;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  border-bottom: 1px solid rgba(212, 175, 55, 0.15);
  padding-bottom: 6px;
}

.markdown-body :deep(h3) {
  font-size: 1.3em;
  margin-top: 20px;
  margin-bottom: 14px;
  color: #f4e4bc;
  font-family: 'Playfair Display', 'Times New Roman', serif;
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
  line-height: 1.8;
}

.markdown-body :deep(code) {
  background: rgba(212, 175, 55, 0.15);
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: #d4af37;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background: rgba(26, 26, 26, 0.8);
  padding: 16px;
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.15);
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body :deep(pre code) {
  background: transparent;
  padding: 0;
  color: rgba(244, 228, 188, 0.9);
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-body :deep(li) {
  margin-bottom: 6px;
  line-height: 1.7;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #d4af37;
  padding-left: 16px;
  color: rgba(244, 228, 188, 0.7);
  margin-bottom: 16px;
  font-style: italic;
}

.markdown-body :deep(a) {
  color: #d4af37;
  text-decoration: none;
  transition: color 0.3s ease;
}

.markdown-body :deep(a:hover) {
  color: #f4e4bc;
  text-decoration: underline;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  border: 1px solid rgba(212, 175, 55, 0.15);
  padding: 10px 14px;
}

.markdown-body :deep(table th) {
  background: rgba(212, 175, 55, 0.15);
  font-weight: 600;
  color: #f4e4bc;
}

.markdown-body :deep(table td) {
  color: rgba(244, 228, 188, 0.85);
}

.markdown-body :deep(table tr:hover) {
  background: rgba(212, 175, 55, 0.05);
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 20px 0;
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.15);
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid rgba(212, 175, 55, 0.2);
  margin: 24px 0;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }

  .luxury-tabs :deep(.el-tabs__content) {
    padding: 20px;
  }

  .action-bar {
    flex-wrap: wrap;
  }

  .action-btn {
    flex: 1;
    min-width: 120px;
  }
}
</style>
