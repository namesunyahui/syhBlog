import request from '@/utils/request'

// 管理员登录
export const login = (data: { username: string; password: string }) => {
  return request.post('/auth/login', data)
}

// 获取管理员信息
export const getAdminInfo = () => {
  return request.get('/auth/info')
}

// 退出登录
export const logout = () => {
  return request.post('/auth/logout')
}

// 用户注册
export const register = (data: {
  username: string
  password: string
  nickname: string
  email: string
}) => {
  return request.post('/auth/register', data)
}

// 检查用户名是否存在
export const checkUsername = (username: string) => {
  return request.get('/admin/users/check-username', { params: { username } })
}

// 检查邮箱是否存在
export const checkEmail = (email: string) => {
  return request.get('/admin/users/check-email', { params: { email } })
}
