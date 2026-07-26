<template>
  <el-container class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="app-header">
      <div class="header-left">
        <div class="logo-area" @click="$router.push('/dashboard')">
          <div class="logo-mark">
            <svg viewBox="0 0 40 40" width="28" height="28">
              <circle cx="20" cy="20" r="18" fill="none" stroke="#d4a843" stroke-width="2.5"/>
              <circle cx="20" cy="20" r="14" fill="#d4a843"/>
              <text x="20" y="20" text-anchor="middle" dominant-baseline="central"
                    font-size="16" font-weight="bold" fill="#0f172a" font-family="Georgia">$</text>
            </svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">CoinMarket</span>
            <span class="logo-sub">管理后台</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-breadcrumb separator="›" class="header-breadcrumb">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item v-if="route.path !== '/dashboard'">{{ currentPageName }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-actions">
          <el-dropdown @command="switchLanguage" trigger="click">
            <button class="h-btn">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
              <span>{{ currentLangLabel }}</span>
            </button>
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
          <button class="h-btn h-btn-logout" @click="logout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            <span>退出</span>
          </button>
        </div>
      </div>
    </el-header>

    <el-container class="app-body">
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="app-aside">
        <div class="sidebar-inner">
          <el-menu
            router
            :default-active="route.path"
            :collapse="isCollapsed"
            class="sidebar-menu"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>控制台</span>
            </el-menu-item>

            <el-sub-menu index="products-group">
              <template #title>
                <el-icon><Goods /></el-icon>
                <span>商品管理</span>
              </template>
              <el-menu-item index="/inventory">📥 入库</el-menu-item>
              <el-menu-item index="/products">商品列表</el-menu-item>
              <el-menu-item index="/print-labels">🏷️ 打印条码</el-menu-item>
              <el-menu-item index="/import-images">📷 导入图片</el-menu-item>
              <el-menu-item index="/product-history">📋 修改记录</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="orders-group">
              <template #title>
                <el-icon><List /></el-icon>
                <span>订单管理</span>
              </template>
              <el-menu-item index="/orders">订单列表</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="users-group">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/users">买家管理</el-menu-item>
              <el-menu-item index="/sellers">卖家管理</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="content-group">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>内容管理</span>
              </template>
              <el-menu-item index="/ads">广告</el-menu-item>
              <el-menu-item index="/news">公告</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="finance-group">
              <template #title>
                <el-icon><Money /></el-icon>
                <span>财务管理</span>
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
                <span>工具</span>
              </template>
              <el-menu-item index="/barcode-codes">条码管理</el-menu-item>
              <el-menu-item index="/audit-logs">审计日志</el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/tickets">
              <el-icon><Message /></el-icon>
              <span>工单管理</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="sidebar-footer">
          <button class="collapse-btn" @click="toggleSidebar">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline v-if="!isCollapsed" points="15 18 9 12 15 6"/>
              <polyline v-else points="9 18 15 12 9 6"/>
            </svg>
          </button>
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
  Odometer, Goods, List, UserFilled, Document, Money, Tools, Message
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { locale } = useI18n()
const isCollapsed = ref(false)
const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '220px')

const currentLangLabel = computed(() => {
  const labels = { en: 'EN', 'zh-CN': '简体', 'zh-TW': '繁體', ja: '日語', ko: '한국어' }
  return labels[locale.value] || locale.value
})

const currentPageName = computed(() => {
  const map = {
    '/inventory': '入库', '/products': '商品列表', '/print-labels': '打印条码',
    '/import-images': '导入图片', '/product-history': '修改记录',
    '/orders': '订单列表', '/users': '买家管理', '/sellers': '卖家管理',
    '/ads': '广告', '/news': '公告', '/tickets': '工单管理',
    '/finance': '财务总览', '/finance/income': '收入', '/finance/purchases': '采购',
    '/finance/profit': '利润', '/finance/banks': '银行', '/finance/expenses': '支出',
    '/barcode-codes': '条码管理', '/audit-logs': '审计日志',
  }
  return map[route.path] || ''
})

function switchLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)
}

function toggleSidebar() { isCollapsed.value = !isCollapsed.value }

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style>
/* ====== Design System: Vault & Treasury ====== */
@import url('https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Sans:opsz,wght@9..40,400;9..40,500;9..40,600;9..40,700&display=swap');

:root {
  --gold: #d4a843;
  --gold-light: #f0dfa8;
  --gold-dark: #b8932a;
  --ink: #0f172a;
  --ink-light: #1e293b;
  --ivory: #f5f2ed;
  --ivory-dark: #e8e2d6;
  --surface: #ffffff;
  --text: #1e293b;
  --text-muted: #94a3b8;
  --text-dim: #cbd5e1;
  --border: #e5e7eb;
  --success: #059669;
  --warning: #d97706;
  --danger: #dc2626;
  --info: #3b82f6;
  --sidebar-bg: #0f172a;
  --sidebar-text: #94a3b8;
  --sidebar-active: #d4a843;
  --header-bg: rgba(255,255,255,0.85);
  --shadow-sm: 0 1px 2px rgba(0,0,0,.04);
  --shadow-md: 0 4px 12px rgba(0,0,0,.06);
  --radius: 10px;
  --radius-sm: 6px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app { height: 100%; }
body {
  font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  background: var(--ivory);
  color: var(--text);
  -webkit-font-smoothing: antialiased;
}

/* Layout */
.app-container { height: 100vh; display: flex; flex-direction: column; }

/* Header */
.app-header {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--header-bg);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border);
  padding: 0 20px; height: 56px !important; z-index: 100;
}
.logo-area { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.logo-mark { display: flex; align-items: center; }
.logo-text { display: flex; flex-direction: column; line-height: 1.2; }
.logo-title { font-family: 'DM Serif Display', Georgia, serif; font-size: 17px; font-weight: 700; color: var(--ink); }
.logo-sub { font-size: 10px; color: var(--text-muted); letter-spacing: 0.03em; text-transform: uppercase; }

.header-right { display: flex; align-items: center; gap: 12px; flex: 1; justify-content: flex-end; }
.header-breadcrumb { margin-right: auto; margin-left: 24px; }
.header-breadcrumb .el-breadcrumb__inner { font-size: 13px; color: var(--text-muted) !important; }
.header-breadcrumb .el-breadcrumb__inner.is-link { color: var(--ink) !important; font-weight: 500; }
.header-actions { display: flex; align-items: center; gap: 8px; }

.h-btn {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 12px; border-radius: var(--radius-sm);
  border: 1px solid var(--border); background: var(--surface);
  font-size: 13px; color: var(--text-muted); cursor: pointer;
  transition: all 0.15s; font-family: inherit;
}
.h-btn:hover { border-color: var(--gold); color: var(--gold-dark); }
.h-btn-logout:hover { border-color: var(--danger); color: var(--danger); }

/* Body */
.app-body { flex: 1; display: flex; overflow: hidden; }

/* Sidebar */
.app-aside {
  background: var(--sidebar-bg);
  display: flex; flex-direction: column;
  transition: width 0.25s ease;
  border-right: 1px solid rgba(255,255,255,.06);
}
.sidebar-inner { flex: 1; overflow-y: auto; padding: 8px 0; }
.sidebar-menu { border-right: none !important; background: transparent !important; }
.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  height: 42px; line-height: 42px; font-size: 13px;
  color: var(--sidebar-text) !important;
  background: transparent !important;
  margin: 1px 8px; border-radius: var(--radius-sm);
  transition: all 0.15s;
}
.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover {
  background: rgba(255,255,255,.06) !important;
  color: #fff !important;
}
.sidebar-menu .el-menu-item.is-active {
  background: rgba(212, 168, 67, 0.12) !important;
  color: var(--sidebar-active) !important;
}
.sidebar-menu .el-sub-menu .el-menu { background: transparent !important; }
.sidebar-menu .el-sub-menu .el-menu .el-menu-item {
  background: transparent !important;
  padding-left: 52px !important;
  font-size: 12px;
}
.sidebar-menu .el-sub-menu .el-menu .el-menu-item.is-active {
  background: rgba(212, 168, 67, 0.1) !important;
  color: var(--sidebar-active) !important;
}
.sidebar-menu .el-menu-item .el-icon,
.sidebar-menu .el-sub-menu__title .el-icon { font-size: 16px; }

/* Sidebar collapse */
.sidebar-footer { border-top: 1px solid rgba(255,255,255,.06); padding: 8px; }
.collapse-btn {
  width: 100%; height: 32px; border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  border: none; background: transparent; color: var(--sidebar-text);
  cursor: pointer; transition: all 0.15s;
}
.collapse-btn:hover { background: rgba(255,255,255,.06); color: #fff; }

/* Main content */
.app-main {
  background: var(--ivory);
  padding: 24px;
  overflow-y: auto; flex: 1;
}

/* Element Plus overrides */
.el-card { border-radius: var(--radius) !important; border: 1px solid var(--border) !important; box-shadow: var(--shadow-sm) !important; }
.el-table { border-radius: var(--radius-sm); }
.el-table th { background: var(--ivory) !important; color: var(--text-muted) !important; font-weight: 500 !important; font-size: 12px !important; }
.el-table--border { border: 1px solid var(--border) !important; }
.el-pagination { font-size: 13px; }
.el-tag { border-radius: 20px; font-weight: 500; }
.el-empty { padding: 40px 0; }
.el-dialog { border-radius: 12px !important; }
.el-dialog__header { padding: 20px 24px 0 !important; }
.el-dialog__body { padding: 16px 24px 20px !important; }
.el-button--primary { background: var(--gold) !important; border-color: var(--gold) !important; color: #fff !important; }
.el-button--primary:hover { opacity: 0.9; }

/* Print */
@media print {
  .el-aside, .app-aside, .el-header, .app-header { display: none !important; }
  .app-main { padding: 0 !important; margin: 0 !important; }
  .el-card { box-shadow: none !important; border: 1px solid var(--border) !important; break-inside: avoid; }
  button { display: none !important; }
}
</style>
