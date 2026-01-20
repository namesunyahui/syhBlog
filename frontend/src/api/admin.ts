import request from '@/utils/request'

// 获取仪表盘统计数据
export const getStats = () => {
  return request.get('/admin/stats')
}
