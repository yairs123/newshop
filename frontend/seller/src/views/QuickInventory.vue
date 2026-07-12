<template>
  <div class="inventory-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>快捷入库</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- Barcode Scanner Input -->
    <el-card shadow="hover" class="scan-card">
      <div class="scan-area">
        <div class="scan-icon">📷</div>
        <h3>扫描条码</h3>
        <p class="scan-hint">使用条码扫描枪对准条码，或手动输入条码号</p>
        <el-input
          ref="barcodeInput"
          v-model="barcode"
          placeholder="扫描或输入条码后按 Enter..."
          size="large"
          clearable
          @keyup.enter="lookupBarcode"
          class="scan-input"
        >
          <template #prefix>
            <el-icon><component :is="'Search'" /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="lookupBarcode" :loading="searching" class="scan-btn">查询</el-button>
      </div>
    </el-card>

    <!-- Product Found -->
    <el-card v-if="product" shadow="hover" class="result-card">
      <template #header>
        <div class="result-header">
          <span>商品信息</span>
          <el-tag type="success" effect="plain">条码: {{ product.barcode }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="名称" :span="2">{{ product.title }}</el-descriptions-item>
        <el-descriptions-item label="售价">{{ product.currency }} {{ formatPrice(product.price) }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">
          <el-tag :type="product.stock <= 3 ? 'danger' : 'info'">{{ product.stock }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评级">{{ product.ratingCompany || '-' }} {{ product.ratingGrade || '' }}</el-descriptions-item>
        <el-descriptions-item label="国家/年份">{{ product.country || '-' }} {{ product.year || '' }}</el-descriptions-item>
      </el-descriptions>

      <div class="stock-action">
        <el-form inline>
          <el-form-item label="入库数量">
            <el-input-number v-model="addStock" :min="1" :max="100" controls-position="right" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addStockToProduct" :loading="updating">确认入库</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- Product Not Found - Quick Create -->
    <el-card v-if="notFound" shadow="hover" class="result-card">
      <template #header>
        <div class="result-header">
          <span>未找到该商品 — 快速创建</span>
          <el-tag type="warning">条码: {{ barcode }}</el-tag>
        </div>
      </template>
      <el-form :model="quickForm" label-position="top">
        <el-form-item label="商品名称">
          <el-input v-model="quickForm.title" placeholder="输入商品名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="售价">
              <el-input-number v-model="quickForm.price" :min="0.01" :precision="2" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="quickForm.stock" :min="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="货币">
              <el-select v-model="quickForm.currency" style="width:100%">
                <el-option label="USD" value="USD" />
                <el-option label="EUR" value="EUR" />
                <el-option label="CNY" value="CNY" />
                <el-option label="JPY" value="JPY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="采购价">
          <el-input-number v-model="quickForm.purchasePrice" :min="0" :precision="2" controls-position="right" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="createQuickProduct" :loading="creating">创建并入库</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Recent scan history -->
    <el-card v-if="history.length" shadow="hover" class="history-card">
      <template #header><span>本次入库记录</span></template>
      <el-table :data="history" size="small">
        <el-table-column prop="barcode" label="条码" width="140" />
        <el-table-column prop="title" label="名称" min-width="200" />
        <el-table-column prop="qty" label="入库数量" width="80" align="center" />
        <el-table-column prop="time" label="时间" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const barcode = ref('')
const product = ref(null)
const notFound = ref(false)
const searching = ref(false)
const updating = ref(false)
const creating = ref(false)
const addStock = ref(1)
const barcodeInput = ref(null)
const history = ref([])

const quickForm = ref({ title: '', price: 0, stock: 1, currency: 'USD', purchasePrice: 0 })

function formatPrice(p) { return Number(p || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

async function lookupBarcode() {
  const code = barcode.value?.trim()
  if (!code) { ElMessage.warning('请输入条码'); return }
  searching.value = true
  product.value = null
  notFound.value = false
  try {
    const res = await api.get('/products/barcode/' + encodeURIComponent(code))
    product.value = res.data
    addStock.value = 1
  } catch (e) {
    if (e.response?.status === 404 || e.response?.data?.message === '商品不存在') {
      notFound.value = true
      quickForm.value = { title: '', price: 0, stock: 1, currency: 'USD', purchasePrice: 0 }
    } else {
      ElMessage.error('查询失败')
    }
  }
  searching.value = false
}

async function addStockToProduct() {
  if (!product.value) return
  updating.value = true
  try {
    const newStock = product.value.stock + addStock.value
    await api.put('/products/' + product.value.id, {
      title: product.value.title,
      price: product.value.price,
      currency: product.value.currency,
      stock: newStock,
      purchasePrice: product.value.purchasePrice,
      purchaseCurrency: product.value.purchaseCurrency,
      supplier: product.value.supplier,
    })
    ElMessage.success(`入库 ${addStock.value} 件成功！当前库存: ${newStock}`)
    history.value.unshift({ barcode: product.value.barcode, title: product.value.title, qty: addStock.value, time: new Date().toLocaleString() })
    product.value.stock = newStock
    addStock.value = 1
    // Ready for next scan
    barcode.value = ''
    product.value = null
    notFound.value = false
    nextTick(() => barcodeInput.value?.focus())
  } catch (e) { /* handled */ }
  updating.value = false
}

async function createQuickProduct() {
  if (!quickForm.value.title) { ElMessage.warning('请输入商品名称'); return }
  creating.value = true
  try {
    const res = await api.post('/products', {
      title: quickForm.value.title,
      price: quickForm.value.price,
      currency: quickForm.value.currency,
      stock: quickForm.value.stock,
      barcode: barcode.value,
      purchasePrice: quickForm.value.purchasePrice,
      purchaseCurrency: quickForm.value.currency || 'USD',
    })
    ElMessage.success('创建并入库成功！')
    history.value.unshift({ barcode: barcode.value, title: res.data?.title, qty: quickForm.value.stock, time: new Date().toLocaleString() })
    barcode.value = ''
    product.value = null
    notFound.value = false
    quickForm.value = { title: '', price: 0, stock: 1, currency: 'USD', purchasePrice: 0 }
    nextTick(() => barcodeInput.value?.focus())
  } catch (e) { /* handled */ }
  creating.value = false
}
</script>

<style scoped>
.scan-card { text-align: center; margin-bottom: 16px; }
.scan-area { padding: 24px 0; }
.scan-icon { font-size: 48px; margin-bottom: 8px; }
.scan-hint { color: #909399; font-size: 13px; margin-bottom: 16px; }
.scan-input { max-width: 400px; margin: 0 auto 12px; }
.scan-btn { min-width: 120px; }
.result-card { margin-bottom: 16px; }
.result-header { display: flex; justify-content: space-between; align-items: center; }
.stock-action { margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0; }
.history-card { margin-bottom: 16px; }
</style>
