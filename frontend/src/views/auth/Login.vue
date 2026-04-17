<template>
  <div class="auth-page">
    <div class="auth-container animate-fade-in-up">
      <div class="auth-left">
        <div class="auth-visual">
          <div class="visual-shape visual-shape-1"></div>
          <div class="visual-shape visual-shape-2"></div>
          <div class="visual-content">
            <span class="visual-emoji">🔐</span>
            <h2>欢迎回来</h2>
            <p>登录后可发布失物/招领信息<br/>帮助失物快速找到主人</p>
          </div>
        </div>
      </div>
      <div class="auth-right">
        <div class="auth-form-wrapper">
          <div class="auth-header">
            <h2>用户登录</h2>
            <p>请输入您的账号和密码</p>
          </div>
          <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%;height:44px" round>
                登录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="auth-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="auth-link">立即注册 →</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res.data.token, res.data.user)
    ElMessage.success('登录成功')
    router.push('/')
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 20px;
}
.auth-container {
  display: flex;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  max-width: 840px;
  width: 100%;
  border: 1px solid rgba(226, 232, 240, 0.5);
}
.auth-left {
  width: 380px;
  flex-shrink: 0;
}
.auth-visual {
  height: 100%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%);
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.visual-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
}
.visual-shape-1 { width: 200px; height: 200px; top: -50px; right: -50px; }
.visual-shape-2 { width: 150px; height: 150px; bottom: -30px; left: -30px; }
.visual-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}
.visual-emoji { font-size: 56px; display: block; margin-bottom: 20px; }
.visual-content h2 { font-size: 26px; font-weight: 700; margin-bottom: 12px; }
.visual-content p { font-size: 14px; opacity: 0.85; line-height: 1.7; }
.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 48px;
}
.auth-form-wrapper { width: 100%; }
.auth-header { margin-bottom: 32px; }
.auth-header h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px; }
.auth-header p { font-size: 14px; color: var(--text-muted); }
.auth-footer {
  text-align: center;
  padding-top: 20px;
  font-size: 14px;
  color: var(--text-muted);
}
.auth-link {
  color: var(--primary) !important;
  font-weight: 600;
  margin-left: 4px;
}

@media (max-width: 768px) {
  .auth-left { display: none; }
  .auth-right { padding: 32px 24px; }
}
</style>
