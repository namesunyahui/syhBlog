import request from '@/utils/request'

// 用户信息接口定义
export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  role: string
  status: string
  lastLoginTime: string
  createdAt: string
}

// 分页响应
export interface UserListResponse {
  list: User[]
  total: number
  current: number
  size: number
  pages: number
}

// 获取用户列表
export const getUserList = (params: {
  current?: number
  size?: number
  keyword?: string
}) => {
  return request.get<UserListResponse>('/admin/users', { params })
}

// 获取用户详情
export const getUserById = (id: number) => {
  return request.get<User>(`/admin/users/${id}`)
}

// 更新用户角色
export const updateUserRole = (id: number, role: string) => {
  return request.put(`/admin/users/${id}/role`, null, { params: { role } })
}

// 更新用户状态
export const updateUserStatus = (id: number, status: string) => {
  return request.put(`/admin/users/${id}/status`, null, { params: { status } })
}

// 删除用户
export const deleteUser = (id: number) => {
  return request.delete(`/admin/users/${id}`)
}

// 创建用户（管理员功能）
export const createUser = (data: {
  username: string
  nickname: string
  email: string
  password: string
  role: string
}) => {
  return request.post('/admin/users', data)
}
