<template>
  <div class="publish-page animate-fade-in-up">
    <div class="page-header">
      <h1 class="page-title">📝 发布信息</h1>
      <p class="page-desc">填写物品信息，帮助失物找到主人</p>
    </div>

    <div class="publish-container">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large">
        <!-- Type Selection -->
        <div class="type-selection">
          <div class="type-card" :class="{ active: form.type === 0 }" @click="form.type = 0">
            <span class="type-emoji">🔍</span>
            <span class="type-label">我丢了东西</span>
            <span class="type-desc">发布失物信息</span>
          </div>
          <div class="type-card" :class="{ active: form.type === 1 }" @click="form.type = 1">
            <span class="type-emoji">📦</span>
            <span class="type-label">我捡到东西</span>
            <span class="type-desc">发布招领信息</span>
          </div>
        </div>

        <div class="form-grid">
          <el-form-item label="物品标题" prop="title" class="span-2">
            <el-input v-model="form.title" placeholder="如：黑色钱包、iPhone 15等" />
          </el-form-item>
          <el-form-item label="物品分类" prop="category">
            <el-select v-model="form.category" placeholder="选择分类" style="width:100%">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="地点">
            <el-input v-model="form.location" placeholder="如：图书馆三楼" />
          </el-form-item>
          <el-form-item label="时间">
            <el-date-picker v-model="form.itemTime" type="datetime" placeholder="丢失/拾到时间" format="YYYY-MM-DD HH:mm" style="width:100%" />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="form.contact" placeholder="手机号/微信/QQ" />
          </el-form-item>
          <el-form-item label="详细描述" class="span-2">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述物品特征，方便寻找" />
          </el-form-item>
          <el-form-item label="上传图片" class="span-2">
            <el-upload action="/api/file/upload" :headers="uploadHeaders" :on-success="handleUploadSuccess"
              list-type="picture-card" :limit="5" :file-list="fileList" accept="image/*">
              <div class="upload-trigger">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" x2="12" y1="3" y2="15"/></svg>
                <span>上传图片</span>
              </div>
            </el-upload>
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button @click="$router.back()" size="large" round>取消</el-button>
          <el-button type="primary" @click="handlePublish" :loading="loading" size="large" round>
            发布信息
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publishItem } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const fileList = ref([])
const imageUrls = ref([])
const categories = ['证件', '电子产品', '书籍', '衣物', '其他']
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const form = reactive({ type: 0, title: '', category: '', location: '', itemTime: null, description: '', contact: '' })
const rules = {
  type: [{ required: true, message: '请选择类型' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类' }]
}

const handleUploadSuccess = (res) => { if (res.code === 200) imageUrls.value.push(res.data) }

const handlePublish = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await publishItem({
      ...form,
      itemTime: form.itemTime ? dayjs(form.itemTime).format('YYYY-MM-DDTHH:mm:ss') : null,
      images: imageUrls.value.join(',')
    })
    ElMessage.success('发布成功，等待审核')
    router.push('/my-items')
  } finally { loading.value = false }
}
</script>

<style scoped>
.page-header { margin-bottom: 28px; }
.page-title { font-size: 28px; font-weight: 800; }
.page-desc { font-size: 14px; color: var(--text-muted); margin-top: 4px; }

.publish-container {
  background: white;
  border-radius: 24px;
  padding: 36px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border);
  max-width: 760px;
}

.type-selection {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 28px;
}
.type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 24px;
  border-radius: 16px;
  border: 2px solid var(--border);
  cursor: pointer;
  transition: var(--transition);
  background: #fafbfc;
}
.type-card:hover { border-color: var(--primary-light); background: #f0f0ff; }
.type-card.active {
  border-color: var(--primary);
  background: linear-gradient(135deg, #eff0ff, #e8e0ff);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
.type-emoji { font-size: 32px; }
.type-label { font-size: 15px; font-weight: 700; color: var(--text-primary); }
.type-desc { font-size: 12px; color: var(--text-muted); }

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}
.span-2 { grid-column: span 2; }

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 12px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
  margin-top: 8px;
}

@media (max-width: 768px) {
  .type-selection, .form-grid { grid-template-columns: 1fr; }
  .span-2 { grid-column: span 1; }
  .publish-container { padding: 24px; }
}
</style>
