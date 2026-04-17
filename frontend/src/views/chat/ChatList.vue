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
          <div class="chat-name">{{ conversation.nickName }}</div>
          <div class="chat-last-message">{{ conversation.lastMessage }}</div>
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
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const conversations = ref([])

const userId = computed(() => userStore.user?.id)

const loadConversations = () => {
  const allMessages = JSON.parse(localStorage.getItem('chatMessages') || '[]')
  
  // 按用户聚合会话
  const conversationMap = new Map()
  
  allMessages.forEach(msg => {
    let otherUserId
    
    if (msg.fromId === userId.value) {
      otherUserId = msg.toId
    } else if (msg.toId === userId.value) {
      otherUserId = msg.fromId
    } else {
      return
    }
    
    if (!conversationMap.has(otherUserId)) {
      conversationMap.set(otherUserId, {
        userId: otherUserId,
        nickName: '用户' + otherUserId,
        lastMessage: msg.content,
        lastTime: msg.createdAt,
        unread: msg.fromId === otherUserId ? 1 : 0,
        messages: []
      })
    }
    
    const conv = conversationMap.get(otherUserId)
    conv.messages.push(msg)
    
    // 更新最后一条消息
    if (new Date(msg.createdAt) > new Date(conv.lastTime)) {
      conv.lastMessage = msg.content
      conv.lastTime = msg.createdAt
    }
    
    // 统计未读
    if (msg.fromId === otherUserId) {
      conv.unread++
    }
  })
  
  // 转换为数组并按时间排序
  conversations.value = Array.from(conversationMap.values()).sort((a, b) => 
    new Date(b.lastTime) - new Date(a.lastTime)
  )
  
  // 格式化时间
  conversations.value.forEach(conv => {
    const date = new Date(conv.lastTime)
    conv.lastTime = `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  })
}

onMounted(() => {
  loadConversations()
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
  background: #f8fafc;
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