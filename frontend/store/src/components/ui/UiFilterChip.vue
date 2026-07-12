<template>
  <button
    class="filter-chip"
    :class="{ active: active }"
    @click="$emit('click')"
  >
    <slot />
    <span v-if="count !== null" class="chip-count">{{ count }}</span>
  </button>
</template>

<script setup>
const props = defineProps({
  active: { type: Boolean, default: false },
  count: { type: [Number, String], default: null }
})
</script>

<style scoped>
.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
  background: #f3f4f6;
  border: 1px solid transparent;
  border-radius: 20px;
  cursor: pointer;
  transition: all .15s;
  white-space: nowrap;
}
.filter-chip:hover {
  background: #eff6ff;
  color: #3b82f6;
  border-color: #bfdbfe;
}
.filter-chip.active {
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
  border-color: #93c5fd;
}
.chip-count {
  font-size: 11px;
  color: #9ca3af;
}
.filter-chip.active .chip-count {
  color: #60a5fa;
}
</style>
