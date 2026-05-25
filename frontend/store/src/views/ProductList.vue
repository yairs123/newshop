<template>
  <div class="shop-page">
    <div class="shop-inner">
      <!-- Breadcrumb -->
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('nav.shop') }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- Top bar: Result count + Sort -->
      <div class="shop-topbar">
        <span class="result-count">
          {{ from }}-{{ to }} {{ $t('shop.of') }} {{ total }} {{ $t('shop.productsFound') }}
        </span>
        <div class="sort-area">
          <span class="sort-label">{{ $t('shop.sortLabel') }}</span>
          <el-select v-model="sort" size="small" style="width:160px" @change="search">
            <el-option :label="$t('shop.sortNewest')" value="createdAt,desc" />
            <el-option :label="$t('shop.sortPriceLow')" value="price,asc" />
            <el-option :label="$t('shop.sortPriceHigh')" value="price,desc" />
          </el-select>
        </div>
      </div>

      <!-- Filter Bar: Categories + Rating + Price -->
      <div class="filter-bar">
        <div class="filter-row">
          <span class="filter-label">{{ $t('shop.categories') }}</span>
          <div class="filter-chips">
            <button
              :class="['chip', { active: !filters.categoryId }]"
              @click="filters.categoryId = null; search()"
            >{{ $t('shop.allCategories') }}</button>
            <button
              v-for="cat in categories"
              :key="cat.id"
              :class="['chip', { active: filters.categoryId === cat.id }]"
              @click="filters.categoryId = cat.id; search()"
            >
              {{ $t('categories.' + cat.slug) }}
              <span class="chip-count">{{ categoryCounts[cat.id] || 0 }}</span>
            </button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">{{ $t('shop.ratingCompany') }}</span>
          <div class="filter-chips">
            <button
              :class="['chip', { active: !filters.ratingCompany }]"
              @click="filters.ratingCompany = null; search()"
            >{{ $t('common.all') }}</button>
            <button
              v-for="r in ['NGC','PCGS','PMG']"
              :key="r"
              :class="['chip', { active: filters.ratingCompany === r }]"
              @click="filters.ratingCompany = r; search()"
            >
              {{ r }}
              <span class="chip-count">{{ ratingCounts[r] || 0 }}</span>
            </button>
          </div>
          <div class="filter-price">
            <span class="filter-label">{{ $t('shop.priceRange') }}</span>
            <el-input v-model="filters.minPrice" :placeholder="$t('shop.min')" size="small" class="price-input" />
            <span class="price-sep">—</span>
            <el-input v-model="filters.maxPrice" :placeholder="$t('shop.max')" size="small" class="price-input" />
            <el-button size="small" type="primary" @click="search">{{ $t('shop.apply') }}</el-button>
          </div>
        </div>
      </div>

      <!-- Content: Country sidebar + Product grid -->
      <div class="shop-content">
        <aside class="country-sidebar">
          <h4 class="country-title">{{ $t('shop.country') }}</h4>
          <ul class="country-list">
            <li
              :class="['country-option', { active: !filters.country }]"
              @click="filters.country = null; search()"
            >
              <span class="co-dot" :class="{ filled: !filters.country }"></span>
              <span>{{ $t('common.all') }}</span>
              <span class="co-count">{{ total }}</span>
            </li>
            <li
              v-for="c in availableCountries"
              :key="c"
              :class="['country-option', { active: filters.country === c }]"
              @click="filters.country = c; search()"
            >
              <span class="co-dot" :class="{ filled: filters.country === c }"></span>
              <span>{{ $t('countries.' + c) }}</span>
              <span class="co-count">{{ countryCounts[c] || 0 }}</span>
            </li>
          </ul>
        </aside>

        <main class="shop-main">
          <!-- Product Grid -->
          <div class="product-grid" v-loading="loading">
            <div
              class="product-card"
              v-for="p in products"
              :key="p.id"
              @click="$router.push(`/products/${p.id}`)"
            >
              <div class="card-image">
                <div class="image-placeholder">
                  <span>{{ p.title.charAt(0) }}</span>
                </div>
                <div class="card-badge" v-if="p.ratingGrade">
                  {{ p.ratingCompany }} {{ p.ratingGrade }}
                </div>
                <button class="favorite-btn" :class="{ active: favoritesStore.isFavorite(p.id) }" @click.stop="favoritesStore.toggle(p)">
                  <el-icon :size="18">
                    <StarFilled v-if="favoritesStore.isFavorite(p.id)" />
                    <Star v-else />
                  </el-icon>
                </button>
              </div>
              <div class="card-body">
                <h4 class="card-title">{{ p.title }}</h4>
                <div class="card-meta">
                  <span v-if="p.country">{{ $t('countries.' + p.country) }}</span>
                  <span v-if="p.year">{{ p.year }}</span>
                  <span v-if="p.material">{{ p.material }}</span>
                </div>
                <div class="card-footer">
                  <span class="card-price">{{ formatPrice(p.price) }} {{ p.currency || 'USD' }}</span>
                  <div class="card-actions">
                    <el-button v-if="p.stock > 0" size="small" type="primary" :icon="ShoppingCart" @click.stop="addToCart(p)">{{ $t('product.addToCart') }}</el-button>
                    <el-tag v-if="p.stock > 0" size="small" type="success" effect="plain">{{ $t('home.inStock') }}</el-tag>
                    <el-tag v-else size="small" type="danger" effect="plain">{{ $t('home.sold') }}</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty -->
          <div class="empty-state" v-if="!loading && products.length === 0">
            <el-icon :size="48"><Search /></el-icon>
            <p>{{ $t('shop.noProducts') }}</p>
          </div>

          <!-- Pagination -->
          <div class="pagination-wrap" v-if="total > 0">
            <span class="page-info">{{ from }}-{{ to }} / {{ total }}</span>
            <div class="pagination-btns">
              <button class="page-btn" :disabled="page <= 1" @click="page--; loadProducts()">{{ $t('shop.prev') }}</button>
              <button
                v-for="p in pageNumbers"
                :key="p"
                :class="['page-btn', { active: p === page }]"
                @click="page = p; loadProducts()"
              >{{ p === '...' ? '...' : p }}</button>
              <button class="page-btn" :disabled="page >= totalPages" @click="page++; loadProducts()">{{ $t('shop.next') }}</button>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../store/cart'
import { useFavoritesStore } from '../store/favorites'
import { api } from '../api'
import { Star, StarFilled, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const favoritesStore = useFavoritesStore()
const openCartDrawer = inject('openCartDrawer')
const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(40)
const loading = ref(false)
const sort = ref('createdAt,desc')

const filters = reactive({
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  ratingCompany: null,
  country: null,
  minPrice: null,
  maxPrice: null,
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const from = computed(() => total.value === 0 ? 0 : (page.value - 1) * size.value + 1)
const to = computed(() => Math.min(page.value * size.value, total.value))

const pageNumbers = computed(() => {
  const tp = totalPages.value
  const cp = page.value
  const pages = []
  if (tp <= 10) {
    for (let i = 1; i <= tp; i++) pages.push(i)
    return pages
  }
  pages.push(1)
  if (cp > 3) pages.push('...')
  for (let i = Math.max(2, cp - 1); i <= Math.min(tp - 1, cp + 1); i++) pages.push(i)
  if (cp < tp - 2) pages.push('...')
  pages.push(tp)
  return pages
})

// Computed filter counts from current products
const categoryCounts = computed(() => {
  const counts = {}
  products.value.forEach(p => {
    if (p.categoryId) counts[p.categoryId] = (counts[p.categoryId] || 0) + 1
  })
  return counts
})

const ratingCounts = computed(() => {
  const counts = {}
  products.value.forEach(p => {
    if (p.ratingCompany) counts[p.ratingCompany] = (counts[p.ratingCompany] || 0) + 1
  })
  return counts
})

const countryCounts = computed(() => {
  const counts = {}
  products.value.forEach(p => {
    if (p.country) counts[p.country] = (counts[p.country] || 0) + 1
  })
  return counts
})

const availableCountries = computed(() => {
  const countries = [...new Set(products.value.map(p => p.country).filter(Boolean))]
  return countries.sort()
})

onMounted(async () => {
  await Promise.all([loadCategories(), loadProducts()])
})

// Watch route query changes (e.g., keyword from header search)
watch(() => route.query, () => {
  filters.keyword = route.query.keyword || ''
  filters.categoryId = route.query.categoryId ? Number(route.query.categoryId) : null
  page.value = 1
  loadProducts()
})

async function loadCategories() {
  try {
    const res = await api.get('/products/categories')
    categories.value = res.data || []
  } catch (e) {}
}

async function loadProducts() {
  loading.value = true
  const params = { page: page.value - 1, size: size.value }
  if (filters.keyword) params.keyword = filters.keyword
  if (filters.categoryId) params.categoryId = filters.categoryId
  if (filters.ratingCompany) params.ratingCompany = filters.ratingCompany
  if (filters.country) params.country = filters.country
  if (filters.minPrice) params.minPrice = filters.minPrice
  if (filters.maxPrice) params.maxPrice = filters.maxPrice
  if (sort.value) {
    const [field, dir] = sort.value.split(',')
    params.sort = `${field},${dir}`
  }
  try {
    const res = await api.get('/products', { params })
    products.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) {}
  loading.value = false
}

function search() {
  page.value = 1
  loadProducts()
}

function addToCart(product) {
  cartStore.addItem(product, 1)
  if (openCartDrawer) openCartDrawer()
}

function formatPrice(p) { return Number(p).toLocaleString() }
</script>

<style>
.shop-page { background: #f9fafb; min-height: 100vh; }
.shop-inner { max-width: 1280px; margin: 0 auto; padding: 24px 24px 40px; }

/* Top bar */
.shop-topbar { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 16px; }
.result-count { font-size: 14px; color: #6b7280; margin-right: auto; }
.sort-area { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.sort-label { font-size: 13px; color: #6b7280; }

/* Filter Bar */
.filter-bar {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 20px;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.filter-row + .filter-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}
.filter-label { font-size: 12px; font-weight: 600; color: #9ca3af; text-transform: uppercase; letter-spacing: .04em; white-space: nowrap; }
.filter-chips { display: flex; gap: 6px; flex-wrap: wrap; }
.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
  background: #f3f4f6;
  border: 1px solid transparent;
  border-radius: 20px;
  cursor: pointer;
  transition: all .15s;
  white-space: nowrap;
}
.chip:hover { background: #eff6ff; color: #3b82f6; border-color: #bfdbfe; }
.chip.active { background: #dbeafe; color: #1d4ed8; font-weight: 600; border-color: #93c5fd; }
.chip-count { font-size: 11px; color: #9ca3af; font-weight: 400; }
.chip.active .chip-count { color: #60a5fa; }

/* Price */
.filter-price { display: flex; align-items: center; gap: 6px; margin-left: auto; }
.price-input { width: 90px; }
.price-sep { color: #9ca3af; font-size: 13px; }

/* Content Area */
.shop-content { display: flex; gap: 24px; }

/* Country Sidebar */
.country-sidebar { width: 180px; flex-shrink: 0; }
.country-title { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: .06em; color: #374151; margin-bottom: 12px; }
.country-list { list-style: none; }
.country-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  font-size: 13px;
  color: #4b5563;
  cursor: pointer;
  border-radius: 6px;
  transition: all .15s;
}
.country-option:hover { background: #f3f4f6; color: #374151; }
.country-option.active { background: #eff6ff; color: #1d4ed8; font-weight: 600; }
.co-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  flex-shrink: 0;
  transition: all .15s;
}
.co-dot.filled { background: #3b82f6; border-color: #3b82f6; }
.co-count { margin-left: auto; font-size: 11px; color: #9ca3af; min-width: 20px; text-align: right; }
.country-option.active .co-count { color: #60a5fa; }

/* Main */
.shop-main { flex: 1; min-width: 0; }

/* Product Grid */
.product-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }

/* Product Card */
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  cursor: pointer;
  transition: box-shadow .2s, transform .2s;
  display: flex;
  flex-direction: column;
}
.product-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,.12); transform: translateY(-2px); }
.card-image { position: relative; height: 200px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
.image-placeholder { width: 80px; height: 80px; background: #e5e7eb; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 700; color: #9ca3af; }
.card-badge { position: absolute; top: 10px; right: 10px; background: #111827; color: #fff; font-size: 11px; font-weight: 600; padding: 4px 8px; border-radius: 6px; }
.favorite-btn { position: absolute; top: 8px; left: 8px; width: 32px; height: 32px; border-radius: 50%; border: none; background: rgba(255,255,255,0.9); color: #9ca3af; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all .15s; z-index: 2; }
.favorite-btn:hover { color: #ef4444; background: #fff; }
.favorite-btn.active { color: #ef4444; }
.card-body { padding: 14px; display: flex; flex-direction: column; flex: 1; }
.card-title { font-size: 14px; font-weight: 600; color: #111827; line-height: 1.4; margin-bottom: 8px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; min-height: 2.8em; }
.card-meta { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; min-height: 20px; }
.card-meta span { font-size: 12px; color: #6b7280; background: #f3f4f6; padding: 2px 8px; border-radius: 4px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-actions { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.card-price { font-size: 18px; font-weight: 700; color: #1f2937; }

/* Empty */
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 64px 0; color: #9ca3af; }
.empty-state p { margin-top: 12px; font-size: 14px; }

/* Pagination */
.pagination-wrap { display: flex; flex-direction: column; align-items: center; gap: 12px; margin-top: 32px; }
.page-info { font-size: 13px; color: #6b7280; }
.pagination-btns { display: flex; gap: 4px; align-items: center; }
.page-btn {
  min-width: 36px; height: 36px;
  padding: 0 10px;
  border: 1px solid #d1d5db;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: all .15s;
}
.page-btn:hover:not(:disabled):not(.active) { border-color: #3b82f6; color: #3b82f6; }
.page-btn.active { background: #3b82f6; border-color: #3b82f6; color: #fff; }
.page-btn:disabled { opacity: .4; cursor: not-allowed; }

/* Responsive */
@media (max-width: 1024px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .country-sidebar { width: 150px; }
  .filter-price { margin-left: 0; }
}
@media (max-width: 768px) {
  .shop-content { flex-direction: column; }
  .country-sidebar { width: 100%; }
  .country-list { display: flex; flex-wrap: wrap; gap: 4px; }
  .country-option { padding: 5px 10px; }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .filter-row { flex-direction: column; align-items: flex-start; }
  .filter-price { margin-left: 0; width: 100%; }
}
@media (max-width: 480px) {
  .product-grid { grid-template-columns: 1fr; }
}
</style>
