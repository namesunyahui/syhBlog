import request from '@/utils/request'

// 获取分类列表
export const getCategoryList = () => {
  return request.get('/categories')
}

// 获取分类详情
export const getCategoryDetail = (id: number) => {
  return request.get(`/categories/${id}`)
}

// 后台：获取所有分类（不带数量统计）
export const getAllCategories = () => {
  return request.get('/categories/all')
}

// 后台：创建分类
export const createCategory = (data: any) => {
  return request.post('/categories', data)
}

// 后台：更新分类
export const updateCategory = (id: number, data: any) => {
  return request.put(`/categories/${id}`, data)
}

// 后台：删除分类
export const deleteCategory = (id: number) => {
  return request.delete(`/categories/${id}`)
}
