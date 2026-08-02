<template>
  <div id="app">
    <!-- Announcement bar -->
    <div class="announcement-bar">
      <div class="announcement-inner">
        <el-icon class="announcement-icon"><Van /></el-icon>
        <span>{{ $t('header.freeShipping') }}</span>
      </div>
    </div>

    <!-- Main header -->
    <header class="main-header">
      <div class="header-inner">

        <!-- Logo -->
        <router-link to="/" class="logo">
          <span class="logo-icon">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
              <circle cx="16" cy="16" r="15" fill="url(#logo-grad)" />
              <text x="16" y="21" text-anchor="middle" fill="#fff" font-size="16" font-weight="800" font-family="serif">C</text>
              <defs>
                <linearGradient id="logo-grad" x1="0" y1="0" x2="32" y2="32">
                  <stop offset="0%" stop-color="#f59e0b" />
                  <stop offset="100%" stop-color="#b45309" />
                </linearGradient>
              </defs>
            </svg>
          </span>
          <span class="logo-text">CoinMarket</span>
        </router-link>

        <!-- Navigation links -->
        <nav class="nav-links">
          <router-link to="/" class="nav-link" exact>
            {{ $t('nav.home') }}
          </router-link>
          <router-link to="/products" class="nav-link">
            {{ $t('nav.shop') }}
          </router-link>
          <router-link to="/contact" class="nav-link">
            {{ $t('nav.contact') }}
          </router-link>
          <template v-if="isLoggedIn">
            <router-link to="/orders" class="nav-link">
              {{ $t('nav.myOrders') }}
            </router-link>
            <router-link to="/account" class="nav-link">
              {{ $t('account.myAccount') }}
            </router-link>
          </template>
        </nav>

        <!-- Search -->
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            :placeholder="$t('shop.searchPlaceholder')"
            size="default"
            class="search-input"
            @keyup.enter="doSearch"
            @clear="clearHeaderSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- Header actions -->
        <div class="header-actions">

          <!-- Language switcher -->
          <el-dropdown @command="switchLanguage" trigger="click" class="lang-dropdown">
            <span class="lang-btn">
              <img :src="flagPath(currentLang)" class="flag-icon" />
              <span class="lang-label">{{ langLabel(currentLang) }}</span>
              <el-icon class="lang-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="en">
                  <img src="https://flagcdn.com/16x12/us.png" class="flag-icon" /> English
                </el-dropdown-item>
                <el-dropdown-item command="zh-CN">
                  <img src="https://flagcdn.com/16x12/cn.png" class="flag-icon" /> 简体中文
                </el-dropdown-item>
                <el-dropdown-item command="zh-TW">
                  <img src="https://flagcdn.com/16x12/tw.png" class="flag-icon" /> 繁體中文
                </el-dropdown-item>
                <el-dropdown-item command="ja">
                  <img src="https://flagcdn.com/16x12/jp.png" class="flag-icon" /> 日本語
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- Cart -->
          <button class="cart-btn" @click="cartDrawerVisible = true">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0" class="cart-badge">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
          </button>

          <!-- Auth -->
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleAccountCommand" trigger="click">
              <div class="user-btn">
                <el-icon><UserFilled /></el-icon>
                <span class="user-name">{{ username || $t('account.myAccount') }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/orders">
                    <el-icon><List /></el-icon>
                    {{ $t('account.orders') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="/account">
                    <el-icon><UserFilled /></el-icon>
                    {{ $t('account.myAccount') }}
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    {{ $t('nav.logout') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/auth" class="btn-login">{{ $t('nav.login') }}</router-link>
            <router-link to="/auth?tab=register" class="btn-register">{{ $t('nav.register') }}</router-link>
          </template>

        </div>
      </div>
    </header>

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
      <div class="footer-wave">
        <svg viewBox="0 0 1440 48" preserveAspectRatio="none">
          <path d="M0,24 C240,48 480,0 720,24 C960,48 1200,0 1440,24 L1440,48 L0,48 Z" fill="#111827" />
        </svg>
      </div>
      <div class="footer-content">
        <div class="footer-inner">

          <!-- Brand column -->
          <div class="footer-col brand-col">
            <div class="footer-logo">
              <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="15" fill="url(#footer-logo-grad)" />
                <text x="16" y="21" text-anchor="middle" fill="#fff" font-size="16" font-weight="800" font-family="serif">C</text>
                <defs>
                  <linearGradient id="footer-logo-grad" x1="0" y1="0" x2="32" y2="32">
                    <stop offset="0%" stop-color="#f59e0b" />
                    <stop offset="100%" stop-color="#b45309" />
                  </linearGradient>
                </defs>
              </svg>
              <span>CoinMarket</span>
            </div>
            <p class="footer-about">{{ $t('footer.about') }}</p>
            <div class="footer-social">
              <a href="#" class="social-link" aria-label="Twitter">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>
              </a>
              <a href="#" class="social-link" aria-label="Facebook">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>
              </a>
              <a href="#" class="social-link" aria-label="Instagram">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324zM12 16a4 4 0 110-8 4 4 0 010 8zm6.406-11.845a1.44 1.44 0 100 2.881 1.44 1.44 0 000-2.881z"/></svg>
              </a>
            </div>
          </div>

          <!-- Quick links -->
          <div class="footer-col">
            <h4>{{ $t('footer.support') }}</h4>
            <ul class="footer-links">
              <li><router-link to="/contact">{{ $t('footer.contact') }}</router-link></li>
              <li><router-link to="/shipping">{{ $t('footer.shipping') }}</router-link></li>
              <li><router-link to="/refund">{{ $t('footer.refund') }}</router-link></li>
              <li><a href="mailto:support@coinmarket.com">{{ $t('footer.email') }}</a></li>
            </ul>
          </div>

          <!-- Legal -->
          <div class="footer-col">
            <h4>{{ $t('footer.legalTitle') }}</h4>
            <ul class="footer-links">
              <li><router-link to="/terms">{{ $t('footer.terms') }}</router-link></li>
              <li><router-link to="/privacy">{{ $t('footer.privacy') }}</router-link></li>
              <li><router-link to="/cookie">{{ $t('footer.cookie') }}</router-link></li>
            </ul>
          </div>

          <!-- Payments & contact -->
          <div class="footer-col">
            <h4>{{ $t('footer.followUs') }}</h4>
            <p class="footer-social-text">{{ $t('footer.social') }}</p>
            <div class="payment-methods">
              <span class="payment-icon visa">
                <svg width="38" height="24" viewBox="0 0 38 24" fill="none"><rect width="38" height="24" rx="4" fill="#1a1f71"/><text x="19" y="16" text-anchor="middle" fill="#fff" font-size="10" font-weight="700" font-family="sans-serif">VISA</text></svg>
              </span>
              <span class="payment-icon mc">
                <svg width="38" height="24" viewBox="0 0 38 24" fill="none"><rect width="38" height="24" rx="4" fill="#222"/><circle cx="14" cy="12" r="7" fill="#eb001b"/><circle cx="24" cy="12" r="7" fill="#f79e1b" opacity="0.8"/></svg>
              </span>
              <span class="payment-icon pp">
                <svg width="38" height="24" viewBox="0 0 38 24" fill="none"><rect width="38" height="24" rx="4" fill="#003087"/><text x="19" y="16" text-anchor="middle" fill="#fff" font-size="8" font-weight="700" font-family="sans-serif">PayPal</text></svg>
              </span>
              <span class="payment-icon amex">
                <svg width="38" height="24" viewBox="0 0 38 24" fill="none"><rect width="38" height="24" rx="4" fill="#2e77bc"/><text x="19" y="16" text-anchor="middle" fill="#fff" font-size="7" font-weight="700" font-family="sans-serif">AMEX</text></svg>
              </span>
            </div>
          </div>

        </div>
      </div>

      <!-- Footer bottom -->
      <div class="footer-bottom">
        <div class="footer-bottom-inner">
          <span>{{ $t('footer.copyright') }}</span>
          <span class="footer-legal-links">
            <router-link to="/terms">{{ $t('footer.terms') }}</router-link>
            <router-link to="/privacy">{{ $t('footer.privacy') }}</router-link>
          </span>
        </div>
      </div>
    </footer>

    <CartDrawer v-model:visible="cartDrawerVisible" />
  </div>
</template>

<script setup>
import { ref, computed, provide, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCartStore } from './store/cart'
import { useFavoritesStore } from './store/favorites'
import { api } from './api'
import CartDrawer from './components/CartDrawer.vue'
import {
  Search, ShoppingCart, UserFilled, ArrowDown, Van,
  List, SwitchButton
} from '@element-plus/icons-vue'

const cartStore = useCartStore()
const favoritesStore = useFavoritesStore()
const router = useRouter()
const route = useRoute()
const { locale } = useI18n()

const searchQuery = ref('')
const currentLang = computed(() => locale.value)
const username = ref('')
const cartDrawerVisible = ref(false)
const isLoggedIn = ref(!!localStorage.getItem('token'))

provide('openCartDrawer', () => { cartDrawerVisible.value = true })

async function checkLogin() {
  const loggedIn = !!localStorage.getItem('token')
  isLoggedIn.value = loggedIn
  if (loggedIn) {
    try {
      const meRes = await api.get('/users/me')
      username.value = meRes.data?.username || ''
    } catch (e) {
      localStorage.removeItem('token')
      isLoggedIn.value = false
      username.value = ''
    }
  } else {
    username.value = ''
  }
}

onMounted(async () => {
  window.addEventListener('storage', checkLogin)
  await checkLogin()
  if (isLoggedIn.value) {
    try {
      await cartStore.mergeLocalToServer()
    } catch (e) { /* cart merge is best-effort */ }
  }
  // Periodically sync cart from server
  const cartSyncInterval = setInterval(() => {
    if (isLoggedIn.value) cartStore.syncFromServer()
  }, 30000)
  onUnmounted(() => {
    clearInterval(cartSyncInterval)
  })
})

onUnmounted(() => {
  window.removeEventListener('storage', checkLogin)
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

// Keep the header search in sync with the URL keyword (e.g. arriving via a link/refresh)
watch(() => route.query.keyword, (kw) => {
  searchQuery.value = kw || ''
})

function doSearch() {
  if (searchQuery.value.trim()) {
    router.push({ path: '/products', query: { keyword: searchQuery.value.trim() } })
  }
}

function clearHeaderSearch() {
  searchQuery.value = ''
  if (route.path === '/products') {
    router.push('/products')
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
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

* { margin: 0; padding: 0; box-sizing: border-box; }

:root {
  --gold: #f59e0b;
  --gold-dark: #b45309;
  --gold-light: #fbbf24;
  --gold-bg: #fffbeb;
  --bg: #f8f9fa;
  --text: #1f2937;
  --text-muted: #6b7280;
  --border: #e5e7eb;
  --header-bg: #ffffff;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  background: var(--bg);
  color: var(--text);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a { text-decoration: none; }
ul { list-style: none; }

/* ====== Announcement bar ====== */
.announcement-bar {
  background: linear-gradient(90deg, #b45309, #f59e0b, #b45309);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.02em;
}
.announcement-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 8px 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.announcement-icon { font-size: 16px; opacity: 0.9; }

/* ====== Main header ====== */
.main-header {
  background: var(--header-bg);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  height: 64px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  text-decoration: none;
}
.logo-icon { display: flex; align-items: center; }
.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.02em;
}

/* Nav links */
.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.nav-link {
  display: flex;
  align-items: center;
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-muted);
  text-decoration: none;
  border-radius: 6px;
  transition: color 0.15s, background 0.15s;
}
.nav-link:hover {
  color: var(--gold-dark);
  background: var(--gold-bg);
}
.nav-link.router-link-exact-active,
.nav-link.router-link-active {
  color: var(--gold-dark);
  font-weight: 600;
}

/* Search */
.search-bar {
  flex: 1;
  max-width: 400px;
  min-width: 160px;
}
.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: #f3f4f6;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: border-color 0.15s, background 0.15s;
}
.search-input :deep(.el-input__wrapper):hover,
.search-input :deep(.el-input__wrapper).is-focus {
  background: #fff;
  border-color: var(--gold);
}
.search-input :deep(.el-input__inner) {
  font-size: 13px;
}
.search-input :deep(.el-input__prefix) {
  color: var(--text-muted);
}

/* Header actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

/* Language switcher */
.lang-dropdown { cursor: pointer; }
.lang-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background 0.15s;
  cursor: pointer;
}
.lang-btn:hover { background: #f3f4f6; }
.flag-icon { width: 18px; height: 14px; border-radius: 2px; object-fit: cover; }
.lang-label { font-size: 13px; font-weight: 500; color: var(--text-muted); }
.lang-arrow { font-size: 12px; color: var(--text-muted); }

/* Cart button */
.cart-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text);
  transition: background 0.15s;
  position: relative;
}
.cart-btn:hover { background: var(--gold-bg); color: var(--gold-dark); }
.cart-badge :deep(.el-badge__content) {
  background: var(--gold);
  border: 2px solid #fff;
  font-size: 11px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  line-height: 14px;
}

/* Auth buttons */
.btn-login {
  display: inline-flex;
  align-items: center;
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  border-radius: 8px;
  transition: background 0.15s;
}
.btn-login:hover { background: #f3f4f6; }

.btn-register {
  display: inline-flex;
  align-items: center;
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, var(--gold), var(--gold-dark));
  border-radius: 8px;
  transition: opacity 0.15s, transform 0.15s;
}
.btn-register:hover {
  opacity: 0.92;
  transform: translateY(-1px);
  color: #fff;
}

/* User dropdown */
.user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  color: var(--text);
}
.user-btn:hover { background: var(--gold-bg); }
.user-name {
  font-size: 13px;
  font-weight: 600;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ====== Page transition ====== */
.page-fade-enter-active,
.page-fade-leave-active { transition: opacity 0.2s ease; }
.page-fade-enter-from,
.page-fade-leave-to { opacity: 0; }

/* ====== Footer ====== */
.main-footer { background: #111827; color: #9ca3af; }
.footer-wave { line-height: 0; }
.footer-wave svg { display: block; width: 100%; height: 48px; }
.footer-content { border-top: 1px solid #1f2937; }
.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 24px 32px;
  display: grid;
  grid-template-columns: 1.6fr 1fr 1fr 1fr;
  gap: 40px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
}

.footer-about {
  font-size: 13px;
  line-height: 1.7;
  color: #6b7280;
  margin-bottom: 20px;
  max-width: 280px;
}

.footer-social {
  display: flex;
  gap: 10px;
}
.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #1f2937;
  color: #9ca3af;
  transition: background 0.2s, color 0.2s;
}
.social-link:hover {
  background: var(--gold);
  color: #fff;
}

.footer-col h4 {
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 16px;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.footer-links li a {
  font-size: 13px;
  color: #6b7280;
  transition: color 0.15s;
}
.footer-links li a:hover { color: var(--gold-light); }

.footer-social-text {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 16px;
  line-height: 1.6;
}

.payment-methods {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.payment-icon {
  display: flex;
  line-height: 0;
  border-radius: 4px;
  overflow: hidden;
}
.payment-icon svg {
  display: block;
  width: 38px;
  height: 24px;
}

/* Footer bottom */
.footer-bottom {
  border-top: 1px solid #1f2937;
  background: #0f172a;
}
.footer-bottom-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #4b5563;
}
.footer-legal-links { display: flex; gap: 16px; }
.footer-legal-links a {
  color: #4b5563;
  transition: color 0.15s;
}
.footer-legal-links a:hover { color: var(--gold-light); }

/* ====== Responsive ====== */
@media (max-width: 1024px) {
  .nav-links { display: none; }
  .footer-inner { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 768px) {
  .search-bar { display: none; }
  .lang-label { display: none; }
  .user-name { display: none; }
  .footer-inner { grid-template-columns: 1fr; gap: 32px; }
  .footer-bottom-inner { flex-direction: column; gap: 8px; text-align: center; }
  .btn-register, .btn-login { padding: 8px 12px; font-size: 12px; }
  .header-inner { gap: 10px; }
}

@media (max-width: 480px) {
  .announcement-bar { font-size: 11px; }
  .announcement-inner { padding: 6px 16px; }
  .header-inner { padding: 0 16px; }
  .logo-text { font-size: 17px; }
}
</style>
