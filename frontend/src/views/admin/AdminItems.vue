<template>
  <div class="admin-page animate-fade-in-up">

    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝理由" width="400px">
      <el-form :model="rejectForm" label-position="top">
        <el-form-item label="请输入拒绝理由">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请说明拒绝的原因..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
    <div class="admin-header">
      <div>
        <h1 class="page-title">⚙️ 管理后台 — 帖子管理</h1>
        <p class="page-desc">审核发布的失物招领信息，管理违规内容</p>
      </div>
      <div class="admin-nav">
        <router-link to="/admin/items" class="admin-tab active">帖子管理</router-link>
        <router-link to="/admin/users" class="admin-tab">用户管理</router-link>
      </div>
    </div>

    <div class="table-wrapper" v-loading="loading">
      <el-table :data="items" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type===0?'danger':'success'" size="small" round>{{ row.type===0?'失物':'招领' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="160" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small" round>{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" round @click="$router.push(`/items/${row.id}`)">查看</el-button>
            <el-button v-if="row.status===0" type="success" size="small" round @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button v-if="row.status===0" type="warning" size="small" round @click="openRejectDialog(row.id)">拒绝</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small" round>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrapper" v-if="total>0">
      <el-pagination :current-page="page" :page-size="10" :total="total" background
        layout="prev,pager,next" @current-change="p=>{page=p;fetch()}" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminGetItems, adminAuditItem, adminDeleteItem } from '@/api'

const items = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const rejectDialogVisible = ref(false)
const currentAuditId = ref(null)
const rejectForm = ref({
  rejectReason: ''
})
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
    const res = await adminGetItems({ page: page.value, size: 10 })
    items.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleAudit = async (id, status, rejectReason = '') => {
  await adminAuditItem(id, { status, rejectReason })
  ElMessage.success('操作成功')
  fetch()
}

const openRejectDialog = (id) => {
  currentAuditId.value = id
  rejectForm.value.rejectReason = ''
  rejectDialogVisible.value = true
}

const confirmReject = () => {
  rejectDialogVisible.value = false
  handleAudit(currentAuditId.value, 2, rejectForm.value.rejectReason)
}
const handleDelete = async (id) => {
  await adminDeleteItem(id)
  ElMessage.success('删除成功')
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.admin-header { margin-bottom: 24px; display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 16px; }
.page-title { font-size: 24px; font-weight: 800; }
.page-desc { font-size: 14px; color: var(--text-muted); margin-top: 4px; }
.admin-nav { display: flex; gap: 4px; background: #f1f5f9; padding: 4px; border-radius: 12px; }
.admin-tab {
  padding: 8px 20px; border-radius: 10px; font-size: 14px; font-weight: 500;
  color: var(--text-secondary); transition: var(--transition);
}
.admin-tab:hover { color: var(--primary); }
.admin-tab.active { background: white; color: var(--primary); font-weight: 600; box-shadow: var(--shadow-sm); }
.table-wrapper {
  background: white; border-radius: var(--radius); padding: 4px;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border); overflow: hidden;
}
.pagination-wrapper { display: flex; justify-content: center; padding: 20px 0; }
</style>
