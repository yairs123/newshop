<template>
  <div class="print-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>打印条码</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="no-print">
      <el-card shadow="hover" class="filter-card">
        <div class="filter-bar">
          <el-select v-model="statusFilter" style="width:140px" @change="loadProducts">
            <el-option label="全部商品" value="" />
            <el-option label="在售" value="ACTIVE" />
            <el-option label="库存中" value="INVENTORY" />
          </el-select>
          <el-input v-model="searchQ" placeholder="搜索名称/条码..." clearable style="width:200px" @input="onSearch" />
          <el-button type="primary" @click="loadProducts">刷新</el-button>
          <el-button type="success" @click="printLabels" :disabled="selected.length === 0">
            打印标签 ({{ selected.length }})
          </el-button>
          <el-button :disabled="selected.length === 0" @click="markPrinted">
            标记已打印
          </el-button>
        </div>
      </el-card>

      <el-card shadow="hover" class="select-card">
        <div class="select-header">
          <el-checkbox v-model="allChecked" :indeterminate="indeterminate" @change="toggleAll">全选</el-checkbox>
          <span style="color:#909399;font-size:13px">仅显示未打印商品</span>
        </div>

        <div v-if="loading" style="height:100px;display:flex;align-items:center;justify-content:center">
          <el-icon class="is-loading" :size="24"><component :is="'Loading'" /></el-icon>
        </div>
        <div v-else-if="filteredProducts.length === 0" class="empty-state">
          <p>暂无符合条件的商品</p>
        </div>
        <div v-else class="product-grid">
          <div
            v-for="p in filteredProducts" :key="p.id"
            class="label-item"
            :class="{ selected: selected.includes(p.id) }"
            @click="toggleItem(p.id)"
          >
            <div class="label-barcode">{{ p.barcode || '—' }}</div>
            <div class="label-title">{{ p.title }}</div>
            <div class="label-meta">{{ p.currency || 'USD' }} {{ formatPrice(p.price) }} · 库存 {{ p.stock }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Printable area with real barcodes -->
    <div ref="printArea" class="print-layout">
      <div v-for="p in selectedProducts" :key="'print-' + p.id" class="print-label">
        <div class="print-barcode-wrap">
          <canvas :ref="el => renderBarcode(el, p.barcode)" width="250" height="50"></canvas>
        </div>
        <div class="print-barcode-text">{{ p.barcode }}</div>
        <div class="print-title">{{ p.title }}</div>
        <div class="print-price">{{ p.currency || 'USD' }} {{ formatPrice(p.price) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const products = ref([])
const loading = ref(true)
const selected = ref([])
const statusFilter = ref('')
const searchQ = ref('')
let searchTimer = null

const filteredProducts = computed(() => {
  let list = products.value.filter(p => !p.printedAt)
  if (statusFilter.value) list = list.filter(p => p.status === statusFilter.value)
  if (searchQ.value) {
    const q = searchQ.value.toLowerCase()
    list = list.filter(p =>
      (p.title || '').toLowerCase().includes(q) ||
      (p.barcode || '').toLowerCase().includes(q)
    )
  }
  return list
})

const allChecked = computed({
  get: () => filteredProducts.value.length > 0 && selected.value.length === filteredProducts.value.length,
  set: (val) => { selected.value = val ? filteredProducts.value.map(p => p.id) : [] }
})
const indeterminate = computed(() => selected.value.length > 0 && selected.value.length < filteredProducts.value.length)

const selectedProducts = computed(() => products.value.filter(p => selected.value.includes(p.id)))

function toggleAll(val) { selected.value = val ? filteredProducts.value.map(p => p.id) : [] }
function toggleItem(id) {
  const idx = selected.value.indexOf(id)
  if (idx >= 0) {
    selected.value = selected.value.filter(i => i !== id)
  } else {
    selected.value = [...selected.value, id]
  }
}

function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(loadProducts, 400)
}

function formatPrice(p) { return Number(p || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

// Simple CODE39 barcode renderer
function renderBarcode(canvas, code) {
  if (!canvas || !code) return
  const ctx = canvas.getContext('2d')
  const w = canvas.width
  const h = canvas.height
  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, w, h)

  // CODE39 character patterns (1=wide bar, 0=narrow bar)
  // Each char: 9 bits (1 narrow space between chars)
  const pat = {
    '0': '000110100', '1': '100100001', '2': '001100001', '3': '101100000',
    '4': '000110001', '5': '100110000', '6': '001110000', '7': '000100101',
    '8': '100100100', '9': '001100100', 'A': '100001001', 'B': '001001001',
    'C': '101001000', 'D': '000011001', 'E': '100011000', 'F': '001011000',
    'G': '000001101', 'H': '100001100', 'I': '001001100', 'J': '000011100',
    'K': '100000011', 'L': '001000011', 'M': '101000010', 'N': '000010011',
    'O': '100010010', 'P': '001010010', 'Q': '000000111', 'R': '100000110',
    'S': '001000110', 'T': '000010110', 'U': '110000001', 'V': '011000001',
    'W': '111000000', 'X': '010010001', 'Y': '110010000', 'Z': '011010000',
    '-': '010000101', '.': '110000100', ' ': '011000100', '$': '010101000',
    '/': '010100010', '+': '010001010', '%': '000101010', '*': '010010100'
  }

  const data = '*' + String(code).toUpperCase() + '*'
  const narrow = 2
  const wide = narrow * 2.5

  let x = 10 // left margin
  ctx.fillStyle = '#000'

  for (let ch of data) {
    const pattern = pat[ch]
    if (!pattern) continue
    for (let i = 0; i < 9; i++) {
      const bw = pattern[i] === '1' ? wide : narrow
      if (i % 2 === 0) {
        // Black bar
        ctx.fillRect(x, 5, bw, h - 10)
      }
      x += bw
    }
    // Narrow space between chars
    x += narrow
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await api.get('/products/my', { params: { page: 0, size: 200 } })
    products.value = res.data?.content || []
  } catch (e) { products.value = [] }
  loading.value = false
}

async function markPrinted() {
  if (selected.value.length === 0) return
  try {
    await api.post('/products/mark-printed-batch', { ids: selected.value })
    ElMessage.success(`已标记 ${selected.value.length} 个商品为已打印`)
    selected.value = []
    loadProducts()
  } catch (e) { /* handled */ }
}

function printLabels() {
  // Give canvases time to render
  setTimeout(() => window.print(), 100)
}

onMounted(loadProducts)
</script>

<style scoped>
.no-print { margin-bottom: 16px; }
.filter-card { margin-bottom: 16px; }
.filter-bar { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.select-card { margin-bottom: 16px; }
.select-header { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }
.empty-state { text-align: center; padding: 40px; color: #909399; }

.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 8px; }
.label-item {
  border: 2px solid #eee; border-radius: 8px; padding: 12px; cursor: pointer;
  transition: all 0.15s; position: relative;
}
.label-item:hover { border-color: #409eff; }
.label-item.selected { border-color: #409eff; background: #f0f7ff; }
.label-barcode { font-family: 'Courier New', monospace; font-size: 14px; font-weight: 700; color: #333; margin-bottom: 4px; }
.label-title { font-size: 13px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 2px; }
.label-meta { font-size: 11px; color: #909399; }

/* Print layout hidden on screen */
.print-layout { display: none; }
</style>

<style>
/* Print styles — use global selectors to hide layout outside this component */
@media print {
  @page { margin: 10mm; }
  .no-print { display: none !important; }
  body { background: #fff; }
  .app-aside, .app-header, .el-header, .el-aside, .app-container > .el-container > .el-aside { display: none !important; }
  .app-main, .el-main { padding: 0 !important; margin: 0 !important; }
  .print-layout {
    display: flex !important; flex-wrap: wrap; gap: 4mm;
    padding: 0; justify-content: flex-start;
    background: #fff;
  }
  .print-label {
    width: 55mm; min-height: 30mm; border: 1px dashed #ccc;
    padding: 3mm; box-sizing: border-box; text-align: center;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    page-break-inside: avoid; break-inside: avoid;
  }
  .print-barcode-wrap { margin-bottom: 1mm; }
  .print-barcode-wrap canvas { max-width: 100%; height: auto; }
  .print-barcode-text { font-family: 'Courier New', monospace; font-size: 10pt; letter-spacing: 1px; color: #000; margin-bottom: 1mm; }
  .print-title { font-size: 9pt; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%; margin-bottom: 1mm; }
  .print-price { font-size: 11pt; font-weight: 700; color: #000; }
}
</style>
