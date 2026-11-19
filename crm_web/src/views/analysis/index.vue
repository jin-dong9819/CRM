<template>
  <div class="page-container">
    <div class="page-card" v-loading="loading">
      <div class="page-header">
        <h2 class="page-title">
          数据分析
          <small>客户、商机与订单的经营全景，数据取自各业务模块的实时记录</small>
        </h2>
        <div class="header-actions">
          <el-radio-group v-model="range" size="default">
            <el-radio-button :value="6">近 6 个月</el-radio-button>
            <el-radio-button :value="12">近 12 个月</el-radio-button>
          </el-radio-group>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>

      <!-- 核心指标 -->
      <div class="kpi-grid">
        <div v-for="item in kpis" :key="item.label" class="kpi">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="kpi-extra">{{ item.extra }}</div>
        </div>
      </div>

      <!-- 图表区 -->
      <div class="chart-grid">
        <section class="chart-card span-12">
          <header class="chart-head">
            <h3>合同金额趋势</h3>
            <span class="hint">近 {{ range }} 个月 · 按下单日期 · 不含已取消订单</span>
          </header>
          <TrendChart :data="monthTrend" unit="元" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>产品销售构成</h3>
            <span class="hint">按合同金额</span>
          </header>
          <DonutChart
            :items="productShare"
            center-label="合同总额"
            :center-value="fmtShort(totalAmount)"
            :formatter="fmtShort"
          />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>回款结构</h3>
            <span class="hint">按付款状态划分金额</span>
          </header>
          <DonutChart
            :items="payShare"
            center-label="回款率"
            :center-value="`${paidRate.toFixed(1)}%`"
            :formatter="fmtShort"
          />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>商机阶段分布</h3>
            <span class="hint">金额 / 商机数</span>
          </header>
          <RankBar :items="oppFunnel" :formatter="fmtShort" color="#00b8d9" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>负责人业绩排行</h3>
            <span class="hint">按合同金额 Top 6</span>
          </header>
          <RankBar :items="ownerRank" :formatter="fmtShort" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>客户行业分布</h3>
            <span class="hint">客户数（家）</span>
          </header>
          <RankBar :items="industryRank" :formatter="(v) => `${v} 家`" color="#8b5cf6" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>客户状态构成</h3>
            <span class="hint">客户数占比</span>
          </header>
          <DonutChart
            :items="customerShare"
            center-label="客户总数"
            :center-value="`${customers.length}`"
            :formatter="(v) => `${v} 家`"
          />
        </section>
      </div>

      <div class="board-footer">
        数据更新于 {{ updatedAt || '—' }} · 共 {{ orders.length }} 笔订单 / {{ customers.length }} 位客户 /
        {{ opportunities.length }} 个商机
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import DonutChart from '@/components/charts/DonutChart.vue'
import RankBar from '@/components/charts/RankBar.vue'
import { getOrderPage, getCustomerPage, getOpportunityPage } from '@/api/business'
import { PAY_STATUS, CUSTOMER_STATUS, OPP_STAGE } from '@/utils/dict'

/** 后端单页上限 500，循环拉全量后在前端聚合，避免新增统计接口 */
const PAGE_SIZE = 500

const loading = ref(false)
const updatedAt = ref('')
const range = ref(6)

const orders = ref([])
const customers = ref([])
const opportunities = ref([])

const PAY_COLOR = { 未付款: '#f2637b', 部分付款: '#f5a623', 已付款: '#22c08a' }
const CUSTOMER_COLOR = { 跟进中: '#2f6bff', 合作中: '#22c08a', 已成交: '#8b5cf6', 已流失: '#cbd5e1' }
const OPEN_STAGES = OPP_STAGE.filter((stage) => stage !== '赢单' && stage !== '输单')

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
    const [orderList, customerList, oppList] = await Promise.all([
      fetchAll(getOrderPage),
      fetchAll(getCustomerPage),
      fetchAll(getOpportunityPage)
    ])
    orders.value = orderList
    customers.value = customerList
    opportunities.value = oppList
    updatedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(load)

/* ---------- 数值格式化 ---------- */

/** 3510000 -> 351万 / 12345 -> 1.2万 */
function fmtShort(value) {
  const num = Number(value) || 0
  if (Math.abs(num) >= 10000) {
    const n = num / 10000
    return `${n.toFixed(n >= 1000 ? 0 : 1).replace(/\.0$/, '')}万`
  }
  return `${Math.round(num)}`
}

/* ---------- 基础聚合 ---------- */

const validOrders = computed(() => orders.value.filter((item) => item.status !== '已取消'))

const sumAmount = (list) => list.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)

const totalAmount = computed(() => sumAmount(validOrders.value))
const paidAmount = computed(() => sumAmount(validOrders.value.filter((item) => item.payStatus === '已付款')))
const pendingOrders = computed(() =>
  validOrders.value.filter((item) => item.payStatus === '未付款' || item.payStatus === '部分付款')
)
const paidRate = computed(() => (totalAmount.value > 0 ? (paidAmount.value / totalAmount.value) * 100 : 0))

const kpis = computed(() => {
  const count = validOrders.value.length
  const avg = count ? totalAmount.value / count : 0
  const activeCustomers = customers.value.filter(
    (item) => item.status === '合作中' || item.status === '已成交'
  ).length
  const openOpps = opportunities.value.filter((item) => OPEN_STAGES.includes(item.stage))
  const weighted = openOpps.reduce(
    (sum, item) => sum + ((Number(item.amount) || 0) * (Number(item.probability) || 0)) / 100,
    0
  )

  return [
    {
      label: '合同总额',
      value: `¥${fmtShort(totalAmount.value)}`,
      color: '#2f6bff',
      extra: `有效订单 ${count} 笔 · 均价 ¥${fmtShort(avg)}`
    },
    {
      label: '已回款金额',
      value: `¥${fmtShort(paidAmount.value)}`,
      color: '#22c08a',
      extra: `回款率 ${paidRate.value.toFixed(1)}%`
    },
    {
      label: '待回款金额',
      value: `¥${fmtShort(totalAmount.value - paidAmount.value)}`,
      color: '#f5a623',
      extra: `未付款 + 部分付款，共 ${pendingOrders.value.length} 笔`
    },
    {
      label: '客户总数',
      value: `${customers.value.length}`,
      color: '#8b5cf6',
      extra: `合作中 / 已成交 ${activeCustomers} 家`
    },
    {
      label: '商机预测金额',
      value: `¥${fmtShort(weighted)}`,
      color: '#00b8d9',
      extra: `在跟进 ${openOpps.length} 个 · 按赢率加权`
    }
  ]
})

/* ---------- 图表数据 ---------- */

const monthTrend = computed(() => {
  const now = new Date()
  const months = []
  for (let i = range.value - 1; i >= 0; i -= 1) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    months.push({ key, label: `${d.getMonth() + 1}月` })
  }

  const bucket = new Map(months.map((item) => [item.key, 0]))
  validOrders.value.forEach((order) => {
    const key = String(order.orderDate || '').slice(0, 7)
    if (bucket.has(key)) bucket.set(key, bucket.get(key) + (Number(order.amount) || 0))
  })

  return months.map((item) => ({ label: item.label, value: bucket.get(item.key) }))
})

const productShare = computed(() => {
  const bucket = new Map()
  validOrders.value.forEach((order) => {
    const key = order.productName || '未分类'
    bucket.set(key, (bucket.get(key) || 0) + (Number(order.amount) || 0))
  })
  const list = [...bucket.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
  if (list.length <= 6) return list
  const rest = list.slice(5).reduce((sum, item) => sum + item.value, 0)
  return [...list.slice(0, 5), { name: '其他', value: rest }]
})

const payShare = computed(() =>
  PAY_STATUS.map((status) => ({
    name: status,
    value: sumAmount(validOrders.value.filter((item) => item.payStatus === status)),
    color: PAY_COLOR[status]
  }))
)

const oppFunnel = computed(() =>
  OPP_STAGE.filter((stage) => stage !== '输单').map((stage) => {
    const list = opportunities.value.filter((item) => item.stage === stage)
    return {
      name: stage,
      value: sumAmount(list),
      extra: `${list.length} 个`
    }
  })
)

const ownerRank = computed(() => {
  const bucket = new Map()
  validOrders.value.forEach((order) => {
    const key = order.owner || '未分配'
    const current = bucket.get(key) || { value: 0, count: 0 }
    current.value += Number(order.amount) || 0
    current.count += 1
    bucket.set(key, current)
  })
  return [...bucket.entries()]
    .map(([name, item]) => ({ name, value: item.value, extra: `${item.count} 笔` }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 6)
})

const industryRank = computed(() => {
  const bucket = new Map()
  customers.value.forEach((customer) => {
    const key = customer.industry || '未填写'
    bucket.set(key, (bucket.get(key) || 0) + 1)
  })
  return [...bucket.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
})

const customerShare = computed(() =>
  CUSTOMER_STATUS.map((status) => ({
    name: status,
    value: customers.value.filter((item) => item.status === status).length,
    color: CUSTOMER_COLOR[status]
  }))
)
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
  padding: 14px 14px 12px;
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
  letter-spacing: -0.3px;
  white-space: nowrap;
}

.kpi-extra {
  font-size: 11px;
  color: #a3aebd;
  line-height: 1.5;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 12px;
}

.chart-card {
  padding: 12px 14px 14px;
  border: 1px solid #eef1f6;
  border-radius: var(--crm-radius);
}

.span-12 {
  grid-column: span 12;
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
  white-space: nowrap;
}

.chart-head .hint {
  font-size: 11px;
  color: #a3aebd;
  white-space: nowrap;
}

.board-footer {
  padding: 12px 0 6px;
  font-size: 11px;
  color: #a3aebd;
}

@media (max-width: 1400px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .span-4 {
    grid-column: span 6;
  }
}

@media (max-width: 820px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .span-4 {
    grid-column: span 12;
  }
}
</style>
