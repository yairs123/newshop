<template>
  <div class="orders-page">
    <div class="page-header">
      <h3>{{ $t('orders.title') }}</h3>
      <div class="page-actions">
        <el-select v-model="statusFilter" :placeholder="$t('common.all', '全部状态')" size="small" clearable style="width:130px">
          <el-option label="待支付" value="PENDING_PAYMENT" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已发货" value="SHIPPED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button size="small" @click="load" :icon="Refresh" circle />
      </div>
    </div>

    <el-table :data="orders" stripe style="width:100%" v-loading="loading" size="large" border class="order-table">
      <el-table-column prop="id" :label="$t('orders.id')" width="70" align="center" />
      <el-table-column prop="orderNo" :label="$t('orders.orderNo')" width="200">
        <template #default="{ row }">
          <span class="order-no-text">{{ row.orderNo }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('orders.status')" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" effect="dark" size="small">
            {{ $t('orders.statuses.' + row.status, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('orders.amount')" width="130" align="right">
        <template #default="{ row }">
          <span class="amount-text">{{ row.currency }} {{ formatPrice(row.totalAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('orders.buyer')" prop="buyerName" min-width="120" />
      <el-table-column :label="$t('orders.seller')" prop="sellerId" width="80" align="center" />
      <el-table-column :label="$t('orders.createdAt', '创建时间')" width="170">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="$t('common.actions', '操作')" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="viewDetail(row)">
            {{ $t('orders.detail', '详情') }}
          </el-button>
          <el-button size="small" type="success" plain @click="$router.push('/orders/' + row.id + '/invoice')">
            {{ $t('orders.invoice', '账单') }}
          </el-button>
          <el-button size="small" type="warning" plain @click="forceComplete(row)"
            v-if="row.status !== 'COMPLETED' && row.status !== 'CANCELLED'">
            {{ $t('orders.forceComplete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next, jumper"
        @current-change="load"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const size = 20
const statusFilter = ref('')

function statusTag(s) {
  const map = { PENDING_PAYMENT: 'danger', PAID: 'success', SHIPPED: 'warning', COMPLETED: 'success', CANCELLED: 'info' }
  return map[s] || 'info'
}

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const p = page.value - 1
    const res = await api.get(`/admin/orders?page=${p}&size=${size}`)
    orders.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

function formatPrice(val) {
  return val != null ? Number(val).toFixed(2) : '0.00'
}

function formatDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleString()
}

function viewDetail(row) {
  router.push('/orders/' + row.id + '/invoice')
}

async function forceComplete(row) {
  try {
    await ElMessageBox.confirm(
      t('orders.forceCompleteTip') + ': ' + row.orderNo,
      t('common.confirm', '确认'),
      { confirmButtonText: t('common.yes', '确认'), cancelButtonText: t('common.cancel', '取消'), type: 'warning' }
    )
    await api.post(`/admin/orders/${row.id}/complete`)
    ElMessage.success(t('common.success', '操作成功'))
    load()
  } catch (e) { /* cancelled or error */ }
}
</script>

<style scoped>
.orders-page { }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px; flex-wrap: wrap; gap: 12px;
}
.page-header h3 { font-size: 18px; font-weight: 600; color: #303133; margin: 0; }
.page-actions { display: flex; gap: 8px; align-items: center; }
.order-table { border-radius: 8px; }
.order-no-text { font-family: 'Courier New', monospace; font-size: 13px; color: #409eff; font-weight: 600; }
.amount-text { font-weight: 600; color: #f56c6c; }
.pagination-wrap {
  display: flex; justify-content: center; margin-top: 20px; padding: 16px 0;
}
</style>
