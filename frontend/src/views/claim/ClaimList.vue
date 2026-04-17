<template>
  <div class="claim-page animate-fade-in-up">
    <el-button text @click="$router.back()" class="back-btn">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
      返回
    </el-button>
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <p class="page-desc">{{ pageDesc }}</p>
    </div>
    <div class="table-wrapper">
      <el-table :data="claims" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="proof" label="证明信息" show-overflow-tooltip min-width="200" />
        <el-table-column label="证明图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.proofImages" :src="row.proofImages.split(',')[0]" :preview-src-list="row.proofImages.split(',')"
              fit="cover" style="width:48px;height:48px;border-radius:8px" />
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status===0?'warning':row.status===1?'success':'danger'" round>
              {{ row.status===0?'待审核':row.status===1?'已通过':'已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" round @click="openViewDialog(row)">🔍 查看</el-button>
            <template v-if="row.status === 0">
              <el-button type="success" size="small" round @click="handleAudit(row.id, 1)">✅ 通过</el-button>
              <el-button type="danger" size="small" round @click="handleAudit(row.id, 2)">❌ 拒绝</el-button>
            </template>
            <template v-else>
              <span style="color:var(--text-muted);font-size:13px;margin-left: 8px">{{ row.remark || '-' }}</span>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="pagination-wrapper" v-if="total>0">
      <el-pagination :current-page="page" :page-size="10" :total="total" background
        layout="prev,pager,next" @current-change="p=>{page=p;fetchClaims()}" />
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" :title="dialogTitle" width="600px" draggable top="5vh" append-to-body :z-index="9999">
      <div v-if="currentClaim">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID" span="1">{{ currentClaim.id }}</el-descriptions-item>
          <el-descriptions-item label="申请状态" span="1">
            <el-tag :type="currentClaim.status===0?'warning':currentClaim.status===1?'success':'danger'" round>
              {{ currentClaim.status===0?'待审核':currentClaim.status===1?'已通过':'已拒绝' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请人" span="1">{{ currentClaim.userName || `用户 #${currentClaim.userId}` || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" span="1">{{ currentClaim.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="证明信息" span="2">{{ currentClaim.proof }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px; max-height: 280px; overflow-y: auto; padding-right: 8px" v-if="currentClaim.proofImages">
          <h4 style="margin-bottom: 12px; position: sticky; top: 0; background: white; padding: 4px 0">证明图片：</h4>
          <div style="display: flex; gap: 12px; flex-wrap: wrap">
            <el-image
              v-for="(img, index) in currentClaim.proofImages.split(',')"
              :key="index"
              :src="img"
              :preview-src-list="currentClaim.proofImages.split(',')"
              fit="cover"
              style="width: 120px; height: 120px; border-radius: 8px; border: 1px solid var(--border)"
            />
          </div>
        </div>

        <div v-if="currentClaim.status !== 0" style="margin-top: 20px">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="审核备注">{{ currentClaim.remark || '无备注' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getItemClaims, auditClaim, getItemDetail } from '@/api'

const route = useRoute()
const claims = ref([])
const item = ref(null)
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const viewDialogVisible = ref(false)
const currentClaim = ref(null)

const pageTitle = computed(() => {
  return item.value?.type === 0 ? '📋 收到的线索列表' : '📋 认领申请列表'
})

const pageDesc = computed(() => {
  return item.value?.type === 0 ? '查看用户提供的线索信息' : '审核用户提交的认领申请'
})

const dialogTitle = computed(() => {
  return item.value?.type === 0 ? '线索详情' : '认领申请详情'
})

const fetchClaims = async () => {
  loading.value = true
  try {
    const res = await getItemClaims(route.params.itemId, { page: page.value, size: 10 })
    claims.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleAudit = async (claimId, status) => {
  await auditClaim(claimId, { status })
  ElMessage.success('操作成功')
  fetchClaims()
}

const openViewDialog = (row) => {
  currentClaim.value = row
  viewDialogVisible.value = true
}

onMounted(async () => {
  const itemRes = await getItemDetail(route.params.itemId)
  item.value = itemRes.data
  fetchClaims()
})

// 监听路由参数变化，当跳转到不同的物品时重新加载数据
watch(() => route.params.itemId, async (newItemId) => {
  if (newItemId) {
    page.value = 1
    const itemRes = await getItemDetail(newItemId)
    item.value = itemRes.data
    fetchClaims()
  }
})
</script>

<style scoped>
.back-btn { margin-bottom: 12px; font-weight: 500; color: var(--text-secondary) !important; }
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
.text-muted { color: var(--text-muted); font-size: 13px; }
.pagination-wrapper { display: flex; justify-content: center; padding: 20px 0; }
</style>
