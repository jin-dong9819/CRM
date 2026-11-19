<template>
  <div class="page-container">
    <div class="page-card" v-loading="loading">
      <div class="page-header">
        <h2 class="page-title">
          客户采购分析
          <small>从客户维度看采购规模、复购与集中度，识别高价值与流失风险</small>
        </h2>
        <div class="header-actions">
          <el-radio-group v-model="dimension" size="default">
            <el-radio-button value="amount">按金额</el-radio-button>
            <el-radio-button value="count">按笔数</el-radio-button>
          </el-radio-group>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>

      <!-- 核心指标 -->
      <div class="kpi-grid">
        <div v-for="item in kpis" :key="item.label" class="kpi" :class="`tone-${item.tone}`">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-extra">{{ item.extra }}</div>
        </div>
      </div>

      <div class="chart-grid">
        <section class="chart-card span-8">
          <header class="chart-head">
            <h3>客户采购金额排行</h3>
            <span class="hint">Top 8 · 按合同金额</span>
          </header>
          <RankBar :items="topCustomers" :formatter="fmtShort" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>采购集中度</h3>
            <span class="hint">Top 5 客户占比</span>
          </header>
          <DonutChart
            :items="concentration"
            center-label="合同总额"
            :center-value="fmtShort(totalAmount)"
            :formatter="fmtShort"
          />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>客户等级构成</h3>
            <span class="hint">按客户数</span>
          </header>
          <DonutChart
            :items="levelShare"
            center-label="客户总数"
            :center-value="`${customers.length}`"
            :formatter="(v) => `${v} 家`"
          />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>行业采购分布</h3>
            <span class="hint">按合同金额</span>
          </header>
          <RankBar :items="industryRank" :formatter="fmtShort" color="#8b5cf6" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>复购情况</h3>
            <span class="hint">按采购笔数分布</span>
          </header>
          <RankBar :items="repeatRank" :formatter="(v) => `${v} 家`" color="#00b8d9" />
        </section>
      </div>

      <!-- 明细 -->
      <section class="detail-block">
        <header class="block-head">
          <h3>客户明细</h3>
          <span class="hint">按 {{ dimension === 'amount' ? '合同金额' : '采购笔数' }} 降序</span>
        </header>
        <el-table :data="sortedDetail" border stripe size="small" empty-text="暂无数据" max-height="420">
          <el-table-column type="index" label="#" width="55" align="center" />
          <el-table-column prop="name" label="客户名称" min-width="150" />
          <el-table-column prop="industry" label="行业" width="110" />
          <el-table-column label="客户等级" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="CUSTOMER_LEVEL_TAG[row.level] || 'info'" effect="light" size="small">
                {{ row.level }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderCount" label="采购笔数" width="100" align="center" sortable />
          <el-table-column prop="amount" label="合同金额" width="140" align="right" sortable>
            <template #default="{ row }">
              <span class="amount">{{ fmtMoney(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="avgAmount" label="平均单笔" width="130" align="right">
            <template #default="{ row }">
              <span class="mono">{{ fmtMoney(row.avgAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="lastOrderDate" label="最近下单" width="115" />
          <el-table-column prop="status" label="合作状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="CUSTOMER_STATUS_TAG[row.status] || 'info'" effect="light" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import RankBar from '@/components/charts/RankBar.vue'
import DonutChart from '@/components/charts/DonutChart.vue'
import { getOrderPage, getCustomerPage } from '@/api/business'
import { CUSTOMER_LEVEL, CUSTOMER_LEVEL_TAG, CUSTOMER_STATUS_TAG, formatMoney } from '@/utils/dict'

const PAGE_SIZE = 500
const loading = ref(false)
const dimension = ref('amount')
const orders = ref([])
const customers = ref([])

async function fetchAll(fetcher) {
  const all = []
  for (let page = 1; page <= 20; page += 1) {
    const data = await fetcher({ page, pageSize: PAGE_SIZE })
    const list = data?.list || []
    all.push(...list)
    if (list.length < PAGE_SIZE || all.length >= (data?.total || 0)) break
  }
  return all
}

async function load() {
  loading.value = true
  try {
    const [orderList, customerList] = await Promise.all([fetchAll(getOrderPage), fetchAll(getCustomerPage)])
    orders.value = orderList
    customers.value = customerList
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(load)

const fmtMoney = (v) => formatMoney(v)

function fmtShort(value) {
  const num = Number(value) || 0
  if (Math.abs(num) >= 10000) {
    const n = num / 10000
    return `${n.toFixed(n >= 1000 ? 0 : 1).replace(/\.0$/, '')}万`
  }
  return `${Math.round(num)}`
}

const validOrders = computed(() => orders.value.filter((item) => item.status !== '已取消'))
const totalAmount = computed(() => validOrders.value.reduce((sum, item) => sum + (Number(item.amount) || 0), 0))

/** 客户维度明细 */
const detail = computed(() => {
  const bucket = new Map()
  validOrders.value.forEach((order) => {
    const key = order.customerName || '未归属客户'
    const current = bucket.get(key) || { amount: 0, orderCount: 0, lastOrderDate: '' }
    current.amount += Number(order.amount) || 0
    current.orderCount += 1
    if (String(order.orderDate) > current.lastOrderDate) current.lastOrderDate = String(order.orderDate)
    bucket.set(key, current)
  })

  return [...bucket.entries()].map(([name, item]) => {
    const customer = customers.value.find((c) => c.name === name) || {}
    return {
      name,
      industry: customer.industry || '未填写',
      level: customer.level || '潜在客户',
      status: customer.status || '跟进中',
      amount: item.amount,
      orderCount: item.orderCount,
      avgAmount: item.orderCount ? item.amount / item.orderCount : 0,
      lastOrderDate: item.lastOrderDate
    }
  })
})

const sortedDetail = computed(() => {
  const list = [...detail.value]
  if (dimension.value === 'count') return list.sort((a, b) => b.orderCount - a.orderCount)
  return list.sort((a, b) => b.amount - a.amount)
})

const kpis = computed(() => {
  const list = detail.value
  const total = totalAmount.value
  const top5 = [...list].sort((a, b) => b.amount - a.amount).slice(0, 5)
  const top5Amount = top5.reduce((sum, item) => sum + item.amount, 0)
  const repeat = list.filter((item) => item.orderCount >= 2)
  return [
    {
      label: '采购客户数',
      value: `${list.length}`,
      extra: `客户档案共 ${customers.value.length} 家`,
      tone: 'primary'
    },
    {
      label: '户均采购额',
      value: `¥${fmtShort(list.length ? total / list.length : 0)}`,
      extra: `合同总额 ¥${fmtShort(total)}`,
      tone: 'info'
    },
    {
      label: '复购客户',
      value: `${repeat.length} 家`,
      extra: `复购率 ${list.length ? ((repeat.length / list.length) * 100).toFixed(1) : 0}%`,
      tone: 'success'
    },
    {
      label: 'Top5 集中度',
      value: `${total ? ((top5Amount / total) * 100).toFixed(1) : 0}%`,
      extra: `合计 ¥${fmtShort(top5Amount)}`,
      tone: 'warning'
    },
    {
      label: '最高单客户',
      value: `¥${fmtShort(list.length ? Math.max(...list.map((i) => i.amount)) : 0)}`,
      extra: list.length ? [...list].sort((a, b) => b.amount - a.amount)[0].name : '—',
      tone: 'violet'
    }
  ]
})

const topCustomers = computed(() =>
  [...detail.value]
    .sort((a, b) => b.amount - a.amount)
    .slice(0, 8)
    .map((item) => ({ name: item.name, value: item.amount, extra: `${item.orderCount} 笔` }))
)

const concentration = computed(() => {
  const list = [...detail.value].sort((a, b) => b.amount - a.amount)
  if (list.length <= 5) return list.map((item) => ({ name: item.name, value: item.amount }))
  const rest = list.slice(5).reduce((sum, item) => sum + item.amount, 0)
  return [...list.slice(0, 5).map((item) => ({ name: item.name, value: item.amount })), { name: '其他客户', value: rest }]
})

const levelShare = computed(() => {
  const colors = { 重要客户: '#f2637b', 普通客户: '#2f6bff', 潜在客户: '#94a3b8' }
  return CUSTOMER_LEVEL.map((level) => ({
    name: level,
    value: customers.value.filter((item) => item.level === level).length,
    color: colors[level]
  }))
})

const industryRank = computed(() => {
  const bucket = new Map()
  detail.value.forEach((item) => {
    bucket.set(item.industry, (bucket.get(item.industry) || 0) + item.amount)
  })
  return [...bucket.entries()]
    .filter(([, value]) => value > 0)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
})

const repeatRank = computed(() => {
  const buckets = [
    { name: '1 笔（新客）', test: (n) => n === 1 },
    { name: '2 笔', test: (n) => n === 2 },
    { name: '3 笔', test: (n) => n === 3 },
    { name: '4 笔及以上', test: (n) => n >= 4 }
  ]
  return buckets.map((b) => ({
    name: b.name,
    value: detail.value.filter((item) => b.test(item.orderCount)).length
  }))
})
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
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
  font-size: 21px;
  font-weight: 700;
  white-space: nowrap;
}

.kpi-extra {
  font-size: 11px;
  color: #a3aebd;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.kpi.tone-primary .kpi-value {
  color: #2f6bff;
}

.kpi.tone-info .kpi-value {
  color: #00b8d9;
}

.kpi.tone-success .kpi-value {
  color: #22c08a;
}

.kpi.tone-warning .kpi-value {
  color: #f5a623;
}

.kpi.tone-violet .kpi-value {
  color: #8b5cf6;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.chart-card {
  padding: 12px 14px 14px;
  border: 1px solid #eef1f6;
  border-radius: var(--crm-radius);
}

.span-8 {
  grid-column: span 8;
}

.span-4 {
  grid-column: span 4;
}

.chart-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.chart-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.chart-head .hint {
  font-size: 11px;
  color: #a3aebd;
  white-space: nowrap;
}

.detail-block {
  border-top: 1px solid #f1f4f9;
  padding-top: 14px;
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

@media (max-width: 1400px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .span-8,
  .span-4 {
    grid-column: span 12;
  }
}
</style>
