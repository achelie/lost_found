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

    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索用户名/昵称/邮箱/手机号"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 140px" @change="handleSearch">
        <el-option :value="1" label="正常" />
        <el-option :value="0" label="禁用" />
      </el-select>
      <el-select v-model="filters.role" placeholder="全部角色" clearable style="width: 140px" @change="handleSearch">
        <el-option :value="0" label="普通用户" />
        <el-option :value="1" label="管理员" />
      </el-select>
      <el-button type="primary" round @click="handleSearch">查询</el-button>
      <el-button round @click="handleReset">重置</el-button>
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
        <el-table-column label="操作" width="420">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                size="small"
                :type="row.status===1 ? 'warning' : 'success'"
                round
                @click="handleToggleStatus(row)"
              >
                {{ row.status===1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                :type="row.role===1 ? 'info' : 'primary'"
                round
                @click="handleToggleRole(row)"
              >
                {{ row.role===1 ? '设为用户' : '设为管理员' }}
              </el-button>
              <el-button size="small" round @click="openPasswordDialog(row)">修改密码</el-button>
              <el-popconfirm title="确认删除该用户？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button type="danger" size="small" round>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrapper" v-if="total>0">
      <el-pagination :current-page="page" :page-size="10" :total="total" background
        layout="prev,pager,next" @current-change="p=>{page=p;fetch()}" />
    </div>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="460px"
      destroy-on-close
      @closed="resetPasswordForm"
    >
      <div v-if="currentUser" style="margin-bottom: 12px; color: var(--text-muted);">
        为用户 {{ currentUser.nickname || currentUser.username }} 修改登录密码
      </div>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="92px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminGetUsers,
  adminDeleteUser,
  adminUpdateUserStatus,
  adminUpdateUserRole,
  adminChangeUserPassword
} from '@/api'

const users = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const filters = ref({ keyword: '', status: null, role: null })
const passwordDialogVisible = ref(false)
const changingPassword = ref(false)
const currentUser = ref(null)
const passwordFormRef = ref()
const passwordForm = reactive({ newPassword: '', confirmPassword: '' })

const validateConfirmPassword = (_, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '新密码长度需在6-50位之间', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminGetUsers({
      page: page.value,
      size: 10,
      keyword: filters.value.keyword,
      status: filters.value.status,
      role: filters.value.role
    })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => {
  page.value = 1
  fetch()
}

const handleReset = () => {
  filters.value = { keyword: '', status: null, role: null }
  handleSearch()
}

const handleToggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  await adminUpdateUserStatus(row.id, targetStatus)
  ElMessage.success(targetStatus === 1 ? '用户已启用' : '用户已禁用')
  fetch()
}

const handleToggleRole = async (row) => {
  const targetRole = row.role === 1 ? 0 : 1
  await adminUpdateUserRole(row.id, targetRole)
  ElMessage.success(targetRole === 1 ? '已设为管理员' : '已设为普通用户')
  fetch()
}

const openPasswordDialog = (row) => {
  currentUser.value = row
  passwordDialogVisible.value = true
}

const resetPasswordForm = () => {
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  currentUser.value = null
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value || !currentUser.value) return

  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPassword.value = true
  try {
    await adminChangeUserPassword(currentUser.value.id, { newPassword: passwordForm.newPassword })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } finally {
    changingPassword.value = false
  }
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
.filter-bar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
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
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}
.pagination-wrapper { display: flex; justify-content: center; padding: 20px 0; }
</style>
