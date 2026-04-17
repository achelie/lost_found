<template>
  <div class="profile-page animate-fade-in-up">
    <div class="page-header">
      <h1 class="page-title">👤 个人中心</h1>
      <p class="page-desc">管理您的个人信息</p>
    </div>

    <div class="profile-layout">
      <!-- Sidebar -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <div class="big-avatar">{{ form.nickname?.charAt(0) || form.username?.charAt(0) || 'U' }}</div>
          <h3>{{ form.nickname || form.username }}</h3>
          <p>@{{ form.username }}</p>
        </div>
        <div class="sidebar-nav">
          <router-link to="/profile" class="sidebar-link active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            个人资料
          </router-link>
          <router-link to="/my-items" class="sidebar-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect width="8" height="4" x="8" y="2" rx="1" ry="1"/></svg>
            我的发布
          </router-link>
          <router-link to="/notifications" class="sidebar-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
            消息通知
          </router-link>
        </div>
      </div>

      <!-- Form -->
      <div class="profile-form" v-loading="loading">
        <h2>编辑资料</h2>
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="用户名">
            <el-input :value="form.username" disabled />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="设置昵称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="设置手机号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="设置邮箱" />
          </el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="handleSave" :loading="saving" round size="large">保存修改</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const form = reactive({ username: '', nickname: '', email: '', phone: '' })

onMounted(async () => {
  loading.value = true
  try {
    const res = await getProfile()
    Object.assign(form, res.data)
  } finally { loading.value = false }
})

const handleSave = async () => {
  saving.value = true
  try {
    await updateProfile({ nickname: form.nickname, email: form.email, phone: form.phone })
    userStore.user = { ...userStore.user, nickname: form.nickname }
    ElMessage.success('修改成功')
  } finally { saving.value = false }
}
</script>

<style scoped>
.page-header { margin-bottom: 28px; }
.page-title { font-size: 24px; font-weight: 800; }
.page-desc { font-size: 14px; color: var(--text-muted); margin-top: 4px; }

.profile-layout {
  display: flex;
  gap: 28px;
}
.profile-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: white;
  border-radius: 24px;
  padding: 28px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  height: fit-content;
}
.avatar-section { text-align: center; margin-bottom: 24px; }
.big-avatar {
  width: 72px; height: 72px;
  background: var(--primary-gradient);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: white;
  font-size: 28px;
  font-weight: 700;
  margin: 0 auto 12px;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.3);
}
.avatar-section h3 { font-size: 16px; font-weight: 700; color: var(--text-primary); }
.avatar-section p { font-size: 13px; color: var(--text-muted); }
.sidebar-nav { display: flex; flex-direction: column; gap: 4px; }
.sidebar-link {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px; font-weight: 500;
  color: var(--text-secondary);
  transition: var(--transition);
}
.sidebar-link:hover { background: rgba(99, 102, 241, 0.06); color: var(--primary); }
.sidebar-link.active { background: rgba(99, 102, 241, 0.1); color: var(--primary); font-weight: 600; }

.profile-form {
  flex: 1;
  background: white;
  border-radius: 24px;
  padding: 36px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
}
.profile-form h2 { font-size: 18px; font-weight: 700; margin-bottom: 24px; }
.form-actions { padding-top: 16px; border-top: 1px solid var(--border); }

@media (max-width: 768px) {
  .profile-layout { flex-direction: column; }
  .profile-sidebar { width: 100%; }
}
</style>
