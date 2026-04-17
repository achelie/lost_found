<template>
  <div class="chat-page animate-fade-in-up">
    <el-button text @click="$router.back()" class="back-btn">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
      返回
    </el-button>
    
    <el-button type="danger" text @click="loadHistoryMessages" style="float: right; margin-right: 10px;">
      加载历史
    </el-button>

    <div class="chat-container">
      <div class="chat-header">
        <div class="chat-avatar">👤</div>
        <div class="chat-info">
          <div class="chat-name">{{ targetUser.nickName || targetUser.username || '用户' }}</div>
        </div>
      </div>

      <div class="chat-messages" ref="messagesRef">
        <div 
          class="message-item" 
          :class="{ self: message.fromUserId === userId }"
          v-for="(message, index) in messages" 
          :key="index"
        >
          <div class="message-avatar">{{ message.fromUserId === userId ? '👤' : '👤' }}</div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
        </div>
        <div class="empty-message" v-if="messages.length === 0">
          <span>还没有消息，开始聊天吧~</span>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input
          v-model="inputMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          maxlength="500"
          show-word-limit
          :disabled="!wsConnected"
        >
          <template #append>
            <el-button 
              type="primary" 
              @click="sendMessage"
              :disabled="!wsConnected"
            >
              {{ wsConnected ? '发送' : '连接中...' }}
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="connection-status" v-if="!wsConnected">
        <span>❌ WebSocket连接中断，请刷新页面</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import websocket from '@/api/websocket'

const route = useRoute()
const userStore = useUserStore()
const messagesRef = ref(null)
const inputMessage = ref('')
const messages = ref([])
const targetUser = ref({})
const wsConnected = ref(false)

const userId = computed(() => userStore.user?.id)
const toUserId = computed(() => parseInt(route.params.userId))

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  if (typeof dateStr === 'string') {
    return dateStr.substring(11, 16)
  }
  return new Date(dateStr).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('消息不能为空')
    return
  }

  if (!wsConnected.value) {
    ElMessage.error('WebSocket未连接')
    return
  }

  try {
    const content = inputMessage.value.trim()
    
    // 通过WebSocket发送消息
    const success = websocket.sendPrivateMessage(toUserId.value, content)
    
    if (success) {
      // 立即显示自己发送的消息
      const newMessage = {
        id: Date.now(),
        fromUserId: userId.value,
        toUserId: toUserId.value,
        content: content,
        createdAt: new Date().toLocaleString('zh-CN')
      }
      messages.value.push(newMessage)
      inputMessage.value = ''
      scrollToBottom()
    } else {
      ElMessage.error('发送消息失败')
    }
  } catch (error) {
    console.error('发送消息异常:', error)
    ElMessage.error('发送消息异常')
  }
}

// 加载历史消息
const loadHistoryMessages = async () => {
  try {
    if (!toUserId.value) {
      console.warn('对方用户ID未定义')
      ElMessage.warning('用户ID获取失败')
      return
    }
    
    console.log('加载历史消息，对方ID:', toUserId.value)
    const res = await request.get(`/api/chat/messages/${toUserId.value}`)
    if (res.data) {
      messages.value = res.data
      console.log('加载了', messages.value.length, '条历史消息')
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
  }
}

// 加载用户信息（获取昵称）
const loadUserInfo = async () => {
  try {
    if (!toUserId.value) return
    const res = await request.get(`/api/user/${toUserId.value}`)
    if (res.data) {
      targetUser.value = {
        id: res.data.id,
        username: res.data.username,
        nickName: res.data.nickname
      }
      console.log('加载用户信息:', targetUser.value)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 设置默认用户名
    targetUser.value = {
      id: toUserId.value,
      username: '用户'
    }
  }
}

// 初始化WebSocket连接
const initWebSocket = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('授权令牌不存在')
      return
    }

    await websocket.connect(userId.value, token)
    wsConnected.value = true
    ElMessage.success('WebSocket连接成功')

    // 监听新消息
    websocket.onMessage((message) => {
      console.log('收到新消息:', message)
      // 检查这条消息是否属于当前对话
      const isFromCurrentConversation = (
        (message.fromUserId === userId.value && message.toUserId === toUserId.value) ||
        (message.fromUserId === toUserId.value && message.toUserId === userId.value)
      )
      
      if (isFromCurrentConversation) {
        const msgItem = {
          id: message.id,
          fromUserId: message.fromUserId,
          toUserId: message.toUserId,
          content: message.content,
          createdAt: message.createdAt
        }
        messages.value.push(msgItem)
        scrollToBottom()
      }
    })


  } catch (error) {
    console.error('WebSocket连接失败:', error)
    ElMessage.error('WebSocket连接失败：' + error.message)
    wsConnected.value = false
  }
}

// 监听路由参数变化
watch(() => route.params.userId, async (newUserId) => {
  console.log('路由参数变化，新用户ID:', newUserId)
  if (newUserId) {
    messages.value = []
    await loadUserInfo()
    await loadHistoryMessages()
  }
})

onMounted(async () => {
  // 加载用户信息
  await loadUserInfo()
  
  // 加载历史消息
  await loadHistoryMessages()

  // 初始化WebSocket连接
  await initWebSocket()
})

// 页面卸载时断开连接
onBeforeUnmount(() => {
  if (websocket.isConnected()) {
    websocket.disconnect()
  }
})
</script>

<style scoped>
.back-btn { margin-bottom: 20px; font-weight: 500; color: var(--text-secondary) !important; }
.chat-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border);
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}
.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 12px;
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
}
.chat-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
}
.chat-status {
  font-size: 12px;
  color: #999;
}
.chat-status.online {
  color: #059669;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.message-item.self {
  flex-direction: row-reverse;
}
.message-avatar {
  width: 36px;
  height: 36px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.message-content {
  max-width: 60%;
}
.message-text {
  padding: 12px 16px;
  background: #f1f5f9;
  border-radius: 16px 16px 16px 4px;
  color: var(--text-primary);
  line-height: 1.5;
}
.message-item.self .message-text {
  background: var(--primary);
  color: white;
  border-radius: 16px 16px 4px 16px;
}
.message-time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}
.message-item.self .message-time {
  text-align: right;
}
.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--border);
}
.empty-message {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 20px;
}
.connection-status {
  padding: 10px;
  text-align: center;
  background-color: #f56c6c;
  color: white;
  border-radius: 4px;
  font-size: 12px;
  margin: 16px 20px 0;
}

</style>