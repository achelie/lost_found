<template>
  <div class="notification-page animate-fade-in-up">
    <div class="page-header">
      <h1 class="page-title">🔔 消息通知</h1>
      <p class="page-desc">查看认领申请、审核结果等通知消息</p>
    </div>

    <div class="notification-list" v-loading="loading">
      <div class="notification-item" v-for="n in notifications" :key="n.id" :class="{ unread: !n.isRead }" @click="handleItemClick(n)">
        <div class="notif-indicator" v-if="!n.isRead"></div>
        <div class="notif-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
        </div>
        <div class="notif-content">
          <h4>{{ n.title }}</h4>
          <p>{{ n.content }}</p>
          <span class="notif-time">{{ n.createdAt }}</span>
        </div>
        <el-button 
          v-if="n.type === 0" 
          type="primary" 
          size="small" 
          round 
          @click.stop="goToClaim(n.relatedId, n)"
        >
          查看申请
        </el-button>
        <el-button v-if="!n.isRead" size="small" round @click="handleRead(n)">标为已读</el-button>
        <span v-else class="read-badge">已读</span>
      </div>
    </div>

    <el-empty v-if="!loading && notifications.length===0" description="暂无通知消息" />
    <div class="pagination-wrapper" v-if="total>0">
      <el-pagination :current-page="page" :page-size="10" :total="total" background
        layout="prev,pager,next" @current-change="p=>{page=p;fetch()}" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markRead } from '@/api'

const router = useRouter()

const notifications = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

const fetch = async () => {
  loading.value = true
  try {
    const res = await getNotifications({ page: page.value, size: 10 })
    // 适配后端返回的数据结构
    if (res.data) {
      // 如果是分页对象结构 { records: [], total: 0 }
      if (res.data.records) {
        notifications.value = res.data.records
        total.value = res.data.total || 0
      } 
      // 如果是直接数组
      else if (Array.isArray(res.data)) {
        notifications.value = res.data
        total.value = res.data.length
      }
      // 如果是单个对象，包装成数组
      else if (typeof res.data === 'object' && res.data.id) {
        notifications.value = [res.data]
        total.value = 1
      }
    }
  } catch (error) {
    console.error('加载通知失败:', error)
  } finally { 
    loading.value = false 
  }
}

const handleRead = async (row) => {
  await markRead(row.id)
  row.isRead = 1
  // 更新全局未读计数
  const unreadCountEvent = new CustomEvent('update-unread-count')
  window.dispatchEvent(unreadCountEvent)
}

const handleItemClick = async (notification) => {
  // 点击通知项自动标记为已读
  if (!notification.isRead) {
    await markRead(notification.id)
    notification.isRead = 1
    // 更新全局未读计数
    const unreadCountEvent = new CustomEvent('update-unread-count')
    window.dispatchEvent(unreadCountEvent)
  }
  
  // 只有新认领申请通知(type=0)才跳转，审核结果通知不跳转
  if (notification.type === 0 && notification.relatedId) {
    // 跳转到该物品的认领申请列表页，与"我的发布"中的查看申请效果完全一致
    // 这里是物品发布者查看所有认领申请的页面，与物品高度绑定
    router.push(`/claims/${notification.relatedId}`)
  }
}

const goToClaim = async (itemId, notification) => {
  // 按钮点击同样走统一处理逻辑
  await handleItemClick(notification)
}

onMounted(fetch)
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; }
.page-desc { font-size: 14px; color: var(--text-muted); margin-top: 4px; }

.notification-list { display: flex; flex-direction: column; gap: 12px; }
.notification-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
  position: relative;
}
.notification-item:hover { box-shadow: var(--shadow-md); }
.notification-item.unread {
  background: linear-gradient(135deg, #fafbff, #f0f0ff);
  border-color: rgba(99, 102, 241, 0.2);
}
.notif-indicator {
  position: absolute;
  top: 24px; left: 12px;
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--primary);
  animation: pulse-glow 2s ease-in-out infinite;
}
.notif-icon {
  width: 44px; height: 44px;
  border-radius: 12px;
  background: #eff6ff;
  display: flex; align-items: center; justify-content: center;
  color: var(--primary);
  flex-shrink: 0;
}
.notif-content { flex: 1; }
.notif-content h4 { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.notif-content p { font-size: 13px; color: var(--text-secondary); line-height: 1.5; }
.notif-time { font-size: 12px; color: var(--text-muted); margin-top: 4px; display: block; }
.read-badge { font-size: 12px; color: var(--text-muted); white-space: nowrap; }
.pagination-wrapper { display: flex; justify-content: center; padding: 20px 0; }
</style>
