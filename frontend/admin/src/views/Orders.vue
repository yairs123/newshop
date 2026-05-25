<template>
  <div>
    <h3>{{ $t('orders.title') }}</h3>
    <el-table :data="orders" stripe style="margin-top:20px" v-loading="loading">
      <el-table-column prop="id" :label="$t('orders.id')" width="80" />
      <el-table-column prop="orderNo" :label="$t('orders.orderNo')" width="220" />
      <el-table-column :label="$t('orders.status')" width="140">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('orders.amount')" width="120">
        <template #default="{ row }">{{ row.currency }} {{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="buyerId" :label="$t('orders.buyer')" width="80" />
      <el-table-column prop="sellerId" :label="$t('orders.seller')" width="80" />
      <el-table-column :label="$t('orders.actions')" min-width="220">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="$router.push('/orders/' + row.id + '/invoice')">
            請求書
          </el-button>
          <el-button size="small" type="warning" @click="forceComplete(row)" v-if="row.status !== 'COMPLETED' && row.status !== 'CANCELLED'">
            {{ $t('orders.forceComplete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="prev, pager, next"
      @current-change="load"
      style="margin-top:20px; justify-content:center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const page = ref(0)
const total = ref(0)
const size = 20

const statusTag = (s) => {
  const map = { PENDING_PAYMENT: 'warning', PAID: 'primary', SHIPPED: 'info', COMPLETED: 'success', CANCELLED: 'danger' }
  return map[s] || 'info'
}

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await api.get(`/admin/orders?page=${page.value}&size=${size}`)
    orders.value = res.data.content
    total.value = res.data.totalElements
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

async function forceComplete(row) {
  try {
    await ElMessageBox.confirm(
      `${row.orderNo} を強制完了しますか？(${row.orderNo} will be force-completed)`,
      '確認',
      { confirmButtonText: 'はい', cancelButtonText: 'キャンセル' }
    )
    await api.post(`/admin/orders/${row.id}/complete`)
    ElMessage.success('Done')
    load()
  } catch (e) { /* cancelled or error */ }
}
</script>
