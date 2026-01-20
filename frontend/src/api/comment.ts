import request from '@/utils/request'

// 获取评论列表
export const getCommentList = (articleId: number) => {
  return request.get(`/comments`, { params: { articleId } })
}

// 提交评论
export const submitComment = (data: any) => {
  return request.post('/comments', data)
}

// ============ 后台管理接口 ============

// 获取评论列表（管理员）
export const getAdminComments = (params: {
  articleId?: number
  nickname?: string
  page?: number
  size?: number
}) => {
  return request.get('/admin/comments', { params })
}

// 删除评论（管理员）
export const deleteComment = (id: number) => {
  return request.delete(`/admin/comments/${id}`)
}

// 批量删除评论（管理员）
export const batchDeleteComments = (ids: number[]) => {
  return request.delete('/admin/comments/batch', { data: ids })
}
