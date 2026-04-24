import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

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

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const requiresAuth = to.matched.some(record => record.meta.auth)
  const requiresAdmin = to.matched.some(record => record.meta.admin)

  if (requiresAuth && !token) {
    ElMessage.warning('请先登录')
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (requiresAdmin && user?.role !== 1) {
    ElMessage.warning('无权限访问该页面')
    return next('/')
  }

  next()
})

export default router
