<template>
  <article class="article-card" :class="{ 'is-featured': isFeatured }">
    <!-- 文章封面图 -->
    <div v-if="article.coverImage || showPlaceholder" class="card-image-wrapper">
      <img
        :src="article.coverImage || placeholderImage"
        :alt="article.title"
        class="card-image"
        loading="lazy"
      />
      <div class="card-image-overlay"></div>
    </div>

    <!-- 卡片内容 -->
    <div class="card-content">
      <!-- 文章元信息 -->
      <div class="card-meta">
        <span v-if="article.category" class="meta-category cursor-interactive" data-cursor-label="分类">
          <router-link :to="{ path: '/category', query: { categoryId: article.category.id } }">
            {{ article.category.name }}
          </router-link>
        </span>
        <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
      </div>

      <!-- 文章标题 -->
      <h2 class="card-title">
        <router-link
          :to="`/article/${article.id}`"
          class="title-link cursor-interactive"
          data-cursor-label="阅读"
        >
          {{ article.title }}
        </router-link>
      </h2>

      <!-- 文章摘要 -->
      <p v-if="article.summary" class="card-summary">
        {{ article.summary }}
      </p>

      <!-- 文章标签 -->
      <div v-if="article.tags && article.tags.length" class="card-tags">
        <router-link
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id"
          :to="`/tag?tag=${tag.name}`"
          class="tag-item cursor-interactive"
          data-cursor-label="标签"
        >
          {{ tag.name }}
        </router-link>
      </div>

      <!-- 卡片底部信息 -->
      <div class="card-footer">
        <div class="footer-stats">
          <span class="stat-item">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke-width="2" stroke-linecap="round"/>
              <circle cx="12" cy="12" r="3" stroke-width="2"/>
            </svg>
            {{ article.viewCount || 0 }}
          </span>
        </div>
        <router-link
          :to="`/article/${article.id}`"
          class="read-more cursor-interactive"
          data-cursor-label="阅读全文"
        >
          <span>阅读全文</span>
          <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M5 12h14M12 5l7 7-7 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </router-link>
      </div>
    </div>

    <!-- 装饰边框 -->
    <div class="card-border"></div>
  </article>
</template>

<script setup lang="ts">
interface Article {
  id: number
  title: string
  summary?: string
  coverImage?: string
  createdAt: string
  viewCount?: number
  category?: {
    id: number
    name: string
  }
  tags?: Array<{
    id: number
    name: string
  }>
}

interface Props {
  article: Article
  isFeatured?: boolean
  showPlaceholder?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isFeatured: false,
  showPlaceholder: true
})

const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300"%3E%3Crect fill="%231a1a1a" width="400" height="300"/%3E%3Cpath fill="%23d4a373" d="M0 300 L400 0 L300 0 L0 200 Z" opacity="0.1"/%3E%3Cpath fill="%23d4a373" d="M0 250 L400 50 L400 0 L200 0 Z" opacity="0.05"/%3E%3C/svg%3E'

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.article-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.5s var(--ease-out);
  display: flex;
  flex-direction: column;
}

.article-card:hover {
  transform: translateY(-8px) scale(1.01);
  border-color: var(--border-accent);
  box-shadow: var(--shadow-xl), var(--shadow-glow);
}

.article-card.is-featured {
  grid-column: span 2;
}

.article-card.is-featured:hover {
  transform: translateY(-12px) scale(1.01);
}

/* ----- 图片区域 ----- */
.card-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 40%; /* 缩小比例，从 16:9 改为更扁平 */
  overflow: hidden;
}

.card-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease-out);
}

.article-card:hover .card-image {
  transform: scale(1.08);
}

.card-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(10, 10, 10, 0.3) 100%
  );
  opacity: 0;
  transition: opacity 0.3s var(--ease-out);
}

.article-card:hover .card-image-overlay {
  opacity: 1;
}

/* ----- 内容区域 ----- */
.card-content {
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  flex: 1;
}

.is-featured .card-content {
  padding: var(--space-8);
}

/* ----- 元信息 ----- */
.card-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
  font-size: var(--text-xs);
}

.meta-category {
  display: inline-flex;
  align-items: center;
}

.meta-category a {
  color: var(--accent-gold);
  text-decoration: none;
  font-weight: var(--font-semibold);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: color 0.3s var(--ease-out);
}

.meta-category a:hover {
  color: var(--accent-gold-light);
}

.meta-date {
  color: var(--text-tertiary);
  font-family: var(--font-body);
  font-weight: var(--font-medium);
}

/* ----- 标题 ----- */
.card-title {
  margin: 0 0 var(--space-3);
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.is-featured .card-title {
  font-size: var(--text-3xl);
}

.title-link {
  color: var(--text-primary);
  text-decoration: none;
  display: block;
  transition: color 0.3s var(--ease-out);
  background: linear-gradient(
    to right,
    var(--text-primary) 0%,
    var(--text-primary) 100%
  );
  background-size: 0% 1px;
  background-repeat: no-repeat;
  background-position: left bottom;
}

.article-card:hover .title-link {
  color: var(--accent-gold);
  background-size: 100% 1px;
  background-image: linear-gradient(
    to right,
    var(--accent-gold) 0%,
    var(--accent-gold) 100%
  );
}

/* ----- 摘要 ----- */
.card-summary {
  margin: 0 0 var(--space-3);
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.is-featured .card-summary {
  font-size: var(--text-base);
  -webkit-line-clamp: 3;
}

/* ----- 标签 ----- */
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  background: rgba(212, 163, 115, 0.08);
  border: 1px solid var(--border-accent);
  border-radius: var(--radius-full);
  color: var(--accent-gold);
  font-family: var(--font-body);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  text-decoration: none;
  transition: all 0.3s var(--ease-out);
}

.tag-item:hover {
  background: var(--accent-gold);
  color: var(--bg-primary);
  transform: translateY(-2px);
}

/* ----- 底部 ----- */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}

.footer-stats {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.stat-icon {
  width: 16px;
  height: 16px;
}

.read-more {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-primary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  text-decoration: none;
  transition: all 0.3s var(--ease-out);
}

.read-more:hover {
  color: var(--accent-gold);
  gap: var(--space-3);
}

.arrow-icon {
  width: 18px;
  height: 18px;
  transition: transform 0.3s var(--ease-out);
}

.read-more:hover .arrow-icon {
  transform: translateX(4px);
}

/* ----- 装饰边框 ----- */
.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: var(--radius-xl);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s var(--ease-out);
  background: linear-gradient(
    135deg,
    var(--accent-gold) 0%,
    transparent 50%,
    var(--accent-gold) 100%
  );
  background-size: 200% 200%;
  z-index: -1;
}

.article-card:hover .card-border {
  opacity: 0.03;
  animation: shimmer 3s linear infinite;
}

/* ----- 响应式 ----- */
@media (max-width: 768px) {
  .is-featured {
    grid-column: span 1;
  }

  .card-content {
    padding: var(--space-5);
  }

  .is-featured .card-content {
    padding: var(--space-6);
  }

  .card-title {
    font-size: var(--text-xl);
  }

  .is-featured .card-title {
    font-size: var(--text-2xl);
  }

  .card-summary {
    font-size: var(--text-sm);
  }

  .is-featured .card-summary {
    font-size: var(--text-base);
  }
}
</style>
