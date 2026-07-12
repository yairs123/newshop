<template>
  <div class="empty-state">
    <el-icon :size="48"><Search /></el-icon>
    <h3 v-if="title">{{ title }}</h3>
    <p v-if="description">{{ description }}</p>
    <p v-else>{{ message }}</p>
  </div>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  title: { type: String, default: '' },
  description: { type: String, default: '' },
  message: { type: String, default: '' }
})

const defaultMessage = 'No results found.'
const message = props.message || defaultMessage
</script>

<style scoped>
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 64px 0; color: #9ca3af; }
.empty-state p { margin-top: 12px; font-size: 14px; }
</style>
