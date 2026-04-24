<template>
  <div class="my-items-page animate-fade-in-up">
    <div class="page-header">
      <h1 class="page-title">📋 我的发布</h1>
      <p class="page-desc">管理您发布的失物和招领信息</p>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索标题/描述/地点/联系方式"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 140px" @change="handleSearch">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="已认领" :value="3" />
        <el-option label="已关闭" :value="4" />
      </el-select>
      <el-select v-model="filters.type" placeholder="全部类型" clearable style="width: 140px" @change="handleSearch">
        <el-option label="失物" :value="0" />
        <el-option label="招领" :value="1" />
      </el-select>
      <el-select v-model="filters.category" placeholder="全部分类" clearable style="width: 150px" @change="handleSearch">
        <el-option v-for="c in categories" :key="c" :value="c" :label="c" />
      </el-select>
      <el-button type="primary" round @click="handleSearch">查询</el-button>
      <el-button round @click="handleReset">重置</el-button>
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
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" round @click="$router.push(`/items/${row.id}`)">查看</el-button>
              <el-button size="small" type="primary" round @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" round @click="handleDelete(row)">删除</el-button>
              <el-button v-if="row.status===1" size="small" type="primary" round @click="$router.push(`/claims/${row.id}`)">认领申请</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-if="!loading && items.length===0" description="您还没有发布任何信息" />
    <div class="pagination-wrapper" v-if="total>0">
      <el-pagination :current-page="page" :page-size="10" :total="total" background
        layout="prev,pager,next" @current-change="p=>{page=p;fetch()}" />
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑发布信息" width="760px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top" size="large">
        <div class="form-grid">
          <el-form-item label="类型" prop="type">
            <el-select v-model="editForm.type" style="width:100%">
              <el-option :value="0" label="失物" />
              <el-option :value="1" label="招领" />
            </el-select>
          </el-form-item>
          <el-form-item label="物品分类" prop="category">
            <el-select v-model="editForm.category" placeholder="选择分类" style="width:100%">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="物品标题" prop="title" class="span-2">
            <el-input v-model="editForm.title" placeholder="如：黑色钱包、iPhone 15等" />
          </el-form-item>
          <el-form-item label="地点">
            <el-input v-model="editForm.location" placeholder="如：图书馆三楼" />
          </el-form-item>
          <el-form-item label="时间">
            <el-date-picker
              v-model="editForm.itemTime"
              type="datetime"
              placeholder="丢失/拾到时间"
              format="YYYY-MM-DD HH:mm"
              style="width:100%"
            />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="editForm.contact" placeholder="手机号/微信/QQ" />
          </el-form-item>
          <el-form-item label="详细描述" class="span-2">
            <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请详细描述物品特征" />
          </el-form-item>
          <el-form-item label="上传图片" class="span-2">
            <el-upload
              action="/api/file/upload"
              :headers="uploadHeaders"
              :on-success="handleEditUploadSuccess"
              :on-remove="handleEditUploadRemove"
              list-type="picture-card"
              :limit="5"
              :file-list="fileList"
              accept="image/*"
            >
              <div class="upload-trigger">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" x2="12" y1="3" y2="15"/></svg>
                <span>上传图片</span>
              </div>
            </el-upload>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" round :loading="editing" @click="handleEditSubmit">保存并重新提交审核</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getMyItems, updateMyItem, deleteMyItem } from '@/api'

const items = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const filters = ref({ keyword: '', status: null, type: null, category: '' })
const editDialogVisible = ref(false)
const editFormRef = ref()
const editing = ref(false)
const fileList = ref([])
const editImageUrls = ref([])
const categories = ['证件', '电子产品', '书籍', '衣物', '其他']
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const editForm = reactive({
  id: null,
  type: 0,
  title: '',
  category: '',
  location: '',
  itemTime: null,
  description: '',
  contact: ''
})

const editRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已认领', type: 'info' },
  4: { label: '已关闭', type: 'info' }
}

const normalizeImageUrl = (url) => {
  if (!url) return ''
  return url.startsWith('/uploads/') ? url : `/uploads/${url}`
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await getMyItems({
      page: page.value,
      size: 10,
      status: filters.value.status,
      type: filters.value.type,
      category: filters.value.category,
      keyword: filters.value.keyword
    })
    items.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => {
  page.value = 1
  fetch()
}

const handleReset = () => {
  filters.value = { keyword: '', status: null, type: null, category: '' }
  handleSearch()
}

const openEditDialog = (row) => {
  editForm.id = row.id
  editForm.type = row.type
  editForm.title = row.title || ''
  editForm.category = row.category || ''
  editForm.location = row.location || ''
  editForm.itemTime = row.itemTime ? dayjs(row.itemTime).toDate() : null
  editForm.description = row.description || ''
  editForm.contact = row.contact || ''

  const urls = (row.images || '')
    .split(',')
    .map(v => normalizeImageUrl(v.trim()))
    .filter(Boolean)

  editImageUrls.value = [...urls]
  fileList.value = urls.map((url, idx) => ({ name: `image-${idx}`, url }))

  editDialogVisible.value = true
}

const handleEditUploadSuccess = (res) => {
  if (res.code !== 200) return
  const url = normalizeImageUrl(res.data)
  if (!editImageUrls.value.includes(url)) {
    editImageUrls.value.push(url)
  }
}

const handleEditUploadRemove = (file) => {
  const url = normalizeImageUrl(file.url || file.response?.data)
  editImageUrls.value = editImageUrls.value.filter(item => item !== url)
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate()

  editing.value = true
  try {
    await updateMyItem(editForm.id, {
      type: editForm.type,
      title: editForm.title,
      category: editForm.category,
      location: editForm.location,
      itemTime: editForm.itemTime ? dayjs(editForm.itemTime).format('YYYY-MM-DDTHH:mm:ss') : null,
      description: editForm.description,
      contact: editForm.contact,
      images: editImageUrls.value.join(',')
    })
    ElMessage.success('编辑成功，已重新进入待审核状态')
    editDialogVisible.value = false
    fetch()
  } finally {
    editing.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除“${row.title}”吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })

  await deleteMyItem(row.id)
  ElMessage.success('删除成功')
  if (items.value.length === 1 && page.value > 1) {
    page.value -= 1
  }
  fetch()
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

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}

.span-2 {
  grid-column: span 2;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .form-grid { grid-template-columns: 1fr; }
  .span-2 { grid-column: span 1; }
}
</style>
