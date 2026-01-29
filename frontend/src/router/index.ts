import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/article/:id',
      name: 'article',
      component: () => import('@/views/ArticleDetail.vue')
    },
    {
      path: '/category',
      name: 'category',
      component: () => import('@/views/Category.vue')
    },
    {
      path: '/category/:id',
      name: 'categoryDetail',
      component: () => import('@/views/CategoryDetail.vue')
    },
    {
      path: '/tag',
      name: 'tag',
      component: () => import('@/views/Tag.vue')
    },
    {
      path: '/archive',
      name: 'archive',
      component: () => import('@/views/Archive.vue')
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/Search.vue')
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/About.vue')
    },
    {
      path: '/admin/login',
      name: 'adminLogin',
      component: () => import('@/views/admin/Login.vue')
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/Layout.vue'),
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: 'articles',
          name: 'articleList',
          component: () => import('@/views/admin/ArticleList.vue')
        },
        {
          path: 'article/edit',
          name: 'articleEdit',
          component: () => import('@/views/admin/ArticleEdit.vue')
        },
        {
          path: 'categories',
          name: 'categoryManage',
          component: () => import('@/views/admin/CategoryManage.vue')
        },
        {
          path: 'tags',
          name: 'tagManage',
          component: () => import('@/views/admin/TagManage.vue')
        },
        {
          path: 'comments',
          name: 'commentManage',
          component: () => import('@/views/admin/CommentManage.vue')
        },
        {
          path: 'users',
          name: 'userManage',
          component: () => import('@/views/admin/UserManage.vue')
        }
      ]
    }
  ]
})

// 全局前置守卫 - 检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token

  // 需要登录的路由路径（白名单）
  const publicRoutes = ['/admin/login']
  const isAdminRoute = to.path.startsWith('/admin')
  const isPublicRoute = publicRoutes.includes(to.path)

  if (isAdminRoute && !isPublicRoute) {
    // 管理页面但不是登录页，需要检查登录
    if (isLoggedIn) {
      next() // 已登录，允许访问
    } else {
      next('/admin/login') // 未登录，重定向到登录页
    }
  } else if (to.path === '/admin/login' && isLoggedIn) {
    // 已登录用户访问登录页，重定向到管理后台
    next('/admin/dashboard')
  } else {
    // 公开页面，直接放行
    next()
  }
})

export default router
