<template>
  <header class="app-header">
    <div class="header-inner">
      <div class="header-left" @click="$router.push('/')">
        <div class="logo-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>
          </svg>
        </div>
        <span class="logo-text">失物招领</span>
      </div>
      <nav class="header-nav">
        <router-link to="/" class="nav-link" exact-active-class="active">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          首页
        </router-link>
        <router-link to="/items" class="nav-link" active-class="active">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="7" height="7" x="3" y="3" rx="1"/><rect width="7" height="7" x="14" y="3" rx="1"/><rect width="7" height="7" x="14" y="14" rx="1"/><rect width="7" height="7" x="3" y="14" rx="1"/></svg>
          浏览信息
        </router-link>
        <router-link v-if="userStore.isLoggedIn" to="/publish" class="nav-link" active-class="active">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/><path d="M12 8v8"/></svg>
          发布
        </router-link>
      </nav>
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleMessage" @visible-change="onNotificationDropdownChange" placement="bottom-end">
            <div class="icon-btn" title="消息">
              <el-badge :value="totalUnread" :hidden="!totalUnread" :max="99">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              </el-badge>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="unified-notification-menu">
                <div class="notification-menu-header">
                  <span>消息中心</span>
                </div>
                <el-tabs class="notification-tabs" v-model="activeNotifTab" @tab-change="onTabChange">
                  <el-tab-pane label="聊天" name="chat">
                    <el-scrollbar max-height="350px">
                      <div v-if="chatConversations.length > 0" class="chat-conversations">
                        <div 
                          v-for="conv in chatConversations" 
                          :key="conv.userId"
                          class="notification-item"
                          @click="goToChat(conv.userId)"
                        >
                          <div class="notification-content">
                            <div class="notification-title">{{ conv.nickName || conv.username }}</div>
                            <div class="notification-preview">{{ conv.lastMessage }}</div>
                          </div>
                          <div v-if="conv.unread > 0" class="notification-badge">{{ conv.unread }}</div>
                        </div>
                      </div>
                      <div v-else class="empty-notification">
                        <span>暂无消息</span>
                      </div>
                    </el-scrollbar>
                  </el-tab-pane>
                  <el-tab-pane label="系统通知" name="notification">
                    <el-scrollbar max-height="350px">
                      <div v-if="notifications.length > 0" class="notifications-list">
                        <div 
                          v-for="notif in notifications" 
                          :key="notif.id"
                          class="notification-item"
                          :class="{ 
                            'is-unread': !notif.isRead,
                            'clickable': notif.type === 0 && notif.relatedId
                          }"
                          @click="handleNotificationClick(notif)"
                        >
                          <div class="notification-content">
                            <div class="notification-title">{{ notif.title }}</div>
                            <div class="notification-preview">{{ notif.content }}</div>
                            <div class="notification-time">{{ formatNotifTime(notif.createdAt) }}</div>
                          </div>
                          <div v-if="!notif.isRead" class="notification-dot"></div>
                        </div>
                      </div>
                      <div v-else class="empty-notification">
                        <span>暂无通知</span>
                      </div>
                    </el-scrollbar>
                  </el-tab-pane>
                </el-tabs>
                <div class="notification-menu-footer">
                  <router-link to="/chats" class="view-all-link">查看全部聊天</router-link>
                  <router-link to="/notifications" class="view-all-link">查看全部通知</router-link>
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="handleCmd">
            <div class="user-avatar">
              <div class="avatar-circle">{{ userStore.user?.nickname?.charAt(0) || userStore.user?.username?.charAt(0) || 'U' }}</div>
              <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:8px"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="myitems">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:8px"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect width="8" height="4" x="8" y="2" rx="1" ry="1"/></svg>
                  我的发布
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.user?.role===1" command="admin" divided>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:8px"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:8px"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login">
            <el-button round>登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button type="primary" round>注册</el-button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount, getNotifications, markRead } from '@/api'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const unreadMessages = ref(0)
const unreadNotif = ref(0)
const totalUnread = ref(0)
const chatConversations = ref([])
const notifications = ref([])
const activeNotifTab = ref('chat')

const handleCmd = (cmd) => {
  if (cmd === 'logout') { userStore.logout(); router.push('/') }
  else if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'myitems') router.push('/my-items')
  else if (cmd === 'admin') router.push('/admin/items')
}

// 加载消息列表
const loadChatConversations = async () => {
  try {
    const res = await request.get('/api/chat/conversations')
    if (res.data) {
      chatConversations.value = res.data
      unreadMessages.value = res.data.reduce((sum, conv) => sum + (conv.unread || 0), 0)
      updateTotalUnread()
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  }
}

// 加载系统通知
const loadNotifications = async () => {
  try {
    const res = await getNotifications({ page: 1, size: 10 })
    if (res.data) {
      // 适配不同的数据结构
      if (Array.isArray(res.data)) {
        notifications.value = res.data
      } else if (res.data.records) {
        notifications.value = res.data.records
      } else if (res.data.id) {
        notifications.value = [res.data]
      } else {
        notifications.value = []
      }
    }
    // 获取未读计数
    const countRes = await getUnreadCount()
    unreadNotif.value = countRes.data || 0
    updateTotalUnread()
  } catch (error) {
    console.error('加载通知失败:', error)
    notifications.value = []
  }
}

// 更新总未读计数
const updateTotalUnread = () => {
  totalUnread.value = unreadMessages.value + unreadNotif.value
}

// 格式化通知时间
const formatNotifTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return Math.floor(diff / 86400000) + '天前'
}

// 进入私聊
const goToChat = (userId) => {
  router.push(`/chat/${userId}`)
}

// 处理通知点击
const handleNotificationClick = async (notif) => {
  if (!notif.isRead) {
    try {
      await markRead(notif.id)
      notif.isRead = true
      unreadNotif.value = Math.max(0, unreadNotif.value - 1)
      updateTotalUnread()
    } catch (error) {
      console.error('标记通知为已读失败:', error)
    }
  }
  
  // 只有新认领申请通知(type=0)才跳转
  if (notif.type === 0 && notif.relatedId) {
    router.push(`/claims/${notif.relatedId}`)
  }
}

// 下拉菜单打开时加载数据
const onNotificationDropdownChange = async (visible) => {
  if (visible) {
    if (activeNotifTab.value === 'chat') {
      await loadChatConversations()
    } else {
      await loadNotifications()
    }
  }
}

// Tab 切换时加载对应数据
const onTabChange = async (tabName) => {
  if (tabName === 'chat') {
    await loadChatConversations()
  } else {
    await loadNotifications()
  }
}

onMounted(() => {
  // 初始化加载
  loadChatConversations()
  loadNotifications()
  
  // 监听全局更新事件
  window.addEventListener('update-unread-count', () => {
    loadNotifications()
  })
})

onUnmounted(() => {
  window.removeEventListener('update-unread-count', loadNotifications)
})
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
}
.header-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: var(--transition);
}
.header-left:hover { opacity: 0.8; }
.logo-icon {
  width: 38px; height: 38px;
  background: var(--primary-gradient);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 2px 10px rgba(99, 102, 241, 0.3);
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: var(--transition);
}
.nav-link:hover {
  color: var(--primary);
  background: rgba(99, 102, 241, 0.06);
}
.nav-link.active {
  color: var(--primary);
  background: rgba(99, 102, 241, 0.1);
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px; height: 40px;
  border-radius: 10px;
  color: var(--text-secondary);
  transition: var(--transition);
  cursor: pointer;
}
.icon-btn:hover {
  background: rgba(99, 102, 241, 0.06);
  color: var(--primary);
}
.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 24px;
  transition: var(--transition);
}
.user-avatar:hover {
  background: rgba(99, 102, 241, 0.06);
}
.avatar-circle {
  width: 32px; height: 32px;
  background: var(--primary-gradient);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}
.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 统一消息中心样式 */
:deep(.unified-notification-menu) {
  padding: 0 !important;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  min-width: 360px;
}

.notification-menu-header {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.3);
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.notification-menu-footer {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.3);
  background: rgba(99, 102, 241, 0.02);
}

.view-all-link {
  flex: 1;
  padding: 6px 8px;
  text-align: center;
  font-size: 12px;
  font-weight: 500;
  color: var(--primary);
  text-decoration: none;
  border-radius: 6px;
  transition: var(--transition);
}

.view-all-link:hover {
  background: rgba(99, 102, 241, 0.1);
}

:deep(.notification-tabs) {
  margin: 0 !important;
  border: none !important;
}

:deep(.notification-tabs .el-tabs__header) {
  margin: 0 !important;
  border-bottom: 1px solid rgba(226, 232, 240, 0.3) !important;
}

:deep(.notification-tabs .el-tabs__nav-wrap) {
  padding: 0 12px !important;
}

.chat-conversations,
.notifications-list {
  padding: 4px 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 10px 12px;
  transition: var(--transition);
  border-radius: 8px;
  margin: 0 4px;
  cursor: default;
}

.notification-item.clickable {
  cursor: pointer;
}

.notification-item:hover {
  background: rgba(99, 102, 241, 0.06);
}

.notification-item.is-unread {
  background: rgba(99, 102, 241, 0.03);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.notification-preview {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 260px;
}

.notification-time {
  font-size: 11px;
  color: var(--text-disabled);
  margin-top: 2px;
}

.notification-badge {
  min-width: 20px;
  height: 20px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  margin-left: 8px;
  flex-shrink: 0;
}

.notification-dot {
  width: 8px;
  height: 8px;
  background: var(--primary);
  border-radius: 50%;
  margin-left: 8px;
  flex-shrink: 0;
  flex: unset;
}

.empty-notification {
  padding: 40px 16px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
