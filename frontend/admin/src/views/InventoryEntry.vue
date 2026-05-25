<template>
  <div class="inventory-page">
    <h2 style="margin-bottom:16px">📦 {{ $t('admin.menuSub.inventory') }}</h2>

    <el-row :gutter="20">
      <!-- Left: Entry Form -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div style="display:flex; justify-content:space-between; align-items:center">
              <span>快速入库 / Quick Entry</span>
              <el-tag v-if="generatedBarcode" type="success" effect="dark" style="font-size:16px; letter-spacing:2px; font-family:monospace">
                {{ generatedBarcode }}
              </el-tag>
            </div>
          </template>

          <el-form ref="formRef" :model="form" label-width="110px" size="default">
            <el-row :gutter="12">
              <!-- Country selector with remote search -->
              <el-col :span="8">
                <el-form-item label="Country" prop="countryCode">
                  <el-select
                    v-model="form.countryCode"
                    filterable
                    remote
                    :remote-method="searchCountries"
                    :loading="loadingCountries"
                    clearable
                    placeholder="国家"
                    style="width:100%"
                    @change="onCountryChange"
                  >
                    <el-option v-for="c in countryOptions" :key="c.codeValue" :label="c.labelZh || c.labelEn" :value="c.codeValue" />
                  </el-select>
                </el-form-item>
              </el-col>

              <!-- Category selector -->
              <el-col :span="8">
                <el-form-item label="Category" prop="categoryCode">
                  <el-select v-model="form.categoryCode" filterable placeholder="类别" style="width:100%" @change="onCategoryChange">
                    <el-option v-for="c in categoryOptions" :key="c.codeValue" :label="c.labelZh || c.labelEn" :value="c.codeValue" />
                  </el-select>
                </el-form-item>
              </el-col>

              <!-- Grade selector -->
              <el-col :span="8">
                <el-form-item label="Grade" prop="gradeCode">
                  <el-select v-model="form.gradeCode" filterable allow-create clearable placeholder="评分" style="width:100%">
                    <el-option-group v-for="group in gradeGroups" :key="group.label" :label="group.label">
                      <el-option v-for="g in group.options" :key="g.codeValue" :label="g.labelEn || g.codeValue" :value="g.codeValue" />
                    </el-option-group>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <!-- Denomination (filtered by country) -->
              <el-col :span="8">
                <el-form-item label="Variety" prop="denominationCode">
                  <el-select v-model="form.denominationCode" filterable allow-create clearable placeholder="品种/面值" style="width:100%">
                    <el-option v-for="d in denomOptions" :key="d.codeValue" :label="d.labelZh || d.labelEn" :value="d.codeValue" />
                  </el-select>
                </el-form-item>
              </el-col>

              <!-- Year / Era -->
              <el-col :span="8">
                <el-form-item label="Year/Era" prop="eraCode">
                  <div style="display:flex; gap:4px">
                    <el-input v-if="form.categoryCode === '1' || form.categoryCode === '4' || form.categoryCode === '5'"
                      v-model="form.year" type="number" placeholder="铸造年份" style="width:100%" />
                    <el-select v-else-if="form.categoryCode === '2'"
                      v-model="form.eraCode" filterable clearable placeholder="朝代/年号" style="width:100%">
                      <el-option v-for="e in eraOptions" :key="e.codeValue" :label="e.labelZh || e.labelEn" :value="e.codeValue" />
                    </el-select>
                    <el-input v-else v-model="form.year" type="number" placeholder="年份" style="width:100%" />
                  </div>
                </el-form-item>
              </el-col>

              <!-- Material -->
              <el-col :span="8">
                <el-form-item label="Material" prop="material">
                  <el-select v-model="form.material" filterable allow-create clearable placeholder="材质" style="width:100%">
                    <el-option label="Gold" value="Gold" />
                    <el-option label="Silver" value="Silver" />
                    <el-option label="Copper" value="Copper" />
                    <el-option label="Bronze" value="Bronze" />
                    <el-option label="Nickel" value="Nickel" />
                    <el-option label="Platinum" value="Platinum" />
                    <el-option label="Palladium" value="Palladium" />
                    <el-option label="Bimetallic" value="Bimetallic" />
                    <el-option label="Iron" value="Iron" />
                    <el-option label="Lead" value="Lead" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="Title" prop="title">
                  <el-input v-model="form.title" placeholder="商品标题 (可选)" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Rating">
                  <div style="display:flex; gap:4px">
                    <el-select v-model="form.ratingCompany" clearable placeholder="评级公司" style="width:120px">
                      <el-option label="NGC" value="NGC" />
                      <el-option label="PCGS" value="PCGS" />
                      <el-option label="PMG" value="PMG" />
                      <el-option label="ANACS" value="ANACS" />
                      <el-option label="ICG" value="ICG" />
                    </el-select>
                    <el-input v-model="form.ratingNumber" placeholder="编号" style="flex:1" />
                  </div>
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">采购信息 Purchase Info</el-divider>

            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item label="Purchase Price" prop="purchasePrice">
                  <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="1" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Currency">
                  <el-select v-model="form.currency" style="width:100%">
                    <el-option label="USD" value="USD" />
                    <el-option label="EUR" value="EUR" />
                    <el-option label="CNY" value="CNY" />
                    <el-option label="JPY" value="JPY" />
                    <el-option label="GBP" value="GBP" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Qty" prop="quantity">
                  <el-input-number v-model="form.quantity" :min="1" :max="9999" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Date">
                  <el-date-picker v-model="form.batchDate" type="date" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="Supplier">
                  <el-input v-model="form.supplier" placeholder="供应商名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Invoice No.">
                  <el-input v-model="form.invoiceNo" placeholder="发票/收据号" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="Description">
              <el-input v-model="form.description" type="textarea" :rows="2" placeholder="备注信息 (可选)" />
            </el-form-item>

            <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:8px">
              <el-button @click="previewBarcode" :disabled="!canGenerateBarcode">
                🔍 预览条码
              </el-button>
              <el-button type="primary" size="large" @click="submitEntry" :loading="saving" :disabled="!canGenerateBarcode">
                ✅ 入库确认
              </el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <!-- Right: Barcode Preview & Recent -->
      <el-col :span="8">
        <!-- Barcode Preview -->
        <el-card style="margin-bottom:16px; text-align:center">
          <template #header>条码预览 / Barcode</template>
          <div v-if="generatedBarcode" class="barcode-preview">
            <div class="barcode-digits">{{ generatedBarcode }}</div>
            <div class="barcode-labels">
              <div class="label-row">
                <span>{{ countryLabel }}</span>
                <span>{{ categoryLabel }}</span>
                <span>{{ denomLabel }}</span>
                <span>{{ eraYearLabel }}</span>
                <span>{{ gradeLabel }}</span>
              </div>
              <div class="pos-row">
                <span>[1-3]</span><span>[4]</span><span>[5-7]</span><span>[8-10]</span><span>[11-12]</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="输入属性后自动生成" />
        </el-card>

        <!-- Recent Entries -->
        <el-card>
          <template #header>最近入库 / Recent</template>
          <div v-if="recentEntries.length">
            <div v-for="e in recentEntries" :key="e.id" class="recent-item">
              <div class="recent-barcode">{{ e.barcode }}</div>
              <div class="recent-info">
                <span>{{ e.country }} · {{ e.quantity }}pcs</span>
                <span class="recent-price">{{ e.currency }} {{ e.purchasePrice }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无记录" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const formRef = ref(null)
const saving = ref(false)

// --- Code table data ---
const countryOptions = ref([])
const categoryOptions = ref([])
const denomOptions = ref([])
const eraOptions = ref([])
const gradeGroups = ref([])
const loadingCountries = ref(false)
const allCountries = ref([])

const form = reactive({
  countryCode: '',
  categoryCode: '',
  denominationCode: '',
  eraCode: '',
  gradeCode: '',
  year: null,
  material: '',
  title: '',
  description: '',
  ratingCompany: '',
  ratingNumber: '',
  purchasePrice: 0,
  currency: 'USD',
  quantity: 1,
  batchDate: new Date(),
  supplier: '',
  invoiceNo: '',
})

const recentEntries = ref([])
const generatedBarcode = ref('')

const canGenerateBarcode = computed(() => {
  return form.countryCode && form.categoryCode
})

const countryLabel = computed(() => {
  const c = countryOptions.value.find(x => x.codeValue === form.countryCode)
  return c ? (c.labelZh || c.labelEn || c.codeValue) : (form.countryCode || '?')
})
const categoryLabel = computed(() => {
  const c = categoryOptions.value.find(x => x.codeValue === form.categoryCode)
  return c ? (c.labelZh || c.labelEn || c.codeValue) : (form.categoryCode || '?')
})
const denomLabel = computed(() => form.denominationCode || '000')
const eraYearLabel = computed(() => form.eraCode || (form.year ? String(form.year % 1000).padStart(3,'0') : '000'))
const gradeLabel = computed(() => form.gradeCode || '00')

onMounted(async () => {
  await loadCountries()
  await loadCategories()
  loadRecent()
})

async function loadCountries() {
  try {
    const res = await api.get('/admin/barcode-codes/type/COUNTRY/active')
    allCountries.value = res.data || []
    countryOptions.value = [...allCountries.value]
  } catch (_) {}
}

function searchCountries(query) {
  if (!query) {
    countryOptions.value = [...allCountries.value]
    return
  }
  loadingCountries.value = true
  const q = query.toLowerCase()
  countryOptions.value = allCountries.value.filter(c =>
    (c.labelEn && c.labelEn.toLowerCase().includes(q)) ||
    (c.labelZh && c.labelZh.toLowerCase().includes(q)) ||
    c.codeValue.includes(q)
  )
  loadingCountries.value = false
}

async function loadCategories() {
  try {
    const res = await api.get('/admin/barcode-codes/type/CATEGORY/active')
    categoryOptions.value = res.data || []
  } catch (_) {}
}

async function onCountryChange() {
  form.denominationCode = ''
  if (form.countryCode) {
    try {
      const res = await api.get('/admin/barcode-codes/parent', { params: { parentType: 'COUNTRY', parentValue: form.countryCode } })
      denomOptions.value = res.data || []
    } catch (_) { denomOptions.value = [] }
  } else {
    denomOptions.value = []
  }
  updateBarcode()
}

function onCategoryChange() {
  if (form.categoryCode === '2') {
    // Ancient coin — load era codes
    loadEras()
  } else {
    form.eraCode = ''
  }
  // Load grade options
  loadGrades()
  updateBarcode()
}

async function loadEras() {
  try {
    const res = await api.get('/admin/barcode-codes/type/ERA/active')
    eraOptions.value = res.data || []
  } catch (_) { eraOptions.value = [] }
}

async function loadGrades() {
  try {
    const res = await api.get('/admin/barcode-codes/type/GRADE/active')
    const all = res.data || []
    // Split into groups for display
    const minted = all.filter(g => parseInt(g.codeValue) >= 1 && parseInt(g.codeValue) <= 70)
    const ancient = all.filter(g => parseInt(g.codeValue) >= 1 && parseInt(g.codeValue) <= 5 && g.labelEn?.includes('Fine'))
    const uncertified = all.filter(g => g.codeValue === '00')
    gradeGroups.value = [
      { label: '评级 Minted', options: minted },
      { label: '古钱 Ancient', options: ancient },
      { label: '其他', options: uncertified },
    ]
  } catch (_) { gradeGroups.value = [] }
}

// Auto-update barcode on field changes
watch(() => [form.countryCode, form.categoryCode, form.denominationCode, form.eraCode, form.year, form.gradeCode], updateBarcode)

async function updateBarcode() {
  if (!canGenerateBarcode.value) {
    generatedBarcode.value = ''
    return
  }
  try {
    const res = await api.post('/admin/inventory/generate-barcode', form)
    generatedBarcode.value = res.data || ''
  } catch (_) {}
}

async function previewBarcode() {
  updateBarcode()
}

async function submitEntry() {
  saving.value = true
  try {
    const res = await api.post('/admin/inventory/entry', form)
    ElMessage.success(`入库成功！条码: ${res.data.barcode}`)
    // Reset form
    Object.assign(form, {
      countryCode: '', categoryCode: '', denominationCode: '', eraCode: '', gradeCode: '',
      year: null, material: '', title: '', description: '',
      ratingCompany: '', ratingNumber: '',
      purchasePrice: 0, currency: 'USD', quantity: 1,
      batchDate: new Date(), supplier: '', invoiceNo: '',
    })
    generatedBarcode.value = ''
    loadRecent()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '入库失败')
  } finally { saving.value = false }
}

async function loadRecent() {
  try {
    const res = await api.get('/admin/inventory/entries', { params: { page: 0, size: 5 } })
    recentEntries.value = res.data?.content || []
  } catch (_) {}
}
</script>

<style scoped>
.inventory-page { padding: 4px; }
.barcode-preview {
  padding: 20px 0;
}
.barcode-digits {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 3px;
  color: #111827;
  background: #f3f4f6;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}
.barcode-labels { font-size: 11px; color: #6b7280; }
.label-row, .pos-row {
  display: flex;
  gap: 2px;
  justify-content: center;
}
.label-row span, .pos-row span {
  flex: 1;
  text-align: center;
  font-size: 11px;
}
.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}
.recent-item:last-child { border-bottom: none; }
.recent-barcode {
  font-family: monospace;
  font-size: 13px;
  font-weight: 600;
}
.recent-info {
  text-align: right;
  font-size: 12px;
  color: #6b7280;
}
.recent-price {
  display: block;
  font-weight: 600;
  color: #059669;
}
</style>
