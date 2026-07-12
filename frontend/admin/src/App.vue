<template>
  <el-container class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="app-header">
      <div class="header-left">
        <div class="logo-area">
          <svg viewBox="0 0 40 40" width="32" height="32" class="logo-icon">
            <circle cx="20" cy="20" r="18" fill="#f59e0b" stroke="#b45309" stroke-width="2"/>
            <text x="20" y="20" text-anchor="middle" dominant-baseline="central"
                  font-size="16" font-weight="bold" fill="#fff" font-family="Arial">$</text>
          </svg>
          <span class="app-title">{{ $t('admin.title') }}</span>
        </div>
      </div>
      <div class="header-right">
        <el-breadcrumb separator="/" class="header-breadcrumb">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">{{ $t('admin.menu.dashboard') }}</el-breadcrumb-item>
          <el-breadcrumb-item v-if="route.path !== '/dashboard'">{{ currentPageName }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-actions">
          <el-dropdown @command="switchLanguage">
            <el-button size="small" class="lang-btn">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
              <span style="margin-left:4px">{{ currentLangLabel }}</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="en">English</el-dropdown-item>
                <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
                <el-dropdown-item command="zh-TW">繁體中文</el-dropdown-item>
                <el-dropdown-item command="ja">日本語</el-dropdown-item>
                <el-dropdown-item command="ko">한국어</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="danger" plain size="small" @click="logout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            {{ $t('common.logout') }}
          </el-button>
        </div>
      </div>
    </el-header>

    <el-container class="app-body">
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="app-aside">
        <el-menu
          router
          :default-active="route.path"
          :collapse="isCollapsed"
          class="sidebar-menu"
          background-color="#1e1e2d"
          text-color="#a2a3b7"
          active-text-color="#fff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>{{ $t('admin.menu.dashboard') }}</span>
          </el-menu-item>

          <el-sub-menu index="products-group">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>{{ $t('admin.menuGroup.products') }}</span>
            </template>
            <el-menu-item index="/inventory">📥 入库</el-menu-item>
            <el-menu-item index="/products">{{ $t('admin.menuSub.productList') }}</el-menu-item>
            <el-menu-item index="/print-labels">🏷️ {{ $t('admin.menuSub.printLabels') }}</el-menu-item>
            <el-menu-item index="/import-images">📷 {{ $t('admin.menuSub.importImages') }}</el-menu-item>
            <el-menu-item index="/product-history">📋 {{ $t('admin.menuSub.history') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="orders-group">
            <template #title>
              <el-icon><List /></el-icon>
              <span>{{ $t('admin.menuGroup.orders') }}</span>
            </template>
            <el-menu-item index="/orders">{{ $t('admin.menuSub.orderList') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="users-group">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>{{ $t('admin.menuGroup.users') }}</span>
            </template>
            <el-menu-item index="/users">{{ $t('admin.menuSub.buyerMgmt') }}</el-menu-item>
            <el-menu-item index="/sellers">{{ $t('admin.menuSub.sellerMgmt') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="content-group">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>{{ $t('admin.menuGroup.content') }}</span>
            </template>
            <el-menu-item index="/ads">{{ $t('admin.menuSub.ads') }}</el-menu-item>
            <el-menu-item index="/news">{{ $t('admin.menuSub.news') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="finance-group">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>{{ $t('admin.menuGroup.finance') }}</span>
            </template>
            <el-menu-item index="/finance">📊 总览</el-menu-item>
            <el-menu-item index="/finance/income">📥 收入</el-menu-item>
            <el-menu-item index="/finance/purchases">📦 采购</el-menu-item>
            <el-menu-item index="/finance/profit">📈 利润</el-menu-item>
            <el-menu-item index="/finance/banks">💳 银行</el-menu-item>
            <el-menu-item index="/finance/expenses">💰 支出</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="tools-group">
            <template #title>
              <el-icon><Tools /></el-icon>
              <span>{{ $t('admin.menuGroup.tools') }}</span>
            </template>
            <el-menu-item index="/barcode-codes">{{ $t('admin.menuSub.barcodeCodes') }}</el-menu-item>
            <el-menu-item index="/audit-logs">{{ $t('admin.menuSub.auditLogs') }}</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/tickets">
            <el-icon><Message /></el-icon>
            <span>{{ $t('admin.menu.tickets') }}</span>
          </el-menu-item>
        </el-menu>

        <!-- 折叠按钮 -->
        <div class="collapse-btn" @click="toggleSidebar">
          <el-icon><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  Odometer, Goods, List, UserFilled, Document, Money, Tools, Message,
  Fold, Expand
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const isCollapsed = ref(false)
const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '240px')

const currentLangLabel = computed(() => {
  const labels = { en: 'English', 'zh-CN': '简体', 'zh-TW': '繁體', ja: '日本語', ko: '한국어' }
  return labels[locale.value] || locale.value
})

const currentPageName = computed(() => {
  const nameMap = {
    '/inventory': t('admin.menuSub.inventory'),
    '/products': t('admin.menuSub.productList'),
    '/print-labels': t('admin.menuSub.printLabels'),
    '/import-images': t('admin.menuSub.importImages'),
    '/product-history': t('admin.menuSub.history'),
    '/orders': t('admin.menuSub.orderList'),
    '/users': t('admin.menuSub.buyerMgmt'),
    '/sellers': t('admin.menuSub.sellerMgmt'),
    '/ads': t('admin.menuSub.ads'),
    '/news': t('admin.menuSub.news'),
    '/finance': '财务总览',
    '/finance/banks': '银行管理',
    '/finance/income': t('admin.menuSub.salesRevenue'),
    '/finance/purchases': t('admin.menuSub.purchaseReport'),
    '/finance/personnel': '人员开支',
    '/finance/reimbursements': '报销管理',
    '/finance/expenses': '其他费用',
    '/finance/profit': t('admin.menuSub.profitReport'),
    '/barcode-codes': t('admin.menuSub.barcodeCodes'),
    '/audit-logs': t('admin.menuSub.auditLogs'),
    '/tickets': t('admin.menu.tickets'),
  }
  return nameMap[route.path] || route.path
})

function switchLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)
}

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style>
/* 全局样式 */
:root {
  --sidebar-bg: #1e1e2d;
  --sidebar-text: #a2a3b7;
  --sidebar-active: #fff;
  --header-bg: #fff;
  --main-bg: #f0f2f5;
  --primary-color: #409eff;
}
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app { height: 100%; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; }

/* 整体布局 */
.app-container { height: 100vh; display: flex; flex-direction: column; }

/* 顶部导航栏 */
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--header-bg);
  border-bottom: 1px solid #e8e8ef;
  padding: 0 24px;
  height: 60px !important;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  z-index: 100;
}
.header-left { display: flex; align-items: center; }
.logo-area { display: flex; align-items: center; gap: 10px; }
.logo-icon { flex-shrink: 0; }
.app-title { font-size: 18px; font-weight: 700; color: #1e1e2d; letter-spacing: -0.3px; }
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  justify-content: flex-end;
}
.header-breadcrumb { margin-right: auto; margin-left: 24px; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.lang-btn { display: flex; align-items: center; }

/* 主体区域 */
.app-body { flex: 1; display: flex; overflow: hidden; }

/* 侧边栏 */
.app-aside {
  background: var(--sidebar-bg);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}
.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
}
.sidebar-menu .el-menu-item:hover {
  background: rgba(255,255,255,0.08) !important;
}
.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #409eff, #337ecc) !important;
  color: #fff !important;
}
.sidebar-menu .el-sub-menu .el-menu {
  background-color: #1a1a2e !important;
}
.sidebar-menu .el-sub-menu .el-menu .el-menu-item {
  background-color: #1a1a2e !important;
  padding-left: 56px !important;
}
.sidebar-menu .el-sub-menu .el-menu .el-menu-item:hover {
  background: rgba(255,255,255,0.06) !important;
}
.sidebar-menu .el-sub-menu .el-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #409eff, #337ecc) !important;
  color: #fff !important;
}

/* 折叠按钮 */
.collapse-btn {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a2a3b7;
  cursor: pointer;
  border-top: 1px solid rgba(255,255,255,0.06);
  transition: color 0.2s;
}
.collapse-btn:hover { color: #fff; }

/* 主内容区 */
.app-main {
  background: var(--main-bg);
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

/* 打印样式 */
@media print {
  .el-aside, .el-menu, .sidebar, .app-aside,
  .el-header, .el-menu--horizontal, .el-scrollbar__bar { display: none !important; }
  .app-main { margin-left: 0 !important; padding: 0 !important; }
  body { background: white; }
  .el-card { box-shadow: none !important; border: 1px solid #e5e7eb !important; break-inside: avoid; }
  button { display: none !important; }
  .page-break { page-break-after: always; }
}
</style>
