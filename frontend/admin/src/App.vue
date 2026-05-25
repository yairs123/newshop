<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <h2>{{ $t('admin.title') }}</h2>
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
              <el-dropdown-item command="ko">한국어</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="logout" size="small">{{ $t('common.logout') }}</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>{{ $t('admin.menu.dashboard') }}</span>
          </el-menu-item>

          <el-sub-menu index="products-group">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>{{ $t('admin.menuGroup.products') }}</span>
            </template>
            <el-menu-item index="/inventory">{{ $t('admin.menuSub.inventory') }}</el-menu-item>
            <el-menu-item index="/products">{{ $t('admin.menuSub.productList') }}</el-menu-item>
            <el-menu-item index="/print-labels">{{ $t('admin.menuSub.printLabels') }}</el-menu-item>
            <el-menu-item index="/import-images">{{ $t('admin.menuSub.importImages') }}</el-menu-item>
            <el-menu-item index="/product-history">{{ $t('admin.menuSub.history') }}</el-menu-item>
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
            <el-menu-item index="/finance">{{ $t('admin.menuSub.financeDashboard') }}</el-menu-item>
            <el-menu-item index="/finance/sales-revenue">{{ $t('admin.menuSub.salesRevenue') }}</el-menu-item>
            <el-menu-item index="/finance/purchase-report">{{ $t('admin.menuSub.purchaseReport') }}</el-menu-item>
            <el-menu-item index="/finance/profit-report">{{ $t('admin.menuSub.profitReport') }}</el-menu-item>
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
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { DataBoard, Goods, List, UserFilled, Avatar, Message, Printer, Upload, Document, Money, Tools } from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { locale } = useI18n()

const currentLang = computed(() => locale.value)

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
.app-aside { background: #f5f7fa; border-right: 1px solid #dcdfe6; }
.app-main { padding: 20px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.lang-switcher { cursor: pointer; }
</style>
