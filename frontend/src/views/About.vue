<template>
  <div class="about-page">
    <AppHeader />

    <main class="main-content">
      <!-- 英雄区 -->
      <section class="hero-section">
        <div class="hero-container">
          <div class="hero-avatar">
            <span class="avatar-symbol">§</span>
          </div>
          <h1 class="hero-title">
            <span class="title-line">关于</span>
            <span class="title-line title-accent">我</span>
          </h1>
          <p class="hero-subtitle">欢迎来到这里</p>
        </div>
      </section>

      <!-- 主内容区 -->
      <div class="content-wrapper">
        <div class="content-main">
          <!-- 问候卡片 -->
          <section class="content-card">
            <div class="card-content">
              <h2 class="section-title">你好 👋</h2>
              <p class="section-text">
                这里是 Syh Blog。我记录技术，分享思考。
              </p>
              <p class="section-text">
                希望这里的某些文字，能给你带来一点启发或帮助。
              </p>
            </div>
          </section>

          <!-- 技术栈 -->
          <section class="content-card">
            <div class="card-header">
              <h2 class="card-title">🚀 技术栈</h2>
            </div>
            <div class="tech-grid">
              <div class="tech-category">
                <h3 class="category-title">前端技术</h3>
                <div class="tech-list">
                  <div class="tech-item">
                    <div class="tech-icon">🎨</div>
                    <div class="tech-info">
                      <h4>Vue 3</h4>
                      <p>Composition API + TypeScript</p>
                    </div>
                  </div>
                  <div class="tech-item">
                    <div class="tech-icon">⚡</div>
                    <div class="tech-info">
                      <h4>Vite</h4>
                      <p>极速热更新</p>
                    </div>
                  </div>
                  <div class="tech-item">
                    <div class="tech-icon">🎯</div>
                    <div class="tech-info">
                      <h4>Element Plus</h4>
                      <p>Vue 3 组件库</p>
                    </div>
                  </div>
                  <div class="tech-item">
                    <div class="tech-icon">📦</div>
                    <div class="tech-info">
                      <h4>Pinia</h4>
                      <p>状态管理</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="tech-category">
                <h3 class="category-title">后端技术</h3>
                <div class="tech-list">
                  <div class="tech-item">
                    <div class="tech-icon">☕</div>
                    <div class="tech-info">
                      <h4>Spring Boot</h4>
                      <p>Java 企业级框架</p>
                    </div>
                  </div>
                  <div class="tech-item">
                    <div class="tech-icon">🗄️</div>
                    <div class="tech-info">
                      <h4>PostgreSQL</h4>
                      <p>关系型数据库</p>
                    </div>
                  </div>
                  <div class="tech-item">
                    <div class="tech-icon">🔴</div>
                    <div class="tech-info">
                      <h4>Redis</h4>
                      <p>缓存中间件</p>
                    </div>
                  </div>
                  <div class="tech-item">
                    <div class="tech-icon">🔐</div>
                    <div class="tech-info">
                      <h4>Spring Security</h4>
                      <p>安全认证</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 功能列表 -->
          <section class="content-card">
            <div class="card-header">
              <h2 class="card-title">这里有什么</h2>
            </div>
            <div class="feature-grid">
              <div class="feature-card">
                <span class="feature-icon">📝</span>
                <h4 class="feature-title">技术文章</h4>
                <p class="feature-desc">学习笔记与实践总结</p>
              </div>
              <div class="feature-card">
                <span class="feature-icon">💭</span>
                <h4 class="feature-title">思考感悟</h4>
                <p class="feature-desc">生活中的碎片记录</p>
              </div>
              <div class="feature-card">
                <span class="feature-icon">💬</span>
                <h4 class="feature-title">评论交流</h4>
                <p class="feature-desc">期待你的声音</p>
              </div>
              <div class="feature-card">
                <span class="feature-icon">🔍</span>
                <h4 class="feature-title">归档检索</h4>
                <p class="feature-desc">按分类与标签查找</p>
              </div>
            </div>
          </section>

          <!-- 技术说明 -->
          <section class="content-card">
            <div class="card-header">
              <h2 class="card-title">技术实现</h2>
            </div>
            <div class="design-philosophy">
              <p>前端 Vue 3，后端 Spring Boot，数据存储 PostgreSQL。</p>
              <p>代码开源在 GitHub，欢迎交流。</p>
            </div>
          </section>

          <!-- 联系方式 -->
          <section class="content-card">
            <div class="card-header">
              <h2 class="card-title">📮 找到我</h2>
            </div>
            <div class="contact-grid">
              <a
                href="https://github.com/namesunyahui"
                target="_blank"
                class="contact-link cursor-interactive"
                data-cursor-label="访问"
              >
                <div class="contact-icon">🐙</div>
                <h4 class="contact-title">GitHub</h4>
                <p class="contact-desc">github.com/namesunyahui</p>
              </a>
              <a
                href="mailto:sunyahui2026@163.com"
                class="contact-link cursor-interactive"
                data-cursor-label="邮件"
              >
                <div class="contact-icon">📧</div>
                <h4 class="contact-title">邮箱</h4>
                <p class="contact-desc">sunyahui2026@163.com</p>
              </a>
            </div>
          </section>

          <!-- 结尾 -->
          <section class="footer-section">
            <p class="footer-text">感谢来访</p>
            <p class="footer-signature">— Syh</p>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-top: 72px;
}

.main-content {
  width: 100%;
}

/* ----- 英雄区 ----- */
.hero-section {
  padding: var(--space-20) 0;
  text-align: center;
  border-bottom: 1px solid var(--border-subtle);
}

.hero-container {
  max-width: var(--container-md);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.hero-avatar {
  width: 100px;
  height: 100px;
  margin: 0 auto var(--space-8);
  background: var(--accent-gold);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-glow);
  animation: scaleIn 0.6s var(--ease-out);
}

.avatar-symbol {
  font-family: var(--font-display);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  color: var(--bg-primary);
}

.hero-title {
  font-family: var(--font-display);
  font-size: var(--text-8xl);
  font-weight: var(--font-black);
  line-height: 0.9;
  letter-spacing: -0.04em;
  margin: 0 0 var(--space-6);
}

.title-line {
  display: block;
  color: var(--text-primary);
}

.title-accent {
  color: var(--accent-gold);
  position: relative;
}

.title-accent::after {
  content: '';
  position: absolute;
  bottom: 0.05em;
  left: 0;
  width: 100%;
  height: 0.08em;
  background: var(--accent-gold);
  opacity: 0.5;
}

.hero-subtitle {
  font-family: var(--font-body);
  font-size: var(--text-xl);
  color: var(--text-secondary);
  margin: 0;
}

/* ----- 内容区 ----- */
.content-wrapper {
  max-width: var(--content-narrow);
  margin: 0 auto;
  padding: var(--space-16) var(--space-6);
}

.content-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-10);
}

/* ----- 内容卡片 ----- */
.content-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s var(--ease-out);
}

.content-card:hover {
  border-color: var(--border-accent);
  box-shadow: var(--shadow-lg);
}

.card-header {
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(212, 163, 115, 0.03);
}

.card-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.02em;
}

.card-content {
  padding: var(--space-6);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-5);
}

.section-text {
  font-family: var(--font-body);
  font-size: var(--text-base);
  line-height: 1.8;
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.section-text:last-child {
  margin-bottom: 0;
}

/* ----- 技术栈 ----- */
.tech-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-8);
  padding: var(--space-6);
}

.category-title {
  font-family: var(--font-body);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--accent-gold);
  margin: 0 0 var(--space-5);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.tech-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.tech-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  transition: all 0.3s var(--ease-out);
}

.tech-item:hover {
  border-color: var(--border-accent);
  transform: translateX(4px);
}

.tech-icon {
  font-size: var(--text-3xl);
  flex-shrink: 0;
}

.tech-info h4 {
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
}

.tech-info p {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

/* ----- 功能网格 ----- */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-5);
  padding: var(--space-6);
}

.feature-card {
  text-align: center;
  padding: var(--space-6);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all 0.3s var(--ease-out);
}

.feature-card:hover {
  border-color: var(--border-accent);
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  font-size: 48px;
  display: block;
  margin-bottom: var(--space-4);
}

.feature-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.feature-desc {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  line-height: 1.6;
  margin: 0;
}

/* ----- 设计理念 ----- */
.design-philosophy {
  padding: var(--space-6);
  background: var(--bg-secondary);
  border-left: 3px solid var(--accent-gold);
  border-radius: var(--radius-lg);
}

.design-philosophy p {
  font-family: var(--font-body);
  font-size: var(--text-base);
  line-height: 1.8;
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.design-philosophy p:last-child {
  margin-bottom: 0;
}

.design-philosophy strong {
  color: var(--accent-gold);
  font-weight: var(--font-semibold);
}

/* ----- 联系方式 ----- */
.contact-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-5);
  padding: var(--space-6);
}

.contact-link {
  display: block;
  text-align: center;
  padding: var(--space-6);
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  text-decoration: none;
  transition: all 0.3s var(--ease-out);
}

.contact-link:hover {
  border-color: var(--border-accent);
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.contact-icon {
  font-size: 48px;
  display: block;
  margin-bottom: var(--space-4);
}

.contact-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.contact-desc {
  font-family: var(--font-code);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
  word-break: break-all;
}

/* ----- 页脚 ----- */
.footer-section {
  text-align: center;
  padding: var(--space-10) var(--space-6);
  border-top: 1px solid var(--border-subtle);
}

.footer-text {
  font-family: var(--font-body);
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: 1.8;
  margin: 0 0 var(--space-2);
}

.footer-text:last-child {
  margin-bottom: 0;
}

.footer-signature {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--accent-gold);
  margin-top: var(--space-4);
}

/* ----- 响应式 ----- */
@media (max-width: 1024px) {
  .tech-grid {
    grid-template-columns: 1fr;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .about-page {
    padding-top: 60px;
  }

  .hero-section {
    padding: var(--space-12) 0;
  }

  .hero-title {
    font-size: var(--text-6xl);
  }

  .hero-avatar {
    width: 80px;
    height: 80px;
  }

  .avatar-symbol {
    font-size: var(--text-4xl);
  }

  .content-wrapper {
    padding: var(--space-10) var(--space-5);
  }

  .contact-grid {
    grid-template-columns: 1fr;
  }

  .hero-subtitle {
    font-size: var(--text-base);
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: var(--text-4xl);
  }

  .card-title {
    font-size: var(--text-xl);
  }

  .section-title {
    font-size: var(--text-2xl);
  }
}
</style>
