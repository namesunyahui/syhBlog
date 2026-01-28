<template>
  <div class="dashboard" v-loading="loading">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-heading">仪表盘</h1>
      <p class="page-subheading">欢迎回来，这里是您的编辑控制中心</p>
    </div>

    <!-- 统计卡片网格 -->
    <div class="stats-grid">
      <div
        v-for="(stat, index) in stats"
        :key="stat.key"
        class="stat-card"
        :class="`stat-${index}`"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <div class="stat-background">
          <div class="stat-gradient"></div>
          <div class="stat-pattern"></div>
        </div>
        <div class="stat-content">
          <div class="stat-icon-wrapper" :style="{ backgroundColor: stat.colorBg }">
            <component :is="stat.icon" class="stat-icon" :style="{ color: stat.color }" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-decoration" :style="{ borderColor: stat.color }"></div>
        </div>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="activity-section">
      <div class="section-header">
        <h3 class="section-title">最近活动</h3>
        <div class="section-decoration"></div>
      </div>
      <div class="activity-list">
        <div class="activity-item" v-for="i in 4" :key="i">
          <div class="activity-dot"></div>
          <div class="activity-content">
            <p class="activity-text">这是一条示例活动记录</p>
            <span class="activity-time">2小时前</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getStats } from '@/api/admin'
import {
  Document,
  View,
  ChatDotRound,
  Folder
} from '@element-plus/icons-vue'

const loading = ref(false)
const rawData = ref({
  articleCount: 0,
  viewCount: 0,
  commentCount: 0,
  categoryCount: 0
})

const stats = computed(() => [
  {
    key: 'articles',
    label: '文章总数',
    value: rawData.value.articleCount,
    icon: Document,
    color: '#d4af37',
    colorBg: 'rgba(212, 175, 55, 0.15)'
  },
  {
    key: 'views',
    label: '总浏览量',
    value: rawData.value.viewCount,
    icon: View,
    color: '#f4e4bc',
    colorBg: 'rgba(244, 228, 188, 0.15)'
  },
  {
    key: 'comments',
    label: '评论总数',
    value: rawData.value.commentCount,
    icon: ChatDotRound,
    color: '#c9a227',
    colorBg: 'rgba(201, 162, 39, 0.15)'
  },
  {
    key: 'categories',
    label: '分类数量',
    value: rawData.value.categoryCount,
    icon: Folder,
    color: '#e8d5a3',
    colorBg: 'rgba(232, 213, 163, 0.15)'
  }
])

const loadStats = async () => {
  try {
    loading.value = true
    const response = await getStats()
    if (response.code === 200) {
      rawData.value = response.data
    } else {
      ElMessage.error(response.message || '加载统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
/* ==================== 页面头部 ==================== */
.page-header {
  margin-bottom: 32px;
  position: relative;
}

.page-heading {
  font-size: 36px;
  font-weight: 600;
  color: #f4e4bc;
  margin: 0 0 8px 0;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  letter-spacing: 1px;
}

.page-subheading {
  font-size: 14px;
  color: rgba(244, 228, 188, 0.5);
  margin: 0;
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* ==================== 统计卡片网格 ==================== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  position: relative;
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.6s ease backwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(212, 175, 55, 0.3);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.4),
    0 0 60px rgba(212, 175, 55, 0.1);
}

.stat-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.stat-gradient {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(212, 175, 55, 0.08) 0%,
    transparent 60%
  );
  transition: transform 0.6s ease;
}

.stat-card:hover .stat-gradient {
  transform: scale(1.2);
}

.stat-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(90deg, rgba(212, 175, 55, 0.02) 1px, transparent 1px),
    linear-gradient(rgba(212, 175, 55, 0.02) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.5;
}

.stat-content {
  position: relative;
  z-index: 1;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(212, 175, 55, 0.2);
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon-wrapper {
  transform: scale(1.1) rotate(5deg);
}

.stat-icon {
  font-size: 26px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #f4e4bc;
  line-height: 1;
  margin-bottom: 6px;
  font-family: 'Playfair Display', 'Times New Roman', serif;
}

.stat-label {
  font-size: 13px;
  color: rgba(244, 228, 188, 0.5);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.stat-decoration {
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 2px;
  background: linear-gradient(90deg, transparent 0%, currentColor 50%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover .stat-decoration {
  opacity: 0.5;
}

/* ==================== 活动区域 ==================== */
.activity-section {
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 16px;
  padding: 24px;
  animation: fadeInUp 0.6s ease 0.4s backwards;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #f4e4bc;
  margin: 0;
  font-family: 'Playfair Display', 'Times New Roman', serif;
}

.section-decoration {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(212, 175, 55, 0.3) 0%, transparent 100%);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px;
  border-radius: 10px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.activity-item:hover {
  background: rgba(212, 175, 55, 0.05);
}

.activity-dot {
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #d4af37, #f4e4bc);
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.4);
}

.activity-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.activity-text {
  font-size: 14px;
  color: rgba(244, 228, 188, 0.8);
  margin: 0;
}

.activity-time {
  font-size: 12px;
  color: rgba(244, 228, 188, 0.4);
  flex-shrink: 0;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
  }

  .page-heading {
    font-size: 28px;
  }

  .stat-content {
    padding: 20px;
  }

  .stat-value {
    font-size: 28px;
  }
}
</style>
