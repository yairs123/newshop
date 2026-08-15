<template>
  <div class="home" v-loading="loading">
    <!-- Hero with search -->
    <section class="hero">
      <div class="hero-content">
        <h1>{{ $t('hero.title') }}</h1>
        <p>{{ $t('hero.subtitle') }}</p>
        <div class="hero-search">
          <el-input
            v-model="searchQ"
            :placeholder="$t('shop.searchPlaceholder')"
            size="large"
            clearable
            @keyup.enter="doSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append><el-button @click="doSearch">{{ $t('hero.cta') }}</el-button></template>
          </el-input>
        </div>
      </div>
    </section>

    <!-- Ads Carousel (compact) -->
    <section class="ads-bar" v-if="ads.length > 0">
      <el-carousel height="240px" indicator-position="inside" :interval="5000">
        <el-carousel-item v-for="ad in ads" :key="ad.id">
          <a :href="ad.linkUrl" target="_blank" class="ad-link">
            <el-image :src="ad.imageUrl" fit="cover" lazy style="width:100%;height:240px" />
          </a>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- Products -->
    <section class="products-section">
      <div class="section-inner">
        <div class="section-header">
          <h2>{{ $t('home.featured') }}</h2>
          <div class="header-right">
            <span class="product-count">{{ $t('home.itemsCount', { count: allProducts.length }) }}</span>
            <router-link to="/products" class="view-all">{{ $t('home.viewAll') }} →</router-link>
          </div>
        </div>
        <div class="product-grid">
          <div class="product-card" v-for="p in displayProducts" :key="p.id" @click="$router.push(`/products/${p.id}`)">
            <div class="card-image">
              <el-image
                v-if="primaryImage(p)"
                :src="primaryImage(p)"
                fit="cover"
                lazy
                class="home-card-img"
              >
                <template #error>
                  <div class="image-placeholder"><span>{{ (p.title || '?').charAt(0) }}</span></div>
                </template>
              </el-image>
              <div v-else class="image-placeholder"><span>{{ (p.title || '?').charAt(0) }}</span></div>
              <div class="card-badge" v-if="p.ratingGrade">{{ p.ratingCompany }} {{ p.ratingGrade }}</div>
              <div class="card-stock" v-if="p.stock <= 3 && p.stock > 0">{{ $t('home.lowStock', { count: p.stock }) }}</div>
            </div>
            <div class="card-body">
              <h4 class="card-title">{{ p.title }}</h4>
              <div class="card-meta">
                <span v-if="p.country">{{ $t('countries.' + p.country) || p.country }}</span>
                <span v-if="p.year">{{ p.year }}</span>
                <span v-if="p.material">{{ p.material }}</span>
              </div>
              <div class="card-footer">
                <span class="card-price">{{ formatPrice(p.price) }} {{ p.currency || 'USD' }}</span>
                <el-tag v-if="p.stock > 0" size="small" type="success" effect="plain">{{ $t('home.inStock') }}</el-tag>
                <el-tag v-else size="small" type="danger" effect="plain">{{ $t('home.sold') }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features (compact) -->
    <section class="features-bar">
      <div class="section-inner">
        <div class="feature-chip" v-for="(f, i) in featureKeys" :key="i">
          <el-icon :size="18"><component :is="f.icon" /></el-icon>
          <span>{{ $t(`features.${f.key}.title`) }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()

const allProducts = ref([])
const ads = ref([])
const loading = ref(true)
const searchQ = ref('')
const featureKeys = [
  { icon: 'Medal', key: 'certified' },
  { icon: 'Checked', key: 'protection' },
  { icon: 'Connection', key: 'global' },
  { icon: 'Van', key: 'shipping' },
]

const displayProducts = computed(() => allProducts.value.slice(0, 8))

function doSearch() {
  if (searchQ.value.trim()) {
    router.push(`/products?keyword=${encodeURIComponent(searchQ.value)}`)
  }
}

onMounted(async () => {
  try {
    const [prodRes, adsRes] = await Promise.all([
      api.get('/products', { params: { page: 0, size: 8 } }),
      api.get('/ads/active').catch(() => ({ data: [] })),
    ])
    allProducts.value = prodRes.data?.content || []
    ads.value = adsRes.data || []
  } catch (e) {}
  loading.value = false
})

function formatPrice(p) { return Number(p || 0).toLocaleString() }

function primaryImage(p) {
  const first = (p.images || [])[0]
  if (!first) return ''
  return typeof first === 'string' ? first : (first.url || first.imageUrl || '')
}
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #1e40af 100%);
  padding: 64px 24px;
  text-align: center;
  color: #fff;
}
.hero-content { max-width: 560px; margin: 0 auto; }
.hero h1 { font-size: 36px; font-weight: 800; margin-bottom: 8px; }
.hero p { font-size: 16px; color: #bfdbfe; margin-bottom: 24px; }
.hero-search :deep(.el-input__wrapper) { border-radius: 8px 0 0 8px; background: #fff; }
.hero-search :deep(.el-input-group__append) { border-radius: 0 8px 8px 0; }

/* Ads */
.ads-bar { background: #f9fafb; }
.ad-link { display: block; position: relative; }

/* Products */
.products-section { background: #f9fafb; }
.section-inner { max-width: 1280px; margin: 0 auto; padding: 40px 24px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-header h2 { font-size: 22px; font-weight: 700; color: #111827; }
.header-right { display: flex; align-items: center; gap: 16px; }
.product-count { font-size: 13px; color: #9ca3af; }
.view-all { color: #3b82f6; font-size: 14px; font-weight: 500; text-decoration: none; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card {
  background: #fff; border-radius: 10px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.06); cursor: pointer;
  transition: box-shadow .2s, transform .2s;
}
.product-card:hover { box-shadow: 0 6px 20px rgba(0,0,0,.1); transform: translateY(-2px); }
.card-image { position: relative; height: 180px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
.home-card-img { position: absolute; inset: 0; width: 100%; height: 100%; }
.image-placeholder { width: 64px; height: 64px; background: #e5e7eb; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; color: #9ca3af; }
.card-badge { position: absolute; top: 8px; right: 8px; background: #111827; color: #fff; font-size: 10px; font-weight: 600; padding: 3px 6px; border-radius: 4px; }
.card-stock { position: absolute; top: 8px; left: 8px; background: #f56c6c; color: #fff; font-size: 10px; font-weight: 600; padding: 3px 6px; border-radius: 4px; }
.card-body { padding: 12px; }
.card-title { font-size: 13px; font-weight: 600; color: #111827; margin-bottom: 6px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-meta { display: flex; gap: 6px; margin-bottom: 8px; flex-wrap: wrap; }
.card-meta span { font-size: 11px; color: #6b7280; background: #f3f4f6; padding: 2px 6px; border-radius: 3px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 16px; font-weight: 700; color: #1f2937; }

/* Features bar (compact) */
.features-bar { background: #fff; border-top: 1px solid #e5e7eb; }
.features-bar .section-inner { display: flex; justify-content: center; gap: 32px; padding: 20px 24px; }
.feature-chip { display: flex; align-items: center; gap: 6px; color: #6b7280; font-size: 13px; }
.feature-chip .el-icon { color: #3b82f6; }

@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } .hero h1 { font-size: 28px; } }
@media (max-width: 480px) { .product-grid { grid-template-columns: 1fr; } .features-bar .section-inner { flex-wrap: wrap; gap: 16px; } }
</style>
