import request from '@/utils/request'

// 获取标签列表
export const getTagList = () => {
  return request.get('/tags')
}

// 后台：创建标签
export const createTag = (data: any) => {
  return request.post('/admin/tags', data)
}

// 后台：更新标签
export const updateTag = (id: number, data: any) => {
  return request.put(`/admin/tags/${id}`, data)
}

// 后台：删除标签
export const deleteTag = (id: number) => {
  return request.delete(`/admin/tags/${id}`)
}
