<template>
  <aside class="app-sidebar">
    <!-- 搜索卡片 -->
    <div class="sidebar-card scroll-reveal">
      <div class="card-header">
        <h3 class="card-title">搜索</h3>
      </div>
      <div class="card-body">
        <div class="search-box">
          <input
            :value="modelValue.searchKeyword"
            type="text"
            placeholder="搜索文章..."
            class="search-input"
            @input="$emit('update:searchKeyword', $event.target.value)"
            @keyup.enter="$emit('search')"
          />
          <button class="search-btn cursor-interactive" data-cursor-label="搜索" @click="$emit('search')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 分类卡片 -->
    <div class="sidebar-card scroll-reveal">
      <div class="card-header">
        <h3 class="card-title">分类</h3>
      </div>
      <div class="card-body">
        <ul v-if="categories.length" class="category-list">
          <li v-for="category in categories" :key="category.id">
            <router-link
              :to="{ path: '/category', query: { categoryId: category.id } }"
              class="category-link cursor-interactive"
              data-cursor-label="查看"
            >
              <span class="category-name">{{ category.name }}</span>
              <span class="category-count">{{ category.articleCount }}</span>
            </router-link>
          </li>
        </ul>
        <div v-else class="empty-mini">
          <span>暂无分类</span>
        </div>
      </div>
    </div>

    <!-- 标签卡片 -->
    <div class="sidebar-card scroll-reveal">
      <div class="card-header">
        <h3 class="card-title">标签</h3>
      </div>
      <div class="card-body">
        <div v-if="tags.length" class="tag-cloud">
          <router-link
            v-for="tag in tags"
            :key="tag.id"
            :to="{ path: '/tag', query: { tag: tag.name } }"
            class="tag-cloud-item cursor-interactive"
            data-cursor-label="查看"
          >
            {{ tag.name }}
          </router-link>
        </div>
        <div v-else class="empty-mini">
          <span>暂无标签</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
interface Props {
  categories?: Array<{ id: number; name: string; articleCount?: number }>
  tags?: Array<{ id: number; name: string }>
  modelValue?: { searchKeyword: string }
}

withDefaults(defineProps<Props>(), {
  categories: () => [],
  tags: () => [],
  modelValue: () => ({ searchKeyword: '' })
})

defineEmits<{
  (e: 'update:searchKeyword', value: string): void
  (e: 'search'): void
}>()
</script>

<style scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.sidebar-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s var(--ease-out);
}

.sidebar-card:hover {
  border-color: var(--border-accent);
  box-shadow: var(--shadow-lg);
}

.card-header {
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(212, 163, 115, 0.03);
}

.card-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.01em;
}

.card-body {
  padding: var(--space-5);
}

/* ----- 搜索 ----- */
.search-box {
  display: flex;
  gap: var(--space-2);
}

.search-input {
  flex: 1;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  color: var(--text-primary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  transition: all 0.3s var(--ease-out);
}

.search-input:focus {
  outline: none;
  border-color: var(--accent-gold);
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  padding: var(--space-3);
  background: var(--accent-gold);
  border: 1px solid var(--accent-gold);
  border-radius: var(--radius-lg);
  color: var(--bg-primary);
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background: transparent;
  color: var(--accent-gold);
}

.search-btn svg {
  width: 18px;
  height: 18px;
}

/* ----- 分类列表 ----- */
.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.category-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  text-decoration: none;
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  border-radius: var(--radius-md);
  transition: all 0.3s var(--ease-out);
}

.category-link:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
  transform: translateX(4px);
}

.category-name {
  flex: 1;
}

.category-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 var(--space-2);
  background: rgba(212, 163, 115, 0.1);
  color: var(--accent-gold);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-full);
}

/* ----- 标签云 ----- */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.tag-cloud-item {
  display: inline-block;
  padding: var(--space-2) var(--space-3);
  background: rgba(212, 163, 115, 0.06);
  border: 1px solid var(--border-accent);
  color: var(--accent-gold);
  font-family: var(--font-body);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all 0.3s var(--ease-out);
}

.tag-cloud-item:hover {
  background: var(--accent-gold);
  color: var(--bg-primary);
  transform: translateY(-2px);
}

/* ----- 空状态 ----- */
.empty-mini {
  text-align: center;
  padding: var(--space-6);
  color: var(--text-muted);
  font-size: var(--text-sm);
}
</style>
