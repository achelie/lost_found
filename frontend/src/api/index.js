import request from './request'

// 认证
export const login = (data) => request.post('/api/auth/login', data)
export const register = (data) => request.post('/api/auth/register', data)

// 物品
export const getItems = (params) => request.get('/api/items/list', { params })
export const getItemDetail = (id) => request.get(`/api/items/detail/${id}`)
export const publishItem = (data) => request.post('/api/items/publish', data)
export const updateMyItem = (id, data) => request.put(`/api/items/${id}`, data)
export const deleteMyItem = (id) => request.delete(`/api/items/${id}`)
export const getMyItems = (params) => request.get('/api/items/my', { params })
export const getStats = () => request.get('/api/items/stats')

// 认领
export const submitClaim = (data) => request.post('/api/claims/submit', data)
export const getItemClaims = (itemId, params) => request.get(`/api/claims/item/${itemId}`, { params })
export const auditClaim = (claimId, params) => request.post(`/api/claims/audit/${claimId}`, null, { params })

// 通知
export const getNotifications = (params) => request.get('/api/notifications', { params })
export const markRead = (id) => request.post(`/api/notifications/read/${id}`)
export const getUnreadCount = () => request.get('/api/notifications/unread-count')

// 用户
export const getProfile = () => request.get('/api/user/profile')
export const updateProfile = (data) => request.put('/api/user/profile', data)
export const changePassword = (data) => request.put('/api/user/password', data)

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 管理员
export const adminGetItems = (params) => request.get('/api/admin/items', { params })
export const adminAuditItem = (id, params) => request.post(`/api/admin/items/${id}/audit`, null, { params })
export const adminDeleteItem = (id) => request.delete(`/api/admin/items/${id}`)
export const adminGetUsers = (params) => request.get('/api/admin/users', { params })
export const adminUpdateUserStatus = (id, status) => request.patch(`/api/admin/users/${id}/status`, null, { params: { status } })
export const adminUpdateUserRole = (id, role) => request.patch(`/api/admin/users/${id}/role`, null, { params: { role } })
export const adminChangeUserPassword = (id, data) => request.put(`/api/admin/users/${id}/password`, data)
export const adminDeleteUser = (id) => request.delete(`/api/admin/users/${id}`)
