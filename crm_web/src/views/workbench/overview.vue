<template>
  <div class="page-container">
    <div class="page-card" v-loading="loading">
      <div class="page-header">
        <h2 class="page-title">
          经营概览
          <small>今日待跟进、本月业绩与回款态势一屏掌握</small>
        </h2>
        <div class="header-actions">
          <span class="update-at">更新于 {{ updatedAt || '—' }}</span>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>

      <!-- 核心指标 -->
      <div class="kpi-grid">
        <div v-for="item in kpis" :key="item.label" class="kpi" :class="`tone-${item.tone}`">
          <div class="kpi-top">
            <span class="kpi-label">{{ item.label }}</span>
            <el-icon class="kpi-icon"><component :is="item.icon" /></el-icon>
          </div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-extra">{{ item.extra }}</div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <section class="block">
        <header class="block-head">
          <h3>快捷入口</h3>
          <span class="hint">常用操作一键直达</span>
        </header>
        <div class="shortcut-grid">
          <div v-for="item in shortcuts" :key="item.path" class="shortcut" @click="go(item.path)">
            <el-icon class="sc-icon"><component :is="item.icon" /></el-icon>
            <div class="sc-text">
              <span class="sc-title">{{ item.title }}</span>
              <span class="sc-desc">{{ item.desc }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 图表区 -->
      <div class="chart-grid">
        <section class="chart-card span-8">
          <header class="chart-head">
            <h3>合同金额趋势</h3>
            <span class="hint">近 6 个月 · 按下单日期</span>
          </header>
          <TrendChart :data="monthTrend" unit="元" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>销售阶段漏斗</h3>
            <span class="hint">商机金额 / 数量</span>
          </header>
          <RankBar :items="oppFunnel" :formatter="fmtShort" color="#00b8d9" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>回款结构</h3>
            <span class="hint">按付款状态划分</span>
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
            <h3>负责人业绩</h3>
            <span class="hint">合同金额 Top 5</span>
          </header>
          <RankBar :items="ownerRank" :formatter="fmtShort" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>售后工单状态</h3>
            <span class="hint">按工单处理阶段</span>
          </header>
          <RankBar :items="ticketRank" :formatter="(v) => `${v} 单`" color="#f5a623" />
        </section>
      </div>

      <!-- 待跟进线索 -->
      <section class="block">
        <header class="block-head">
          <h3>近期需跟进</h3>
          <span class="hint">下次跟进日期在今明两天内的线索</span>
          <el-button text type="primary" class="more" @click="go('/customer/lead')">全部线索</el-button>
        </header>
        <el-table :data="urgentLeads" border stripe size="small" empty-text="暂无待跟进线索">
          <el-table-column prop="leadNo" label="线索编号" width="115">
            <template #default="{ row }">
              <span class="mono">{{ row.leadNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="线索 / 公司" min-width="200">
            <template #default="{ row }">
              <div class="cell-main">{{ row.name }}</div>
              <div class="cell-sub">{{ row.company }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="contact" label="联系人" width="85" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column label="意向" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="LEAD_LEVEL_TAG[row.level] || 'info'" effect="light" size="small">
                {{ row.level }} 类
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="owner" label="负责人" width="85" />
          <el-table-column prop="nextFollowTime" label="下次跟进" width="110" />
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import DonutChart from '@/components/charts/DonutChart.vue'
import RankBar from '@/components/charts/RankBar.vue'
import { getLeadPage, getOrderPage, getCustomerPage, getOpportunityPage } from '@/api/business'
import { getExtraDb } from '@/mock/extraDb'
import { PAY_STATUS, OPP_STAGE, LEAD_LEVEL_TAG, TICKET_STATUS } from '@/utils/dict'

const PAGE_SIZE = 500
const router = useRouter()

const loading = ref(false)
const updatedAt = ref('')
const leads = ref([])
const orders = ref([])
const customers = ref([])
const opportunities = ref([])
const extra = ref(getExtraDb())

const PAY_COLOR = { 未付款: '#f2637b', 部分付款: '#f5a623', 已付款: '#22c08a' }
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
    const [leadList, orderList, customerList, oppList] = await Promise.all([
      fetchAll(getLeadPage),
      fetchAll(getOrderPage),
      fetchAll(getCustomerPage),
      fetchAll(getOpportunityPage)
    ])
    leads.value = leadList
    orders.value = orderList
    customers.value = customerList
    opportunities.value = oppList
    extra.value = getExtraDb()
    updatedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(load)

function fmtShort(value) {
  const num = Number(value) || 0
  if (Math.abs(num) >= 10000) {
    const n = num / 10000
    return `${n.toFixed(n >= 1000 ? 0 : 1).replace(/\.0$/, '')}万`
  }
  return `${Math.round(num)}`
}

const go = (path) => router.push(path)

/* ---------- 指标 ---------- */

const validOrders = computed(() => orders.value.filter((item) => item.status !== '已取消'))
const sumAmount = (list) => list.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
const totalAmount = computed(() => sumAmount(validOrders.value))
const paidAmount = computed(() => sumAmount(validOrders.value.filter((item) => item.payStatus === '已付款')))
const paidRate = computed(() => (totalAmount.value > 0 ? (paidAmount.value / totalAmount.value) * 100 : 0))

/** 近 30 天订单：演示数据下单日期多在最近一个月内 */
const monthOrders = computed(() => {
  const now = new Date()
  const from = new Date(now.getTime() - 30 * 86400000)
  const p = (v) => String(v).padStart(2, '0')
  const fromKey = `${from.getFullYear()}-${p(from.getMonth() + 1)}-${p(from.getDate())}`
  return validOrders.value.filter((item) => String(item.orderDate || '') >= fromKey)
})

const urgentLeads = computed(() => {
  const today = new Date()
  const keys = [0, 1].map((offset) => {
    const d = new Date(today.getTime() + offset * 86400000)
    const p = (v) => String(v).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
  })
  return leads.value
    .filter((item) => keys.includes(item.nextFollowTime) && item.status !== '已失效')
    .slice(0, 6)
})

const openTickets = computed(() =>
  (extra.value.tickets || []).filter((item) => item.status === '待受理' || item.status === '处理中')
)

const overdueReceivables = computed(() =>
  (extra.value.receivables || []).filter((item) => item.status === '已逾期')
)

const kpis = computed(() => [
  {
    label: '近 30 天合同额',
    value: `¥${fmtShort(sumAmount(monthOrders.value))}`,
    extra: `近 30 天新签 ${monthOrders.value.length} 笔`,
    icon: 'Money',
    tone: 'primary'
  },
  {
    label: '待跟进线索',
    value: `${leads.value.filter((item) => item.status === '未跟进' || item.status === '跟进中').length}`,
    extra: `其中今明两天到期 ${urgentLeads.value.length} 条`,
    icon: 'Aim',
    tone: 'warning'
  },
  {
    label: '在谈商机',
    value: `${opportunities.value.filter((item) => OPEN_STAGES.includes(item.stage)).length}`,
    extra: `加权预测 ¥${fmtShort(
      opportunities.value
        .filter((item) => OPEN_STAGES.includes(item.stage))
        .reduce((sum, item) => sum + ((Number(item.amount) || 0) * (Number(item.probability) || 0)) / 100, 0)
    )}`,
    icon: 'TrendCharts',
    tone: 'info'
  },
  {
    label: '逾期应收',
    value: `¥${fmtShort(sumAmount(overdueReceivables.value.map((i) => ({ amount: i.totalAmount - i.receivedAmount }))))}`,
    extra: `${overdueReceivables.value.length} 笔已超期，需催收`,
    icon: 'WarningFilled',
    tone: 'danger'
  },
  {
    label: '待处理工单',
    value: `${openTickets.value.length}`,
    extra: `高优先级 ${openTickets.value.filter((item) => item.priority === '高').length} 单`,
    icon: 'Service',
    tone: 'success'
  },
  {
    label: '合作中客户',
    value: `${customers.value.filter((item) => item.status === '合作中' || item.status === '已成交').length}`,
    extra: `客户总数 ${customers.value.length} 家`,
    icon: 'User',
    tone: 'violet'
  }
])

const shortcuts = [
  { title: '新增线索', desc: '登记潜在客户', path: '/customer/lead', icon: 'Aim' },
  { title: '客户档案', desc: '查看客户全景', path: '/customer/archive', icon: 'Postcard' },
  { title: '销售商机', desc: '推进漏斗阶段', path: '/sales/opportunity', icon: 'Histogram' },
  { title: '报价单', desc: '快速生成报价', path: '/sales/quote', icon: 'Document' },
  { title: '收款记录', desc: '登记回款', path: '/delivery/receipt', icon: 'Wallet' },
  { title: '售后工单', desc: '受理客户报障', path: '/service/ticket', icon: 'Service' }
]

/* ---------- 图表 ---------- */

const monthTrend = computed(() => {
  const now = new Date()
  const months = []
  for (let i = 5; i >= 0; i -= 1) {
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

const oppFunnel = computed(() =>
  OPP_STAGE.filter((stage) => stage !== '输单').map((stage) => {
    const list = opportunities.value.filter((item) => item.stage === stage)
    return { name: stage, value: sumAmount(list), extra: `${list.length} 个` }
  })
)

const payShare = computed(() =>
  PAY_STATUS.map((status) => ({
    name: status,
    value: sumAmount(validOrders.value.filter((item) => item.payStatus === status)),
    color: PAY_COLOR[status]
  }))
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
    .slice(0, 5)
})

const ticketRank = computed(() =>
  TICKET_STATUS.map((status) => ({
    name: status,
    value: (extra.value.tickets || []).filter((item) => item.status === status).length
  }))
)
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.update-at {
  font-size: 12px;
  color: #a3aebd;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

@media (min-width: 1500px) {
  .kpi-grid {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}

.kpi {
  padding: 13px 14px 12px;
  border: 1px solid #eef1f8;
  border-radius: var(--crm-radius);
  background: linear-gradient(180deg, #fbfcff, #f6f8fd);
}

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kpi-label {
  font-size: 12px;
  color: #8b97a8;
}

.kpi-icon {
  font-size: 15px;
  color: #b6c0cc;
}

.kpi-value {
  margin: 7px 0 4px;
  font-size: 21px;
  font-weight: 700;
  letter-spacing: -0.3px;
  white-space: nowrap;
}

.kpi-extra {
  font-size: 11px;
  color: #a3aebd;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.kpi.tone-primary .kpi-value {
  color: #2f6bff;
}

.kpi.tone-warning .kpi-value {
  color: #f5a623;
}

.kpi.tone-info .kpi-value {
  color: #00b8d9;
}

.kpi.tone-danger .kpi-value {
  color: #f2637b;
}

.kpi.tone-success .kpi-value {
  color: #22c08a;
}

.kpi.tone-violet .kpi-value {
  color: #8b5cf6;
}

.block {
  margin-bottom: 16px;
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

.block-head .more {
  margin-left: auto;
  font-size: 12px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.shortcut {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border: 1px solid #eef1f6;
  border-radius: var(--crm-radius);
  cursor: pointer;
  transition: border-color 0.18s, box-shadow 0.18s, transform 0.18s;
}

.shortcut:hover {
  border-color: #c3d2ff;
  box-shadow: 0 6px 16px rgba(47, 107, 255, 0.09);
  transform: translateY(-1px);
}

.sc-icon {
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  border-radius: 8px;
  font-size: 15px;
  color: #2f6bff;
  background: #eaf0ff;
}

.sc-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.35;
}

.sc-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--crm-text);
}

.sc-desc {
  font-size: 11px;
  color: #a3aebd;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
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

.cell-main {
  font-size: 13px;
}

.cell-sub {
  font-size: 12px;
  color: var(--crm-text-secondary);
  margin-top: 2px;
}

@media (max-width: 1500px) {
  .shortcut-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (min-width: 1500px) {
  .shortcut-grid {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .span-8,
  .span-4 {
    grid-column: span 6;
  }
}

@media (max-width: 820px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .shortcut-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .span-8,
  .span-4 {
    grid-column: span 12;
  }
}
</style>
