<template>
  <div class="pagination-wrap" v-if="totalPages > 1">
    <span class="page-info">{{ from }}-{{ to }} / {{ total }}</span>
    <div class="pagination-btns">
      <button class="page-btn" :disabled="page <= 1" @click="selectPage(page - 1)">{{ $t('shop.prev') }}</button>
      <button
        v-for="pageNumber in pageNumbers"
        :key="pageNumber"
        :class="['page-btn', { active: pageNumber === page } ]"
        :disabled="pageNumber === '...'"
        @click="pageNumber !== '...' && selectPage(pageNumber)"
      >
        {{ pageNumber === '...' ? '...' : pageNumber }}
      </button>
      <button class="page-btn" :disabled="page >= totalPages" @click="selectPage(page + 1)">{{ $t('shop.next') }}</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, required: true },
  total: { type: Number, required: true },
  size: { type: Number, required: true }
})

const emits = defineEmits(['update:page'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
const from = computed(() => props.total === 0 ? 0 : (props.page - 1) * props.size + 1)
const to = computed(() => Math.min(props.page * props.size, props.total))

const pageNumbers = computed(() => {
  const tp = totalPages.value
  const cp = props.page
  const pages = []
  if (tp <= 10) {
    for (let i = 1; i <= tp; i += 1) pages.push(i)
    return pages
  }
  pages.push(1)
  if (cp > 3) pages.push('...')
  for (let i = Math.max(2, cp - 1); i <= Math.min(tp - 1, cp + 1); i += 1) pages.push(i)
  if (cp < tp - 2) pages.push('...')
  pages.push(tp)
  return pages
})

function selectPage(pageNumber) {
  emits('update:page', pageNumber)
}
</script>

<style scoped>
.pagination-wrap { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-top: 30px; }
.page-info { font-size: 14px; color: #6b7280; }
.pagination-btns { display: flex; gap: 6px; flex-wrap: wrap; }
.page-btn {
  min-width: 36px;
  padding: 8px 12px;
  font-size: 13px;
  color: #4b5563;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all .15s;
}
.page-btn:hover:not(:disabled) { border-color: #93c5fd; color: #1d4ed8; }
.page-btn.active { background: #dbeafe; color: #1d4ed8; border-color: #93c5fd; }
.page-btn:disabled { cursor: not-allowed; opacity: .5; }
</style>
