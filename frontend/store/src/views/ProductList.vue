<template>
  <div class="shop-page">
    <div class="shop-inner">
      <!-- Breadcrumb -->
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('nav.shop') }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- Top bar: Result count + active search indicator + Sort -->
      <div class="shop-topbar">
        <div class="topbar-left">
          <span class="result-count">
            {{ from }}-{{ to }} {{ $t('shop.of') }} {{ total }} {{ $t('shop.productsFound') }}
          </span>
          <span v-if="filters.keyword" class="active-search-tag">
            <span class="active-search-label">{{ $t('shop.searchingFor') }}</span>
            <span class="active-search-value">{{ filters.keyword }}</span>
            <button class="active-search-clear" @click="clearSearch" aria-label="Clear search">×</button>
          </span>
        </div>
        <div class="topbar-right">
          <el-button
            v-if="hasActiveFilters"
            size="small"
            class="clear-filters-btn"
            @click="clearAllFilters"
          >{{ $t('shop.clearFilters') }}</el-button>
          <div class="sort-area">
            <span class="sort-label">{{ $t('shop.sortLabel') }}</span>
            <el-select v-model="sort" size="small" style="width:160px" @change="changeSort">
              <el-option :label="$t('shop.sortNewest')" value="createdAt,desc" />
              <el-option :label="$t('shop.sortPriceLow')" value="price,asc" />
              <el-option :label="$t('shop.sortPriceHigh')" value="price,desc" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- Search input (debounced) -->
      <div class="search-row">
        <el-input
          v-model="searchInput"
          :placeholder="$t('shop.searchPlaceholder')"
          size="default"
          class="list-search-input"
          clearable
          @input="onSearchInput"
          @clear="clearSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- Filter Bar (sticky) -->
      <div class="filter-bar sticky-filter">
        <div v-if="popularCategories.length" class="filter-row">
          <span class="filter-label">{{ $t('shop.popular') }}</span>
          <div class="filter-chips">
            <UiFilterChip
              v-for="cat in popularCategories"
              :key="cat.id"
              :active="filters.categoryId === cat.id"
              :count="categoryCounts[cat.id] || 0"
              @click="selectCategory(cat.id)"
            >
              {{ $t('categories.' + cat.slug) }}
            </UiFilterChip>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">{{ $t('shop.categories') }}</span>
          <div class="filter-chips">
            <UiFilterChip :active="!filters.categoryId" @click="selectCategory(null)">
              {{ $t('shop.allCategories') }}
            </UiFilterChip>
            <UiFilterChip
              v-for="cat in categories"
              :key="cat.id"
              :active="filters.categoryId === cat.id"
              :count="categoryCounts[cat.id] || 0"
              @click="selectCategory(cat.id)"
            >
              {{ $t('categories.' + cat.slug) }}
            </UiFilterChip>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">{{ $t('shop.ratingCompany') }}</span>
          <div class="filter-chips">
            <UiFilterChip :active="!filters.ratingCompany" @click="selectRatingCompany(null)">
              {{ $t('common.all') }}
            </UiFilterChip>
            <UiFilterChip
              v-for="r in ['NGC','PCGS','PMG']"
              :key="r"
              :active="filters.ratingCompany === r"
              :count="ratingCounts[r] || 0"
              @click="selectRatingCompany(r)"
            >
              {{ r }}
            </UiFilterChip>
          </div>
          <div class="filter-price">
            <span class="filter-label">{{ $t('shop.priceRange') }}</span>
            <el-input v-model="filters.minPrice" :placeholder="$t('shop.min')" size="small" class="price-input" />
            <span class="price-sep">—</span>
            <el-input v-model="filters.maxPrice" :placeholder="$t('shop.max')" size="small" class="price-input" />
            <el-button size="small" type="primary" @click="applyFilters">{{ $t('shop.apply') }}</el-button>
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
              @click="selectCountry(null)"
            >
              <span class="co-dot" :class="{ filled: !filters.country }"></span>
              <span>{{ $t('common.all') }}</span>
              <span class="co-count">{{ total }}</span>
            </li>
            <li
              v-for="c in availableCountries"
              :key="c"
              :class="['country-option', { active: filters.country === c }]"
              @click="selectCountry(c)"
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
            <UiProductCard
              v-for="p in products"
              :key="p.id"
              :product="p"
              :isFavorite="favoritesStore.isFavorite(p.id)"
              @add-to-cart="addToCart"
              @toggle-favorite="handleFavorite"
              @open-product="goToProduct"
            />
          </div>

          <!-- Empty -->
          <UiEmptyState v-if="!loading && products.length === 0" :message="$t('shop.noProducts')" />

          <!-- Pagination -->
          <UiPagination
            :page="page"
            :total="total"
            :size="size"
            @update:page="onPageChange"
          />
        </main>
      </div>
    </div>

    <!-- Back to top button -->
    <transition name="fade">
      <button v-if="showBackToTop" class="back-to-top" @click="scrollToTop" aria-label="Back to top">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 15l-6-6-6 6"/></svg>
      </button>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../store/cart'
import { useFavoritesStore } from '../store/favorites'
import UiProductCard from '../components/ui/UiProductCard.vue'
import UiPagination from '../components/ui/UiPagination.vue'
import UiEmptyState from '../components/ui/UiEmptyState.vue'
import UiFilterChip from '../components/ui/UiFilterChip.vue'
import { useProductSearch } from '../composables/useProductSearch'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const favoritesStore = useFavoritesStore()
const openCartDrawer = inject('openCartDrawer')

const {
  products,
  categories,
  total,
  loading,
  page,
  size,
  sort,
  filters,
  loadCategories,
  loadProducts
} = useProductSearch({
  page: route.query.page ? Number(route.query.page) : 1,
  size: 40,
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null
})

// === Keyword search (debounced) ===
const searchInput = ref(route.query.keyword || '')
let debounceTimer = null

const hasActiveFilters = computed(() =>
  !!filters.keyword || !!filters.categoryId || !!filters.ratingCompany ||
  !!filters.country || !!filters.minPrice || !!filters.maxPrice
)

const POPULAR_CATEGORY_SLUGS = [
  'ancient-coins', 'gold-coins', 'silver-coins',
  'chinese-coins', 'world-coins', 'commemorative-coins'
]

const popularCategories = computed(() =>
  POPULAR_CATEGORY_SLUGS
    .map(slug => categories.value.find(c => c.slug === slug))
    .filter(Boolean)
)

function onSearchInput() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    filters.keyword = searchInput.value.trim()
    page.value = 1
    updateRouteQuery()
  }, 300)
}

function handleSearch() {
  clearTimeout(debounceTimer)
  filters.keyword = searchInput.value.trim()
  page.value = 1
  updateRouteQuery()
}

function clearSearch() {
  clearTimeout(debounceTimer)
  searchInput.value = ''
  filters.keyword = ''
  page.value = 1
  updateRouteQuery()
}

function clearAllFilters() {
  clearTimeout(debounceTimer)
  searchInput.value = ''
  filters.keyword = ''
  filters.categoryId = null
  filters.ratingCompany = null
  filters.country = null
  filters.minPrice = null
  filters.maxPrice = null
  page.value = 1
  updateRouteQuery()
}

const from = computed(() => total.value === 0 ? 0 : (page.value - 1) * size.value + 1)
const to = computed(() => Math.min(page.value * size.value, total.value))

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

watch(() => route.query, () => {
  searchInput.value = route.query.keyword || ''
  filters.keyword = route.query.keyword || ''
  filters.categoryId = route.query.categoryId ? Number(route.query.categoryId) : null
  filters.ratingCompany = route.query.ratingCompany || null
  filters.country = route.query.country || null
  filters.minPrice = route.query.minPrice ? Number(route.query.minPrice) : null
  filters.maxPrice = route.query.maxPrice ? Number(route.query.maxPrice) : null
  sort.value = route.query.sort || 'createdAt,desc'
  page.value = route.query.page ? Number(route.query.page) : 1
  loadProducts()
})

function changeSort(value) {
  // Push the new sort to the route; the route watcher performs the reload.
  // (Previously setSort() also reloaded here, causing a redundant second call.)
  sort.value = value
  page.value = 1
  updateRouteQuery()
}

function addToCart(product) {
  cartStore.addItem(product, 1)
  if (openCartDrawer) openCartDrawer()
}

function handleFavorite(product) {
  favoritesStore.toggle(product)
}

function goToProduct(productId) {
  router.push(`/products/${productId}`)
}

function updateRouteQuery() {
  const query = {}
  if (filters.keyword) query.keyword = filters.keyword
  if (filters.categoryId) query.categoryId = String(filters.categoryId)
  if (filters.ratingCompany) query.ratingCompany = filters.ratingCompany
  if (filters.country) query.country = filters.country
  if (filters.minPrice) query.minPrice = String(filters.minPrice)
  if (filters.maxPrice) query.maxPrice = String(filters.maxPrice)
  if (sort.value) query.sort = sort.value
  query.page = String(page.value)
  router.replace({ query })
}

function applyFilters() {
  page.value = 1
  updateRouteQuery()
}

function selectCategory(categoryId) {
  filters.categoryId = categoryId
  page.value = 1
  updateRouteQuery()
}

function selectRatingCompany(ratingCompany) {
  filters.ratingCompany = ratingCompany
  page.value = 1
  updateRouteQuery()
}

function selectCountry(country) {
  filters.country = country
  page.value = 1
  updateRouteQuery()
}

// === UX: Back to top + sticky filter ===
const showBackToTop = ref(false)
let scrollHandler = null

onMounted(function() {
  scrollHandler = function() {
    showBackToTop.value = window.scrollY > 400
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
})

onUnmounted(function() {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
  if (debounceTimer) clearTimeout(debounceTimer)
})

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onPageChange(newPage) {
  page.value = newPage
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.shop-page { background: #f9fafb; min-height: 100vh; }
.shop-inner { max-width: 1280px; margin: 0 auto; padding: 24px 24px 40px; }

/* Top bar */
.shop-topbar { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 16px; flex-wrap: wrap; }
.topbar-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.topbar-left .result-count { margin-right: 0; white-space: nowrap; }
.topbar-right { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.result-count { font-size: 14px; color: #6b7280; }
.sort-area { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.sort-label { font-size: 13px; color: #6b7280; }

/* Active search indicator */
.active-search-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 8px 3px 12px;
  font-size: 13px;
  background: #fffbeb;
  border: 1px solid #fcd34d;
  border-radius: 20px;
  max-width: 100%;
}
.active-search-label { color: #b45309; font-weight: 600; white-space: nowrap; }
.active-search-value {
  color: #92400e;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 220px;
}
.active-search-clear {
  width: 18px;
  height: 18px;
  border: none;
  border-radius: 50%;
  background: #fde68a;
  color: #92400e;
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.15s;
}
.active-search-clear:hover { background: #f59e0b; color: #fff; }

.clear-filters-btn { color: #b45309; }
.clear-filters-btn:hover { border-color: #f59e0b; background: #fffbeb; }

/* Search input */
.search-row { margin-bottom: 16px; }
.list-search-input { max-width: 480px; }
.list-search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  border: 1px solid #e5e7eb;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.list-search-input :deep(.el-input__wrapper:hover),
.list-search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #f59e0b;
  box-shadow: 0 0 0 1px rgba(245,158,11,.25);
}

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
  .list-search-input { max-width: 100%; }
  .active-search-value { max-width: 140px; }
  .topbar-right { flex-wrap: wrap; }
}
.back-to-top {
	  position: fixed;
	  bottom: 32px;
	  right: 24px;
	  width: 44px;
	  height: 44px;
	  border-radius: 50%;
	  background: #f59e0b;
	  color: #fff;
	  border: none;
	  box-shadow: 0 4px 12px rgba(245,158,11,0.3);
	  cursor: pointer;
	  display: flex;
	  align-items: center;
	  justify-content: center;
	  z-index: 999;
	  transition: all 0.2s;
	}
	.back-to-top:hover {
	  background: #d97706;
	  transform: translateY(-2px);
	  box-shadow: 0 6px 20px rgba(245,158,11,0.4);
	}
	.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
	.fade-enter-from, .fade-leave-to { opacity: 0; }
	
	@media (max-width: 480px) {
  .product-grid { grid-template-columns: 1fr; }
}
</style>
