<template>
  <div class="products-page">
    <!-- Page Header -->
    <div class="page-head">
      <div>
        <h2>{{ $t('products.title') || '商品管理' }}</h2>
        <p class="page-desc">管理平台上的所有商品 — 查看库存、上架、下架与编辑</p>
      </div>
      <div class="page-actions">
        <el-button @click="$router.push('/inventory')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          入库
        </el-button>
        <el-button @click="$router.push('/print-labels')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px"><path d="M20 3H4a1 1 0 0 0-1 1v16a1 1 0 0 0 1 1h16a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1Z"/><path d="M9 3v18"/></svg>
          打印条码
        </el-button>
      </div>
    </div>

    <!-- Status Stat Cards -->
    <div class="stats-row">
      <div class="stat-card" @click="statusFilter = ''">
        <div class="stat-icon stat-icon-total">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">全部</span>
          <span class="stat-value">{{ total }}</span>
        </div>
      </div>
      <div class="stat-card" @click="statusFilter = 'INVENTORY'">
        <div class="stat-icon stat-icon-inventory">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">库存中</span>
          <span class="stat-value">{{ stats.inventory }}</span>
        </div>
      </div>
      <div class="stat-card" @click="statusFilter = 'ACTIVE'">
        <div class="stat-icon stat-icon-active">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">在售</span>
          <span class="stat-value">{{ stats.active }}</span>
        </div>
      </div>
      <div class="stat-card" @click="statusFilter = 'INACTIVE'">
        <div class="stat-icon stat-icon-inactive">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="9" x2="15" y2="15"/><line x1="15" y1="9" x2="9" y2="15"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">已下架</span>
          <span class="stat-value">{{ stats.inactive }}</span>
        </div>
      </div>
    </div>

    <!-- Search & Filters -->
    <div class="action-bar">
      <div class="filter-tabs">
        <button class="filter-btn" :class="{ active: statusFilter === '' }" @click="statusFilter = ''">全部</button>
        <button class="filter-btn" :class="{ active: statusFilter === 'INVENTORY' }" @click="statusFilter = 'INVENTORY'">库存中</button>
        <button class="filter-btn" :class="{ active: statusFilter === 'ACTIVE' }" @click="statusFilter = 'ACTIVE'">在售</button>
        <button class="filter-btn" :class="{ active: statusFilter === 'INACTIVE' }" @click="statusFilter = 'INACTIVE'">已下架</button>
      </div>
      <div class="search-area">
        <el-input v-model="searchKeyword" placeholder="搜索条码/名称/国家..." clearable style="width:260px" @input="onSearchInput">
          <template #prefix>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </template>
        </el-input>
      </div>
    </div>

    <!-- Products Table -->
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
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <button class="tbl-btn tbl-btn-edit" @click="openEdit(row)">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                编辑
              </button>
              <button v-if="row.status === 'INVENTORY'" class="tbl-btn tbl-btn-list" @click.stop="openListDialog(row)">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                上架
              </button>
              <button v-else-if="row.status === 'ACTIVE'" class="tbl-btn tbl-btn-off" @click="toggleStatus(row.id, 'INACTIVE')">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="9" x2="15" y2="15"/><line x1="15" y1="9" x2="9" y2="15"/></svg>
                下架
              </button>
              <button v-else class="tbl-btn tbl-btn-list" @click="openListDialog(row)">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                上架
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="total, prev, pager, next" background @current-change="load" />
      </div>
    </el-card>

    <!-- List for Sale Dialog -->
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

    <!-- Edit Product Dialog -->
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
.products-page { max-width: 1400px; }

/* Page head */
.page-head {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 20px;
}
.page-head h2 {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 24px; font-weight: 700; color: var(--ink, #0f172a); margin: 0;
}
.page-desc { font-size: 13px; color: var(--text-muted, #94a3b8); margin-top: 2px; }
.page-actions { display: flex; gap: 8px; }

/* Stats row */
.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 16px; margin-bottom: 20px;
}
.stat-card {
  background: var(--surface, #fff);
  border: 1px solid var(--border, #e5e7eb);
  border-radius: var(--radius, 10px);
  padding: 18px 20px;
  display: flex; align-items: center; gap: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md, 0 4px 12px rgba(0,0,0,.06));
}
.stat-icon {
  width: 44px; height: 44px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; color: #fff;
}
.stat-icon-total { background: linear-gradient(135deg, #6366f1, #818cf8); }
.stat-icon-inventory { background: linear-gradient(135deg, var(--gold-dark, #b8932a), var(--gold, #d4a843)); }
.stat-icon-active { background: linear-gradient(135deg, #059669, #10b981); }
.stat-icon-inactive { background: linear-gradient(135deg, #6b7280, #9ca3af); }
.stat-info { display: flex; flex-direction: column; }
.stat-label {
  font-size: 12px; color: var(--text-muted, #94a3b8);
  font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px;
}
.stat-value {
  font-size: 26px; font-weight: 800;
  color: var(--ink, #0f172a); line-height: 1.2;
}

/* Action bar */
.action-bar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; gap: 12px; flex-wrap: wrap;
}
.filter-tabs { display: flex; gap: 4px; background: var(--ivory-dark, #e8e2d6); padding: 3px; border-radius: 8px; }
.filter-btn {
  padding: 6px 14px; border-radius: 6px; border: none;
  font-size: 12px; font-weight: 500; font-family: inherit;
  color: var(--text-muted, #94a3b8);
  background: transparent; cursor: pointer; transition: all 0.15s;
}
.filter-btn:hover { color: var(--text, #1e293b); }
.filter-btn.active { background: var(--surface, #fff); color: var(--ink, #0f172a); box-shadow: 0 1px 3px rgba(0,0,0,.06); }
.search-area { display: flex; gap: 8px; align-items: center; }

/* Table card */
.list-card {
  border-radius: var(--radius, 10px);
  border: 1px solid var(--border, #e5e7eb);
  margin-bottom: 16px;
}

/* Pagination */
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }

/* Table action buttons */
.action-btns { display: flex; gap: 6px; justify-content: center; }
.tbl-btn {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 5px 10px; border-radius: 6px; border: 1px solid var(--border, #e5e7eb);
  font-size: 12px; font-weight: 500; font-family: inherit;
  background: var(--surface, #fff); color: var(--text, #1e293b);
  cursor: pointer; transition: all 0.15s;
}
.tbl-btn:hover { border-color: var(--gold, #d4a843); color: var(--gold-dark, #b8932a); }
.tbl-btn-edit:hover { border-color: var(--info, #3b82f6); color: #3b82f6; }
.tbl-btn-list:hover { border-color: var(--success, #059669); color: var(--success, #059669); }
.tbl-btn-off:hover { border-color: var(--danger, #dc2626); color: var(--danger, #dc2626); }

/* Barcode */
.barcode-text {
  font-family: 'Courier New', monospace;
  font-size: 13px; color: var(--gold-dark, #b8932a);
  font-weight: 700; letter-spacing: 1px;
}
.price-cell { font-weight: 600; color: var(--success, #059669); }

/* Dialog product summary */
.product-summary { font-size: 13px; }
.summary-row { display: flex; align-items: center; gap: 12px; padding: 5px 0; }
.summary-label {
  color: var(--text-muted, #94a3b8);
  min-width: 60px; font-size: 12px; font-weight: 500;
}
.info-card { border: 1px solid var(--border, #e5e7eb); border-radius: var(--radius-sm, 6px); }

/* ====== Mobile Responsive ====== */
@media (max-width: 1024px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .page-head { flex-direction: column; gap: 12px; }
  .page-actions { width: 100%; }
  .page-actions .el-button { flex: 1; justify-content: center; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .action-bar { flex-direction: column; align-items: stretch; }
  .search-area { width: 100%; }
  .search-area .el-input { width: 100% !important; }
  .filter-tabs { overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .el-table { overflow-x: auto; }
  .action-btns { flex-wrap: wrap; }
  .tbl-btn { flex: 1; justify-content: center; }
  .pagination-wrap { overflow-x: auto; }
  .el-dialog { width: 95% !important; max-width: 95vw !important; }
}
@media (max-width: 480px) {
  .stats-row { grid-template-columns: 1fr; }
  .products-page { padding: 0; }
  .stat-value { font-size: 22px; }
  .stat-card { padding: 14px; }
}
</style>
