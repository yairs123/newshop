<template>
  <div class="products-page">
    <!-- Stats Cards -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">📦</div>
        <div class="stat-info">
          <span class="stat-label">Total Products</span>
          <span class="stat-value">{{ total }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-active">✅</div>
        <div class="stat-info">
          <span class="stat-label">Active</span>
          <span class="stat-value">{{ products.filter(p => p.status === 'ACTIVE').length }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-inactive">⏸️</div>
        <div class="stat-info">
          <span class="stat-label">Inactive</span>
          <span class="stat-value">{{ products.filter(p => p.status !== 'ACTIVE').length }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-barcode">🏷️</div>
        <div class="stat-info">
          <span class="stat-label">With Barcode</span>
          <span class="stat-value">{{ products.filter(p => p.barcode).length }}</span>
        </div>
      </div>
    </div>

    <!-- Search & Actions -->
    <div class="action-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称 / Search products..."
        clearable
        style="width:320px"
        @input="onSearchInput"
      />
      <div class="action-buttons">
        <el-button @click="$router.push('/print-labels')">🖨️ 打印条码</el-button>
        <el-button @click="$router.push('/import-images')">📷 导入图片</el-button>
        <el-button type="primary" @click="openCreate">＋ 添加商品</el-button>
      </div>
    </div>

    <el-table :data="products" stripe style="margin-top:16px" v-loading="loading" class="products-table">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="barcode" label="条码" width="140">
        <template #default="{ row }">
          <code v-if="row.barcode" class="barcode-text">{{ row.barcode }}</code>
          <el-tag v-else size="small" type="warning">无</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="商品名称" min-width="240">
        <template #default="{ row }">
          <div class="product-name-cell">
            <span class="product-title">{{ row.title }}</span>
            <span v-if="row.ratingGrade" class="product-grade">{{ row.ratingGrade }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120" align="right">
        <template #default="{ row }">
          <span class="price-cell">{{ row.currency }} {{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="Stock" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small" effect="plain">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" align="center" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="openEdit(row)">Edit</el-button>
          <el-button
            :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
            size="small"
            plain
            @click="toggleStatus(row.id, row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')"
          >
            {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="total, prev, pager, next"
      @current-change="load"
      style="margin-top:20px; justify-content:center"
    />

    <!-- Product Form Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? 'Edit Product' : 'Add Product'" width="780px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <!-- Basic Info -->
        <el-collapse v-model="activeSections" style="border:none">
          <el-collapse-item title="基本信息 Basic Info" name="basic">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="Seller" prop="sellerId">
                  <el-input-number v-model="form.sellerId" :min="1" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Category" prop="categoryId">
                  <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width:100%" filterable>
                    <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="Title" prop="title">
              <el-input v-model="form.title" placeholder="商品标题 / Coin Title" />
            </el-form-item>
            <el-form-item label="Description">
              <el-input v-model="form.description" type="textarea" :rows="2" placeholder="商品描述" />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="Price" prop="price">
                  <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="0.5" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
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
              <el-col :span="8">
                <el-form-item label="Stock" prop="stock">
                  <el-input-number v-model="form.stock" :min="0" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="Barcode">
              <div style="display:flex; gap:8px; align-items:center">
                <el-input v-model="form.barcode" placeholder="留空自动生成" style="flex:1" />
                <el-button size="small" @click="form.barcode = ''">自动生成</el-button>
              </div>
            </el-form-item>
          </el-collapse-item>

          <!-- Grading Info -->
          <el-collapse-item title="评级信息 Grading" name="grading">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="Rating Company">
                  <el-select v-model="form.ratingCompany" clearable style="width:100%">
                    <el-option label="NGC" value="NGC" />
                    <el-option label="PCGS" value="PCGS" />
                    <el-option label="PMG" value="PMG" />
                    <el-option label="ANACS" value="ANACS" />
                    <el-option label="ICG" value="ICG" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Grade">
                  <el-select v-model="form.ratingGrade" clearable filterable allow-create style="width:100%" placeholder="选或输入">
                    <el-option-group label="Mint State">
                      <el-option v-for="g in mintStateGrades" :key="g" :label="g" :value="g" />
                    </el-option-group>
                    <el-option-group label="About Uncirculated">
                      <el-option v-for="g in auGrades" :key="g" :label="g" :value="g" />
                    </el-option-group>
                    <el-option-group label="Extremely Fine">
                      <el-option v-for="g in xfGrades" :key="g" :label="g" :value="g" />
                    </el-option-group>
                    <el-option-group label="Other">
                      <el-option v-for="g in otherGrades" :key="g" :label="g" :value="g" />
                    </el-option-group>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Cert Number">
                  <el-input v-model="form.ratingNumber" placeholder="证书编号" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- Physical Details -->
          <el-collapse-item title="物理属性 Physical Details" name="physical">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="Country">
                  <el-select v-model="form.country" clearable filterable style="width:100%" placeholder="选择国家">
                    <el-option v-for="c in countries" :key="c" :label="c" :value="c" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Year">
                  <el-input-number v-model="form.year" :min="0" :max="2030" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Material">
                  <el-select v-model="form.material" clearable filterable allow-create style="width:100%" placeholder="选或输入">
                    <el-option label="Gold" value="Gold" />
                    <el-option label="Silver" value="Silver" />
                    <el-option label="Copper" value="Copper" />
                    <el-option label="Bronze" value="Bronze" />
                    <el-option label="Nickel" value="Nickel" />
                    <el-option label="Platinum" value="Platinum" />
                    <el-option label="Palladium" value="Palladium" />
                    <el-option label="Bimetallic" value="Bimetallic" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="Denomination">
                  <el-select v-model="form.denomination" clearable filterable allow-create style="width:100%" placeholder="选或输入">
                    <el-option label="$1" value="$1" />
                    <el-option label="$5" value="$5" />
                    <el-option label="$10" value="$10" />
                    <el-option label="$20" value="$20" />
                    <el-option label="$50" value="$50" />
                    <el-option label="$100" value="$100" />
                    <el-option label="1¢" value="1¢" />
                    <el-option label="5¢" value="5¢" />
                    <el-option label="10¢" value="10¢" />
                    <el-option label="25¢" value="25¢" />
                    <el-option label="50¢" value="50¢" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Weight (g)">
                  <el-input-number v-model="form.weight" :min="0" :precision="2" :step="0.1" controls-position="right" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save" :loading="saving">
          {{ isEdit ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()

const products = ref([])
const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const total = ref(0)
const size = 20
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const activeSections = ref(['basic'])
const searchKeyword = ref('')
let searchTimer = null

const countries = ['USA','China','Canada','UK','Germany','France','Japan','Spain','Italy','Greece','Roman Empire','Byzantine Empire','Luxembourg','Vatican','Vietnam','Australia','Austria','Belgium','Brazil','Bulgaria','Croatia','Cuba','Cyprus','Czech Republic','Denmark','Egypt','Estonia','Finland','Hungary','India','Indonesia','Ireland','Israel','South Korea','Latvia','Lebanon','Lithuania','Malaysia','Mexico','Netherlands','New Zealand','Norway','Peru','Philippines','Poland','Portugal','Romania','Russia','Saudi Arabia','Serbia','Singapore','Slovakia','Slovenia','South Africa','Sri Lanka','Sweden','Switzerland','Taiwan','Thailand','Turkey','Ukraine']

const mintStateGrades = ['MS60','MS61','MS62','MS63','MS64','MS65','MS66','MS67','MS68','MS69','MS70']
const auGrades = ['AU50','AU53','AU55','AU58']
const xfGrades = ['XF40','XF45']
const otherGrades = ['VF20','VF25','VF30','VF35','F12','F15','VG8','VG10','G4','G6','AG3','PO1']

const emptyForm = () => ({
  sellerId: 2,
  categoryId: null,
  title: '',
  description: '',
  price: 0,
  currency: 'USD',
  stock: 0,
  barcode: '',
  ratingCompany: null,
  ratingGrade: null,
  ratingNumber: null,
  country: '',
  year: null,
  material: '',
  denomination: '',
  weight: null,
})

const form = reactive(emptyForm())

const rules = {
  sellerId: [{ required: true, message: 'Required', trigger: 'blur' }],
  title: [{ required: true, message: 'Required', trigger: 'blur' }],
  price: [{ required: true, message: 'Required', trigger: 'blur' }],
  stock: [{ required: true, message: 'Required', trigger: 'blur' }],
}

onMounted(async () => {
  try {
    const catRes = await api.get('/products/categories')
    categories.value = catRes.data || []
  } catch (_) {}
  load()

  // Handle copy product from Product History
  const copyBarcode = route.query.copyBarcode
  if (copyBarcode) {
    try {
      const res = await api.get(`/admin/products/barcode/${copyBarcode}`)
      const p = res.data
      if (p) {
        Object.assign(form, {
          sellerId: p.sellerId || 2,
          categoryId: p.categoryId,
          title: p.title,
          description: p.description || '',
          price: p.price,
          currency: p.currency || 'USD',
          stock: p.stock || 0,
          barcode: '',
          ratingCompany: p.ratingCompany || null,
          ratingGrade: p.ratingGrade || null,
          ratingNumber: p.ratingNumber || null,
          country: p.country || '',
          year: p.year,
          material: p.material || '',
          denomination: p.denomination || '',
          weight: p.weight,
        })
        isEdit.value = false
        editId.value = null
        dialogVisible.value = true
        ElMessage.success(`已从条码 ${copyBarcode} 复制商品信息，请输入新条码`)
      }
    } catch (e) {
      ElMessage.warning(`未找到条码为 ${copyBarcode} 的商品`)
    }
  }
})

async function load() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await api.get('/admin/products', { params })
    products.value = res.data.content
    total.value = res.data.totalElements
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    load()
  }, 400)
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    sellerId: row.sellerId || 2,
    categoryId: row.categoryId,
    title: row.title,
    description: row.description || '',
    price: row.price,
    currency: row.currency || 'USD',
    stock: row.stock,
    barcode: row.barcode || '',
    ratingCompany: row.ratingCompany || null,
    ratingGrade: row.ratingGrade || null,
    ratingNumber: row.ratingNumber || null,
    country: row.country || '',
    year: row.year,
    material: row.material || '',
    denomination: row.denomination || '',
    weight: row.weight,
  })
  activeSections.value = ['basic', 'grading', 'physical']
  dialogVisible.value = true
}

function resetForm() {
  Object.assign(form, emptyForm())
  if (formRef.value) formRef.value.resetFields()
}

async function save() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) { return }

  saving.value = true
  try {
    const body = { ...form }
    if (isEdit.value) {
      await api.put(`/admin/products/${editId.value}`, body)
      ElMessage.success('Product updated')
    } else {
      await api.post(`/admin/products?sellerId=${form.sellerId}`, body)
      ElMessage.success('Product created')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Operation failed')
  } finally { saving.value = false }
}

async function toggleStatus(id, status) {
  try {
    await ElMessageBox.confirm(`确定${status === 'ACTIVE' ? '启用' : '停用'}该商品？`)
    await api.put(`/admin/products/${id}/status?status=${status}`)
    ElMessage.success('Updated')
    load()
  } catch (_) {}
}
</script>

<style scoped>
.products-page {
  padding: 4px;
}

/* Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  transition: transform .15s, box-shadow .15s;
}
.stat-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,.08);
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-icon-total { background: #eef2ff; }
.stat-icon-active { background: #ecfdf5; }
.stat-icon-inactive { background: #fef3c7; }
.stat-icon-barcode { background: #f0f9ff; }
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: .5px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

/* Action Bar */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.action-buttons {
  display: flex;
  gap: 8px;
}

/* Table */
.products-table {
  border-radius: 10px;
  overflow: hidden;
}
.products-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #374151;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: .5px;
}
.product-name-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.product-title {
  font-weight: 500;
  color: #111827;
}
.product-grade {
  background: #f3f4f6;
  color: #6b7280;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.barcode-text {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
  color: #374151;
  letter-spacing: .5px;
}
.price-cell {
  font-weight: 600;
  color: #059669;
}

/* Responsive */
@media (max-width: 900px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .stats-row { grid-template-columns: 1fr; }
  .action-bar { flex-direction: column; align-items: stretch; }
  .action-bar .el-input { width: 100% !important; }
  .action-buttons { justify-content: stretch; }
  .action-buttons .el-button { flex: 1; }
}
</style>
