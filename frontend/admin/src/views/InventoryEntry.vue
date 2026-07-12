<template>
  <div class="inventory-page">
    <div class="page-toolbar">
      <div class="toolbar-left">
        <h2>商品入库</h2>
        <span class="toolbar-hint">{{ $t('inventory.defaults') }}</span>
      </div>
      <div class="toolbar-right">
        <el-input v-model="searchQuery" :placeholder="$t('inventory.search')" clearable style="width:260px" size="small" @keyup.enter="fetchList(1)">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="small" @click="showAddDialog">
          <el-icon style="margin-right:4px"><Plus /></el-icon>{{ $t('inventory.quickEntry') }}
        </el-button>
      </div>
    </div>

    <el-card class="list-card" shadow="never">
      <el-table :data="entries" v-loading="loading" stripe border style="width:100%" @row-click="showDetail" :empty-text="$t('inventory.noData')">
        <el-table-column prop="barcode" :label="$t('inventory.barcode')" width="160">
          <template #default="{ row }"><span class="barcode-text">{{ row.barcode }}</span></template>
        </el-table-column>
        <el-table-column prop="title" :label="$t('inventory.title')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" :label="$t('inventory.category')" width="90" align="center" />
        <el-table-column prop="grade" :label="$t('inventory.grade')" width="80" align="center" />
        <el-table-column prop="purchasePrice" :label="$t('inventory.purchasePrice')" width="130" align="right">
          <template #default="{ row }">{{ row.currency || 'USD' }} {{ formatNumber(row.purchasePrice) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" :label="$t('inventory.quantity')" width="70" align="center">
          <template #default="{ row }"><el-tag size="small" type="info" effect="plain">{{ row.quantity }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="supplier" :label="$t('inventory.supplier')" width="130" show-overflow-tooltip />
        <el-table-column prop="batchDate" :label="$t('inventory.batchDate')" width="105" align="center" />
        <el-table-column prop="createdAt" :label="$t('inventory.createdAt')" width="100" align="center">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="total, prev, pager, next" background style="margin-top:16px;justify-content:center" @current-change="fetchList" />
    </el-card>

    <!-- ========== 入库对话框 ========== -->
    <el-dialog v-model="addDialog" title="商品入库" width="800px" :close-on-click-modal="false" destroy-on-close>
      <!-- 模式 A: 扫码 -->
      <template v-if="mode === 'scan'">
        <div class="scan-area">
          <div class="scan-icon"><el-icon :size="48"><Search /></el-icon></div>
          <div class="scan-title">扫码或输入条码</div>
          <div class="scan-input-row">
            <el-input ref="scanInput" v-model="barcodeInput" placeholder="扫描或输入13位条码..." size="large" clearable autofocus @input="onScanInput">
              <template #prefix><el-icon :size="20"><Search /></el-icon></template>
            </el-input>
            <div v-if="scanning" class="scan-spinner"><el-icon class="is-loading" :size="20"><Loading /></el-icon></div>
          </div>
          <div v-if="scanStatus" class="scan-status" :class="scanStatus.type">{{ scanStatus.text }}</div>
          <div class="scan-footer">
            <el-button type="primary" link @click="switchToNewProduct">或者新建商品 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
        </div>
      </template>

      <!-- 模式 B: 新建/追加商品 -->
      <template v-if="mode === 'new-product'">
        <el-form ref="formRef" label-width="100px" size="small" label-position="top">
          <div class="barcode-bar">
            <el-space>
              <span style="color:#909399;font-size:12px">条码</span>
              <span class="barcode-text">{{ finalBarcode || '自动生成' }}</span>
              <el-tag v-if="existingProduct" size="small" type="success">已有商品</el-tag>
            </el-space>
            <el-button v-if="!existingProduct" size="small" @click="switchToScan"><el-icon><Search /></el-icon> 扫码</el-button>
          </div>

          <el-card v-if="existingProduct" shadow="never" class="product-info-card">
            <div class="product-info-grid">
              <div class="info-item"><span class="info-label">条码</span><span class="barcode-text">{{ existingProduct.barcode }}</span></div>
              <div class="info-item"><span class="info-label">名称</span><span>{{ existingProduct.title }}</span></div>
              <div class="info-item"><span class="info-label">国家</span><span>{{ existingProduct.country || '-' }}</span></div>
              <div class="info-item"><span class="info-label">面值</span><span>{{ existingProduct.denomination || '-' }}</span></div>
              <div class="info-item"><span class="info-label">年份</span><span>{{ existingProduct.year || '-' }}</span></div>
              <div class="info-item"><span class="info-label">评分</span><span>{{ existingProduct.ratingGrade || '-' }}</span></div>
              <div class="info-item"><span class="info-label">材质</span><span>{{ existingProduct.material || '-' }}</span></div>
              <div class="info-item"><span class="info-label">库存</span><el-tag size="small" type="info">{{ existingProduct.stock || 0 }}</el-tag></div>
            </div>
          </el-card>

          <template v-if="!existingProduct">
            <el-card shadow="never" class="form-section">
              <template #header>商品信息</template>
              <el-row :gutter="16">
                <el-col :span="8"><el-form-item label="国家"><el-select v-model="form.countryCode" filterable remote :remote-method="fetchCountries" :loading="loadingCountries" clearable style="width:100%" @change="onCountryChange"><el-option v-for="c in countryOptions" :key="c.codeValue" :label="c.labelZh || c.labelEn" :value="c.codeValue" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="类别"><el-select v-model="form.categoryCode" filterable clearable style="width:100%" @change="onCategoryChange"><el-option v-for="c in categoryOptions" :key="c.codeValue" :label="c.labelZh || c.labelEn" :value="c.codeValue" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="品种/面值"><el-select v-model="form.denominationCode" filterable allow-create clearable style="width:100%"><el-option v-for="d in denomOptions" :key="d.codeValue" :label="d.labelZh || d.labelEn || d.codeValue" :value="d.codeValue" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="8"><el-form-item label="年份/朝代"><el-input v-if="isModernCat" v-model="form.year" type="number" placeholder="年份" style="width:100%" /><el-select v-else-if="isAncientCat" v-model="form.eraCode" filterable clearable placeholder="朝代" style="width:100%"><el-option v-for="e in eraOptions" :key="e.codeValue" :label="e.labelZh || e.labelEn" :value="e.codeValue" /></el-select><el-input v-else v-model="form.year" type="number" placeholder="年份" style="width:100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="评分"><el-select v-model="form.gradeCode" filterable allow-create clearable style="width:100%"><el-option v-for="g in gradeOptions" :key="g.codeValue" :label="g.labelEn || g.codeValue" :value="g.codeValue" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="材质"><el-select v-model="form.material" filterable allow-create clearable style="width:100%"><el-option v-for="m in materialOptions" :key="m.codeValue" :label="m.labelZh || m.labelEn || m.codeValue" :value="m.codeValue" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="标题"><el-input v-model="form.title" placeholder="商品名称（可选）" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="评级"><div style="display:flex;gap:6px"><el-select v-model="form.ratingCompany" filterable allow-create clearable style="width:130px"><el-option v-for="r in ratingOptions" :key="r.codeValue" :label="r.labelZh || r.labelEn || r.codeValue" :value="r.codeValue" /></el-select><el-input v-model="form.ratingNumber" placeholder="编号" style="flex:1" /></div></el-form-item></el-col>
              </el-row>
              <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
            </el-card>
          </template>

          <el-card shadow="never" class="form-section">
            <template #header>采购信息</template>
            <el-row :gutter="16">
              <el-col :span="6"><el-form-item label="采购价"><el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="1" controls-position="right" style="width:100%" /></el-form-item></el-col>
              <el-col :span="6"><el-form-item label="货币"><el-select v-model="form.currency" style="width:100%"><el-option label="USD" value="USD" /><el-option label="EUR" value="EUR" /><el-option label="CNY" value="CNY" /><el-option label="JPY" value="JPY" /><el-option label="GBP" value="GBP" /></el-select></el-form-item></el-col>
              <el-col :span="6"><el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" :max="9999" controls-position="right" style="width:100%" /></el-form-item></el-col>
              <el-col :span="6"><el-form-item label="日期"><el-date-picker v-model="form.batchDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8"><el-form-item label="供应商"><el-select v-model="form.supplier" filterable allow-create clearable style="width:100%"><el-option v-for="s in supplierOptions" :key="s.codeValue" :label="s.labelZh || s.labelEn || s.codeValue" :value="s.codeValue" /></el-select></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="发票号"><el-input v-model="form.sourceInvoice" placeholder="发票号（可选）" /></el-form-item></el-col>
            </el-row>
          </el-card>
        </el-form>
      </template>

      <template #footer>
        <el-button @click="addDialog = false">取消</el-button>
        <el-button v-if="mode === 'new-product'" type="primary" @click="saveEntry" :loading="saving">{{ existingProduct ? '确认入库' : '保存并入库' }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情/编辑对话框 -->
    <el-dialog v-model="detailDialog" :title="editMode ? '编辑入库商品' : '入库详情'" width="620px" destroy-on-close>
      <template v-if="detailData">
        <!-- 查看模式 -->
        <template v-if="!editMode">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="条码" :span="2"><span class="barcode-text">{{ detailData.barcode }}</span></el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">{{ detailData.title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="类别">{{ detailData.category || '-' }}</el-descriptions-item>
            <el-descriptions-item label="评分">{{ detailData.grade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="面值">{{ detailData.denomination || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年份">{{ detailData.year || '-' }}</el-descriptions-item>
            <el-descriptions-item label="材质">{{ detailData.material || '-' }}</el-descriptions-item>
            <el-descriptions-item label="数量"><el-tag size="small" type="info">{{ detailData.quantity }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="采购价">{{ detailData.currency || 'USD' }} {{ formatNumber(detailData.purchasePrice) }}</el-descriptions-item>
            <el-descriptions-item label="供应商">{{ detailData.supplier || '-' }}</el-descriptions-item>
            <el-descriptions-item label="发票号">{{ detailData.invoiceNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="批次日期">{{ detailData.batchDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(detailData.createdAt) }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="detailData.description" style="margin-top:12px"><div class="detail-label">描述</div><p style="color:#606266;margin:4px 0 0;font-size:13px">{{ detailData.description }}</p></div>
        </template>
        <!-- 编辑模式 -->
        <template v-if="editMode">
          <el-form ref="editFormRef" :model="editForm" label-width="100px" size="small" label-position="top">
            <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="采购价"><el-input-number v-model="editForm.purchasePrice" :min="0" :precision="2" :step="1" controls-position="right" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="货币"><el-select v-model="editForm.currency" style="width:100%"><el-option label="USD" value="USD" /><el-option label="EUR" value="EUR" /><el-option label="CNY" value="CNY" /><el-option label="JPY" value="JPY" /><el-option label="GBP" value="GBP" /></el-select></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="年份"><el-input-number v-model="editForm.year" :min="0" :max="2030" controls-position="right" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="材质"><el-select v-model="editForm.material" filterable allow-create clearable style="width:100%"><el-option v-for="m in materialOptions" :key="m.codeValue" :label="m.labelZh || m.labelEn || m.codeValue" :value="m.codeValue" /></el-select></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="供应商"><el-input v-model="editForm.supplier" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="发票号"><el-input v-model="editForm.invoiceNo" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="2" /></el-form-item>
          </el-form>
        </template>
      </template>
      <template #footer>
        <template v-if="!editMode">
          <el-button type="primary" @click="enterEditMode">编辑</el-button>
          <el-button @click="detailDialog = false">关闭</el-button>
        </template>
        <template v-if="editMode">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveEdit" :loading="savingEdit">保存修改</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import { Search, Plus, Loading, ArrowRight } from '@element-plus/icons-vue'

const entries = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const size = 20
const searchQuery = ref('')

async function fetchList(targetPage) {
  if (targetPage) page.value = targetPage
  loading.value = true
  try {
    const p = page.value - 1
    const q = searchQuery.value ? '&q=' + encodeURIComponent(searchQuery.value) : ''
    const res = await api.get('/admin/inventory/entries?page=' + p + '&size=' + size + q)
    entries.value = res.data?.content || res.data || []
    total.value = res.data?.totalElements || entries.value.length
  } catch (e) { entries.value = []; total.value = 0 }
  loading.value = false
}

function formatDate(d) { return d ? new Date(d).toLocaleDateString() : '-' }
function formatNumber(n) { return n != null ? Number(n).toFixed(2) : '0.00' }

const addDialog = ref(false)
const detailDialog = ref(false)
const detailData = ref(null)
const formRef = ref(null)
const saving = ref(false)
const scanInput = ref(null)
const mode = ref('scan')

// Scan mode
const barcodeInput = ref('')
const scanning = ref(false)
const scanStatus = ref(null)
let scanTimer = null
const existingProduct = ref(null)

function onScanInput() {
  if (scanTimer) clearTimeout(scanTimer)
  scanStatus.value = null
  existingProduct.value = null
  if (barcodeInput.value.trim().length < 13) return
  scanning.value = true
  scanTimer = setTimeout(function() { lookupBarcode(barcodeInput.value.trim()) }, 500)
}

async function lookupBarcode(barcode) {
  try {
    const res = await api.get('/admin/products/barcode/' + encodeURIComponent(barcode))
    const product = res.data
    if (!product || !product.id) {
      scanStatus.value = { type: 'warning', text: '未找到条码 ' + barcode + ' 的商品' }
      setTimeout(function() { form.barcode = barcode; switchToNewProduct() }, 1200)
      return
    }
    existingProduct.value = product
    scanStatus.value = { type: 'success', text: '已找到：' + (product.title || product.barcode) }
    form.barcode = product.barcode; form.title = product.title || ''
    form.material = product.material || ''; form.year = product.year || ''
    form.ratingCompany = product.ratingCompany || ''; form.ratingNumber = product.ratingNumber || ''
    form.ratingGrade = product.ratingGrade || ''; form.supplier = product.supplier || ''
    form.sourceInvoice = product.sourceInvoice || ''; form.description = product.description || ''
    setTimeout(function() { switchToNewProduct() }, 800)
  } catch (e) { scanStatus.value = { type: 'error', text: '查询失败' } }
  finally { scanning.value = false }
}

function switchToScan() { mode.value = 'scan'; barcodeInput.value = ''; scanStatus.value = null; existingProduct.value = null; nextTick(function() { if (scanInput.value) scanInput.value.focus() }) }
function switchToNewProduct() { mode.value = 'new-product' }

// Barcode
function padValue(v, len) { if (!v) return '0'.repeat(len); var t = String(v).trim(); return t.length >= len ? t.substring(0, len) : '0'.repeat(len - t.length) + t }
function luhnCheckDigit(digits) { var s = 0, alt = true; for (var i = digits.length - 1; i >= 0; i--) { var n = Number(digits[i]); if (alt) { n *= 2; if (n > 9) n = (n % 10) + 1 } s += n; alt = !alt } return String((10 - (s % 10)) % 10) }
function computeBarcode() {
  if (form.barcode) return form.barcode.trim()
  var c = padValue(form.countryCode, 3); var cat = padValue(form.categoryCode, 1)
  var d = padValue(form.denominationCode, 3)
  var ey = form.eraCode ? padValue(form.eraCode, 3) : form.year ? padValue(String(form.year % 1000), 3) : '000'
  var g = padValue(form.gradeCode, 2); var body = c + cat + d + ey + g
  return body ? body + luhnCheckDigit(body) : ''
}
var finalBarcode = computed(function() { return existingProduct.value ? existingProduct.value.barcode : (computeBarcode() || '请选择国家/类别/面值/评级') })

// New product form
const defaultForm = { barcode: '', countryCode: '', categoryCode: '', gradeCode: '', denominationCode: '', eraCode: '', year: '', material: '', title: '', ratingCompany: '', ratingNumber: '', ratingGrade: '', purchasePrice: null, currency: 'USD', quantity: 1, batchDate: '', supplier: '', sourceInvoice: '', description: '' }
const form = reactive({ ...defaultForm })
const countryOptions = ref([])
const categoryOptions = ref([])
const denomOptions = ref([])
const eraOptions = ref([])
const gradeOptions = ref([])
const materialOptions = ref([])
const ratingOptions = ref([])
const supplierOptions = ref([])
const loadingCountries = ref(false)
const isModernCat = computed(function() { return ['1', '4', '5'].includes(form.categoryCode) })
const isAncientCat = computed(function() { return form.categoryCode === '2' })

async function fetchCodeItems(path) { try { var r = await api.get(path); return r.data || r || [] } catch (e) { return [] } }
async function loadCodeOptions() {
  loadingCountries.value = true
  try {
    countryOptions.value = await fetchCodeItems('/admin/barcode-codes/type/COUNTRY/active')
    categoryOptions.value = await fetchCodeItems('/admin/barcode-codes/type/CATEGORY/active')
    gradeOptions.value = await fetchCodeItems('/admin/barcode-codes/type/GRADE/active')
    eraOptions.value = await fetchCodeItems('/admin/barcode-codes/type/ERA/active')
    materialOptions.value = await fetchCodeItems('/admin/barcode-codes/type/MATERIAL/active')
    ratingOptions.value = await fetchCodeItems('/admin/barcode-codes/type/RATING_COMPANY/active')
    supplierOptions.value = await fetchCodeItems('/admin/barcode-codes/type/SUPPLIER/active')
  } finally { loadingCountries.value = false }
}
async function fetchCountries() { if (!countryOptions.value.length) await loadCodeOptions() }
async function loadDenomOptions(c) { denomOptions.value = c ? await fetchCodeItems('/admin/barcode-codes/parent?parentType=COUNTRY&parentValue=' + encodeURIComponent(c)) : [] }
function onCountryChange(v) { form.denominationCode = ''; loadDenomOptions(v) }
function onCategoryChange() { form.eraCode = ''; form.year = '' }

// Save
function resetForm() { Object.assign(form, JSON.parse(JSON.stringify(defaultForm))); existingProduct.value = null; barcodeInput.value = ''; scanStatus.value = null; mode.value = 'scan' }
function showAddDialog() { resetForm(); addDialog.value = true; nextTick(function() { if (scanInput.value) scanInput.value.focus() }) }
function normalizeForm() {
  var b = form.barcode?.trim() || computeBarcode()
  return { barcode: b || undefined, countryCode: form.countryCode || undefined, categoryCode: form.categoryCode || undefined, denominationCode: form.denominationCode || undefined, eraCode: form.eraCode || undefined, gradeCode: form.gradeCode || undefined, title: form.title || undefined, description: form.description || undefined, material: form.material || undefined, year: form.year || undefined, ratingCompany: form.ratingCompany || undefined, ratingNumber: form.ratingNumber || undefined, ratingGrade: form.ratingGrade || undefined, quantity: form.quantity || 1, purchasePrice: form.purchasePrice || undefined, currency: form.currency || 'USD', supplier: form.supplier || undefined, invoiceNo: form.sourceInvoice || undefined, batchDate: form.batchDate || undefined }
}
async function saveEntry() {
  saving.value = true
  try { await api.post('/admin/inventory/entry', normalizeForm()); ElMessage.success('入库成功'); addDialog.value = false; fetchList() }
  catch (e) { /* interceptor handles */ }
  finally { saving.value = false }
}
function showDetail(row) {
  detailData.value = row
  editMode.value = false
  editForm.title = row.title || ''
  editForm.purchasePrice = row.purchasePrice
  editForm.currency = row.currency || 'USD'
  editForm.year = row.year
  editForm.material = row.material || ''
  editForm.supplier = row.supplier || ''
  editForm.invoiceNo = row.invoiceNo || ''
  editForm.description = row.description || ''
  detailDialog.value = true
}

// Edit mode
const editMode = ref(false)
const editFormRef = ref(null)
const savingEdit = ref(false)
const editForm = reactive({ title: '', purchasePrice: null, currency: 'USD', year: null, material: '', supplier: '', invoiceNo: '', description: '' })

async function enterEditMode() { editMode.value = true }
function cancelEdit() { editMode.value = false }

async function saveEdit() {
  savingEdit.value = true
  try {
    // 更新商品信息（标题、描述等）
    await api.put('/admin/products/' + detailData.value.productId, {
      title: editForm.title,
      description: editForm.description,
      currency: editForm.currency
    })
    // 更新批次信息（采购价、供应商、发票号）— 这才是列表显示的字段
    await api.put('/admin/inventory/batch/' + detailData.value.id, {
      purchasePrice: editForm.purchasePrice,
      currency: editForm.currency,
      supplier: editForm.supplier,
      invoiceNo: editForm.invoiceNo
    })
    ElMessage.success('保存成功')
    editMode.value = false
    detailDialog.value = false
    fetchList()
  } catch (e) { /* handled */ }
  finally { savingEdit.value = false }
}

onMounted(function() { fetchList(); loadCodeOptions() })
</script>

<style scoped>
.page-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; flex-wrap: wrap; gap: 12px;
}
.toolbar-left { display: flex; align-items: baseline; gap: 12px; }
.toolbar-left h2 { font-size: 18px; font-weight: 600; color: #303133; margin: 0; }
.toolbar-hint { font-size: 12px; color: #909399; }
.toolbar-right { display: flex; gap: 8px; align-items: center; }
.list-card { border-radius: 8px; border: 1px solid #ebeef5; }
.barcode-text { font-family: 'Courier New', monospace; font-size: 13px; color: #409eff; font-weight: 700; letter-spacing: 1.5px; }
.detail-label { font-size: 13px; font-weight: 600; color: #606266; padding-bottom: 4px; border-bottom: 1px solid #ebeef5; }

.scan-area { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; gap: 16px; }
.scan-icon { color: #409eff; opacity: 0.6; }
.scan-title { font-size: 18px; font-weight: 600; color: #303133; }
.scan-input-row { display: flex; align-items: center; gap: 12px; width: 100%; max-width: 420px; }
.scan-input-row .el-input { flex: 1; }
.scan-input-row .el-input :deep(.el-input__wrapper) { border-radius: 8px; }
.scan-spinner { color: #409eff; }
.scan-status { font-size: 14px; padding: 8px 16px; border-radius: 6px; max-width: 420px; }
.scan-status.success { background: #f0f9eb; color: #67c23a; }
.scan-status.warning { background: #fdf6ec; color: #e6a23c; }
.scan-status.error { background: #fef0f0; color: #f56c6c; }
.scan-footer { margin-top: 8px; }

.barcode-bar { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; background: #f0f5ff; border: 1px dashed #b3d8ff; border-radius: 6px; margin-bottom: 16px; }
.product-info-card { margin-bottom: 16px; border: 1px solid #e8eaed; border-radius: 6px; }
.product-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px; }
.info-item { display: flex; align-items: center; gap: 8px; padding: 3px 0; }
.info-label { color: #909399; min-width: 50px; font-size: 12px; }
.form-section { margin-bottom: 16px; border: 1px solid #e8eaed; border-radius: 6px; }
.form-section :deep(.el-card__header) { padding: 10px 16px; background: #f5f7fa; border-bottom: 1px solid #e8eaed; font-size: 14px; font-weight: 600; color: #303133; }
</style>
