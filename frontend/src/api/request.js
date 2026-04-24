import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/',
  timeout: 10000
})

const authMessagePattern = /未认证|未登录|请先登录|token|登录状态/i

const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

const redirectToLogin = (message) => {
  if (message) {
    ElMessage.warning(message)
  }

  const currentPath = router.currentRoute.value?.path
  const currentFullPath = router.currentRoute.value?.fullPath
  if (currentPath === '/login') {
    return
  }

  const query = {}
  if (currentFullPath && currentFullPath !== '/login') {
    query.redirect = currentFullPath
  }

  router.push({ path: '/login', query })
}

const handleUnauthorized = (fallbackMessage) => {
  const hasToken = !!localStorage.getItem('token')
  const currentRoute = router.currentRoute.value
  const isProtectedRoute = !!currentRoute?.matched?.some(record => record.meta.auth)

  clearAuth()

  if (hasToken) {
    redirectToLogin('登录状态已失效，请重新登录')
    return
  }

  if (isProtectedRoute) {
    redirectToLogin('请先登录')
    return
  }

  if (fallbackMessage) {
    ElMessage.error(fallbackMessage)
  }
}

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      const isUnauthorized = res.code === 401 || res.code === 403 || authMessagePattern.test(res.message || '')
      if (isUnauthorized) {
        handleUnauthorized()
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message
    if (status === 401 || status === 403 || authMessagePattern.test(message || '')) {
      handleUnauthorized()
      return Promise.reject(error)
    }

    ElMessage.error(message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
