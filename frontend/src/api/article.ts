import request from '@/utils/request'

// 文章列表
export const getArticleList = (params: any) => {
  return request.get('/articles', { params })
}

// 根据多个标签查询文章
export const getArticlesByTags = (params: {
  tagIds: string  // 逗号分隔的标签ID，如 "1,2,3"
  page: number
  size: number
}) => {
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
export const searchArticles = (params: {
  keyword?: string
  page?: number
  size?: number
  categoryId?: number
  tagId?: number
}) => {
  return request.get('/articles/search', { params })
}

// 获取热门文章
export const getHotArticles = (limit = 5) => {
  return request.get('/articles/hot', { params: { limit } })
}

// 获取相关文章
export const getRelatedArticles = (articleId: number, limit = 5) => {
  return request.get(`/articles/${articleId}/related`, { params: { limit } })
}

// ============ 后台管理接口 ============

// 获取所有文章（包括草稿）
export const getAdminArticles = (params: any) => {
  return request.get('/articles/all', { params })
}

// 创建文章
export const createArticle = (data: any) => {
  return request.post('/articles', data)
}

// 更新文章
export const updateArticle = (id: number, data: any) => {
  return request.put(`/articles/${id}`, data)
}

// 删除文章
export const deleteArticle = (id: number) => {
  return request.delete(`/articles/${id}`)
}

// 发布/撤回文章
export const publishArticle = (id: number, publish: boolean) => {
  return request.put(`/articles/${id}/publish`, { publish })
}

// 获取分组归档数据
export const getGroupedArchive = () => {
  return request.get('/articles/archive/grouped')
}
