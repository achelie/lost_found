<template>
  <div class="my-items-page animate-fade-in-up">
    <div class="page-header">
      <h1 class="page-title">📋 我的发布</h1>
      <p class="page-desc">管理您发布的失物和招领信息</p>
    </div>

    <div class="table-wrapper" v-loading="loading">
      <el-table :data="items" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type===0?'danger':'success'" size="small" round>{{ row.type===0?'失物':'招领' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="180" />
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small" round>{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" round @click="$router.push(`/items/${row.id}`)">查看</el-button>
            <el-button v-if="row.status===1" size="small" type="primary" round @click="$router.push(`/claims/${row.id}`)">认领申请</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-if="!loading && items.length===0" description="您还没有发布任何信息" />
    <div class="pagination-wrapper" v-if="total>0">
      <el-pagination :current-page="page" :page-size="10" :total="total" background
        layout="prev,pager,next" @current-change="p=>{page=p;fetch()}" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyItems } from '@/api'

const items = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已认领', type: 'info' },
  4: { label: '已关闭', type: 'info' }
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await getMyItems({ page: page.value, size: 10 })
    items.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

onMounted(fetch)
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; }
.page-desc { font-size: 14px; color: var(--text-muted); margin-top: 4px; }
.table-wrapper {
  background: white;
  border-radius: var(--radius);
  padding: 4px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  overflow: hidden;
}
.pagination-wrapper { display: flex; justify-content: center; padding: 20px 0; }
</style>
