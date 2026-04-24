import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 路由按功能分组：公开页、登录注册、物品、认领、通知、个人中心、后台、聊天
const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/auth/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/auth/Register.vue') },
  { path: '/items', name: 'Items', component: () => import('@/views/item/ItemList.vue') },
  { path: '/items/:id', name: 'ItemDetail', component: () => import('@/views/item/ItemDetail.vue') },
  { path: '/publish', name: 'Publish', component: () => import('@/views/item/Publish.vue'), meta: { auth: true } },
  { path: '/claim/:itemId', name: 'SubmitClaim', component: () => import('@/views/claim/SubmitClaim.vue'), meta: { auth: true } },
  { path: '/claims/:itemId', name: 'ClaimList', component: () => import('@/views/claim/ClaimList.vue'), meta: { auth: true } },
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/notification/NotificationList.vue'), meta: { auth: true } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/profile/Profile.vue'), meta: { auth: true } },
  { path: '/my-items', name: 'MyItems', component: () => import('@/views/profile/MyItems.vue'), meta: { auth: true } },
  { path: '/admin/items', name: 'AdminItems', component: () => import('@/views/admin/AdminItems.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUsers.vue'), meta: { auth: true, admin: true } },
  { path: '/chats', name: 'ChatList', component: () => import('@/views/chat/ChatList.vue'), meta: { auth: true } },
  { path: '/chat/:userId', name: 'Chat', component: () => import('@/views/chat/Chat.vue'), meta: { auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：保护需要登录或管理员权限的页面
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const requiresAuth = to.matched.some(record => record.meta.auth)
  const requiresAdmin = to.matched.some(record => record.meta.admin)

  // 需要登录的页面，未登录就先跳登录页
  if (requiresAuth && !token) {
    ElMessage.warning('请先登录')
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 管理员页面额外校验 role=1
  if (requiresAdmin && user?.role !== 1) {
    ElMessage.warning('无权限访问该页面')
    return next('/')
  }

  next()
})

export default router
