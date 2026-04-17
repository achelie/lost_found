<template>
  <div class="detail-page animate-fade-in-up" v-loading="loading">
    <el-button text @click="$router.back()" class="back-btn">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
      返回列表
    </el-button>

    <div class="detail-container" v-if="item">
      <!-- Left: Images -->
      <div class="detail-left">
        <div class="main-image" v-if="item.images">
          <el-image :src="currentImage" fit="cover" :preview-src-list="item.images.split(',')" class="big-img" />
        </div>
        <div class="main-image placeholder-img" v-else>
          <span style="font-size:64px">{{ item.type === 0 ? '🔍' : '📦' }}</span>
        </div>
        <div class="thumb-list" v-if="item.images && item.images.split(',').length > 1">
          <div class="thumb" v-for="(img, i) in item.images.split(',')" :key="i"
            :class="{ active: currentImage === img }" @click="currentImage = img">
            <img :src="img" alt="" />
          </div>
        </div>
      </div>

      <!-- Right: Info -->
      <div class="detail-right">
        <div class="detail-badges">
          <span class="type-badge" :class="item.type === 0 ? 'badge-lost' : 'badge-found'">
            {{ item.type === 0 ? '🔍 失物' : '📦 招领' }}
          </span>
          <el-tag type="info" round>{{ item.category }}</el-tag>
          <el-tag :type="statusMap[item.status]?.type" round>{{ statusMap[item.status]?.label }}</el-tag>
        </div>

        <h1 class="detail-title">{{ item.title }}</h1>

        <div class="info-grid">
          <div class="info-item">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <div>
              <span class="info-label">地点</span>
              <span class="info-value">{{ item.location || '未知' }}</span>
            </div>
          </div>
          <div class="info-item">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <div>
              <span class="info-label">时间</span>
              <span class="info-value">{{ item.itemTime?.substring(0, 16) || '未知时间' }}</span>
            </div>
          </div>
          <div class="info-item">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
            <div>
              <span class="info-label">联系方式</span>
              <span class="info-value">{{ item.contact || '未提供' }}</span>
            </div>
          </div>
           <div class="info-item">
             <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"/><line x1="16" x2="16" y1="2" y2="6"/><line x1="8" x2="8" y1="2" y2="6"/><line x1="3" x2="21" y1="10" y2="10"/></svg>
             <div>
               <span class="info-label">发布日期</span>
               <span class="info-value">{{ item.createdAt?.substring(0, 10) }}</span>
             </div>
           </div>
           <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              <div>
                <span class="info-label">发布人</span>
                <span 
                  class="info-value publisher-link" 
                  @click="goToChat"
                  :style="{ cursor: item?.userId && item.userId !== userId ? 'pointer' : 'default', opacity: item?.userId && item.userId !== userId ? 1 : 0.6 }"
                >
                  {{ item.nickName || '匿名用户' }} 
                  <span v-if="item?.userId && item.userId !== userId">💬</span>
                </span>
              </div>
            </div>
         </div>

        <div class="desc-section">
          <h3>详细描述</h3>
          <p>{{ item.description || '暂无描述' }}</p>
        </div>

        <div class="action-buttons" v-if="userStore.isLoggedIn">
          <el-button v-if="item.userId !== userId && item.status === 1" type="primary" size="large" round
            @click="$router.push(`/claim/${item.id}`)">
            {{ item.type === 0 ? '💡 提供线索' : '✋ 我要认领' }}
          </el-button>
          <el-button v-if="item.userId === userId" size="large" round
            @click="$router.push(`/claims/${item.id}`)">
            {{ item.type === 0 ? '📋 查看收到的线索' : '📋 查看认领申请' }}
          </el-button>
        </div>
        <div class="login-tip" v-else>
          <p>{{ item && item.type === 0 ? '登录后可提供线索' : '登录后可提交认领申请' }}</p>
          <el-button type="primary" round @click="$router.push('/login')">去登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemDetail } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const item = ref(null)
const loading = ref(false)
const currentImage = ref('')
const userId = computed(() => userStore.user?.id)

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已认领', type: 'info' },
  4: { label: '已关闭', type: 'info' }
}

const canChat = computed(() => {
  return userStore.isLoggedIn && item.value?.userId && item.value.userId !== userId.value
})

const goToChat = () => {
  // 检查是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!item.value || !item.value.userId) {
    ElMessage.error('发布者信息不完整')
    return
  }

  if (item.value.userId === userId.value) {
    ElMessage.warning('不能与自己聊天')
    return
  }

  console.log('跳转到私聊，对方用户ID:', item.value.userId)
  router.push({
    name: 'Chat',
    params: { userId: item.value.userId }
  })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getItemDetail(route.params.id)
    item.value = res.data
    if (res.data?.images) currentImage.value = res.data.images.split(',')[0]
  } finally { loading.value = false }
})
</script>

<style scoped>
.back-btn { margin-bottom: 20px; font-weight: 500; color: var(--text-secondary) !important; }
.detail-container {
  display: flex;
  gap: 36px;
  background: white;
  border-radius: 24px;
  padding: 32px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border);
}
.detail-left { width: 420px; flex-shrink: 0; }
.main-image {
  border-radius: 16px;
  overflow: hidden;
  height: 360px;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
}
.big-img { width: 100%; height: 100%; }
.placeholder-img { display: flex; align-items: center; justify-content: center; }
.thumb-list { display: flex; gap: 10px; margin-top: 12px; }
.thumb {
  width: 64px; height: 64px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: var(--transition);
}
.thumb.active, .thumb:hover { border-color: var(--primary); }
.thumb img { width: 100%; height: 100%; object-fit: cover; }

.detail-right { flex: 1; }
.detail-badges { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.type-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}
.badge-lost { background: #fef2f2; color: #dc2626; }
.badge-found { background: #ecfdf5; color: #059669; }
.detail-title { font-size: 26px; font-weight: 800; color: var(--text-primary); margin-bottom: 24px; line-height: 1.3; }

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: #f8fafc;
  border-radius: 12px;
}
.info-item svg { color: var(--primary); flex-shrink: 0; margin-top: 2px; }
.info-label { display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 2px; }
.info-value { display: block; font-size: 14px; font-weight: 600; color: var(--text-primary); }
.publisher-link { 
  cursor: pointer; 
  color: var(--primary);
  transition: all 0.3s ease;
  user-select: none;
}
.publisher-link:hover { 
  text-decoration: underline;
  transform: translateY(-1px);
}

.desc-section {
  margin-bottom: 28px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 14px;
}
.desc-section h3 { font-size: 15px; font-weight: 700; margin-bottom: 10px; color: var(--text-primary); }
.desc-section p { font-size: 14px; color: var(--text-secondary); line-height: 1.8; white-space: pre-wrap; }

.action-buttons { display: flex; gap: 12px; }
.login-tip {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 20px;
  background: #eff6ff;
  border-radius: 12px;
}
.login-tip p { font-size: 14px; color: #3b82f6; font-weight: 500; }

@media (max-width: 768px) {
  .detail-container { flex-direction: column; padding: 20px; }
  .detail-left { width: 100%; }
  .main-image { height: 260px; }
  .info-grid { grid-template-columns: 1fr; }
}
</style>
