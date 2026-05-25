<template>
  <div class="print-page">
    <!-- Header with filters -->
    <div class="page-header no-print">
      <h3>打印条码标签</h3>
      <div class="filter-bar">
        <el-select v-model="dateGroup" style="width:160px" @change="loadProducts">
          <el-option label="📅 今日" value="today" />
          <el-option label="📅 昨日" value="yesterday" />
          <el-option label="📅 本周" value="this-week" />
          <el-option label="📅 本月" value="this-month" />
          <el-option label="📅 自定义" value="custom" />
        </el-select>
        <template v-if="dateGroup === 'custom'">
          <el-date-picker v-model="dateFrom" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" @change="loadProducts" />
          <el-date-picker v-model="dateTo" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" @change="loadProducts" />
        </template>
        <el-select v-model="printedFilter" clearable placeholder="打印状态" style="width:150px" @change="loadProducts">
          <el-option label="未打印" :value="false" />
          <el-option label="已打印" :value="true" />
        </el-select>
        <el-select v-model="categoryFilter" clearable placeholder="按分类筛选" style="width:180px" @change="loadProducts">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadProducts">刷新</el-button>
        <el-button @click="fullscreen" :type="isFullscreen ? 'warning' : 'default'">
          {{ isFullscreen ? '退出全屏' : '全屏模式' }}
        </el-button>
        <el-button type="success" @click="printAll" :disabled="selectedProducts.length === 0">
          打印 ({{ selectedProducts.length }} 个标签)
        </el-button>
      </div>
    </div>

    <!-- Date group tabs -->
    <div class="date-groups no-print" v-if="!loading">
      <el-collapse v-model="openGroups">
        <el-collapse-item v-for="group in dateGroups" :key="group.label" :title="group.label" :name="group.label">
          <div class="product-check-list">
            <el-checkbox v-model="group.allChecked" @change="val => toggleGroup(group, val)" style="margin-bottom:8px">
              全选 ({{ group.products.length }})
            </el-checkbox>
            <el-checkbox-group v-model="group.selectedIds" @change="val => onGroupChange(group, val)">
              <div v-for="p in group.products" :key="p.id" class="product-check-item" :class="{ printed: p.printedAt }">
                <el-checkbox :label="p.id" :value="p.id">
                  <span class="check-barcode">{{ p.barcode }}</span>
                  <span class="check-title">{{ p.title }}</span>
                  <el-tag v-if="p.printedAt" size="small" type="success" effect="plain">已打印 {{ formatDate(p.printedAt) }}</el-tag>
                  <el-tag v-else size="small" type="warning" effect="plain">未打印</el-tag>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </el-collapse-item>
      </el-collapse>
      <div style="margin-top:16px; display:flex; gap:8px; justify-content:center">
        <el-button type="primary" :disabled="selectedProducts.length === 0" @click="markSelectedAsPrinted">
          标记已打印 ({{ selectedProducts.length }})
        </el-button>
      </div>
    </div>

    <!-- Print area -->
    <div v-loading="loading">
      <div v-if="allProducts.length === 0" style="text-align:center; padding:60px; color:#999">
        暂无商品，请先创建商品或调整筛选条件。
      </div>

      <div ref="printArea" class="label-sheet">
        <template v-for="(product, idx) in allProducts" :key="product.id">
          <div class="label-item">
            <div class="label-header">
              <div class="label-logo">
                <svg viewBox="0 0 40 40" width="18" height="18">
                  <circle cx="20" cy="20" r="18" fill="#f59e0b" stroke="#b45309" stroke-width="2"/>
                  <text x="20" y="20" text-anchor="middle" dominant-baseline="central"
                        font-size="14" font-weight="bold" fill="#fff" font-family="Arial">$</text>
                </svg>
              </div>
              <div class="label-meta">
                <span v-if="product.country">{{ product.country }}</span>
                <span v-if="product.year">{{ product.year }}</span>
              </div>
            </div>
            <div class="label-title">{{ product.title }}</div>
            <div class="label-details">
              <span v-if="product.material">{{ product.material }}</span>
              <span v-if="product.denomination">{{ product.denomination }}</span>
              <span v-if="product.weight">{{ product.weight }}g</span>
              <span v-if="product.ratingGrade">{{ product.ratingCompany }} {{ product.ratingGrade }}</span>
            </div>
            <div class="label-barcode">
              <svg :ref="el => renderBarcode(el, product.barcode)"></svg>
            </div>
            <div class="label-number">{{ product.barcode }}</div>
          </div>
          <div v-if="(idx + 1) % 20 === 0 && idx + 1 < allProducts.length" class="page-break"></div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import JsBarcode from 'jsbarcode'

const products = ref([])
const categories = ref([])
const categoryFilter = ref(null)
const loading = ref(false)
const isFullscreen = ref(false)
const printArea = ref(null)
const dateGroup = ref('today')
const dateFrom = ref(null)
const dateTo = ref(null)
const printedFilter = ref(false)
const openGroups = ref([])
const selectedForPrint = ref(new Set())

// Compute date range from group selection
function getDateRange() {
  const now = new Date()
  const today = now.toISOString().slice(0, 10)
  if (dateGroup.value === 'today') {
    return { from: today, to: today }
  } else if (dateGroup.value === 'yesterday') {
    const y = new Date(now)
    y.setDate(y.getDate() - 1)
    const ys = y.toISOString().slice(0, 10)
    return { from: ys, to: ys }
  } else if (dateGroup.value === 'this-week') {
    const m = new Date(now)
    m.setDate(m.getDate() - m.getDay())
    return { from: m.toISOString().slice(0, 10), to: today }
  } else if (dateGroup.value === 'this-month') {
    return { from: now.toISOString().slice(0, 7) + '-01', to: today }
  } else if (dateGroup.value === 'custom') {
    return { from: dateFrom.value, to: dateTo.value }
  }
  return { from: today, to: today }
}

// Products actually being printed (filtered by selected)
const allProducts = computed(() => {
  if (selectedForPrint.value.size === 0) return []
  return products.value.filter(p => selectedForPrint.value.has(p.id))
})

// Selected product IDs for printing
const selectedProducts = computed(() => {
  return products.value.filter(p => selectedForPrint.value.has(p.id))
})

// Group products by date for the UI
const dateGroups = computed(() => {
  const groups = {}
  for (const p of products.value) {
    const d = p.createdAt ? p.createdAt.slice(0, 10) : 'unknown'
    if (!groups[d]) groups[d] = []
    groups[d].push(p)
  }
  return Object.entries(groups).map(([date, prods]) => ({
    label: date + (date === new Date().toISOString().slice(0, 10) ? ' (今日)' : ''),
    products: prods,
    allChecked: false,
    selectedIds: [],
  }))
})

watch(dateGroups, (groups) => {
  // Sync selectedForPrint from groups
  const ids = new Set()
  for (const g of groups) {
    for (const id of g.selectedIds) ids.add(id)
  }
  selectedForPrint.value = ids
}, { deep: true })

function toggleGroup(group, val) {
  if (val) {
    group.selectedIds = group.products.map(p => p.id)
  } else {
    group.selectedIds = []
  }
}

function onGroupChange(group, val) {
  group.allChecked = val.length === group.products.length
}

onMounted(async () => {
  try {
    const catRes = await api.get('/products/categories')
    categories.value = catRes.data || []
  } catch (_) {}
  await loadProducts()
})

async function loadProducts() {
  loading.value = true
  try {
    const range = getDateRange()
    const params = { page: 0, size: 500 }
    if (categoryFilter.value) params.categoryId = categoryFilter.value
    if (printedFilter.value !== null && printedFilter.value !== undefined) {
      params.printed = printedFilter.value
    }
    if (range.from) params.dateFrom = range.from
    if (range.to) params.dateTo = range.to
    const res = await api.get('/admin/products', { params })
    products.value = res.data?.content || []
    await nextTick()
    products.value.forEach(p => {
      if (p.barcode) {
        const svg = document.querySelector(`svg[data-barcode="${p.barcode}"]`)
        if (svg) renderBarcode(svg, p.barcode)
      }
    })
    // Auto-select unprinted products
    selectedForPrint.value = new Set(products.value.filter(p => !p.printedAt).map(p => p.id))
  } catch (_) { products.value = [] }
  loading.value = false
}

function renderBarcode(el, code) {
  if (!el || !code) return
  try {
    JsBarcode(el, code, {
      format: 'CODE128',
      width: 1.0,
      height: 18,
      displayValue: false,
      margin: 0,
    })
    el.setAttribute('data-barcode', code)
  } catch (_) {}
}

function fullscreen() {
  isFullscreen.value = !isFullscreen.value
  document.body.classList.toggle('print-fullscreen', isFullscreen.value)
  const appEl = document.querySelector('.app-container')
  if (appEl) appEl.classList.toggle('print-fullscreen-mode', isFullscreen.value)
}

function printAll() {
  if (allProducts.value.length === 0) return
  isFullscreen.value = true
  document.body.classList.add('print-fullscreen')
  setTimeout(() => window.print(), 300)
}

async function markSelectedAsPrinted() {
  const ids = Array.from(selectedForPrint.value)
  if (ids.length === 0) return
  try {
    await api.post('/admin/products/mark-printed-batch', ids)
    ElMessage.success(`已标记 ${ids.length} 个商品为已打印`)
    // Update local state
    const now = new Date().toISOString()
    for (const p of products.value) {
      if (ids.includes(p.id)) p.printedAt = now
    }
  } catch (e) {
    ElMessage.error('标记失败')
  }
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('en-US', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.page-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}
.filter-bar {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.date-groups {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}
.product-check-list {
  padding: 8px 0;
}
.product-check-item {
  padding: 4px 8px;
  border-radius: 4px;
  margin-bottom: 2px;
}
.product-check-item.printed {
  opacity: 0.6;
}
.product-check-item:hover {
  background: #f9fafb;
}
.check-barcode {
  font-family: 'SF Mono', monospace;
  font-size: 12px;
  color: #6b7280;
  margin-right: 8px;
}
.check-title {
  font-size: 13px;
  color: #111827;
  margin-right: 8px;
}

/* Label Sheet */
.label-sheet {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 3px;
  padding: 4px;
  background: #fff;
}

.label-item {
  border: 1px dashed #ccc;
  padding: 3px 3px 2px;
  text-align: center;
  page-break-inside: avoid;
  break-inside: avoid;
  display: flex;
  flex-direction: column;
  min-height: 88px;
}

.label-header {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-bottom: 1px;
}

.label-logo {
  flex-shrink: 0;
}

.label-meta {
  flex: 1;
  text-align: right;
  font-size: 6px;
  color: #666;
  line-height: 1.1;
}
.label-meta span {
  display: inline-block;
  margin-left: 3px;
  text-transform: uppercase;
}

.label-title {
  font-size: 7px;
  font-weight: 700;
  color: #111;
  line-height: 1.2;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  margin-bottom: 1px;
  min-height: 8px;
}

.label-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1px 3px;
  justify-content: center;
  font-size: 6px;
  color: #555;
  margin-bottom: auto;
  padding: 0;
}
.label-details span {
  background: #f3f4f6;
  padding: 0 2px;
  border-radius: 2px;
  line-height: 1.3;
}

.label-barcode {
  margin: 1px 0 0;
}
.label-barcode svg {
  max-width: 100%;
  height: auto;
  display: block;
}

.label-number {
  font-size: 6px;
  color: #333;
  letter-spacing: 0.3px;
  font-family: 'Courier New', monospace;
  font-weight: 600;
  line-height: 1.2;
}

.page-break {
  page-break-after: always;
  break-after: page;
  height: 0;
}

@media print {
  body { margin: 0; padding: 0; }
  .page-header { display: none !important; }
  .date-groups { display: none !important; }
  .label-sheet {
    padding: 0;
    gap: 4px;
  }
  .label-item {
    border: 1px dashed #bbb;
    min-height: 120px;
  }
}

@page {
  size: A4;
  margin: 8mm;
}
</style>

<style>
.print-fullscreen-mode .app-aside,
.print-fullscreen-mode .app-header,
.print-fullscreen .app-aside,
.print-fullscreen .app-header {
  display: none !important;
}
.print-fullscreen-mode .app-main,
.print-fullscreen .app-main {
  padding: 0 !important;
  margin-left: 0 !important;
}

@media print {
  .app-aside,
  .app-header {
    display: none !important;
  }
  .app-main {
    padding: 0 !important;
    margin-left: 0 !important;
  }
}
</style>
