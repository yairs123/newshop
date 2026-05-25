<template>
  <div class="legal-page">
    <div class="page-inner">
      <h1>{{ title }}</h1>
      <p class="last-updated">{{ $t('legal.lastUpdated') }}</p>
      <div class="legal-content" v-html="content"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const { t } = useI18n()

const pageType = computed(() => route.meta.page)
const title = computed(() => t(`legal.${pageType.value}Title`))
const content = computed(() => t(`legal.${pageType.value}Content`))
</script>

<style scoped>
.legal-page { background: #fff; min-height: 60vh; }
.page-inner { max-width: 800px; margin: 0 auto; padding: 48px 24px; }
h1 { font-size: 28px; font-weight: 700; color: #111827; margin-bottom: 8px; }
.last-updated { font-size: 13px; color: #9ca3af; margin-bottom: 32px; }
.legal-content h2 { font-size: 18px; font-weight: 600; color: #1f2937; margin: 24px 0 8px; }
.legal-content p { font-size: 14px; color: #6b7280; line-height: 1.8; }
</style>
