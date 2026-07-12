<template>
  <div class="profile-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>{{ $t('profile.title', '店铺设置') }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <template #header>
        <div class="card-header">
          <span style="font-weight:600">{{ $t('profile.shopInfo', '店铺信息') }}</span>
          <el-tag v-if="isLocked" type="warning" size="small">资料已锁定</el-tag>
        </div>
      </template>

      <el-form :model="form" label-position="top" v-if="loaded">
        <el-form-item :label="$t('profile.shopName', '店铺名称')">
          <el-input v-model="form.shopName" :disabled="isLocked" placeholder="输入店铺名称" />
        </el-form-item>
        <el-form-item :label="$t('profile.shopDescription', '店铺描述')">
          <el-input v-model="form.shopDescription" :disabled="isLocked" type="textarea" :rows="4" placeholder="介绍您的店铺和商品" />
        </el-form-item>
        <el-form-item v-if="!isLocked">
          <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
        </el-form-item>
        <el-alert v-if="isLocked" type="warning" :title="$t('profile.lockedHint', '资料已锁定，如需修改请联系管理员')" show-icon :closable="false" />
      </el-form>
      <el-skeleton v-else :rows="4" animated />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const loaded = ref(false)
const saving = ref(false)
const isLocked = ref(false)
const form = reactive({ shopName: '', shopDescription: '' })

async function loadProfile() {
  try {
    const res = await api.get('/seller/status')
    const data = res.data || {}
    form.shopName = data.shopName || ''
    form.shopDescription = data.shopDescription || ''
    isLocked.value = data.locked || false
  } catch (e) { /* ignore */ }
  loaded.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    await api.put('/seller/profile', { shopName: form.shopName, shopDescription: form.shopDescription })
    ElMessage.success('保存成功')
  } catch (e) { /* handled */ }
  saving.value = false
}

onMounted(loadProfile)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
