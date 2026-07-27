<template>
  <div class="product-detail-page" v-loading="loading">
    <div class="detail-inner">
      <!-- Breadcrumb -->
      <el-breadcrumb separator="/" class="detail-breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products' }">{{ $t('nav.shop') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="product">{{ product.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 404 State -->
      <div v-if="notFound" class="not-found-state">
        <el-empty :description="$t('productDetail.notFound')">
          <template #image>
            <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
              <circle cx="60" cy="60" r="56" stroke="#e5e7eb" stroke-width="3" />
              <circle cx="40" cy="50" r="4" fill="#d1d5db" />
              <circle cx="80" cy="50" r="4" fill="#d1d5db" />
              <path d="M40 75 Q60 90 80 75" stroke="#d1d5db" stroke-width="3" fill="none" stroke-linecap="round" />
            </svg>
          </template>
          <p class="not-found-desc">{{ $t('productDetail.notFoundDesc') }}</p>
          <el-button type="primary" @click="$router.push('/products')">
            {{ $t('productDetail.backToShop') }}
          </el-button>
        </el-empty>
      </div>

      <!-- Product Detail -->
      <template v-if="product && !notFound">
        <div class="product-main">
          <!-- Left: Image Gallery -->
          <div class="product-gallery">
            <div class="main-image-container">
              <div v-if="images.length > 0" class="main-image-wrapper">
                <img :src="images[activeImageIndex]" :alt="product.title" class="main-image" @error="onImageError" />
              </div>
              <div v-else class="image-placeholder main-placeholder">
                <span>{{ titleInitial }}</span>
              </div>
              <div v-if="product.ratingGrade" class="rating-badge">
                {{ product.ratingCompany }} {{ product.ratingGrade }}
              </div>
            </div>
            <div v-if="images.length > 1" class="thumbnail-list">
              <div
                v-for="(img, i) in images"
                :key="i"
                :class="['thumb-item', { active: i === activeImageIndex }]"
                @click="activeImageIndex = i"
              >
                <img :src="img" :alt="'Image ' + (i + 1)" class="thumb-image" @error="onThumbError(i)" />
              </div>
            </div>
          </div>

          <!-- Right: Product Info -->
          <div class="product-info">
            <h1 class="product-title">{{ product.title }}</h1>

            <div v-if="product.ratingCompany" class="rating-row">
              <span class="rating-tag" :class="ratingClass">
                {{ product.ratingCompany }}
              </span>
              <span v-if="product.ratingGrade" class="grade-tag">{{ product.ratingGrade }}</span>
            </div>

            <div class="price-section">
              <span class="product-price">
                <span class="price-symbol">{{ product.currency || 'USD' }}</span>
                {{ formatPrice(product.price) }}
              </span>
              <span v-if="product.barcode" class="barcode-info">
                {{ $t('productDetail.barcode') }}: {{ product.barcode }}
              </span>
            </div>

            <!-- Stock Status -->
            <div class="stock-section">
              <div v-if="product.stock > 5" class="stock-status in-stock">
                <span class="stock-dot"></span>
                <span>{{ $t('productDetail.inStock') }}</span>
                <span class="stock-qty">({{ $t('product.inStockWithQty', { stock: product.stock }) }})</span>
              </div>
              <div v-else-if="product.stock > 0" class="stock-status low-stock">
                <span class="stock-dot"></span>
                <span>{{ $t('productDetail.lowStockWarning', { stock: product.stock }) }}</span>
              </div>
              <div v-else class="stock-status out-of-stock">
                <span class="stock-dot"></span>
                <span>{{ $t('productDetail.outOfStock') }}</span>
              </div>
            </div>

            <!-- Quantity + Cart -->
            <div class="cart-section">
              <div class="quantity-row">
                <span class="quantity-label">{{ $t('product.quantity') }}</span>
                <el-input-number
                  v-model="quantity"
                  :min="1"
                  :max="Math.min(product.stock, 99)"
                  :disabled="product.stock <= 0"
                  size="large"
                  controls-position="right"
                  class="quantity-input"
                />
              </div>

              <div class="cart-actions">
                <el-button
                  type="primary"
                  size="large"
                  :disabled="product.stock <= 0"
                  :icon="ShoppingCart"
                  class="add-to-cart-btn"
                  @click="addToCart"
                >
                  {{ $t('product.addToCart') }}
                </el-button>
                <el-button
                  size="large"
                  plain
                  :class="['fav-btn', { active: isFavorite }]"
                  @click="toggleFavorite"
                >
                  <el-icon><StarFilled v-if="isFavorite" /><Star v-else /></el-icon>
                  <span v-if="isFavorite">Favorited</span>
                  <span v-else>Favorite</span>
                </el-button>
              </div>
            </div>

            <!-- Seller Info -->
            <div v-if="product.sellerName" class="seller-info">
              <span class="seller-label">{{ $t('productDetail.seller') }}:</span>
              <span class="seller-name">{{ product.sellerName }}</span>
              <span v-if="product.createdAt" class="added-date">
                | {{ $t('productDetail.addedDate') }}: {{ formatDate(product.createdAt) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Product Details & Specs -->
        <div class="detail-sections">
          <div class="detail-card specs-card">
            <h3 class="section-title">{{ $t('productDetail.specifications') }}</h3>
            <el-descriptions :column="2" border class="specs-table">
              <el-descriptions-item v-if="product.country" :label="$t('productDetail.country')" width="160">
                {{ $t('countries.' + product.country) || product.country }}
              </el-descriptions-item>
              <el-descriptions-item v-if="product.year" :label="$t('productDetail.year')">
                {{ product.year }}
              </el-descriptions-item>
              <el-descriptions-item v-if="product.material" :label="$t('productDetail.material')">
                {{ product.material }}
              </el-descriptions-item>
              <el-descriptions-item v-if="product.denomination" :label="$t('productDetail.denomination')">
                {{ product.denomination }}
              </el-descriptions-item>
              <el-descriptions-item v-if="product.weight" :label="$t('productDetail.weight')">
                {{ product.weight }}
              </el-descriptions-item>
              <el-descriptions-item v-if="product.ratingCompany" :label="$t('product.ratingCompany')">
                {{ product.ratingCompany }}
              </el-descriptions-item>
              <el-descriptions-item v-if="product.ratingGrade" :label="$t('product.ratingGrade')">
                {{ product.ratingGrade }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div v-if="product.description" class="detail-card desc-card">
            <h3 class="section-title">{{ $t('product.description') }}</h3>
            <div class="description-content">{{ product.description }}</div>
          </div>
        </div>

        <!-- Related Products -->
        <div v-if="relatedProducts.length > 0" class="related-section">
          <h3 class="section-title">{{ $t('productDetail.relatedProducts') }}</h3>
          <div class="related-grid">
            <div
              v-for="p in relatedProducts"
              :key="p.id"
              class="related-card"
              @click="$router.push(`/products/${p.id}`)"
            >
              <div class="related-image">
                <div class="image-placeholder small-placeholder">
                  <span>{{ (p.title || '?').charAt(0) }}</span>
                </div>
                <div v-if="p.ratingGrade" class="related-badge">
                  {{ p.ratingCompany }} {{ p.ratingGrade }}
                </div>
              </div>
              <div class="related-body">
                <h4 class="related-title">{{ p.title }}</h4>
                <div class="related-meta">
                  <span v-if="p.country">{{ $t('countries.' + p.country) || p.country }}</span>
                  <span v-if="p.year">{{ p.year }}</span>
                </div>
                <div class="related-footer">
                  <span class="related-price">
                    {{ formatPrice(p.price) }} {{ p.currency || 'USD' }}
                  </span>
                  <span v-if="p.stock > 0" class="related-instock">{{ $t('productDetail.inStock') }}</span>
                  <span v-else class="related-soldout">{{ $t('productDetail.outOfStock') }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../store/cart'
import { useFavoritesStore } from '../store/favorites'
import { api } from '../api'
import { ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const favoritesStore = useFavoritesStore()
const openCartDrawer = inject('openCartDrawer')

const product = ref(null)
const loading = ref(true)
const notFound = ref(false)
const activeImageIndex = ref(0)
const quantity = ref(1)
const relatedProducts = ref([])

const images = computed(() => {
  if (!product.value || !product.value.images) return []
  const imgs = product.value.images
  // Handle both array of strings and array of objects with url/imageUrl property
  return imgs.map(i => {
    if (typeof i === 'string') return i
    return i.url || i.imageUrl || ''
  }).filter(Boolean)
})

const titleInitial = computed(() => {
  if (!product.value) return ''
  return product.value.title?.charAt(0).toUpperCase() || ''
})

const isFavorite = computed(() => {
  if (!product.value) return false
  return favoritesStore.isFavorite(product.value.id)
})

const ratingClass = computed(() => {
  const company = product.value?.ratingCompany
  if (!company) return ''
  const map = {
    'NGC': 'ngc',
    'PCGS': 'pcgs',
    'PMG': 'pmg',
    'ANACS': 'anacs',
    'ICG': 'icg'
  }
  return map[company] || 'default-rating'
})

async function loadProduct(id) {
  loading.value = true
  notFound.value = false
  activeImageIndex.value = 0
  quantity.value = 1
  relatedProducts.value = []

  try {
    const res = await api.get(`/products/${id}`)
    if (!res.data) {
      notFound.value = true
      return
    }
    product.value = res.data
    document.title = product.value.title + ' - CoinMarket'

    // Load related products
    try {
      const relatedRes = await api.get(`/products/${id}/related`)
      relatedProducts.value = (relatedRes.data?.content || relatedRes.data || []).slice(0, 4)
    } catch (_) {
      // Related products are optional
      relatedProducts.value = []
    }
  } catch (e) {
    if (e.response?.status === 404) {
      notFound.value = true
    } else {
      notFound.value = true
    }
  } finally {
    loading.value = false
  }
}

function formatPrice(p) {
  return Number(p || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

function addToCart() {
  if (!product.value || product.value.stock <= 0) return
  cartStore.addItem(product.value, quantity.value)
  if (openCartDrawer) openCartDrawer()
}

function toggleFavorite() {
  if (product.value) favoritesStore.toggle(product.value)
}

function onImageError(e) {
  e.target.style.display = 'none'
}

function onThumbError(index) {
  // Remove broken thumb
}

// Watch for route param changes
watch(() => route.params.id, (newId) => {
  if (newId) loadProduct(newId)
})

onMounted(() => {
  if (route.params.id) loadProduct(route.params.id)
})
</script>

<style scoped>
.product-detail-page {
  background: #f9fafb;
  min-height: 80vh;
}

.detail-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 24px 60px;
}

/* Breadcrumb */
.detail-breadcrumb {
  margin-bottom: 24px;
}
.detail-breadcrumb :deep(.el-breadcrumb__inner) {
  font-size: 13px;
}

/* ====== 404 State ====== */
.not-found-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}
.not-found-desc {
  color: #6b7280;
  font-size: 14px;
  margin: 8px 0 16px;
}

/* ====== Product Main Layout ====== */
.product-main {
  display: flex;
  gap: 40px;
  margin-bottom: 32px;
}

/* ====== Image Gallery ====== */
.product-gallery {
  width: 480px;
  flex-shrink: 0;
}

.main-image-container {
  position: relative;
  width: 480px;
  height: 480px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.main-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: opacity 0.3s;
}

.main-placeholder {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  font-weight: 700;
  color: #9ca3af;
}

.rating-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #111827;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 8px;
  letter-spacing: 0.02em;
}

/* Thumbnails */
.thumbnail-list {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.thumb-item {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  border: 2px solid #e5e7eb;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  transition: border-color 0.15s;
}

.thumb-item:hover {
  border-color: var(--gold-light, #fbbf24);
}

.thumb-item.active {
  border-color: var(--gold, #f59e0b);
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
}

.thumb-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

/* ====== Product Info ====== */
.product-info {
  flex: 1;
  min-width: 0;
}

.product-title {
  font-size: 26px;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
  margin-bottom: 12px;
}

.rating-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.rating-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.rating-tag.ngc {
  background: linear-gradient(135deg, #1a3a5c, #2d6a9f);
  color: #fff;
}

.rating-tag.pcgs {
  background: linear-gradient(135deg, #0d6b3e, #1a9e5f);
  color: #fff;
}

.rating-tag.pmg {
  background: linear-gradient(135deg, #5b2d8e, #8b5cf6);
  color: #fff;
}

.rating-tag.anacs {
  background: linear-gradient(135deg, #8b4513, #c7752e);
  color: #fff;
}

.rating-tag.default-rating {
  background: #374151;
  color: #fff;
}

.grade-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

/* Price */
.price-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 20px 0;
  border-top: 1px solid #f3f4f6;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 20px;
}

.product-price {
  font-size: 32px;
  font-weight: 800;
  color: var(--gold-dark, #b45309);
}

.price-symbol {
  font-size: 18px;
  font-weight: 600;
  margin-right: 4px;
  opacity: 0.7;
}

.barcode-info {
  font-size: 12px;
  color: #9ca3af;
}

/* Stock Status */
.stock-section {
  margin-bottom: 20px;
}

.stock-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.stock-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.in-stock .stock-dot {
  background: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

.in-stock {
  color: #059669;
}

.low-stock .stock-dot {
  background: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
}

.low-stock {
  color: #b45309;
}

.out-of-stock .stock-dot {
  background: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15);
}

.out-of-stock {
  color: #dc2626;
}

.stock-qty {
  font-size: 13px;
  color: #6b7280;
  font-weight: 400;
}

/* Cart Section */
.cart-section {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.quantity-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
}

.quantity-input {
  width: 140px;
}

.cart-actions {
  display: flex;
  gap: 12px;
}

.add-to-cart-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--gold, #f59e0b), var(--gold-dark, #b45309));
  border: none;
  color: #fff;
  transition: opacity 0.15s, transform 0.15s;
}

.add-to-cart-btn:hover {
  opacity: 0.92;
  transform: translateY(-1px);
}

.add-to-cart-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  opacity: 1;
  transform: none;
}

.fav-btn {
  width: 48px;
  height: 48px;
  padding: 0;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #6b7280;
  border: 1px solid #d1d5db;
  transition: all 0.15s;
}

.fav-btn:hover {
  color: #ef4444;
  border-color: #ef4444;
  background: #fef2f2;
}

.fav-btn.active {
  color: #ef4444;
  border-color: #ef4444;
  background: #fef2f2;
}

/* Seller Info */
.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  padding: 12px 0;
}

.seller-label {
  font-weight: 500;
  color: #9ca3af;
}

.seller-name {
  font-weight: 600;
  color: #374151;
}

.added-date {
  color: #9ca3af;
}

/* ====== Detail Sections ====== */
.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 40px;
}

.detail-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 28px 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f3f4f6;
}

/* Specs Table */
.specs-table {
  width: 100%;
}

.specs-table :deep(.el-descriptions__title) {
  font-size: 14px;
}

.specs-table :deep(.el-descriptions__label) {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  background: #f9fafb;
  padding: 12px 16px;
}

.specs-table :deep(.el-descriptions__content) {
  font-size: 14px;
  color: #111827;
  padding: 12px 16px;
}

.specs-table :deep(.el-descriptions__cell) {
  padding: 0;
}

/* Description */
.description-content {
  font-size: 14px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
}

/* ====== Related Products ====== */
.related-section {
  margin-top: 8px;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.related-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.related-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.related-image {
  position: relative;
  height: 160px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.small-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #9ca3af;
}

.related-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #111827;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 3px 7px;
  border-radius: 4px;
}

.related-body {
  padding: 12px;
}

.related-title {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
  margin-bottom: 6px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 2.6em;
}

.related-meta {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.related-meta span {
  font-size: 11px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 3px;
}

.related-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.related-price {
  font-size: 15px;
  font-weight: 700;
  color: var(--gold-dark, #b45309);
}

.related-instock {
  font-size: 11px;
  color: #10b981;
  font-weight: 500;
}

.related-soldout {
  font-size: 11px;
  color: #ef4444;
  font-weight: 500;
}

/* ====== Loading overlay ====== */
.product-detail-page :deep(.el-loading-mask) {
  background: rgba(249, 250, 251, 0.8);
}

/* ====== Responsive ====== */
@media (max-width: 1024px) {
  .product-main {
    flex-direction: column;
    gap: 28px;
  }

  .product-gallery {
    width: 100%;
    max-width: 480px;
    margin: 0 auto;
  }

  .main-image-container {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }

  .related-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .detail-inner {
    padding: 16px 16px 40px;
  }

  .product-title {
    font-size: 22px;
  }

  .product-price {
    font-size: 26px;
  }

  .price-section {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .cart-section {
    padding: 16px;
  }

  .cart-actions {
    flex-direction: column;
  }

  .fav-btn {
    width: 100%;
  }

  .detail-card {
    padding: 20px;
  }

  .related-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .specs-table :deep(.el-descriptions__label) {
    width: 100px !important;
  }
}

@media (max-width: 480px) {
  .product-title {
    font-size: 18px;
  }

  .product-price {
    font-size: 22px;
  }

  .related-grid {
    grid-template-columns: 1fr 1fr;
  }

  .quantity-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .quantity-input {
    width: 120px;
  }
}
</style>
