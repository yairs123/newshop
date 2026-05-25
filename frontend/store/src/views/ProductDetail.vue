<template>
  <div class="product-detail" v-loading="loading">
    <div class="detail-container" v-if="product">

      <!-- Breadcrumb -->
      <el-breadcrumb separator="/" style="margin-bottom: 24px; font-size: 13px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products' }">{{ $t('nav.shop') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-main">
        <!-- Image Carousel -->
        <div class="detail-image">
          <el-carousel v-if="product.images && product.images.length" height="420px" indicator-position="outside">
            <el-carousel-item v-for="(img, i) in product.images" :key="i">
              <div class="image-frame">
                <img :src="img" :alt="product.title" class="product-img" />
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="image-large" v-else>
            <span class="image-letter">{{ product.title.charAt(0) }}</span>
          </div>
          <div class="badge-row" v-if="product.ratingGrade">
            <span class="grade-badge">{{ product.ratingCompany }} {{ product.ratingGrade }}</span>
          </div>
        </div>

        <!-- Info -->
        <div class="detail-info">
          <h1 class="product-title">{{ product.title }}</h1>

          <!-- Meta tags -->
          <div class="meta-tags">
            <span v-if="product.country" class="meta-tag">🌍 {{ $t('countries.' + product.country) || product.country }}</span>
            <span v-if="product.year" class="meta-tag">📅 {{ product.year }}</span>
            <span v-if="product.material" class="meta-tag">🪙 {{ product.material }}</span>
            <span v-if="product.weight" class="meta-tag">⚖ {{ product.weight }}g</span>
          </div>

          <!-- Price + 3-tier Stock Badge -->
          <div class="price-block">
            <span class="price-amount">{{ product.currency || 'USD' }} ${{ formatPrice(product.price) }}</span>
            <el-tag v-if="product.stock > 5" type="success" effect="light" size="large">{{ $t('product.inStockWithQty', { stock: product.stock }) }}</el-tag>
            <el-tag v-else-if="product.stock > 0" type="warning" effect="light" size="large">{{ $t('product.lowStock', { stock: product.stock }) }}</el-tag>
            <el-tag v-else type="danger" effect="light" size="large">{{ $t('product.soldOut') }}</el-tag>
          </div>

          <!-- Rating Card -->
          <div class="rating-card" v-if="product.ratingCompany">
            <h4 class="rating-title">{{ $t('product.ratingInfo') }}</h4>
            <div class="rating-grid">
              <div class="rating-item">
                <span class="rating-label">{{ $t('product.ratingCompany') }}</span>
                <el-tag>{{ product.ratingCompany }}</el-tag>
              </div>
              <div class="rating-item">
                <span class="rating-label">{{ $t('product.ratingGrade') }}</span>
                <el-tag type="success">{{ product.ratingGrade }}</el-tag>
              </div>
              <div class="rating-item" v-if="product.ratingNumber">
                <span class="rating-label">{{ $t('product.ratingNumber') }}</span>
                <span class="rating-value">{{ product.ratingNumber }}</span>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div class="description" v-if="product.description">
            <h4>{{ $t('product.description') }}</h4>
            <p>{{ product.description }}</p>
          </div>

          <!-- Actions -->
          <div class="actions" v-if="product.stock > 0">
            <div class="qty-row">
              <span class="qty-label">{{ $t('product.quantity') }}</span>
              <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
            </div>
            <div class="btn-row">
              <el-button size="large" class="btn-cart" @click="addToCart">
                <el-icon><ShoppingCart /></el-icon>
                {{ $t('product.addToCart') }}
              </el-button>
              <el-button size="large" type="primary" class="btn-buy" @click="buyNow">
                {{ $t('product.buyNow') }}
              </el-button>
            </div>
          </div>

          <!-- Seller info card -->
          <div class="seller-card" v-if="product.sellerName">
            <div class="seller-avatar">{{ product.sellerName.charAt(0).toUpperCase() }}</div>
            <div class="seller-info">
              <span class="seller-label">{{ $t('product.soldBy') }}</span>
              <span class="seller-name">{{ product.sellerName }}</span>
            </div>
            <el-button size="small" text class="seller-contact">Contact</el-button>
          </div>
        </div>
      </div>

      <!-- Related Products -->
      <section class="related-section" v-if="relatedProducts.length">
        <h2 class="related-title">Related Products</h2>
        <div class="related-grid">
          <div
            class="product-card"
            v-for="p in relatedProducts"
            :key="p.id"
            @click="$router.push(`/products/${p.id}`)"
          >
            <div class="card-image">
              <div class="image-placeholder">
                <img v-if="p.images && p.images[0]" :src="p.images[0]" :alt="p.title" />
                <span v-else>{{ p.title.charAt(0) }}</span>
              </div>
              <div class="card-badge" v-if="p.ratingGrade">{{ p.ratingCompany }} {{ p.ratingGrade }}</div>
            </div>
            <div class="card-body">
              <h4 class="card-title">{{ p.title }}</h4>
              <div class="card-meta">
                <span v-if="p.country">{{ $t('countries.' + p.country) || p.country }}</span>
                <span v-if="p.year">{{ p.year }}</span>
              </div>
              <div class="card-footer">
                <span class="card-price">{{ formatPrice(p.price) }} {{ p.currency || 'USD' }}</span>
                <el-tag v-if="p.stock > 5" size="small" type="success" effect="plain">{{ $t('home.inStock') }}</el-tag>
                <el-tag v-else-if="p.stock > 0" size="small" type="warning" effect="plain">{{ $t('product.lowStock', { stock: p.stock }) }}</el-tag>
                <el-tag v-else size="small" type="danger" effect="plain">{{ $t('home.sold') }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'
import { useCartStore } from '../store/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const openCartDrawer = inject('openCartDrawer')
const product = ref(null)
const loading = ref(true)
const quantity = ref(1)
const relatedProducts = ref([])

onMounted(async () => {
  try {
    const res = await api.get(`/products/${route.params.id}`)
    product.value = res.data
    // Fetch related products
    try {
      const relatedRes = await api.get(`/products/${route.params.id}/related`)
      relatedProducts.value = relatedRes.data || []
    } catch (_) {}
  } catch (e) {}
  loading.value = false
})

function addToCart() {
  if (product.value) {
    cartStore.addItem(product.value, quantity.value)
    if (openCartDrawer) openCartDrawer()
  }
}

function buyNow() {
  addToCart()
  router.push('/checkout')
}

function formatPrice(p) { return Number(p).toLocaleString() }
</script>

<style>
.product-detail { background: #f9fafb; min-height: 100vh; }
.detail-container { max-width: 1280px; margin: 0 auto; padding: 32px 24px; }

/* Breadcrumb */
.breadcrumb { margin-bottom: 24px; font-size: 13px; color: #9ca3af; }
.breadcrumb a { color: #6b7280; text-decoration: none; }
.breadcrumb a:hover { color: #3b82f6; }
.breadcrumb .sep { margin: 0 8px; }
.breadcrumb .current { color: #111827; }

/* Main layout */
.detail-main { display: flex; gap: 40px; }

/* Image */
.detail-image { flex: 0 0 42%; }
.image-large {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.image-letter { font-size: 96px; font-weight: 800; color: #d1d5db; }
.image-frame {
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
.product-img { max-width: 100%; max-height: 100%; object-fit: contain; }
.badge-row { margin-top: 12px; text-align: center; }
.grade-badge {
  display: inline-block;
  background: #111827;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 8px;
}
.el-carousel__button { background-color: #d1d5db; }

/* Info */
.detail-info { flex: 1; }
.product-title { font-size: 28px; font-weight: 700; color: #111827; line-height: 1.3; margin-bottom: 14px; }

.meta-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
.meta-tag {
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.price-block {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 0;
  border-top: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 20px;
}
.price-amount { font-size: 32px; font-weight: 800; color: #111827; }

/* Rating */
.rating-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 18px; margin-bottom: 20px; }
.rating-title { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 12px; }
.rating-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.rating-item { display: flex; flex-direction: column; gap: 4px; }
.rating-label { font-size: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: .03em; }
.rating-value { font-size: 15px; font-weight: 600; color: #111827; }

/* Description */
.description { margin-bottom: 24px; }
.description h4 { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 8px; }
.description p { font-size: 14px; color: #6b7280; line-height: 1.7; }

/* Actions */
.actions { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 20px; }
.qty-row { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.qty-label { font-size: 14px; font-weight: 500; color: #374151; }
.btn-row { display: flex; gap: 12px; }
.btn-cart { flex: 1; }
.btn-buy { flex: 2; }

/* Seller card */
.seller-card {
  margin-top: 16px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.seller-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #3b82f6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}
.seller-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.seller-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: .04em; }
.seller-name { font-size: 14px; font-weight: 600; color: #374151; }
.seller-contact { color: #3b82f6; }

/* Related Products */
.related-section { margin-top: 48px; }
.related-title { font-size: 22px; font-weight: 700; color: #111827; margin-bottom: 20px; }
.related-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  cursor: pointer;
  transition: box-shadow .2s, transform .2s;
}
.product-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,.12); transform: translateY(-2px); }
.card-image { position: relative; height: 200px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.image-placeholder img { width: 100%; height: 100%; object-fit: contain; }
.image-placeholder span { font-size: 32px; font-weight: 700; color: #9ca3af; }
.card-badge { position: absolute; top: 10px; right: 10px; background: #111827; color: #fff; font-size: 11px; font-weight: 600; padding: 4px 8px; border-radius: 6px; }
.card-body { padding: 14px; }
.card-title { font-size: 14px; font-weight: 600; color: #111827; line-height: 1.4; margin-bottom: 8px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-meta { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.card-meta span { font-size: 12px; color: #6b7280; background: #f3f4f6; padding: 2px 8px; border-radius: 4px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 18px; font-weight: 700; color: #1f2937; }

@media (max-width: 1024px) { .related-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .detail-main { flex-direction: column; }
  .detail-image { flex: none; }
  .image-large { height: 300px; }
  .product-title { font-size: 22px; }
  .rating-grid { grid-template-columns: repeat(2, 1fr); }
  .related-grid { grid-template-columns: 1fr; }
}
</style>
