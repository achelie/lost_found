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
    <div class="claim-container">
      <div class="tip-box">
        <span class="tip-icon">💡</span>
        <div>
          <strong>温馨提示</strong>
          <p>{{ tipText }}</p>
        </div>
      </div>
      <el-form :model="form" ref="formRef" label-position="top" size="large">
        <el-form-item label="证明信息" prop="proof" :rules="[{required:true,message:'请填写证明'}]">
          <el-input v-model="form.proof" type="textarea" :rows="5" :placeholder="item?.type === 0 ? '请描述您发现的线索信息' : '请描述您是物品主人的证据，如物品特征、购买凭证等'" />
        </el-form-item>
        <el-form-item label="证明图片（可选）">
          <el-upload action="/api/file/upload" :headers="uploadHeaders" :on-success="handleUpload"
            list-type="picture-card" :limit="3" accept="image/*">
            <div class="upload-trigger">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" x2="12" y1="3" y2="15"/></svg>
              <span>上传</span>
            </div>
          </el-upload>
        </el-form-item>
        <div class="form-actions">
          <el-button @click="$router.back()" size="large" round>取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading" size="large" round>{{ buttonText }}</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitClaim, getItemDetail } from '@/api'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const item = ref(null)
const proofImages = ref([])
const form = reactive({ proof: '' })
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const pageTitle = computed(() => {
  return item.value?.type === 0 ? '💡 提供线索' : '✋ 提交认领申请'
})

const pageDesc = computed(() => {
  return item.value?.type === 0 ? '请描述您发现的线索信息帮助失主找回物品' : '请提供证明信息以便发布者核实'
})

const tipText = computed(() => {
  return item.value?.type === 0 
    ? '请尽可能详细的描述您发现的相关线索，包括时间、地点、物品特征等信息' 
    : '请提供尽可能详细的物品描述或购买凭证，帮助发布者验证您的身份'
})

const buttonText = computed(() => {
  return item.value?.type === 0 ? '提交线索' : '提交申请'
})

const successMessage = computed(() => {
  return item.value?.type === 0 ? '线索已提交成功' : '认领申请已提交'
})

const handleUpload = (res) => { if (res.code === 200) proofImages.value.push(res.data) }

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await submitClaim({ itemId: Number(route.params.itemId), proof: form.proof, proofImages: proofImages.value.join(',') })
    ElMessage.success(successMessage.value)
    router.back()
  } finally { loading.value = false }
}

onMounted(async () => {
  const res = await getItemDetail(route.params.itemId)
  item.value = res.data
})
</script>

<style scoped>
.back-btn { margin-bottom: 12px; font-weight: 500; color: var(--text-secondary) !important; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; }
.page-desc { font-size: 14px; color: var(--text-muted); margin-top: 4px; }
.claim-container {
  background: white;
  border-radius: 24px;
  padding: 36px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border);
  max-width: 640px;
}
.tip-box {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #eff6ff;
  border-radius: 14px;
  margin-bottom: 28px;
}
.tip-icon { font-size: 24px; }
.tip-box strong { font-size: 14px; color: #1e40af; display: block; margin-bottom: 4px; }
.tip-box p { font-size: 13px; color: #3b82f6; line-height: 1.5; }
.upload-trigger {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  color: var(--text-muted); font-size: 12px;
}
.form-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  padding-top: 16px; border-top: 1px solid var(--border);
}
</style>
