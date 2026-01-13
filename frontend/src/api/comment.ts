import request from '@/utils/request'

// 获取评论列表
export const getCommentList = (articleId: number) => {
  return request.get(`/comments`, { params: { articleId } })
}

// 提交评论
export const submitComment = (data: any) => {
  return request.post('/comments', data)
}

// 后台：审核评论
export const approveComment = (id: number) => {
  return request.put(`/admin/comments/${id}/approve`)
}

// 后台：删除评论
export const deleteComment = (id: number) => {
  return request.delete(`/admin/comments/${id}`)
}
