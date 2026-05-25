<template>
  <div id="app">
    <!-- Top bar -->
    <div class="top-bar">
      <div class="top-bar-inner">
        <span>{{ $t('header.freeShipping') }}</span>
        <div class="top-bar-right">
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleAccountCommand">
              <span class="user-dropdown-trigger">
                <el-icon style="margin-right:4px"><UserFilled /></el-icon>
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/orders">{{ $t('account.orders') }}</el-dropdown-item>
                  <el-dropdown-item command="/addresses">{{ $t('account.addresses') }}</el-dropdown-item>
                  <el-dropdown-item command="/payment-methods">{{ $t('account.paymentMethods') }}</el-dropdown-item>
                  <el-dropdown-item divided command="/messages">{{ $t('account.messages') }}</el-dropdown-item>
                  <el-dropdown-item command="/seller/apply">{{ $t('account.openStore') }}</el-dropdown-item>
                  <el-dropdown-item divided command="/contact">{{ $t('account.contactUs') }}</el-dropdown-item>
                  <el-dropdown-item divided command="logout">{{ $t('nav.logout') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/auth" class="top-btn">{{ $t('nav.login') }}</router-link>
            <router-link to="/auth?tab=register" class="top-btn-reg">{{ $t('nav.register') }}</router-link>
          </template>
          <div class="lang-switcher">
            <el-dropdown @command="switchLanguage">
              <span class="lang-btn">
                <img :src="flagPath(currentLang)" class="flag-icon" />
                {{ langLabel(currentLang) }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="en"><img src="https://flagcdn.com/16x12/us.png" class="flag-icon" /> English</el-dropdown-item>
                  <el-dropdown-item command="zh-CN"><img src="https://flagcdn.com/16x12/cn.png" class="flag-icon" /> 简体中文</el-dropdown-item>
                  <el-dropdown-item command="zh-TW"><img src="https://flagcdn.com/16x12/tw.png" class="flag-icon" /> 繁體中文</el-dropdown-item>
                  <el-dropdown-item command="ja"><img src="https://flagcdn.com/16x12/jp.png" class="flag-icon" /> 日本語</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- Header with logo, search, cart -->
    <header class="main-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <span class="logo-icon">◈</span>
          <span class="logo-text">CoinMarket</span>
        </router-link>

        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            :placeholder="$t('shop.searchPlaceholder')"
            size="large"
            class="search-input"
            @keyup.enter="doSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="header-actions">
          <a href="#" class="cart-link" @click.prevent="cartDrawerVisible = true">
            <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
          </a>
        </div>
      </div>
    </header>

    <!-- Navigation -->
    <nav class="main-nav">
      <div class="nav-inner">
        <router-link to="/" class="nav-item" exact>
          <el-icon style="margin-right:4px"><HomeFilled /></el-icon>
          {{ $t('nav.home') }}
        </router-link>
        <router-link to="/products" class="nav-item">
          <el-icon style="margin-right:4px"><ShoppingBag /></el-icon>
          {{ $t('nav.shop') }}
        </router-link>
        <router-link v-if="isLoggedIn" to="/account" class="nav-item nav-account">
          <el-icon style="margin-right:4px"><UserFilled /></el-icon>
          {{ $t('account.myAccount') }}
        </router-link>
        <router-link to="/contact" class="nav-item">
          <el-icon style="margin-right:4px"><Message /></el-icon>
          {{ $t('nav.contact') }}
        </router-link>
      </div>
    </nav>

    <!-- Main content -->
    <main>
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="main-footer">
      <div class="footer-inner">
        <div class="footer-col">
          <h4>CoinMarket</h4>
          <p>{{ $t('footer.about') }}</p>
          <div class="footer-social"><span>Twitter</span><span>Facebook</span><span>Instagram</span></div>
        </div>
        <div class="footer-col">
          <h4>{{ $t('footer.support') }}</h4>
          <p><router-link to="/contact">{{ $t('footer.contact') }}</router-link></p>
          <p>support@coinmarket.com</p>
        </div>
        <div class="footer-col">
          <h4>{{ $t('footer.followUs') }}</h4>
          <p>{{ $t('footer.social') }}</p>
        </div>
        <div class="footer-col">
          <h4>{{ $t('footer.legalTitle') }}</h4>
          <p><router-link to="/terms">{{ $t('footer.terms') }}</router-link></p>
          <p><router-link to="/privacy">{{ $t('footer.privacy') }}</router-link></p>
          <p><router-link to="/cookie">{{ $t('footer.cookie') }}</router-link></p>
          <p><router-link to="/refund">{{ $t('footer.refund') }}</router-link></p>
          <p><router-link to="/shipping">{{ $t('footer.shipping') }}</router-link></p>
        </div>
      </div>
      <div class="footer-bottom">
        <div class="footer-bottom-inner">
          <span>{{ $t('footer.copyright') }}</span>
          <div class="payment-icons"><span>Visa</span><span>MC</span><span>PayPal</span></div>
        </div>
      </div>
    </footer>
    <CartDrawer v-model:visible="cartDrawerVisible" />
  </div>
</template>

<script setup>
import { ref, computed, provide, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCartStore } from './store/cart'
import { useFavoritesStore } from './store/favorites'
import { api } from './api'
import CartDrawer from './components/CartDrawer.vue'
import { Search, ShoppingCart, HomeFilled, ShoppingBag, Message, UserFilled, ArrowDown } from '@element-plus/icons-vue'

const cartStore = useCartStore()
let cartSyncInterval = null
const favoritesStore = useFavoritesStore()
const router = useRouter()
const { locale } = useI18n()
const searchQuery = ref('')
const currentLang = computed(() => locale.value)
const username = ref('')
const cartDrawerVisible = ref(false)
const isLoggedIn = ref(!!localStorage.getItem('token'))
provide('openCartDrawer', () => { cartDrawerVisible.value = true })

function checkLogin() {
  isLoggedIn.value = !!localStorage.getItem('token')
}

onMounted(async () => {
  window.addEventListener('storage', checkLogin)
  if (isLoggedIn.value) {
    try {
      const [meRes] = await Promise.all([
        api.get('/users/me'),
        cartStore.mergeLocalToServer(),
      ])
      username.value = meRes.data?.username || ''
    } catch (e) {
      localStorage.removeItem('token')
      isLoggedIn.value = false
    }
  }
  // Periodic cart sync
  cartSyncInterval = setInterval(() => {
    if (isLoggedIn.value) cartStore.syncFromServer()
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('storage', checkLogin)
  if (cartSyncInterval) clearInterval(cartSyncInterval)
})

function handleAccountCommand(command) {
  if (command === 'logout') {
    logout()
  } else {
    router.push(command)
  }
}

function flagPath(lang) {
  const map = { en: 'us', 'zh-CN': 'cn', 'zh-TW': 'tw', ja: 'jp' }
  return `https://flagcdn.com/16x12/${map[lang] || 'us'}.png`
}
function langLabel(lang) {
  const map = { en: 'EN', 'zh-CN': '简体', 'zh-TW': '繁體', ja: '日語' }
  return map[lang] || 'EN'
}
function switchLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)
}
function doSearch() {
  if (searchQuery.value.trim()) {
    router.push(`/products?keyword=${encodeURIComponent(searchQuery.value)}`)
  }
}
function logout() {
  localStorage.removeItem('token')
  isLoggedIn.value = false
  username.value = ''
  router.push('/')
}

</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif; background: #f5f5f5; color: #333; }

/* Top bar */
.top-bar { background: #111827; color: #9ca3af; font-size: 13px; }
.top-bar-inner { max-width: 1280px; margin: 0 auto; padding: 8px 24px; display: flex; justify-content: space-between; align-items: center; }
.top-bar-right { display: flex; align-items: center; gap: 12px; }
.top-bar-right a { color: #9ca3af; text-decoration: none; }
.top-bar-right a:hover { color: #fff; }
.top-btn-reg { background: #3b82f6; color: #fff !important; padding: 2px 10px; border-radius: 4px; font-size: 12px; }
.user-dropdown-trigger { cursor: pointer; color: #9ca3af; display: flex; align-items: center; gap: 2px; }
.user-dropdown-trigger:hover { color: #fff; }

.lang-switcher { margin-left: 12px; padding-left: 12px; border-left: 1px solid #374151; }
.lang-btn { cursor: pointer; color: #9ca3af; font-size: 13px; display: flex; align-items: center; gap: 4px; }
.lang-btn:hover { color: #fff; }
.flag-icon { width: 16px; height: 12px; border-radius: 1px; vertical-align: middle; }

/* Header */
.main-header { background: #fff; border-bottom: 1px solid #e5e7eb; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1280px; margin: 0 auto; padding: 12px 24px; display: flex; align-items: center; gap: 24px; }
.logo { display: flex; align-items: center; gap: 8px; text-decoration: none; flex-shrink: 0; }
.logo-icon { font-size: 28px; color: #3B82F6; }
.logo-text { font-size: 20px; font-weight: 700; color: #111827; }
.search-bar { flex: 1; max-width: 520px; margin: 0 auto; }
.search-input :deep(.el-input__wrapper) { border-radius: 8px; }
.header-actions { display: flex; align-items: center; gap: 16px; flex-shrink: 0; }
.cart-link { color: #374151; text-decoration: none; }

/* Navigation */
.main-nav { background: #fff; border-bottom: 1px solid #e5e7eb; }
.nav-inner { max-width: 1280px; margin: 0 auto; padding: 0 24px; display: flex; }
.nav-item {
  display: flex; align-items: center;
  padding: 12px 20px; font-size: 14px; font-weight: 500;
  color: #4b5563; text-decoration: none;
  border-bottom: 2px solid transparent;
  transition: color .15s, border-color .15s;
}
.nav-item:hover { color: #3B82F6; border-bottom-color: #3B82F6; }
.nav-item.router-link-exact-active { color: #1d4ed8; border-bottom-color: #3B82F6; }

/* Footer */
.main-footer { background: #111827; color: #9ca3af; margin-top: 60px; }
.footer-inner { max-width: 1280px; margin: 0 auto; padding: 40px 24px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 32px; }
.footer-col h4 { color: #fff; margin-bottom: 12px; font-size: 14px; text-transform: uppercase; letter-spacing: .05em; }
.footer-col p { font-size: 13px; margin-bottom: 6px; line-height: 1.6; }
.footer-col a { color: #9ca3af; text-decoration: none; }
.footer-col a:hover { color: #fff; }
.footer-social { display: flex; gap: 10px; margin-top: 12px; font-size: 13px; }
.footer-bottom { border-top: 1px solid #1f2937; }
.footer-bottom-inner { max-width: 1280px; margin: 0 auto; padding: 16px 24px; display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #6b7280; }
.payment-icons { display: flex; gap: 12px; }
.payment-icons span { background: #1f2937; padding: 2px 8px; border-radius: 3px; font-size: 11px; }

/* Page transition */
.page-fade-enter-active, .page-fade-leave-active { transition: opacity .2s ease; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .footer-inner { grid-template-columns: repeat(2, 1fr); }
  .nav-inner { overflow-x: auto; }
  .nav-item { padding: 12px 14px; font-size: 13px; }
  .nav-account { display: none; }
}
</style>
