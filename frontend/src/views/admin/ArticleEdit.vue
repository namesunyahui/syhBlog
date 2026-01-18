<template>
  <div class="article-edit">
    <el-card class="edit-card" shadow="never">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="编辑" name="edit">
          <el-form :model="form" label-width="80px" class="edit-form">
            <el-form-item label="文章标题">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>

            <el-form-item label="文章摘要">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="2"
                placeholder="请输入文章摘要"
              />
            </el-form-item>

            <el-row :gutter="8">
              <el-col :span="12">
                <el-form-item label="分类">
                  <el-select
                    v-model="form.categoryId"
                    placeholder="请选择分类"
                    clearable
                    style="width: 100%"
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
                <el-form-item label="标签">
                  <el-select
                    v-model="form.tagIds"
                    placeholder="请选择标签"
                    multiple
                    clearable
                    style="width: 100%"
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

            <el-form-item label="文章内容" class="content-item">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="15"
                placeholder="请输入Markdown格式的文章内容"
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
        <el-button type="primary" @click="handleSave" :loading="loading">保存草稿</el-button>
        <el-button type="success" @click="handlePublish" :loading="loading">发布文章</el-button>
        <el-button @click="handleBack">返回</el-button>
      </div>
    </el-card>
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
.article-edit {
  padding: 8px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.edit-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.edit-card :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.edit-card :deep(.el-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.edit-card :deep(.el-tabs__content) {
  flex: 1;
  overflow: auto;
  padding: 8px 12px 0 12px;
}

.edit-card :deep(.el-tab-pane) {
  height: 100%;
}

.edit-form {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.edit-form :deep(.el-form-item) {
  margin-bottom: 10px;
}

.edit-form :deep(.el-form-item__label) {
  padding: 0 8px 0 0;
}

.content-item {
  margin-bottom: 0 !important;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-item :deep(.el-form-item__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-item :deep(.el-textarea) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-item :deep(.el-textarea__inner) {
  flex: 1;
  min-height: 200px;
  font-family: 'Courier New', monospace;
  line-height: 1.6;
  resize: none;
}

/* 预览区域 */
.preview-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  padding: 8px 12px 0 12px;
}

.preview-title {
  font-size: 20px;
  margin-bottom: 8px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 6px;
  flex-shrink: 0;
}

.preview-summary {
  padding: 6px 10px;
  background-color: #f5f7fa;
  border-left: 4px solid #409eff;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
  flex-shrink: 0;
}

.preview-content {
  line-height: 1.6;
  color: #333;
  overflow: auto;
  flex: 1;
}

.placeholder {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 40px;
}

/* 固定底部的操作按钮 */
.action-bar {
  padding: 8px 12px;
  border-top: 1px solid #ebeef5;
  background-color: #fff;
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
}

/* Markdown 样式 */
.markdown-body :deep(h1) {
  font-size: 1.8em;
  margin-top: 16px;
  margin-bottom: 12px;
  border-bottom: 1px solid #eee;
  padding-bottom: 6px;
}

.markdown-body :deep(h2) {
  font-size: 1.4em;
  margin-top: 16px;
  margin-bottom: 12px;
  border-bottom: 1px solid #eee;
  padding-bottom: 6px;
}

.markdown-body :deep(h3) {
  font-size: 1.2em;
  margin-top: 14px;
  margin-bottom: 10px;
}

.markdown-body :deep(p) {
  margin-bottom: 12px;
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
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 12px;
}

.markdown-body :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: #24292e;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-bottom: 12px;
  padding-left: 2em;
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #dfe2e5;
  padding-left: 12px;
  color: #6a737d;
  margin-bottom: 12px;
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
  margin-bottom: 12px;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  border: 1px solid #dfe2e5;
  padding: 6px 10px;
}

.markdown-body :deep(table th) {
  background-color: #f6f8fa;
  font-weight: 600;
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 12px 0;
}
</style>
