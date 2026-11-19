<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          应收账款
          <small>按订单跟踪应收余额与账龄，及时识别逾期风险</small>
        </h2>
        <el-button :icon="Refresh" @click="reload">刷新</el-button>
      </div>

      <!-- 汇总 -->
      <div class="kpi-grid">
        <div v-for="item in kpis" :key="item.label" class="kpi" :class="`tone-${item.tone}`">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-extra">{{ item.extra }}</div>
        </div>
      </div>

      <!-- 账龄分布 -->
      <section class="age-block">
        <header class="block-head">
          <h3>账龄分布</h3>
          <span class="hint">按未收金额划分区间</span>
        </header>
        <RankBar :items="ageRank" :formatter="fmtMoneyShort" color="#f5a623" />
      </section>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="订单号 / 客户 / 负责人"
            clearable
            style="width: 250px"
            @keyup.enter="query.page = 1"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in RECEIVABLE_STATUS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 笔应收</span>
        </div>
      </div>

      <el-table :data="pageList" border stripe style="width: 100%" empty-text="暂无应收记录">
        <el-table-column prop="orderNo" label="订单号" width="120">
          <template #default="{ row }">
            <span class="mono">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="150" />
        <el-table-column prop="totalAmount" label="应收总额" width="130" align="right">
          <template #default="{ row }">
            <span class="amount">{{ fmtMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="receivedAmount" label="已收金额" width="130" align="right">
          <template #default="{ row }">
            <span class="mono">{{ fmtMoney(row.receivedAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未收余额" width="130" align="right">
          <template #default="{ row }">
            <span class="balance">{{ fmtMoney(row.totalAmount - row.receivedAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="应收日期" width="115" />
        <el-table-column label="账龄" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.balance <= 0" class="settled-text">已结清</span>
            <el-tag v-else-if="row.aging > 0" type="danger" effect="light" size="small">
              逾期 {{ row.aging }} 天
            </el-tag>
            <el-tag v-else type="info" effect="light" size="small">未到期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="RECEIVABLE_STATUS_TAG[row.status] || 'info'" effect="light" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="负责人" width="85" />
        <el-table-column label="操作" width="105" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openReceipt(row)">登记收款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 登记收款 -->
    <el-dialog v-model="dialogVisible" title="登记收款" width="480px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <el-input v-model="form.orderNo" disabled />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="form.customerName" disabled />
        </el-form-item>
        <el-form-item label="未收余额">
          <el-input :model-value="fmtMoney(form.balance)" disabled />
        </el-form-item>
        <el-form-item label="本次收款">
          <el-input-number v-model="form.amount" :min="0" :max="form.balance" :controls-position="'right'" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="form.payMethod" style="width: 100%">
            <el-option v-for="item in PAY_METHOD" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReceipt">确认收款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import RankBar from '@/components/charts/RankBar.vue'
import { getExtraDb, persistExtra, nextExtraId } from '@/mock/extraDb'
import { RECEIVABLE_STATUS, RECEIVABLE_STATUS_TAG, PAY_METHOD, formatMoney } from '@/utils/dict'

const store = getExtraDb()
const tick = ref(0)

const query = reactive({ page: 1, pageSize: 10, keyword: '', status: '' })

const fmtMoney = (v) => formatMoney(v)

const fmtMoneyShort = (v) => {
  const num = Number(v) || 0
  if (Math.abs(num) >= 10000) {
    const n = num / 10000
    return `¥${n.toFixed(n >= 1000 ? 0 : 1).replace(/\.0$/, '')}万`
  }
  return `¥${Math.round(num)}`
}

/** 逾期天数：> 0 表示已逾期 */
const agingOf = (row) => {
  const due = new Date(`${row.dueDate}T00:00:00`)
  const diff = Math.floor((Date.now() - due.getTime()) / 86400000)
  return diff
}

const rows = computed(() => {
  tick.value
  return [...(store.receivables || [])].map((item) => {
    const aging = agingOf(item)
    const balance = item.totalAmount - item.receivedAmount
    let status = item.status
    if (balance <= 0) status = '已结清'
    else if (aging > 0) status = '已逾期'
    else status = '未到期'
    return { ...item, aging, balance, status }
  })
})

const filtered = computed(() => {
  const kw = String(query.keyword || '').trim().toLowerCase()
  return rows.value.filter((row) => {
    if (query.status && row.status !== query.status) return false
    if (!kw) return true
    return ['orderNo', 'customerName', 'owner'].some((f) =>
      String(row[f] ?? '').toLowerCase().includes(kw)
    )
  })
})

const total = computed(() => filtered.value.length)

const pageList = computed(() => {
  const start = (query.page - 1) * query.pageSize
  return filtered.value.slice(start, start + query.pageSize)
})

function reload() {
  tick.value += 1
}

function handleReset() {
  Object.assign(query, { page: 1, pageSize: query.pageSize, keyword: '', status: '' })
}

function handleSizeChange(size) {
  query.pageSize = size
  query.page = 1
}

/* ---------- 汇总 ---------- */

const kpis = computed(() => {
  const all = rows.value
  const overdue = all.filter((item) => item.aging > 0 && item.balance > 0)
  const unreceived = all.reduce((sum, item) => sum + Math.max(item.balance, 0), 0)
  const overdueAmount = overdue.reduce((sum, item) => sum + Math.max(item.balance, 0), 0)
  const settled = all.filter((item) => item.status === '已结清')
  return [
    {
      label: '应收总额',
      value: fmtMoneyShort(all.reduce((sum, item) => sum + item.totalAmount, 0)),
      extra: `共 ${all.length} 笔订单`,
      tone: 'primary'
    },
    {
      label: '未收金额',
      value: fmtMoneyShort(unreceived),
      extra: `占应收 ${all.length ? ((unreceived / all.reduce((s, i) => s + i.totalAmount, 0)) * 100).toFixed(1) : 0}%`,
      tone: 'warning'
    },
    {
      label: '逾期金额',
      value: fmtMoneyShort(overdueAmount),
      extra: `${overdue.length} 笔已逾期，需重点催收`,
      tone: 'danger'
    },
    {
      label: '已结清',
      value: `${settled.length} 笔`,
      extra: `结清率 ${all.length ? ((settled.length / all.length) * 100).toFixed(1) : 0}%`,
      tone: 'success'
    }
  ]
})

/** 账龄区间：未到期 / 1-30 / 31-60 / 61-90 / 90+ */
const ageRank = computed(() => {
  const buckets = [
    { name: '未到期', min: -Infinity, max: 0 },
    { name: '逾期 1-30 天', min: 1, max: 30 },
    { name: '逾期 31-60 天', min: 31, max: 60 },
    { name: '逾期 61-90 天', min: 61, max: 90 },
    { name: '逾期 90 天以上', min: 91, max: Infinity }
  ]
  return buckets.map((b) => {
    const list = rows.value.filter((item) => item.balance > 0 && item.aging >= b.min && item.aging <= b.max)
    return {
      name: b.name,
      value: list.reduce((sum, item) => sum + item.balance, 0),
      extra: `${list.length} 笔`
    }
  })
})

/* ---------- 登记收款 ---------- */

const dialogVisible = ref(false)
const submitting = ref(false)
const form = reactive({ orderNo: '', customerName: '', balance: 0, amount: 0, payMethod: '银行转账' })
const editingId = ref(null)

function openReceipt(row) {
  editingId.value = row.id
  Object.assign(form, {
    orderNo: row.orderNo,
    customerName: row.customerName,
    balance: Math.max(row.balance, 0),
    amount: Math.max(row.balance, 0),
    payMethod: '银行转账'
  })
  dialogVisible.value = true
}

function submitReceipt() {
  if (!form.amount || form.amount <= 0) {
    ElMessage.warning('请输入有效的收款金额')
    return
  }
  submitting.value = true
  try {
    const target = store.receivables.find((item) => item.id === editingId.value)
    if (target) {
      target.receivedAmount = Number(target.receivedAmount) + Number(form.amount)
    }
    const d = new Date()
    const p = (v) => String(v).padStart(2, '0')
    store.receipts.unshift({
      id: nextExtraId(store.receipts),
      receiptNo: '',
      orderNo: form.orderNo,
      customerName: form.customerName,
      amount: form.amount,
      payMethod: form.payMethod,
      payDate: `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`,
      remark: '应收页面登记',
      createTime: d.toLocaleString('zh-CN', { hour12: false })
    })
    persistExtra()
    tick.value += 1
    dialogVisible.value = false
    ElMessage.success('收款已登记，并同步写入收款记录')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.kpi {
  padding: 13px 14px 12px;
  border: 1px solid #eef1f8;
  border-radius: var(--crm-radius);
  background: linear-gradient(180deg, #fbfcff, #f6f8fd);
}

.kpi-label {
  font-size: 12px;
  color: #8b97a8;
}

.kpi-value {
  margin: 6px 0 4px;
  font-size: 22px;
  font-weight: 700;
}

.kpi-extra {
  font-size: 11px;
  color: #a3aebd;
}

.kpi.tone-primary .kpi-value {
  color: #2f6bff;
}

.kpi.tone-warning .kpi-value {
  color: #f5a623;
}

.kpi.tone-danger .kpi-value {
  color: #f2637b;
}

.kpi.tone-success .kpi-value {
  color: #22c08a;
}

.age-block {
  padding: 12px 14px 14px;
  border: 1px solid #eef1f6;
  border-radius: var(--crm-radius);
  margin-bottom: 14px;
}

.block-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.block-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.block-head .hint {
  font-size: 11px;
  color: #a3aebd;
}

.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.balance {
  font-weight: 600;
  color: #f2637b;
}

.settled-text {
  font-size: 12px;
  color: #a3aebd;
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
