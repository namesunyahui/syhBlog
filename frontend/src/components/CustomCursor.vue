<template>
  <div class="custom-cursor">
    <!-- 外圈光标 -->
    <div
      ref="cursorOuter"
      class="cursor-outer"
      :class="{ 'is-hovering': isHovering, 'is-clicking': isClicking }"
    >
      <span v-if="showLabel" class="cursor-label">{{ cursorLabel }}</span>
    </div>
    <!-- 内圈光标 -->
    <div ref="cursorInner" class="cursor-inner"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const cursorOuter = ref<HTMLElement>()
const cursorInner = ref<HTMLElement>()
const isHovering = ref(false)
const isClicking = ref(false)
const showLabel = ref(false)
const cursorLabel = ref('')

// 光标跟随鼠标位置
const updateCursorPosition = (e: MouseEvent) => {
  if (cursorOuter.value && cursorInner.value) {
    // 外圈延迟跟随
    setTimeout(() => {
      if (cursorOuter.value) {
        cursorOuter.value.style.left = e.clientX + 'px'
        cursorOuter.value.style.top = e.clientY + 'px'
      }
    }, 80)

    // 内圈立即跟随
    cursorInner.value.style.left = e.clientX + 'px'
    cursorInner.value.style.top = e.clientY + 'px'
  }
}

// 检测可交互元素
const isInteractiveElement = (element: HTMLElement): boolean => {
  const interactiveTags = ['A', 'BUTTON', 'INPUT', 'TEXTAREA', 'SELECT']
  const tagName = element.tagName

  if (interactiveTags.includes(tagName)) {
    return true
  }

  // 检查是否有交互类
  if (element.classList.contains('cursor-interactive')) {
    return true
  }

  return false
}

// 获取元素的光标标签
const getElementLabel = (element: HTMLElement): string => {
  // 检查 data-cursor-label 属性
  if (element.hasAttribute('data-cursor-label')) {
    return element.getAttribute('data-cursor-label') || ''
  }

  // 根据元素类型返回默认标签
  const tagName = element.tagName

  if (tagName === 'A') {
    const href = element.getAttribute('href')
    if (href?.startsWith('http')) return '访问'
    if (href?.startsWith('/')) return '查看'
    return '链接'
  }

  if (tagName === 'BUTTON') {
    return '点击'
  }

  if (tagName === 'INPUT' || tagName === 'TEXTAREA') {
    return '输入'
  }

  return ''
}

// 鼠标进入元素
const handleMouseEnter = (e: Event) => {
  const target = e.target as HTMLElement

  if (isInteractiveElement(target)) {
    isHovering.value = true
    const label = getElementLabel(target)
    if (label) {
      showLabel.value = true
      cursorLabel.value = label
    }
  }
}

// 鼠标离开元素
const handleMouseLeave = (e: Event) => {
  const target = e.target as HTMLElement

  if (isInteractiveElement(target)) {
    isHovering.value = false
    showLabel.value = false
    cursorLabel.value = ''
  }
}

// 鼠标按下
const handleMouseDown = () => {
  isClicking.value = true
}

// 鼠标释放
const handleMouseUp = () => {
  isClicking.value = false
}

onMounted(() => {
  document.addEventListener('mousemove', updateCursorPosition)
  document.addEventListener('mouseover', handleMouseEnter)
  document.addEventListener('mouseout', handleMouseLeave)
  document.addEventListener('mousedown', handleMouseDown)
  document.addEventListener('mouseup', handleMouseUp)

  // 初始化光标位置在屏幕中心
  if (cursorOuter.value && cursorInner.value) {
    const centerX = window.innerWidth / 2
    const centerY = window.innerHeight / 2
    cursorOuter.value.style.left = centerX + 'px'
    cursorOuter.value.style.top = centerY + 'px'
    cursorInner.value.style.left = centerX + 'px'
    cursorInner.value.style.top = centerY + 'px'
  }
})

onUnmounted(() => {
  document.removeEventListener('mousemove', updateCursorPosition)
  document.removeEventListener('mouseover', handleMouseEnter)
  document.removeEventListener('mouseout', handleMouseLeave)
  document.removeEventListener('mousedown', handleMouseDown)
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style scoped>
.custom-cursor {
  pointer-events: none;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  mix-blend-mode: difference;
}

/* 内圈光标 - 小点 */
.cursor-inner {
  position: fixed;
  width: 6px;
  height: 6px;
  background: var(--accent-gold);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.2s cubic-bezier(0.23, 1, 0.32, 1),
              height 0.2s cubic-bezier(0.23, 1, 0.32, 1),
              background 0.2s cubic-bezier(0.23, 1, 0.32, 1);
  will-change: transform;
}

/* 外圈光标 - 圆环 */
.cursor-outer {
  position: fixed;
  width: 40px;
  height: 40px;
  border: 1px solid var(--accent-gold);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.3s cubic-bezier(0.23, 1, 0.32, 1),
              height 0.3s cubic-bezier(0.23, 1, 0.32, 1),
              border-color 0.3s cubic-bezier(0.23, 1, 0.32, 1),
              background 0.3s cubic-bezier(0.23, 1, 0.32, 1),
              transform 0.1s ease-out;
  will-change: transform;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 悬停状态 - 外圈放大 */
.cursor-outer.is-hovering {
  width: 60px;
  height: 60px;
  border-color: var(--text-primary);
  background: rgba(212, 163, 115, 0.1);
  backdrop-filter: blur(4px);
}

/* 悬停状态 - 内圈 */
.cursor-outer.is-hovering + .cursor-inner {
  width: 4px;
  height: 4px;
  background: var(--text-primary);
}

/* 点击状态 */
.cursor-outer.is-clicking {
  width: 35px;
  height: 35px;
  background: rgba(212, 163, 115, 0.2);
  border-color: var(--accent-gold-light);
}

/* 光标标签 */
.cursor-label {
  position: absolute;
  font-family: var(--font-body);
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  opacity: 0;
  transform: translateY(4px);
  transition: opacity 0.2s cubic-bezier(0.23, 1, 0.32, 1),
              transform 0.2s cubic-bezier(0.23, 1, 0.32, 1);
  pointer-events: none;
}

.cursor-outer.is-hovering .cursor-label {
  opacity: 1;
  transform: translateY(0);
}

/* 移动端隐藏自定义光标 */
@media (max-width: 768px) {
  .custom-cursor {
    display: none;
  }

  body {
    cursor: auto;
  }
}

/* 减弱动画效果设置 */
@media (prefers-reduced-motion: reduce) {
  .cursor-inner,
  .cursor-outer {
    transition: none;
  }
}
</style>
