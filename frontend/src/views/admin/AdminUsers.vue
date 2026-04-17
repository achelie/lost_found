<template>
  <div class="admin-page animate-fade-in-up">
    <div class="admin-header">
      <div>
        <h1 class="page-title">⚙️ 管理后台 — 用户管理</h1>
        <p class="page-desc">管理平台注册用户</p>
      </div>
      <div class="admin-nav">
        <router-link to="/admin/items" class="admin-tab">帖子管理</router-link>
        <router-link to="/admin/users" class="admin-tab active">用户管理</router-link>
      </div>
    </div>

    <div class="table-wrapper" v-loading="loading">
      <el-table :data="users" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:10px">
              <div class="mini-avatar">{{ row.nickname?.charAt(0) || row.username?.charAt(0) || 'U' }}</div>
              <div>
                <div style="font-weight:600;font-size:14px">{{ row.nickname || row.username }}</div>
                <div style="font-size:12px;color:var(--text-muted)">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role===1?'':'info'" size="small" round>{{ row.role===1?'管理员':'用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small" round>{{ row.status===1?'正常':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认删除该用户？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" round>
                  删除
                </el-button>
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
import { adminGetUsers, adminDeleteUser } from '@/api'

const users = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminGetUsers({ page: page.value, size: 10 })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleDelete = async (row) => {
  try {
    await adminDeleteUser(row.id)
    ElMessage.success('删除成功')
    fetch()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
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
.mini-avatar {
  width: 36px; height: 36px;
  background: var(--primary-gradient);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: white; font-weight: 600; font-size: 14px;
  flex-shrink: 0;
}
.table-wrapper {
  background: white; border-radius: var(--radius); padding: 4px;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border); overflow: hidden;
}
.pagination-wrapper { display: flex; justify-content: center; padding: 20px 0; }
</style>
