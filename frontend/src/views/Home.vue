<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1"></div>
        <div class="hero-shape hero-shape-2"></div>
        <div class="hero-shape hero-shape-3"></div>
      </div>
      <div class="hero-content animate-fade-in-up">
        <div class="hero-badge">🏫 校园失物招领平台</div>
        <h1 class="hero-title">让每一件失物<br/><span class="gradient-text">都能找到主人</span></h1>
        <p class="hero-desc">在这里发布丢失或拾到的物品信息，帮助校园师生快速找回失物</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/items?type=0')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:6px"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
            我丢了东西
          </el-button>
          <el-button size="large" round class="btn-success" @click="$router.push('/items?type=1')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:6px"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            我捡到东西
          </el-button>
          <el-button size="large" round @click="$router.push('/publish')" v-if="true">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:6px"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/><path d="M12 8v8"/></svg>
            发布信息
          </el-button>
        </div>
      </div>
      <div class="hero-illustration animate-fade-in">
        <div class="float-card float-card-1">
          <span class="float-emoji">📱</span>
          <span>iPhone 15 已找回</span>
        </div>
        <div class="float-card float-card-2">
          <span class="float-emoji">🎒</span>
          <span>黑色背包 待认领</span>
        </div>
        <div class="float-card float-card-3">
          <span class="float-emoji">🪪</span>
          <span>学生证 已归还</span>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section stagger-children">
      <div class="stat-card">
        <div class="stat-icon stat-icon-lost">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.lost }}</span>
          <span class="stat-label">失物信息</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-found">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.found }}</span>
          <span class="stat-label">招领信息</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-claimed">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.claimed }}</span>
          <span class="stat-label">已认领</span>
        </div>
      </div>
    </section>

    <!-- Category Quick Filter -->
    <section class="category-section animate-fade-in-up">
      <h2 class="section-title">
        <span class="title-icon">📂</span>
        快速分类
      </h2>
      <div class="category-grid">
        <div class="category-card" v-for="cat in categories" :key="cat.name" @click="$router.push(`/items?category=${cat.name}`)">
          <span class="category-emoji">{{ cat.icon }}</span>
          <span class="category-name">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- Latest Items -->
    <section class="latest-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">🕐</span>
          最新信息
        </h2>
        <el-button text type="primary" class="view-all-btn" @click="$router.push('/items')">
          查看全部
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-left:4px"><path d="m9 18 6-6-6-6"/></svg>
        </el-button>
      </div>
      <div class="items-grid stagger-children" v-loading="loading">
        <div class="item-card" v-for="item in latestItems" :key="item.id" @click="$router.push(`/items/${item.id}`)">
          <div class="item-card-img" v-if="item.images">
            <img :src="item.images.split(',')[0]" alt="" />
            <div class="item-type-badge" :class="item.type === 0 ? 'badge-lost' : 'badge-found'">
              {{ item.type === 0 ? '失物' : '招领' }}
            </div>
          </div>
          <div class="item-card-img item-card-placeholder" v-else>
            <span style="font-size:36px">{{ item.type === 0 ? '🔍' : '📦' }}</span>
            <div class="item-type-badge" :class="item.type === 0 ? 'badge-lost' : 'badge-found'">
              {{ item.type === 0 ? '失物' : '招领' }}
            </div>
          </div>
          <div class="item-card-body">
            <div class="item-card-tags">
              <el-tag size="small" type="info" effect="plain" round>{{ item.category }}</el-tag>
              <el-tag v-if="item.status === 3" size="small" type="warning" effect="plain" round>已认领</el-tag>
            </div>
            <h3 class="item-card-title">{{ item.title }}</h3>
            <div class="item-card-meta">
              <span>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                {{ item.location || '未知' }}
              </span>
              <span>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                {{ item.createdAt?.substring(0, 10) }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && latestItems.length === 0" description="暂无数据" />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getItems, getStats } from '@/api'

const stats = ref({ lost: 0, found: 0, claimed: 0 })
const latestItems = ref([])
const loading = ref(false)
const categories = [
  { name: '证件', icon: '🪪' },
  { name: '电子产品', icon: '📱' },
  { name: '书籍', icon: '📚' },
  { name: '衣物', icon: '👕' },
  { name: '其他', icon: '📦' },
]

onMounted(async () => {
  loading.value = true
  try {
    // 获取统计数据（全部数据）
    const statsRes = await getStats()
    stats.value = statsRes.data || { lost: 0, found: 0, claimed: 0 }
    
    // 获取最新物品列表
    const res = await getItems({ page: 1, size: 8 })
    latestItems.value = res.data?.records || []
  } catch {} finally { loading.value = false }
})
</script>

<style scoped>
/* Hero */
.hero {
  position: relative;
  border-radius: 24px;
  padding: 64px 48px;
  overflow: hidden;
  margin-bottom: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
  min-height: 380px;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #10b981 0%, #14b8a6 40%, #06b6d4 70%, #22d3ee 100%);
  z-index: 0;
}
.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  background: white;
}
.hero-shape-1 { width: 300px; height: 300px; top: -80px; right: -60px; }
.hero-shape-2 { width: 200px; height: 200px; bottom: -60px; left: 10%; }
.hero-shape-3 { width: 150px; height: 150px; top: 20%; right: 30%; opacity: 0.08; }

.hero-content { position: relative; z-index: 1; flex: 1; }
.hero-badge {
  display: inline-block;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(10px);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
}
.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: white;
  line-height: 1.2;
  margin-bottom: 16px;
}
.gradient-text {
  background: linear-gradient(135deg, #fde68a, #fbbf24);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-desc {
  font-size: 16px;
  color: rgba(255,255,255,0.85);
  margin-bottom: 28px;
  max-width: 480px;
  line-height: 1.7;
}
.hero-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.btn-success {
  background: linear-gradient(135deg, #10b981, #34d399) !important;
  border: none !important;
  color: white !important;
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.35) !important;
  font-weight: 600 !important;
}
.btn-success:hover {
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5) !important;
  transform: translateY(-1px) !important;
}

/* Floating cards */
.hero-illustration {
  position: relative;
  z-index: 1;
  width: 280px;
  height: 260px;
  flex-shrink: 0;
}
.float-card {
  position: absolute;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-radius: 14px;
  padding: 12px 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  white-space: nowrap;
}
.float-card-1 { top: 0; left: 0; animation: float 4s ease-in-out infinite; }
.float-card-2 { top: 90px; right: 0; animation: float 4s ease-in-out infinite 1s; }
.float-card-3 { bottom: 20px; left: 20px; animation: float 4s ease-in-out infinite 2s; }
.float-emoji { font-size: 22px; }

/* Stats */
.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}
.stat-card {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  transition: var(--transition);
}
.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.stat-icon {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon-lost { background: #fef2f2; color: #ef4444; }
.stat-icon-found { background: #ecfdf5; color: #10b981; }
.stat-icon-claimed { background: #eff6ff; color: #3b82f6; }
.stat-info { display: flex; flex-direction: column; }
.stat-number { font-size: 28px; font-weight: 800; color: var(--text-primary); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--text-muted); font-weight: 500; }

/* Category */
.category-section { margin-bottom: 40px; }
.section-title {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}
.title-icon { font-size: 22px; }
.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}
.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 16px;
  background: white;
  border-radius: var(--radius);
  border: 1px solid var(--border);
  cursor: pointer;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
}
.category-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
  border-color: var(--primary-light);
}
.category-emoji { font-size: 32px; }
.category-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }

/* Latest Items */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.view-all-btn {
  color: white !important;
}
.view-all-btn:hover {
  color: white !important;
}
.items-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.item-card {
  background: white;
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
}
.item-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}
.item-card-img {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
}
.item-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.item-card:hover .item-card-img img { transform: scale(1.05); }
.item-card-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}
.item-type-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(8px);
}
.badge-lost { background: rgba(239,68,68,0.9); color: white; }
.badge-found { background: rgba(16,185,129,0.9); color: white; }
.item-card-body { padding: 16px; }
.item-card-tags { display: flex; gap: 6px; margin-bottom: 10px; flex-wrap: wrap; }
.item-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-card-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.item-card-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .hero { padding: 40px 24px; flex-direction: column; text-align: center; }
  .hero-title { font-size: 28px; }
  .hero-actions { justify-content: center; }
  .hero-illustration { display: none; }
  .stats-section { grid-template-columns: 1fr; }
  .category-grid { grid-template-columns: repeat(3, 1fr); }
  .items-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
