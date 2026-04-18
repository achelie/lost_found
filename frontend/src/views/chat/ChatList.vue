<template>
  <div class="chat-list-page animate-fade-in-up">
    <h2 class="page-title">💬 消息列表</h2>
    
    <div class="chat-list-container">
      <div 
        class="chat-item" 
        v-for="(conversation, index) in conversations" 
        :key="index"
        @click="$router.push(`/chat/${conversation.userId}`)"
      >
        <div class="chat-avatar">👤</div>
        <div class="chat-info">
          <div class="chat-name">{{ conversation.nickName || conversation.username || '用户' }}</div>
          <div class="chat-last-message">{{ conversation.lastMessage || '暂无消息' }}</div>
        </div>
        <div class="chat-meta">
          <div class="chat-time">{{ conversation.lastTime }}</div>
          <div class="chat-unread" v-if="conversation.unread > 0">{{ conversation.unread }}</div>
        </div>
      </div>
      
      <div class="empty-list" v-if="conversations.length === 0">
        <span>暂无消息</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const conversations = ref([])

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}/${day}`
}

const loadConversations = async () => {
  try {
    const res = await request.get('/api/chat/conversations')
    if (res.data) {
      conversations.value = res.data.map(conv => ({
        ...conv,
        lastTime: formatTime(conv.lastTime)
      }))
      console.log('加载了', conversations.value.length, '个聊天对话')
    }
  } catch (error) {
    console.error('加载聊天列表失败:', error)
  }
}

onMounted(() => {
  loadConversations()
  
  // 监听聊天消息标记为已读事件
  window.addEventListener('chat-messages-read', () => {
    loadConversations()
  })
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 20px;
  color: var(--text-primary);
}
.chat-list-container {
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border);
  overflow: hidden;
}
.chat-item {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.chat-item:hover {
  background: #ecfdf5;
}
.chat-item:last-child {
  border-bottom: none;
}
.chat-avatar {
  width: 48px;
  height: 48px;
  background: var(--primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}
.chat-info {
  flex: 1;
  overflow: hidden;
}
.chat-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.chat-last-message {
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chat-meta {
  text-align: right;
  flex-shrink: 0;
}
.chat-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}
.chat-unread {
  background: #dc2626;
  color: white;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  display: inline-block;
}
.empty-list {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 20px;
}
</style>