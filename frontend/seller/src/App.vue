<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <button class="mobile-menu-btn" @click="toggleMobileMenu">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <h2>{{ $t('seller.title') }}</h2>
      </div>
      <div class="header-right">
        <el-dropdown @command="switchLanguage">
          <span class="lang-switcher">{{ currentLang }}</span>
          <template #dropdown>
            <el-dropdown-menu>
            <el-dropdown-item command="en">English</el-dropdown-item>
            <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
            <el-dropdown-item command="zh-TW">繁體中文</el-dropdown-item>
            <el-dropdown-item command="ja">日本語</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="logout" size="small">{{ $t('common.logout') }}</el-button>
      </div>
    </el-header>
    <div v-if="mobileMenuOpen" class="mobile-overlay" @click="mobileMenuOpen = false"></div>
    <el-container>
      <el-aside width="220px" class="app-aside" :class="{'sidebar-open': mobileMenuOpen}">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>{{ $t('seller.menu.dashboard') }}</span>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            <span>{{ $t('seller.menu.products') }}</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <span>{{ $t('seller.menu.orders') }}</span>
          </el-menu-item>
          <el-menu-item index="/inventory">
            <el-icon><Download /></el-icon>
            <span>{{ $t('nav.quickInventory') }}</span>
          </el-menu-item>
          <el-menu-item index="/sales-report">
            <el-icon><DataAnalysis /></el-icon>
            <span>{{ $t('nav.salesReport') }}</span>
          </el-menu-item>
          <el-menu-item index="/print-labels">
            <el-icon><Printer /></el-icon>
            <span>{{ $t('nav.printLabels') }}</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>{{ $t('seller.menu.profile') }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Download, Printer, DataAnalysis } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { locale, t } = useI18n()

const mobileMenuOpen = ref(false)
const currentLang = computed(() => locale.value)

function toggleMobileMenu() { mobileMenuOpen.value = !mobileMenuOpen.value }

function switchLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)
}

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style>
.app-container { height: 100vh; }
.app-header { display: flex; justify-content: space-between; align-items: center; background: #fff; border-bottom: 1px solid #dcdfe6; padding: 0 20px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.app-aside { background: #f5f7fa; border-right: 1px solid #dcdfe6; }
.app-main { padding: 20px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.lang-switcher { cursor: pointer; }

/* Mobile responsive */
.mobile-menu-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: #303133;
  cursor: pointer;
  border-radius: 6px;
  flex-shrink: 0;
}
.mobile-menu-btn:hover { background: #f0f0f0; }
.mobile-overlay { display: none; }

@media (max-width: 768px) {
  .mobile-menu-btn { display: flex; }

  .app-aside {
    position: fixed !important;
    top: 60px !important;
    left: 0 !important;
    bottom: 0 !important;
    width: 240px !important;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    box-shadow: 4px 0 20px rgba(0,0,0,0.1);
  }
  .app-aside.sidebar-open { transform: translateX(0); }

  .mobile-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.3);
    z-index: 999;
  }

  .app-main { padding: 12px !important; margin-left: 0 !important; }
  .app-header { padding: 0 12px !important; height: 48px !important; }
  .header-left h2 { font-size: 16px; }
  .lang-switcher { font-size: 13px; }
}
@media (max-width: 480px) {
  .app-main { padding: 8px !important; }
}
</style>
