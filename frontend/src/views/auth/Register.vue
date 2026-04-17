<template>
  <div class="auth-page">
    <div class="auth-container animate-fade-in-up">
      <div class="auth-left">
        <div class="auth-visual">
          <div class="visual-shape visual-shape-1"></div>
          <div class="visual-shape visual-shape-2"></div>
          <div class="visual-content">
            <span class="visual-emoji">🎓</span>
            <h2>加入我们</h2>
            <p>注册账号后即可发布和管理<br/>您的失物招领信息</p>
          </div>
        </div>
      </div>
      <div class="auth-right">
        <div class="auth-form-wrapper">
          <div class="auth-header">
            <h2>用户注册</h2>
            <p>创建您的账号，开始使用平台</p>
          </div>
          <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="3-20位字符" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="6-20位字符" show-password prefix-icon="Lock" />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号">
                  <el-input v-model="form.phone" placeholder="可选" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="可选" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%;height:44px" round>
                注册
              </el-button>
            </el-form-item>
          </el-form>
          <div class="auth-footer">
            <span>已有账号？</span>
            <router-link to="/login" class="auth-link">去登录 →</router-link>
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
import { register } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '', email: '', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 20, message: '3-20位字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '6-20位字符', trigger: 'blur' }]
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
.auth-left { width: 380px; flex-shrink: 0; }
.auth-visual {
  height: 100%;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 50%, #4f46e5 100%);
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.visual-shape { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.1); }
.visual-shape-1 { width: 200px; height: 200px; top: -50px; right: -50px; }
.visual-shape-2 { width: 150px; height: 150px; bottom: -30px; left: -30px; }
.visual-content { text-align: center; color: white; position: relative; z-index: 1; }
.visual-emoji { font-size: 56px; display: block; margin-bottom: 20px; }
.visual-content h2 { font-size: 26px; font-weight: 700; margin-bottom: 12px; }
.visual-content p { font-size: 14px; opacity: 0.85; line-height: 1.7; }
.auth-right { flex: 1; display: flex; align-items: center; padding: 40px 48px; }
.auth-form-wrapper { width: 100%; }
.auth-header { margin-bottom: 28px; }
.auth-header h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px; }
.auth-header p { font-size: 14px; color: var(--text-muted); }
.auth-footer { text-align: center; padding-top: 16px; font-size: 14px; color: var(--text-muted); }
.auth-link { color: var(--primary) !important; font-weight: 600; margin-left: 4px; }

@media (max-width: 768px) {
  .auth-left { display: none; }
  .auth-right { padding: 32px 24px; }
}
</style>
