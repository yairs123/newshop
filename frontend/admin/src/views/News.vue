<template>
  <div class="news-page">
    <div class="page-header">
      <h2>{{ $t('news.title') }}</h2>
      <el-button type="primary" @click="openCreate">{{ $t('news.addNews') }}</el-button>
    </div>

    <el-table :data="news" border stripe v-loading="loading" style="width:100%">
      <el-table-column :label="$t('news.title_')" prop="title" min-width="160" />
      <el-table-column :label="$t('news.summary')" prop="summary" min-width="200">
        <template #default="{ row }">
          <span>{{ row.summary ? row.summary.substring(0, 60) + (row.summary.length > 60 ? '...' : '') : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('news.isPublished')" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isPublished ? 'success' : 'info'" size="small">
            {{ row.isPublished ? $t('common.yes') : $t('common.no') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('news.publishedAt')" prop="publishedAt" width="160" />
      <el-table-column :label="$t('products.actions')" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">{{ $t('common.submit') }} edit</el-button>
          <el-button v-if="row.isPublished" size="small" type="warning" @click="unpublish(row)">{{ $t('news.unpublish') }}</el-button>
          <el-button v-else size="small" type="success" @click="publish(row)">{{ $t('news.publish') }}</el-button>
          <el-popconfirm :title="$t('news.deleteConfirm')" @confirm="remove(row)">
            <template #reference>
              <el-button size="small" type="danger">{{ $t('common.cancel') }} del</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? $t('news.editNews') : $t('news.addNews')" width="700px">
      <el-form :model="form" label-position="top" v-if="dialogVisible">
        <el-form-item :label="$t('news.title_')">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item :label="$t('news.summary')">
          <el-input v-model="form.summary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('news.content')">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
        <el-form-item :label="$t('news.imageUrl')">
          <el-input v-model="form.imageUrl" placeholder="https://..." />
          <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:120px;height:80px;margin-top:8px" fit="cover" />
        </el-form-item>
        <el-form-item :label="$t('news.isPublished')">
          <el-switch v-model="form.isPublished" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ $t('common.submit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const news = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref({ title: '', summary: '', content: '', imageUrl: '', isPublished: false })

async function fetch() {
  loading.value = true
  try {
    const res = await api.get('/admin/news')
    news.value = res.data || []
  } catch (e) {}
  loading.value = false
}

function openCreate() {
  editing.value = false
  form.value = { title: '', summary: '', content: '', imageUrl: '', isPublished: false }
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    if (editing.value) {
      await api.put(`/admin/news/${form.value.id}`, form.value)
    } else {
      await api.post('/admin/news', form.value)
    }
    dialogVisible.value = false
    await fetch()
  } catch (e) {}
}

async function publish(row) {
  try {
    await api.put(`/admin/news/${row.id}/publish`)
    await fetch()
  } catch (e) {}
}

async function unpublish(row) {
  try {
    await api.put(`/admin/news/${row.id}/unpublish`)
    await fetch()
  } catch (e) {}
}

async function remove(row) {
  try {
    await api.delete(`/admin/news/${row.id}`)
    await fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
