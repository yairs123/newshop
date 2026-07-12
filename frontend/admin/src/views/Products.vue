<template>
  <div class="products-page">
    <!-- 状态统计卡片 -->
    <div class="stats-row">
      <div class="stat-card" @click="statusFilter = ''">
        <div class="stat-icon stat-icon-total">&#x1F4E6;</div>
        <div class="stat-info">
          <span class="stat-label">全部</span>
          <span class="stat-value">{{ total }}</span>
        </div>
      </div>
      <div class="stat-card" @click="statusFilter = 'INVENTORY'">
        <div class="stat-icon stat-icon-inventory">&#x1F4E5;</div>
        <div class="stat-info">
          <span class="stat-label">库存中</span>
          <span class="stat-value">{{ stats.inventory }}</span>
        </div>
      </div>
      <div class="stat-card" @click="statusFilter = 'ACTIVE'">
        <div class="stat-icon stat-icon-active">&#x1F6D2;</div>
        <div class="stat-info">
          <span class="stat-label">在售</span>
          <span class="stat-value">{{ stats.active }}</span>
        </div>
      </div>
      <div class="stat-card" @click="statusFilter = 'INACTIVE'">
        <div class="stat-icon stat-icon-inactive">&#x23F8;&#xFE0F;</div>
        <div class="stat-info">
          <span class="stat-label">已下架</span>
          <span class="stat-value">{{ stats.inactive }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索与操作栏 -->
    <div class="action-bar">
      <div class="filter-tabs">
        <el-tag :type="statusFilter === '' ? 'primary' : 'info'" effect="plain" style="cursor:pointer" @click="statusFilter = ''">全部</el-tag>
        <el-tag :type="statusFilter === 'INVENTORY' ? 'warning' : 'info'" effect="plain" style="cursor:pointer" @click="statusFilter = 'INVENTORY'">库存中</el-tag>
        <el-tag :type="statusFilter === 'ACTIVE' ? 'success' : 'info'" effect="plain" style="cursor:pointer" @click="statusFilter = 'ACTIVE'">在售</el-tag>
        <el-tag :type="statusFilter === 'INACTIVE' ? 'danger' : 'info'" effect="plain" style="cursor:pointer" @click="statusFilter = 'INACTIVE'">已下架</el-tag>
      </div>
      <div style="display:flex; gap:8px; align-items:center">
        <el-input v-model="searchKeyword" placeholder="搜索条码/名称/国家..." clearable style="width:240px" @input="onSearchInput" />
        <el-button @click="$router.push('/print-labels')">&#x1F5A8;&#xFE0F; 打印条码</el-button>
        <el-button @click="$router.push('/inventory')">&#x1F4E5; 入库</el-button>
      </div>
    </div>

    <!-- 商品列表 -->
    <el-card shadow="never" class="list-card">
      <el-table :data="products" v-loading="loading" stripe border style="width:100%" :empty-text="statusFilter ? '暂无此状态商品' : '暂无商品，请先入库'">
        <el-table-column prop="barcode" label="条码" width="150">
          <template #default="{ row }">
            <span v-if="row.barcode" class="barcode-text">{{ row.barcode }}</span>
            <el-tag v-else size="small" type="warning">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'INVENTORY' ? 'warning' : 'info'" size="small" effect="plain">{{ STATUS_LABELS[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120" align="right">
          <template #default="{ row }"><span class="price-cell">{{ row.currency || 'USD' }} {{ row.price || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" align="center">
          <template #default="{ row }"><el-tag size="small" :type="row.stock > 0 ? 'info' : 'danger'" effect="plain">{{ row.stock || 0 }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="country" label="国家" width="90" show-overflow-tooltip />
        <el-table-column prop="denomination" label="面值" width="80" show-overflow-tooltip />
        <el-table-column prop="ratingGrade" label="评分" width="80" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-space size="small">
              <el-button size="small" plain @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 'INVENTORY'" type="warning" size="small" @click.stop="openListDialog(row)">上架</el-button>
              <el-button v-else-if="row.status === 'ACTIVE'" type="danger" size="small" plain @click="toggleStatus(row.id, 'INACTIVE')">下架</el-button>
              <el-button v-else type="success" size="small" @click="openListDialog(row)">上架</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="total, prev, pager, next" background style="margin-top:16px; justify-content:center" @current-change="load" />
    </el-card>

    <!-- 上架弹窗 -->
    <el-dialog v-model="listDialog" title="商品上架" width="420px" :close-on-click-modal="false" destroy-on-close>
      <template v-if="selectedProduct">
        <el-card shadow="never" class="info-card">
          <div class="product-summary">
            <div class="summary-row"><span class="summary-label">条码</span><span class="barcode-text">{{ selectedProduct.barcode }}</span></div>
            <div class="summary-row"><span class="summary-label">名称</span><span>{{ selectedProduct.title }}</span></div>
            <div class="summary-row"><span class="summary-label">库存</span><el-tag size="small" type="info">{{ selectedProduct.stock }}</el-tag></div>
            <div class="summary-row"><span class="summary-label">采购价</span><span>{{ selectedProduct.purchaseCurrency || 'USD' }} {{ selectedProduct.purchasePrice || '-' }}</span></div>
          </div>
        </el-card>
        <el-form label-position="top" style="margin-top:16px">
          <el-form-item label="售价（必填）">
            <el-input-number v-model="salePrice" :min="0.01" :precision="2" :step="1" controls-position="right" style="width:100%" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="listDialog = false">取消</el-button>
        <el-button type="warning" @click="confirmListForSale" :loading="listing">{{ listing ? '上架中...' : '确认上架' }}</el-button>
      </template>
    </el-dialog>

    <!-- 编辑商品弹窗 -->
    <el-dialog v-model="editDialog" title="编辑商品" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="editForm" label-width="100px" size="small" label-position="top">
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="售价"><el-input-number v-model="editForm.price" :min="0" :precision="2" :step="0.5" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="货币"><el-select v-model="editForm.currency" style="width:100%"><el-option label="USD" value="USD" /><el-option label="EUR" value="EUR" /><el-option label="CNY" value="CNY" /><el-option label="GBP" value="GBP" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="库存"><el-input-number v-model="editForm.stock" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="条码"><el-input v-model="editForm.barcode" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="国家"><el-select v-model="editForm.country" filterable clearable style="width:100%"><el-option v-for="c in COUNTRIES" :key="c" :label="c" :value="c" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="面值"><el-select v-model="editForm.denomination" filterable allow-create clearable style="width:100%"><el-option label="$1" value="$1" /><el-option label="$5" value="$5" /><el-option label="$10" value="$10" /><el-option label="$20" value="$20" /><el-option label="$50" value="$50" /><el-option label="$100" value="$100" /><el-option label="1&cent;" value="1&cent;" /><el-option label="5&cent;" value="5&cent;" /><el-option label="10&cent;" value="10&cent;" /><el-option label="25&cent;" value="25&cent;" /><el-option label="50&cent;" value="50&cent;" /></el-select></el-form-item></el-col>
          <el-col :span="4"><el-form-item label="年份"><el-input-number v-model="editForm.year" :min="0" :max="2030" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="4"><el-form-item label="材质"><el-select v-model="editForm.material" filterable allow-create clearable style="width:100%"><el-option label="Gold" value="Gold" /><el-option label="Silver" value="Silver" /><el-option label="Copper" value="Copper" /><el-option label="Bronze" value="Bronze" /><el-option label="Nickel" value="Nickel" /><el-option label="Platinum" value="Platinum" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="saving">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const STATUS_LABELS = { INVENTORY: '库存中', ACTIVE: '在售', INACTIVE: '已下架' }
const COUNTRIES = ['USA','China','Canada','UK','Germany','France','Japan','Spain','Italy','Greece','Roman Empire','Byzantine Empire','Australia','Austria','Belgium','Brazil','Bulgaria','Croatia','Cuba','Czech Republic','Denmark','Egypt','Finland','Hungary','India','Indonesia','Ireland','Israel','South Korea','Mexico','Netherlands','New Zealand','Norway','Philippines','Poland','Portugal','Romania','Russia','Singapore','Switzerland','Taiwan','Thailand','Turkey','Ukraine','Vietnam']

// List
const products = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const size = 20
const searchKeyword = ref('')
const statusFilter = ref('')
let searchTimer = null

const stats = computed(() => {
  const all = products.value
  return { inventory: all.filter(p => p.status === 'INVENTORY').length, active: all.filter(p => p.status === 'ACTIVE').length, inactive: all.filter(p => p.status !== 'INVENTORY' && p.status !== 'ACTIVE').length }
})

async function load() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size }
    if (searchKeyword.value) params.q = searchKeyword.value
    if (statusFilter.value) params.status = statusFilter.value
    const res = await api.get('/admin/products', { params })
    products.value = res.data?.content || res.data || []
    total.value = res.data?.totalElements || products.value.length
  } catch (e) { products.value = []; total.value = 0 }
  loading.value = false
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; load() }, 400)
}
watch(statusFilter, () => { page.value = 1; load() })

// List for sale dialog
const listDialog = ref(false)
const selectedProduct = ref(null)
const salePrice = ref(null)
const listing = ref(false)

function openListDialog(product) {
  selectedProduct.value = product
  salePrice.value = product.price || product.purchasePrice || null
  listDialog.value = true
}

async function confirmListForSale() {
  if (!salePrice.value || salePrice.value <= 0) { ElMessage.warning('请设置有效的售价'); return }
  listing.value = true
  try {
    await api.post('/admin/inventory/list-for-sale/' + selectedProduct.value.id + '?price=' + salePrice.value)
    ElMessage.success('上架成功！')
    listDialog.value = false; load()
  } catch (e) { /* handled */ }
  finally { listing.value = false }
}

// Toggle status
async function toggleStatus(id, status) {
  try {
    await api.put('/admin/products/' + id + '/status?status=' + status)
    ElMessage.success(status === 'ACTIVE' ? '已上架' : '已下架')
    load()
  } catch (e) { /* handled */ }
}

// Edit dialog
const editDialog = ref(false)
const editId = ref(null)
const formRef = ref(null)
const saving = ref(false)

const editForm = reactive({ title: '', price: null, currency: 'USD', stock: 0, barcode: '', country: '', denomination: '', year: null, material: '', description: '' })

function openEdit(row) {
  editId.value = row.id
  editForm.title = row.title || ''
  editForm.price = row.price
  editForm.currency = row.currency || 'USD'
  editForm.stock = row.stock || 0
  editForm.barcode = row.barcode || ''
  editForm.country = row.country || ''
  editForm.denomination = row.denomination || ''
  editForm.year = row.year
  editForm.material = row.material || ''
  editForm.description = row.description || ''
  editDialog.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await api.put('/admin/products/' + editId.value, editForm)
    ElMessage.success('保存成功')
    editDialog.value = false; load()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

onMounted(() => { load() })
</script>

<style scoped>
.products-page { padding: 0; }
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 12px; padding: 18px 20px; display: flex; align-items: center; gap: 14px; box-shadow: 0 1px 3px rgba(0,0,0,.06); cursor: pointer; transition: transform .15s, box-shadow .15s; }
.stat-card:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }
.stat-icon-total { background: #eef2ff; }
.stat-icon-inventory { background: #fef3c7; }
.stat-icon-active { background: #ecfdf5; }
.stat-icon-inactive { background: #fce7f3; }
.stat-info { display: flex; flex-direction: column; }
.stat-label { font-size: 12px; color: #6b7280; font-weight: 500; text-transform: uppercase; letter-spacing: .5px; }
.stat-value { font-size: 26px; font-weight: 700; color: #111827; line-height: 1.2; }
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; gap: 12px; flex-wrap: wrap; }
.filter-tabs { display: flex; gap: 8px; }
.list-card { border-radius: 8px; border: 1px solid #ebeef5; margin-bottom: 16px; }
.barcode-text { font-family: 'Courier New', monospace; font-size: 13px; color: #409eff; font-weight: 700; letter-spacing: 1px; }
.price-cell { font-weight: 600; color: #059669; }
.product-summary { font-size: 13px; }
.summary-row { display: flex; align-items: center; gap: 12px; padding: 5px 0; }
.summary-label { color: #909399; min-width: 60px; font-size: 12px; font-weight: 500; }
.info-card { border: 1px solid #e8eaed; border-radius: 6px; }
.form-section { margin-bottom: 16px; border: 1px solid #e8eaed; border-radius: 6px; }
.form-section :deep(.el-card__header) { padding: 10px 16px; background: #f5f7fa; border-bottom: 1px solid #e8eaed; font-size: 14px; font-weight: 600; color: #303133; }
</style>
