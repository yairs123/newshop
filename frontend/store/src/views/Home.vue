<template>
  <div class="home" v-loading="loading">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-content">
        <h1>{{ $t('hero.title') }}</h1>
        <p>{{ $t('hero.subtitle') }}</p>
        <el-button type="primary" size="large" round @click="$router.push('/products')">
          {{ $t('hero.cta') }}
        </el-button>
      </div>
    </section>

    <!-- Ads Carousel -->
    <section class="ads-carousel" v-if="ads.length > 0">
      <el-carousel height="360px" indicator-position="outside">
        <el-carousel-item v-for="ad in ads" :key="ad.id">
          <a :href="ad.linkUrl" target="_blank" class="ad-link">
            <el-image :src="ad.imageUrl" fit="cover" style="width:100%;height:360px" />
            <div class="ad-caption">
              <h3>{{ ad.title }}</h3>
            </div>
          </a>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- Features -->
    <section class="features">
      <div class="section-inner">
        <div class="feature-card" v-for="(f, i) in featureKeys" :key="i">
          <el-icon :size="28"><component :is="f.icon" /></el-icon>
          <h4>{{ $t(`features.${f.key}.title`) }}</h4>
          <p>{{ $t(`features.${f.key}.desc`) }}</p>
        </div>
      </div>
    </section>

    <!-- Featured Products -->
    <section class="featured-section">
      <div class="section-inner">
        <div class="section-header">
          <h2>{{ $t('home.featured') }}</h2>
          <router-link to="/products" class="view-all">{{ $t('home.viewAll') }}</router-link>
        </div>
        <div class="product-grid">
          <div class="product-card" v-for="p in products" :key="p.id" @click="$router.push(`/products/${p.id}`)">
            <div class="card-image">
              <div class="image-placeholder">
                <span>{{ p.title.charAt(0) }}</span>
              </div>
              <div class="card-badge" v-if="p.ratingGrade">
                {{ p.ratingCompany }} {{ p.ratingGrade }}
              </div>
            </div>
            <div class="card-body">
              <h4 class="card-title">{{ p.title }}</h4>
              <div class="card-meta">
                <span v-if="p.country">{{ p.country }}</span>
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

    <!-- About Us -->
    <section class="about-section">
      <div class="section-inner">
        <h2>{{ $t('home.aboutTitle') }}</h2>
        <p>{{ $t('home.aboutContent') }}</p>
      </div>
    </section>

    <!-- News Section -->
    <section class="news-section" v-if="newsList.length > 0">
      <div class="section-inner">
        <div class="section-header">
          <h2>{{ $t('home.newsSectionTitle') }}</h2>
          <router-link to="/products" class="view-all">{{ $t('home.newsViewAll') }}</router-link>
        </div>
        <div class="news-grid">
          <div class="news-card" v-for="n in newsList" :key="n.id" @click="$router.push(`/news/${n.id}`)">
            <div class="news-image">
              <el-image v-if="n.imageUrl" :src="n.imageUrl" fit="cover" style="width:100%;height:180px" />
              <div class="news-image-placeholder" v-else>
                <span>{{ n.title.charAt(0) }}</span>
              </div>
            </div>
            <div class="news-body">
              <h4 class="news-title">{{ n.title }}</h4>
              <p class="news-summary">{{ n.summary ? n.summary.substring(0, 80) + (n.summary.length > 80 ? '...' : '') : '' }}</p>
              <div class="news-footer">
                <span class="news-date">{{ n.publishedAt ? n.publishedAt.substring(0, 10) : '' }}</span>
                <span class="news-read-more">{{ $t('home.newsReadMore') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Latest Products -->
    <section class="latest-section">
      <div class="section-inner">
        <div class="section-header">
          <h2>{{ $t('home.latestTitle') }}</h2>
          <router-link to="/products" class="view-all">{{ $t('home.viewAll') }}</router-link>
        </div>
        <div class="product-grid">
          <div class="product-card" v-for="p in latestProducts" :key="p.id" @click="$router.push(`/products/${p.id}`)">
            <div class="card-image">
              <div class="image-placeholder"><span>{{ p.title.charAt(0) }}</span></div>
              <div class="card-badge" v-if="p.ratingGrade">{{ p.ratingCompany }} {{ p.ratingGrade }}</div>
            </div>
            <div class="card-body">
              <h4 class="card-title">{{ p.title }}</h4>
              <div class="card-meta">
                <span v-if="p.country">{{ p.country }}</span>
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

    <!-- Partner With Us -->
    <section class="partner-section">
      <div class="section-inner">
        <h2>{{ $t('home.partnerTitle') }}</h2>
        <p>{{ $t('home.partnerContent') }}</p>
        <el-button type="primary" size="large" round @click="$router.push('/contact')">
          {{ $t('home.partnerCta') }}
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const products = ref([])
const latestProducts = ref([])
const ads = ref([])
const newsList = ref([])
const loading = ref(true)
const featureKeys = [
  { icon: 'Medal', key: 'certified' },
  { icon: 'Checked', key: 'protection' },
  { icon: 'Connection', key: 'global' },
  { icon: 'Van', key: 'shipping' },
]

onMounted(async () => {
  try {
    const [featuredRes, latestRes, adsRes, newsRes] = await Promise.all([
      api.get('/products', { params: { page: 0, size: 8 } }),
      api.get('/products', { params: { page: 0, size: 4, sort: 'createdAt,desc' } }),
      api.get('/ads/active').catch(() => ({ data: [] })),
      api.get('/news').catch(() => ({ data: [] })),
    ])
    products.value = featuredRes.data?.content || []
    latestProducts.value = latestRes.data?.content || []
    ads.value = adsRes.data || []
    newsList.value = (newsRes.data || []).slice(0, 3)
  } catch (e) {}
  loading.value = false
})

function formatPrice(p) { return Number(p).toLocaleString() }
</script>

<style>
/* Hero */
.hero {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #1e40af 100%);
  padding: 80px 24px;
  text-align: center;
  color: #fff;
}
.hero-content { max-width: 640px; margin: 0 auto; }
.hero h1 { font-size: 42px; font-weight: 800; margin-bottom: 12px; letter-spacing: -.02em; }
.hero p { font-size: 18px; color: #bfdbfe; margin-bottom: 28px; }

/* Features */
.features { background: #fff; border-bottom: 1px solid #e5e7eb; }
.section-inner { max-width: 1280px; margin: 0 auto; padding: 48px 24px; }
.features .section-inner { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.feature-card { text-align: center; padding: 24px 16px; }
.feature-card .el-icon { color: #3b82f6; margin-bottom: 12px; }
.feature-card h4 { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.feature-card p { font-size: 13px; color: #6b7280; }

/* Featured Products */
.featured-section { background: #f9fafb; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-header h2 { font-size: 24px; font-weight: 700; color: #111827; }
.view-all { color: #3b82f6; font-size: 14px; font-weight: 500; text-decoration: none; }
.view-all:hover { text-decoration: underline; }

/* Product Grid */
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }

/* Product Card */
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
.image-placeholder { width: 80px; height: 80px; background: #e5e7eb; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 700; color: #9ca3af; }
.card-badge { position: absolute; top: 10px; right: 10px; background: #111827; color: #fff; font-size: 11px; font-weight: 600; padding: 4px 8px; border-radius: 6px; }
.card-body { padding: 14px; }
.card-title { font-size: 14px; font-weight: 600; color: #111827; line-height: 1.4; margin-bottom: 8px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-meta { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.card-meta span { font-size: 12px; color: #6b7280; background: #f3f4f6; padding: 2px 8px; border-radius: 4px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 18px; font-weight: 700; color: #1f2937; }

/* About */
.about-section { background: #fff; border-bottom: 1px solid #e5e7eb; }
.about-section .section-inner { max-width: 800px; margin: 0 auto; text-align: center; }
.about-section h2 { font-size: 28px; font-weight: 700; color: #111827; margin-bottom: 16px; }
.about-section p { font-size: 15px; color: #6b7280; line-height: 1.8; }

/* Latest Products */
.latest-section { background: #f9fafb; }

/* Partner */
.partner-section { background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); color: #fff; text-align: center; }
.partner-section .section-inner { text-align: center; }
.partner-section h2 { font-size: 28px; font-weight: 700; margin-bottom: 12px; }
.partner-section p { font-size: 16px; color: #bfdbfe; margin-bottom: 24px; max-width: 600px; margin-left: auto; margin-right: auto; }

/* Ads Carousel */
.ads-carousel { background: #f9fafb; }
.ad-link { display: block; position: relative; }
.ad-caption { position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(transparent, rgba(0,0,0,.7)); padding: 40px 24px 16px; }
.ad-caption h3 { color: #fff; font-size: 20px; font-weight: 600; margin: 0; }

/* News */
.news-section { background: #fff; border-bottom: 1px solid #e5e7eb; }
.news-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.news-card {
  background: #fff; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.08); cursor: pointer;
  transition: box-shadow .2s, transform .2s; border: 1px solid #e5e7eb;
}
.news-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,.12); transform: translateY(-2px); }
.news-image-placeholder { height: 180px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; font-size: 36px; font-weight: 700; color: #9ca3af; }
.news-body { padding: 14px; }
.news-title { font-size: 15px; font-weight: 600; color: #111827; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.news-summary { font-size: 13px; color: #6b7280; line-height: 1.5; margin-bottom: 12px; }
.news-footer { display: flex; justify-content: space-between; align-items: center; }
.news-date { font-size: 12px; color: #9ca3af; }
.news-read-more { font-size: 13px; color: #3b82f6; font-weight: 500; }

@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } .features .section-inner { grid-template-columns: repeat(2, 1fr); } .news-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .product-grid { grid-template-columns: 1fr; } .news-grid { grid-template-columns: 1fr; } .features .section-inner { grid-template-columns: 1fr; } }
</style>
