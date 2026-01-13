import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    // 处理 401 未认证
    if (error.response?.status === 401) {
      // 只在管理后台页面（除了登录和注册页面）才跳转登录页
      const pathname = window.location.pathname
      const isAdminPage = pathname.startsWith('/admin') &&
                          !pathname.startsWith('/admin/login') &&
                          !pathname.startsWith('/admin/register')
      if (isAdminPage) {
        ElMessage.error('请先登录')
        // 清除过期的 token
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        // 跳转到登录页
        window.location.href = '/admin/login'
      }
      return Promise.reject(error)
    }
    // 处理 403 权限不足
    if (error.response?.status === 403) {
      ElMessage.error('权限不足，无法访问该功能')
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
