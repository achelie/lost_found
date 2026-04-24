import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

class WebSocketService {
  constructor() {
    this.client = null
    this.connected = false
    this.messageCallbacks = []
    this.statusCallbacks = []
    this.userId = null
  }

  // 建立 WebSocket + STOMP 连接，并订阅私信和在线状态通道
  connect(userId, token) {
    return new Promise((resolve, reject) => {
      try {
        this.userId = userId
        
        // 获取WebSocket服务器URL - SockJS需要使用HTTP/HTTPS，它会自动升级到WebSocket
        // 开发环境：连接到8080（后端服务器）
        // 生产环境：从window.location.host获取
        const wsUrl = process.env.NODE_ENV === 'development'
          ? `http://${window.location.hostname}:8080/ws`
          : `${window.location.protocol}//${window.location.host}/ws`
        
        console.log('🌐 WebSocket连接URL:', wsUrl)
        
        // 创建SockJS连接（会自动升级到WebSocket）
        const socket = new SockJS(wsUrl, null, {
          transports: ['websocket', 'xhr-streaming', 'xhr-polling']
        })
        this.client = Stomp.over(socket)

        // 心跳用于检测连接是否还活着
        this.client.heartbeat.outgoing = 20000
        this.client.heartbeat.incoming = 20000

        // 携带 JWT 进入后端 WebSocket 鉴权流程
        const headers = {
          'Authorization': `Bearer ${token}`
        }

        // 连接成功后订阅个人消息和全局在线状态
        this.client.connect(headers, (frame) => {
          console.log('✅ WebSocket连接成功:', frame)
          this.connected = true

          // 个人私聊消息只发给当前用户
          this.client.subscribe(`/user/${userId}/queue/messages`, (message) => {
            const chatMsg = JSON.parse(message.body)
            console.log('收到消息:', chatMsg)
            this.messageCallbacks.forEach(cb => cb(chatMsg))
          })

          // 在线/离线状态广播给所有人
          this.client.subscribe('/topic/online', (message) => {
            const statusMsg = JSON.parse(message.body)
            console.log('状态变化:', statusMsg)
            this.statusCallbacks.forEach(cb => cb(statusMsg))
          })

          // 连接建立后先向后端报一次上线
          this.sendOnlineStatus()

          resolve(true)
        }, (error) => {
          console.error('❌ WebSocket连接失败:', error)
          console.error('错误详情:', {
            message: error?.message,
            headers: error?.headers,
            body: error?.body,
            fullError: error
          })
          this.connected = false
          reject(error || new Error('WebSocket连接失败'))
        })
      } catch (error) {
        console.error('❌ 创建WebSocket失败:', error)
        console.error('完整错误信息:', {
          message: error?.message,
          stack: error?.stack,
          fullError: error
        })
        reject(error)
      }
    })
  }

  // 断开连接前先通知后端自己离线
  disconnect() {
    if (this.connected && this.client) {
      this.sendOfflineStatus()
      
      this.client.disconnect(() => {
        console.log('WebSocket连接已断开')
        this.connected = false
      })
    }
  }

  // 发送一条私聊消息到后端处理器
  sendPrivateMessage(toUserId, content) {
    if (!this.connected || !this.client) {
      console.error('WebSocket未连接')
      return false
    }

    try {
      const message = {
        fromUserId: this.userId,
        toUserId: toUserId,
        content: content,
        timestamp: Date.now()
      }

      this.client.send('/app/chat/private', {}, JSON.stringify(message))
      return true
    } catch (error) {
      console.error('发送消息失败:', error)
      return false
    }
  }

  // 向服务器广播“我上线了”
  sendOnlineStatus() {
    if (!this.connected || !this.client) return

    try {
      const message = {
        fromUserId: this.userId,
        timestamp: Date.now()
      }
      this.client.send('/app/chat/online', {}, JSON.stringify(message))
    } catch (error) {
      console.error('发送上线状态失败:', error)
    }
  }

  // 向服务器广播“我离线了”
  sendOfflineStatus() {
    if (!this.connected || !this.client) return

    try {
      const message = {
        fromUserId: this.userId,
        timestamp: Date.now()
      }
      this.client.send('/app/chat/offline', {}, JSON.stringify(message))
    } catch (error) {
      console.error('发送离线状态失败:', error)
    }
  }

  /**
   * 注册消息回调
   */
  onMessage(callback) {
    this.messageCallbacks.push(callback)
  }

  /**
   * 注册状态变化回调
   */
  onStatusChange(callback) {
    this.statusCallbacks.push(callback)
  }

  /**
   * 是否已连接
   */
  isConnected() {
    return this.connected
  }
}

export default new WebSocketService()
