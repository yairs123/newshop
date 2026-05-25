<template>
  <div class="history-page">
    <div class="page-header">
      <h3>商品历史查询</h3>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索条码 / 名称 / 国家 / 材质 / 评级..."
          clearable
          style="width:400px"
          @input="onSearchInput"
          @clear="load"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-label">总计</span>
        <span class="stat-value">{{ total }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">已打印标签</span>
        <span class="stat-value">{{ products.filter(p => p.printedAt).length }}</span>
      </div>
    </div>

    <el-table :data="products" stripe v-loading="loading" class="history-table" @row-click="openDetail">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column label="图片" width="70">
        <template #default="{ row }">
          <el-image
            v-if="row.images && row.images.length > 0"
            :src="row.images[0]"
            style="width:48px; height:48px; border-radius:4px; cursor:pointer"
            fit="cover"
            :preview-src-list="row.images"
          />
          <div v-else class="no-image">📷</div>
        </template>
      </el-table-column>
      <el-table-column prop="barcode" label="条码" width="140">
        <template #default="{ row }">
          <code v-if="row.barcode" class="barcode-text">{{ row.barcode }}</code>
          <el-tag v-else size="small" type="warning">无</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="商品名称" min-width="200">
        <template #default="{ row }">
          <div class="product-name-cell">
            <span class="product-title">{{ row.title }}</span>
            <span v-if="row.ratingGrade" class="product-grade">{{ row.ratingGrade }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="100" align="right">
        <template #default="{ row }">
          <span class="price-cell">{{ row.currency }} {{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物理属性" width="200">
        <template #default="{ row }">
          <div class="attr-cell">
            <span v-if="row.country" class="attr-tag">{{ row.country }}</span>
            <span v-if="row.year" class="attr-tag">{{ row.year }}</span>
            <span v-if="row.material" class="attr-tag">{{ row.material }}</span>
            <span v-if="row.denomination" class="attr-tag">{{ row.denomination }}</span>
            <span v-if="row.weight" class="attr-tag">{{ row.weight }}g</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="打印状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.printedAt" size="small" type="success" effect="plain">已打印</el-tag>
          <el-tag v-else size="small" type="warning" effect="plain">未打印</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="150" align="center" />
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click.stop="copyProduct(row)">
            Copy
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && products.length === 0" description="暂无商品记录" />
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="total, prev, pager, next"
      @current-change="load"
      style="margin-top:20px; justify-content:center"
    />

    <!-- Product Detail Drawer -->
    <el-drawer v-model="drawerVisible" :title="detailProduct?.title || '商品详情'" size="500px">
      <div v-if="detailProduct" class="product-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">条码</span>
              <code class="detail-value">{{ detailProduct.barcode }}</code>
            </div>
            <div class="detail-item">
              <span class="detail-label">价格</span>
              <span class="detail-value">{{ detailProduct.currency }} {{ detailProduct.price }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">库存</span>
              <span class="detail-value">{{ detailProduct.stock }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">状态</span>
              <el-tag :type="detailProduct.status === 'ACTIVE' ? 'success' : 'info'" size="small">{{ detailProduct.status }}</el-tag>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>描述</h4>
          <p class="detail-desc">{{ detailProduct.description || '—' }}</p>
        </div>
        <div class="detail-section">
          <h4>物理属性</h4>
          <div class="detail-grid">
            <div class="detail-item" v-if="detailProduct.country"><span class="detail-label">国家</span><span class="detail-value">{{ detailProduct.country }}</span></div>
            <div class="detail-item" v-if="detailProduct.year"><span class="detail-label">年份</span><span class="detail-value">{{ detailProduct.year }}</span></div>
            <div class="detail-item" v-if="detailProduct.material"><span class="detail-label">材质</span><span class="detail-value">{{ detailProduct.material }}</span></div>
            <div class="detail-item" v-if="detailProduct.denomination"><span class="detail-label">面值</span><span class="detail-value">{{ detailProduct.denomination }}</span></div>
            <div class="detail-item" v-if="detailProduct.weight"><span class="detail-label">重量</span><span class="detail-value">{{ detailProduct.weight }}g</span></div>
          </div>
        </div>
        <div class="detail-section" v-if="detailProduct.ratingCompany">
          <h4>评级信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><span class="detail-label">公司</span><span class="detail-value">{{ detailProduct.ratingCompany }}</span></div>
            <div class="detail-item"><span class="detail-label">等级</span><span class="detail-value">{{ detailProduct.ratingGrade }}</span></div>
            <div class="detail-item"><span class="detail-label">编号</span><span class="detail-value">{{ detailProduct.ratingNumber }}</span></div>
          </div>
        </div>
        <div class="detail-section">
          <h4>图片</h4>
          <div class="detail-images" v-if="detailProduct.images && detailProduct.images.length > 0">
            <el-image
              v-for="(img, i) in detailProduct.images"
              :key="i"
              :src="img"
              style="width:100px; height:100px; border-radius:6px; cursor:pointer"
              fit="cover"
              :preview-src-list="detailProduct.images"
            />
          </div>
          <p v-else class="detail-desc">暂无图片</p>
        </div>
        <div class="detail-section">
          <h4>记录</h4>
          <div class="detail-grid">
            <div class="detail-item"><span class="detail-label">创建时间</span><span class="detail-value">{{ detailProduct.createdAt }}</span></div>
            <div class="detail-item"><span class="detail-label">打印时间</span><span class="detail-value">{{ detailProduct.printedAt || '未打印' }}</span></div>
            <div class="detail-item"><span class="detail-label">浏览量</span><span class="detail-value">{{ detailProduct.viewCount }}</span></div>
            <div class="detail-item"><span class="detail-label">销量</span><span class="detail-value">{{ detailProduct.salesCount }}</span></div>
          </div>
        </div>
        <div style="margin-top:20px">
          <el-button type="warning" @click="copyProduct(detailProduct)" style="width:100%">
            复制此商品 (使用此条码创建新商品)
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const products = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const size = 20
const keyword = ref('')
const drawerVisible = ref(false)
const detailProduct = ref(null)
let searchTimer = null

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    let res
    if (keyword.value) {
      res = await api.get('/admin/products/search', { params: { q: keyword.value, page: page.value - 1, size } })
    } else {
      res = await api.get('/admin/products', { params: { page: page.value - 1, size } })
    }
    products.value = res.data.content
    total.value = res.data.totalElements
  } catch (_) {}
  loading.value = false
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    load()
  }, 400)
}

function openDetail(row) {
  detailProduct.value = row
  drawerVisible.value = true
}

function copyProduct(row) {
  if (!row.barcode) {
    ElMessage.warning('该商品无条码，无法复制')
    return
  }
  // Navigate to products page with barcode in query to pre-fill
  router.push({ path: '/products', query: { copyBarcode: row.barcode } })
  ElMessage.success(`已复制条码: ${row.barcode}，将在商品列表页面加载数据`)
}
</script>

<style scoped>
.history-page {
  padding: 4px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  min-width: 140px;
}
.stat-label {
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: .5px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.history-table {
  border-radius: 10px;
  overflow: hidden;
}
.history-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #374151;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
}
.history-table :deep(.el-table__row) {
  cursor: pointer;
}

.barcode-text {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
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
.price-cell {
  font-weight: 600;
  color: #059669;
}
.attr-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 2px 4px;
}
.attr-tag {
  background: #f3f4f6;
  color: #6b7280;
  font-size: 11px;
  padding: 0 4px;
  border-radius: 3px;
  line-height: 1.6;
}
.no-image {
  width: 48px;
  height: 48px;
  background: #f3f4f6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

/* Drawer */
.product-detail {
  padding: 0 4px;
}
.detail-section {
  margin-bottom: 20px;
}
.detail-section h4 {
  font-size: 12px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: .5px;
  margin: 0 0 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f3f4f6;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.detail-item {
  display: flex;
  flex-direction: column;
}
.detail-label {
  font-size: 11px;
  color: #9ca3af;
}
.detail-value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}
.detail-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}
.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
