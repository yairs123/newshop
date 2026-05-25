<template>
  <div>
    <div class="page-header">
      <h3>批量导入图片</h3>
      <div class="header-actions">
        <el-select v-model="dateGroup" style="width:140px" @change="loadProducts">
          <el-option label="今日" value="today" />
          <el-option label="昨日" value="yesterday" />
          <el-option label="本周" value="this-week" />
          <el-option label="本月" value="this-month" />
          <el-option label="全部" value="all" />
        </el-select>
        <el-button type="primary" @click="loadProducts">刷新</el-button>
        <el-button @click="$router.push('/products')">← 返回商品列表</el-button>
      </div>
    </div>

    <!-- Product image status -->
    <div class="status-summary no-print" v-if="!loading">
      <div class="status-card">
        <span class="status-num">{{ products.length }}</span>
        <span class="status-label">本批商品</span>
      </div>
      <div class="status-card success">
        <span class="status-num">{{ products.filter(p => p.images?.length > 0).length }}</span>
        <span class="status-label">已有图片</span>
      </div>
      <div class="status-card warning">
        <span class="status-num">{{ products.filter(p => !p.images || p.images.length === 0).length }}</span>
        <span class="status-label">缺少图片</span>
      </div>
    </div>

    <!-- Date-grouped product list -->
    <div class="product-list no-print" v-loading="loading">
      <el-collapse v-model="openGroups">
        <el-collapse-item
          v-for="group in dateGroups"
          :key="group.date"
          :title="group.label + ' (' + group.products.length + ')'"
          :name="group.date"
        >
          <div v-if="group.products.length === 0" style="color:#999; padding:12px; text-align:center">暂无商品</div>
          <el-table v-else :data="group.products" stripe size="small" @row-click="selectForUpload">
            <el-table-column label="图片" width="60">
              <template #default="{ row }">
                <el-image
                  v-if="row.images && row.images.length > 0"
                  :src="row.images[0]"
                  style="width:36px; height:36px; border-radius:4px"
                  fit="cover"
                />
                <div v-else class="no-img-icon">📷</div>
              </template>
            </el-table-column>
            <el-table-column prop="barcode" label="条码" width="130" />
            <el-table-column prop="title" label="名称" min-width="180" />
            <el-table-column prop="country" label="国家" width="100" />
            <el-table-column label="图片数" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.images?.length > 0 ? 'success' : 'danger'" size="small" effect="plain">
                  {{ row.images?.length || 0 }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </div>

    <el-alert
      title="命名规则"
      type="info"
      show-icon
      :closable="false"
      style="margin-top:16px"
      description="文件名必须包含商品条码，格式如：762202220222.01.jpg、762202220222.02.png 或 762202220222_01.jpg。系统会自动解析条码匹配商品。"
    />

    <div class="import-layout" style="margin-top:16px">
      <!-- Drop zone -->
      <div
        class="drop-zone"
        :class="{ 'drop-active': dragging }"
        @dragenter.prevent="dragging = true"
        @dragover.prevent="dragging = true"
        @dragleave.prevent="dragging = false"
        @drop.prevent="onDrop"
        @click="fileInput.click()"
      >
        <input
          ref="fileInput"
          type="file"
          multiple
          accept="image/*"
          style="display:none"
          @change="onFileSelect"
        />
        <div class="drop-icon">📁</div>
        <p class="drop-text">拖拽图片到此处，或点击选择文件</p>
        <p class="drop-hint">支持 JPG、PNG、GIF、WebP，文件名需包含条码</p>
      </div>

      <!-- File list before import -->
      <div v-if="pendingFiles.length > 0" class="file-list">
        <h4>待导入文件 ({{ pendingFiles.length }})</h4>
        <div class="file-grid">
          <div v-for="(f, i) in pendingFiles" :key="i" class="file-card" :class="f.status">
            <img :src="f.url" class="file-preview" />
            <div class="file-info">
              <span class="file-name">{{ f.name }}</span>
              <span class="file-barcode" v-if="f.barcode">条码: {{ f.barcode }}</span>
              <span class="file-barcode" v-else style="color:#dc2626">未识别条码</span>
            </div>
            <el-tag v-if="f.status === 'matched'" size="small" type="success">已匹配</el-tag>
            <el-tag v-else-if="f.status === 'unmatched'" size="small" type="danger">未匹配</el-tag>
            <el-tag v-else size="small" type="info">待检查</el-tag>
          </div>
        </div>
        <div style="margin-top:16px; text-align:center">
          <el-button
            type="primary"
            size="large"
            :loading="importing"
            :disabled="matchedCount === 0"
            @click="startImport"
          >
            导入 {{ matchedCount }} 个已匹配文件
          </el-button>
        </div>
      </div>

      <!-- Import results -->
      <div v-if="importResult" class="import-result">
        <el-alert
          :title="`导入完成：成功 ${importResult.successCount} 个，失败 ${importResult.failCount} 个`"
          :type="importResult.failCount === 0 ? 'success' : 'warning'"
          show-icon
        />
        <div v-if="importResult.succeeded?.length" style="margin-top:12px">
          <h4>导入成功 ({{ importResult.succeeded.length }})</h4>
          <div class="file-grid">
            <div v-for="(s, i) in importResult.succeeded" :key="i" class="file-card success">
              <div class="file-info">
                <span class="file-name">{{ s.file }}</span>
                <span class="file-barcode">→ {{ s.barcode }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="importResult.failed?.length" style="margin-top:12px">
          <h4 style="color:#dc2626">导入失败 ({{ importResult.failed.length }})</h4>
          <div class="file-grid">
            <div v-for="(f, i) in importResult.failed" :key="i" class="file-card fail">
              <div class="file-info">
                <span class="file-name">{{ f.file }}</span>
                <span class="file-barcode" style="color:#dc2626">{{ f.reason }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-button style="margin-top:16px" @click="resetImport">继续导入</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const fileInput = ref(null)
const dragging = ref(false)
const pendingFiles = ref([])
const importing = ref(false)
const importResult = ref(null)
const dateGroup = ref('today')
const products = ref([])
const loading = ref(false)
const openGroups = ref([])

const matchedCount = computed(() => pendingFiles.value.filter(f => f.status === 'matched').length)

// Date groups for product list
const dateGroups = computed(() => {
  const groups = {}
  for (const p of products.value) {
    const d = p.createdAt ? p.createdAt.slice(0, 10) : 'unknown'
    if (!groups[d]) groups[d] = []
    groups[d].push(p)
  }
  return Object.entries(groups).map(([date, prods]) => ({
    date,
    label: date === new Date().toISOString().slice(0, 10) ? date + ' (今日)' : date,
    products: prods,
  }))
})

onMounted(() => loadProducts())

async function loadProducts() {
  loading.value = true
  try {
    const range = getDateRange()
    const params = { page: 0, size: 500 }
    if (range.from) params.dateFrom = range.from
    if (range.to) params.dateTo = range.to
    const res = await api.get('/admin/products', { params })
    products.value = res.data?.content || []
    if (dateGroups.value.length > 0) {
      openGroups.value = dateGroups.value.map(g => g.date)
    }
  } catch (_) { products.value = [] }
  loading.value = false
}

function getDateRange() {
  const now = new Date()
  const today = now.toISOString().slice(0, 10)
  if (dateGroup.value === 'today') return { from: today, to: today }
  if (dateGroup.value === 'yesterday') {
    const y = new Date(now); y.setDate(y.getDate() - 1)
    return { from: y.toISOString().slice(0, 10), to: y.toISOString().slice(0, 10) }
  }
  if (dateGroup.value === 'this-week') {
    const m = new Date(now); m.setDate(m.getDate() - m.getDay())
    return { from: m.toISOString().slice(0, 10), to: today }
  }
  if (dateGroup.value === 'this-month') {
    return { from: now.toISOString().slice(0, 7) + '-01', to: today }
  }
  return { from: null, to: null }
}

function selectForUpload(row) {
  // Pre-fill: when clicking a product without images, user can upload for it
  if (row.barcode) {
    // We'll highlight this barcode as the target
  }
}

function extractBarcode(name) {
  const match = name.match(/^(\d{13})[._]/)
  return match ? match[1] : null
}

function addFiles(files) {
  importResult.value = null
  for (const file of files) {
    const name = file.name
    const barcode = extractBarcode(name)
    pendingFiles.value.push({
      name,
      file,
      barcode,
      url: URL.createObjectURL(file),
      status: barcode ? 'matched' : 'unmatched',
    })
  }
}

function onDrop(e) {
  dragging.value = false
  if (e.dataTransfer?.files?.length) {
    addFiles(Array.from(e.dataTransfer.files))
  }
}

function onFileSelect(e) {
  if (e.target?.files?.length) {
    addFiles(Array.from(e.target.files))
  }
  e.target.value = ''
}

async function startImport() {
  const matched = pendingFiles.value.filter(f => f.status === 'matched')
  if (matched.length === 0) return

  importing.value = true
  try {
    const formData = new FormData()
    for (const f of matched) {
      formData.append('files', f.file)
    }
    const res = await api.post('/files/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000,
    })
    importResult.value = res.data
    pendingFiles.value = []
    ElMessage.success(`导入完成：${res.data.successCount} 成功`)
    // Refresh product list
    loadProducts()
  } catch (e) {
    ElMessage.error('导入失败：' + (e.response?.data?.message || e.message))
  }
  importing.value = false
}

function resetImport() {
  pendingFiles.value = []
  importResult.value = null
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.status-summary {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.status-card {
  background: #fff;
  border-radius: 10px;
  padding: 12px 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  min-width: 100px;
}
.status-num {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}
.status-label {
  font-size: 11px;
  color: #6b7280;
  text-transform: uppercase;
}
.status-card.success .status-num { color: #059669; }
.status-card.warning .status-num { color: #d97706; }

.product-list {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 16px;
  max-height: 500px;
  overflow-y: auto;
}

.no-img-icon {
  width: 36px;
  height: 36px;
  background: #f3f4f6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.import-layout {
  max-width: 900px;
  margin: 0 auto;
}

.drop-zone {
  border: 2px dashed #d9d9d9;
  border-radius: 16px;
  padding: 36px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}
.drop-zone:hover, .drop-active {
  border-color: #409eff;
  background: #f0f7ff;
}
.drop-icon { font-size: 40px; margin-bottom: 8px; }
.drop-text { font-size: 15px; color: #333; margin-bottom: 4px; }
.drop-hint { font-size: 12px; color: #999; }

.file-list, .import-result {
  margin-top: 20px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 8px;
  margin-top: 8px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fff;
}
.file-card.success { border-color: #b7eb8f; background: #f6ffed; }
.file-card.fail { border-color: #ffccc7; background: #fff2f0; }

.file-preview {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}
.file-name {
  display: block;
  font-size: 12px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-barcode {
  display: block;
  font-size: 11px;
  color: #666;
  margin-top: 2px;
}
</style>
