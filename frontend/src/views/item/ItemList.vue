<template>
  <div class="item-list-page animate-fade-in-up">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-desc">浏览校园失物与招领信息，帮助物品快速找到主人</p>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar glass-card">
      <div class="filter-row">
        <div class="filter-group">
          <span class="filter-label">类型</span>
          <el-radio-group v-model="filters.type" @change="fetchItems" size="default">
            <el-radio-button :value="null">全部</el-radio-button>
            <el-radio-button :value="0">🔍 失物</el-radio-button>
            <el-radio-button :value="1">📦 招领</el-radio-button>
          </el-radio-group>
        </div>
        <div class="filter-group">
          <span class="filter-label">分类</span>
          <el-select v-model="filters.category" placeholder="全部分类" clearable @change="fetchItems" style="width:150px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </div>
        <div class="filter-group search-group">
          <el-input v-model="filters.keyword" placeholder="搜索物品名称、地点…" clearable @keyup.enter="fetchItems" class="search-input">
            <template #prefix>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
            </template>
          </el-input>
          <el-button type="primary" @click="fetchItems" round>搜索</el-button>
        </div>
      </div>
    </div>

    <!-- Items Grid -->
    <div class="items-grid stagger-children" v-loading="loading">
      <div class="item-card" v-for="item in items" :key="item.id" @click="$router.push(`/items/${item.id}`)">
        <div class="item-card-img" v-if="item.images">
          <img :src="item.images.split(',')[0]" alt="" loading="lazy" />
          <div class="item-type-badge" :class="item.type === 0 ? 'badge-lost' : 'badge-found'">
            {{ item.type === 0 ? '失物' : '招领' }}
          </div>
        </div>
        <div class="item-card-img item-card-placeholder" v-else>
          <span style="font-size:40px">{{ item.type === 0 ? '🔍' : '📦' }}</span>
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

    <el-empty v-if="!loading && items.length === 0" description="暂无数据，试试调整筛选条件" class="empty-state" />

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        :current-page="filters.page"
        :page-size="12"
        :total="total"
        layout="prev, pager, next"
        @current-change="p => { filters.page = p; fetchItems() }"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getItems } from '@/api'

const route = useRoute()
const items = ref([])
const loading = ref(false)
const total = ref(0)
const categories = ['证件', '电子产品', '书籍', '衣物', '其他']
const filters = reactive({
  type: route.query.type != null ? Number(route.query.type) : null,
  category: route.query.category || '',
  keyword: '',
  page: 1
})

const title = computed(() => {
  if (filters.type === 0) return '失物信息'
  if (filters.type === 1) return '招领信息'
  return '全部信息'
})

const fetchItems = async () => {
  loading.value = true
  try {
    const res = await getItems({ page: filters.page, size: 12, type: filters.type, category: filters.category, keyword: filters.keyword })
    items.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

onMounted(fetchItems)
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 28px; font-weight: 800; color: var(--text-primary); margin-bottom: 6px; }
.page-desc { font-size: 14px; color: var(--text-muted); }

.filter-bar {
  padding: 20px 24px;
  margin-bottom: 28px;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-label { font-size: 13px; font-weight: 600; color: var(--text-secondary); white-space: nowrap; }
.search-group { flex: 1; min-width: 260px; }
.search-input { flex: 1; }

.items-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
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
.item-card-img img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.item-card:hover .item-card-img img { transform: scale(1.05); }
.item-card-placeholder { display: flex; align-items: center; justify-content: center; }
.item-type-badge {
  position: absolute;
  top: 12px; left: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(8px);
}
.badge-lost { background: rgba(239,68,68,0.9); color: white; }
.badge-found { background: rgba(16,185,129,0.9); color: white; }
.item-card-body { padding: 16px; }
.item-card-tags { display: flex; gap: 6px; margin-bottom: 10px; }
.item-card-title {
  font-size: 15px; font-weight: 600; color: var(--text-primary);
  margin-bottom: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.item-card-meta { display: flex; flex-direction: column; gap: 4px; }
.item-card-meta span {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: var(--text-muted);
}
.empty-state { padding: 60px 0; }
.pagination-wrapper { display: flex; justify-content: center; padding: 16px 0; }

@media (max-width: 1024px) { .items-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .items-grid { grid-template-columns: repeat(2, 1fr); } .filter-row { flex-direction: column; align-items: stretch; } }
@media (max-width: 480px) { .items-grid { grid-template-columns: 1fr; } }
</style>
