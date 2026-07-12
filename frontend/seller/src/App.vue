<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
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
    <el-container>
      <el-aside width="220px" class="app-aside">
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
            <span>快捷入库</span>
          </el-menu-item>
          <el-menu-item index="/print-labels">
            <el-icon><Printer /></el-icon>
            <span>打印条码</span>
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
import { computed } from 'vue'
import { Download, Printer } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { locale, t } = useI18n()

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
