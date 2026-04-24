import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 所有接口请求都走这个 axios 实例，统一处理 token 和错误提示
const request = axios.create({
  baseURL: '/',
  timeout: 10000
})

// 后端常见的未登录/未认证提示，统一拦截处理
const authMessagePattern = /未认证|未登录|请先登录|token|登录状态/i

// 清理本地登录态，避免失效 token 一直影响后续请求
const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

// 统一跳转登录页，并把当前地址带回去，登录后可继续回到原页面
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

// 401/403 时优先判断是不是登录失效，再决定是否静默跳转
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

// 请求前把 token 统一塞进 Authorization 请求头
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应里统一解包后端 Result，并处理非 200 和鉴权失败
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
