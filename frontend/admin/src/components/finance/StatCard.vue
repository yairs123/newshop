<template>
  <el-card shadow="hover">
    <div class="stat-card" @click="$emit('click')">
      <div class="stat-label">{{ label }}</div>
      <div class="stat-value" :style="{ color }">{{ prefix }}{{ value }}</div>
      <div v-if="subtext" class="stat-subtext">{{ subtext }}</div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  label: String,
  value: [String, Number],
  color: { type: String, default: '#6b7280' },
  prefix: { type: String, default: '' },
  subtext: String,
})
defineEmits(['click'])
</script>

<style scoped>
.stat-card { text-align: center; cursor: pointer; }
.stat-label { font-size: 13px; color: #6b7280; margin-bottom: 4px; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-subtext { font-size: 12px; color: #9ca3af; margin-top: 4px; }
</style>
