<template>
  <div class="products-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>Products</el-breadcrumb-item>
    </el-breadcrumb>
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="resetForm(); showForm = true">发布商品</el-button>
        <el-button :disabled="selectedIds.length === 0" @click="batchStatus('ACTIVE')">批量上架</el-button>
        <el-button :disabled="selectedIds.length === 0" @click="batchStatus('INACTIVE')">批量下架</el-button>
        <el-button @click="$router.push('/inventory')">📥 快捷入库</el-button>
        <el-button @click="$router.push('/print-labels')">🏷️ 打印条码</el-button>
      </div>
      <div class="action-right">
        <span v-if="selectedIds.length" class="selected-count">已选 {{ selectedIds.length }} 项</span>
      </div>
    </div>

    <!-- 利润汇总卡片 -->
    <el-row :gutter="16" class="profit-summary">
      <el-col :span="8">
        <el-card shadow="hover" class="profit-card">
          <div class="profit-body">
            <span class="profit-label">总投资</span>
            <span class="profit-value">${{ totalInvestment }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="profit-card">
          <div class="profit-body">
            <span class="profit-label">总售价</span>
            <span class="profit-value">${{ totalValue }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="profit-card" :style="{ borderLeft: '4px solid ' + (profitPercent >= 0 ? '#67c23a' : '#f56c6c') }">
          <div class="profit-body">
            <span class="profit-label">预期利润</span>
            <span class="profit-value" :style="{ color: profitPercent >= 0 ? '#67c23a' : '#f56c6c' }">${{ totalProfit }} ({{ profitPercent }}%)</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="products" border stripe @selection-change="onSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="title" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="barcode" label="条码" width="130" />
      <el-table-column label="售价" width="100" align="right">
        <template #default="{ row }"><span class="price-cell">{{ row.currency || 'USD' }} {{ row.price?.toLocaleString() }}</span></template>
      </el-table-column>
      <el-table-column label="采购价" width="100" align="right">
        <template #default="{ row }">
          <span v-if="row.purchasePrice" class="cost-cell">{{ row.purchaseCurrency || 'USD' }} {{ row.purchasePrice.toLocaleString() }}</span>
          <span v-else style="color:#999">—</span>
        </template>
      </el-table-column>
      <el-table-column label="利润" width="100" align="right">
        <template #default="{ row }">
          <span v-if="row.purchasePrice" :style="{ color: profitColor(row) }" class="profit-cell">
            {{ row.currency || 'USD' }} {{ (row.price - row.purchasePrice).toLocaleString(undefined, {minimumFractionDigits:2}) }}
          </span>
          <span v-else style="color:#999">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="60" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.stock <= 3 ? 'danger' : 'info'" effect="plain">{{ row.stock }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'INVENTORY' ? 'warning' : 'info'" size="small" effect="plain">{{ STATUS_LABELS[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" width="150" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-space size="small">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-button size="small" @click="uploadImage(row)">图片</el-button>
            <el-button size="small" @click="copyProduct(row)">复制</el-button>
            <el-button v-if="row.status !== 'INACTIVE'" size="small" type="danger" @click="remove(row)">下架</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <el-dialog v-model="showForm" :title="isEdit ? '编辑商品' : '发布商品'" width="680px" @closed="resetForm">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width:100%" filterable>
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="条码">
              <el-input v-model="form.barcode" placeholder="留空自动生成" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0.01" :precision="2" controls-position="right" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" controls-position="right" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="货币">
              <el-select v-model="form.currency" style="width:100%">
                <el-option label="USD" value="USD" />
                <el-option label="EUR" value="EUR" />
                <el-option label="CNY" value="CNY" />
                <el-option label="JPY" value="JPY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-collapse style="border:none">
          <el-collapse-item title="采购信息" name="purchase">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="采购价">
                  <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="采购币种">
                  <el-select v-model="form.purchaseCurrency" style="width:100%">
                    <el-option label="USD" value="USD" />
                    <el-option label="EUR" value="EUR" />
                    <el-option label="CNY" value="CNY" />
                    <el-option label="JPY" value="JPY" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="供应商">
                  <el-input v-model="form.supplier" placeholder="来源/供应商" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="评级信息 (Grading)" name="grading">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="评级公司">
                  <el-select v-model="form.ratingCompany" clearable style="width:100%">
                    <el-option label="NGC" value="NGC" />
                    <el-option label="PCGS" value="PCGS" />
                    <el-option label="PMG" value="PMG" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="评级编号"><el-input v-model="form.ratingNumber" /></el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="品相等级">
                  <el-select v-model="form.ratingGrade" clearable filterable allow-create style="width:100%" placeholder="选或输入">
                    <el-option label="MS70" value="MS70" />
                    <el-option label="MS68" value="MS68" />
                    <el-option label="MS66" value="MS66" />
                    <el-option label="MS65" value="MS65" />
                    <el-option label="MS64" value="MS64" />
                    <el-option label="AU58" value="AU58" />
                    <el-option label="AU55" value="AU55" />
                    <el-option label="XF45" value="XF45" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="物理属性 (Physical Details)" name="physical">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="国家">
                  <el-select v-model="form.country" clearable filterable style="width:100%" placeholder="选择">
                    <el-option v-for="c in countries" :key="c" :label="c" :value="c" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="年份"><el-input-number v-model="form.year" :min="0" :max="2030" controls-position="right" style="width:100%" /></el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="材质">
                  <el-select v-model="form.material" clearable filterable allow-create style="width:100%" placeholder="选或输入">
                    <el-option label="Gold" value="Gold" />
                    <el-option label="Silver" value="Silver" />
                    <el-option label="Copper" value="Copper" />
                    <el-option label="Bronze" value="Bronze" />
                    <el-option label="Nickel" value="Nickel" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="面额">
                  <el-select v-model="form.denomination" clearable filterable allow-create style="width:100%" placeholder="选或输入">
                    <el-option label="$1" value="$1" />
                    <el-option label="$5" value="$5" />
                    <el-option label="$10" value="$10" />
                    <el-option label="$20" value="$20" />
                    <el-option label="1¢" value="1¢" />
                    <el-option label="10¢" value="10¢" />
                    <el-option label="25¢" value="25¢" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="重量 (g)">
                  <el-input-number v-model="form.weight" :min="0" :precision="2" :step="0.1" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">{{ isEdit ? '更新' : '发布' }}</el-button>
      </template>
    </el-dialog>

    <!-- Image Upload Dialog -->
    <el-dialog v-model="showUpload" title="上传图片" width="500px">
      <div v-if="uploadProduct">
        <p style="margin-bottom:12px">
          商品：<strong>{{ uploadProduct.title }}</strong>
          <el-tag style="margin-left:8px" size="small">条码: {{ uploadProduct.barcode }}</el-tag>
        </p>
        <div
          class="upload-zone"
          @dragenter.prevent="uploadDragging = true"
          @dragover.prevent="uploadDragging = true"
          @dragleave.prevent="uploadDragging = false"
          @drop.prevent="onUploadDrop"
          @click="uploadInput.click()"
          :class="{ 'upload-active': uploadDragging }"
        >
          <input ref="uploadInput" type="file" multiple accept="image/*" style="display:none" @change="onUploadSelect" />
          <p style="font-size:24px; margin-bottom:8px">📷</p>
          <p style="color:#666">拖拽或点击上传图片</p>
        </div>
        <div v-if="uploadPreviews.length > 0" class="upload-previews">
          <div v-for="(p, i) in uploadPreviews" :key="i" class="preview-item">
            <img :src="p.url" />
            <span>{{ p.name }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showUpload = false">关闭</el-button>
        <el-button type="primary" :loading="uploading" :disabled="uploadPreviews.length === 0" @click="doUpload">
          上传 ({{ uploadPreviews.length }} 张)
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'
import { ElMessageBox, ElMessage } from 'element-plus'

const products = ref([])
const categories = ref([])
const loading = ref(true)
const saving = ref(false)
const showForm = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const form = ref({ title: '', description: '', price: 0, stock: 0, currency: 'USD', categoryId: null, barcode: '', ratingCompany: '', ratingNumber: '', ratingGrade: '', country: '', year: null, material: '', denomination: '', weight: null, purchasePrice: null, purchaseCurrency: 'USD', supplier: '' })

const STATUS_LABELS = { INVENTORY: '库存中', ACTIVE: '在售', INACTIVE: '已下架' }

// Profit calculations
const totalInvestment = computed(() => {
  return products.value.reduce((sum, p) => sum + (p.purchasePrice || 0) * (p.stock || 0), 0).toLocaleString(undefined, {minimumFractionDigits:2})
})
const totalValue = computed(() => {
  return products.value.reduce((sum, p) => sum + (p.price || 0) * (p.stock || 0), 0).toLocaleString(undefined, {minimumFractionDigits:2})
})
const totalProfit = computed(() => {
  const invest = products.value.reduce((sum, p) => sum + (p.purchasePrice || 0) * (p.stock || 0), 0)
  const value = products.value.reduce((sum, p) => sum + (p.price || 0) * (p.stock || 0), 0)
  return (value - invest).toLocaleString(undefined, {minimumFractionDigits:2})
})
const profitPercent = computed(() => {
  const invest = products.value.reduce((sum, p) => sum + (p.purchasePrice || 0) * (p.stock || 0), 0)
  if (invest === 0) return 0
  const value = products.value.reduce((sum, p) => sum + (p.price || 0) * (p.stock || 0), 0)
  return ((value - invest) / invest * 100).toFixed(1)
})
function profitColor(row) {
  if (!row.purchasePrice) return '#999'
  return (row.price - row.purchasePrice) >= 0 ? '#67c23a' : '#f56c6c'
}

const countries = ['USA','China','Canada','UK','Germany','France','Japan','Spain','Italy','Greece','Roman Empire','Byzantine Empire','Luxembourg','Vatican','Vietnam','Australia','Austria','India','Mexico','Netherlands','Switzerland','Russia']

// Batch selection
const selectedIds = ref([])
function onSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

async function batchStatus(status) {
  try {
    await api.put('/products/batch-status', { ids: selectedIds.value, status })
    ElMessage.success(status === 'ACTIVE' ? '批量上架成功' : '批量下架成功')
    selectedIds.value = []
    loadProducts()
  } catch (e) { /* handled */ }
}

async function copyProduct(row) {
  try {
    await api.post('/products/' + row.id + '/copy')
    ElMessage.success('复制成功！请在原商品名后加"副本"字样区分')
    loadProducts()
  } catch (e) { /* handled */ }
}

const showUpload = ref(false)
const uploadProduct = ref(null)
const uploadDragging = ref(false)
const uploadPreviews = ref([])
const uploading = ref(false)
const uploadInput = ref(null)

function uploadImage(row) {
  uploadProduct.value = row
  uploadPreviews.value = []
  showUpload.value = true
}

function onUploadDrop(e) {
  uploadDragging.value = false
  if (e.dataTransfer?.files?.length) {
    addUploadFiles(Array.from(e.dataTransfer.files))
  }
}

function onUploadSelect(e) {
  if (e.target?.files?.length) {
    addUploadFiles(Array.from(e.target.files))
  }
  e.target.value = ''
}

function addUploadFiles(files) {
  for (const file of files) {
    uploadPreviews.value.push({
      file,
      name: file.name,
      url: URL.createObjectURL(file),
    })
  }
}

async function doUpload() {
  if (!uploadProduct.value?.barcode || uploadPreviews.value.length === 0) return
  uploading.value = true
  try {
    const formData = new FormData()
    for (const p of uploadPreviews.value) {
      formData.append('files', p.file)
    }
    const res = await api.post(`/files/import`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    const data = res.data
    if (data.failCount === 0) {
      ElMessage.success(`成功上传 ${data.successCount} 张图片`)
    } else {
      ElMessage.warning(`上传 ${data.successCount} 张成功，${data.failCount} 张失败`)
    }
    showUpload.value = false
  } catch (e) {
    ElMessage.error('上传失败')
  }
  uploading.value = false
}

function resetForm() {
  form.value = { title: '', description: '', price: 0, stock: 0, currency: 'USD', categoryId: null, barcode: '', ratingCompany: '', ratingNumber: '', ratingGrade: '', country: '', year: null, material: '', denomination: '', weight: null, purchasePrice: null, purchaseCurrency: 'USD', supplier: '' }
  isEdit.value = false
  editId.value = null
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await api.get('/products/my', { params: { page: 0, size: 200 } })
    products.value = res.data?.content || []
  } catch (e) { products.value = [] }
  loading.value = false
}

async function saveProduct() {
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put(`/products/${editId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.post('/products', form.value)
      ElMessage.success('发布成功')
    }
    showForm.value = false
    loadProducts()
  } catch (e) { /* handled by interceptor */ }
  saving.value = false
}

function edit(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    title: row.title,
    description: row.description || '',
    price: row.price,
    stock: row.stock,
    currency: row.currency || 'USD',
    categoryId: row.categoryId,
    barcode: row.barcode || '',
    ratingCompany: row.ratingCompany || '',
    ratingNumber: row.ratingNumber || '',
    ratingGrade: row.ratingGrade || '',
    country: row.country || '',
    year: row.year,
    material: row.material || '',
    denomination: row.denomination || '',
    weight: row.weight,
    purchasePrice: row.purchasePrice,
    purchaseCurrency: row.purchaseCurrency || 'USD',
    supplier: row.supplier || '',
  }
  showForm.value = true
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确认下架该商品？')
    await api.put(`/products/${row.id}/status?status=INACTIVE`)
    ElMessage.success('已下架')
    loadProducts()
  } catch (_) {}
}

onMounted(async () => {
  try {
    const catRes = await api.get('/products/categories')
    categories.value = catRes.data || []
  } catch (_) {}
  loadProducts()
})
</script>

<style scoped>
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.profit-summary { margin-bottom: 16px; }
.profit-card { border-radius: 8px; }
.profit-body { display: flex; flex-direction: column; gap: 4px; }
.profit-label { font-size: 12px; color: #909399; }
.profit-value { font-size: 20px; font-weight: 700; }
.price-cell { font-weight: 600; color: #059669; }
.cost-cell { color: #e6a23c; }
.profit-cell { font-weight: 600; }
.action-left { display: flex; gap: 8px; flex-wrap: wrap; }
.action-right { color: #909399; font-size: 13px; }
.selected-count { font-weight: 600; color: #409eff; }
.upload-zone {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}
.upload-zone:hover, .upload-active {
  border-color: #409eff;
  background: #f0f7ff;
}
.upload-previews {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}
.preview-item {
  width: 100px;
  text-align: center;
}
.preview-item img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}
.preview-item span {
  display: block;
  font-size: 11px;
  color: #666;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
