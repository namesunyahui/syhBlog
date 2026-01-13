import request from '@/utils/request'

// 文章列表
export const getArticleList = (params: any) => {
  return request.get('/articles', { params })
}

// 文章详情
export const getArticleDetail = (id: number) => {
  return request.get(`/articles/${id}`)
}

// 增加浏览量
export const addViewCount = (id: number) => {
  return request.post(`/articles/${id}/view`)
}

// 文章归档
export const getArticleArchive = () => {
  return request.get('/articles/archive')
}

// 搜索文章
export const searchArticles = (keyword: string) => {
  return request.get('/articles/search', { params: { keyword } })
}

// ============ 后台管理接口 ============

// 获取所有文章（包括草稿）
export const getAdminArticles = (params: any) => {
  return request.get('/admin/articles', { params })
}

// 创建文章
export const createArticle = (data: any) => {
  return request.post('/admin/articles', data)
}

// 更新文章
export const updateArticle = (id: number, data: any) => {
  return request.put(`/admin/articles/${id}`, data)
}

// 删除文章
export const deleteArticle = (id: number) => {
  return request.delete(`/admin/articles/${id}`)
}

// 发布/撤回文章
export const publishArticle = (id: number, publish: boolean) => {
  return request.put(`/admin/articles/${id}/publish`, { publish })
}
