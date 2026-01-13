<template>
  <div class="article-edit">
    <el-card>
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="编辑" name="edit">
          <el-form :model="form" label-width="100px">
            <el-form-item label="文章标题">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>

            <el-form-item label="文章摘要">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="3"
                placeholder="请输入文章摘要"
              />
            </el-form-item>

            <el-form-item label="文章内容">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="20"
                placeholder="请输入Markdown格式的文章内容"
                @input="updatePreview"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="loading">保存草稿</el-button>
              <el-button type="success" @click="handlePublish" :loading="loading">发布文章</el-button>
              <el-button @click="handleBack">返回</el-button>
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

          <div class="preview-actions">
            <el-button type="primary" @click="handleSave" :loading="loading">保存草稿</el-button>
            <el-button type="success" @click="handlePublish" :loading="loading">发布文章</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createArticle, updateArticle, getArticleDetail, publishArticle } from '@/api/article'

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
  content: ''
})

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
      content: article.content || ''
    }
  } catch (error) {
    ElMessage.error('加载文章详情失败')
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
  const id = route.query.id as number
  if (id) {
    loadArticleDetail(id)
  }
})
</script>

<style scoped>
.article-edit {
  padding: 20px;
}

.preview-container {
  padding: 20px;
  min-height: 500px;
}

.preview-title {
  font-size: 28px;
  margin-bottom: 16px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 12px;
}

.preview-summary {
  padding: 12px;
  background-color: #f5f7fa;
  border-left: 4px solid #409eff;
  margin-bottom: 20px;
  color: #606266;
}

.preview-content {
  line-height: 1.8;
  color: #333;
}

.preview-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  text-align: center;
}

.placeholder {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 40px;
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
