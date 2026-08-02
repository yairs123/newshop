<template>
  <div class="news-detail" v-loading="loading">
    <div class="detail-inner">
      <el-button text @click="$router.back()" class="back-link">← Back</el-button>
      <div v-if="article" class="article">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span v-if="article.publishedAt">{{ article.publishedAt.substring(0, 10) }}</span>
        </div>
        <el-image v-if="article.imageUrl" :src="article.imageUrl" fit="cover" lazy class="article-image" />
        <div class="article-content" v-html="article.content"></div>
      </div>
      <el-empty v-else-if="!loading" :description="$t('common.noData')" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'

const route = useRoute()
const article = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get(`/news/${route.params.id}`)
    article.value = res.data
  } catch (e) {}
  loading.value = false
})
</script>

<style scoped>
.news-detail { background: #f9fafb; min-height: 60vh; }
.detail-inner { max-width: 800px; margin: 0 auto; padding: 32px 24px; }
.back-link { margin-bottom: 16px; font-size: 14px; }
.article { background: #fff; border-radius: 12px; padding: 40px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
.article-title { font-size: 28px; font-weight: 700; color: #111827; margin: 0 0 12px; }
.article-meta { font-size: 14px; color: #9ca3af; margin-bottom: 24px; }
.article-image { width: 100%; max-height: 400px; border-radius: 8px; margin-bottom: 24px; }
.article-content { font-size: 16px; line-height: 1.8; color: #374151; }
.article-content :deep(p) { margin-bottom: 16px; }
.article-content :deep(h2) { font-size: 22px; font-weight: 600; margin: 24px 0 12px; }
.article-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 16px 0; }
</style>
