<template>
  <div class="page-container">
    <div class="page-card" v-loading="loading">
      <div class="page-header">
        <h2 class="page-title">
          产品销售分析
          <small>按产品线看销售额、毛利与客户覆盖，辅助定价与主推决策</small>
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
        <div v-for="item in kpis" :key="item.label" class="kpi" :class="`tone-${item.tone}`">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-extra">{{ item.extra }}</div>
        </div>
      </div>

      <div class="chart-grid">
        <section class="chart-card span-12">
          <header class="chart-head">
            <h3>销售额趋势</h3>
            <span class="hint">近 {{ range }} 个月 · 按订单日期</span>
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
            <h3>毛利贡献</h3>
            <span class="hint">按毛利额 Top 6</span>
          </header>
          <RankBar :items="profitRank" :formatter="fmtShort" color="#22c08a" />
        </section>

        <section class="chart-card span-4">
          <header class="chart-head">
            <h3>分类销售占比</h3>
            <span class="hint">按合同金额</span>
          </header>
          <RankBar :items="categoryRank" :formatter="fmtShort" color="#8b5cf6" />
        </section>
      </div>

      <!-- 明细 -->
      <section class="detail-block">
        <header class="block-head">
          <h3>产品明细</h3>
          <span class="hint">毛利率 =（合同金额 - 成本）/ 合同金额</span>
        </header>
        <el-table :data="productDetail" border stripe size="small" empty-text="暂无数据" max-height="420">
          <el-table-column type="index" label="#" width="55" align="center" />
          <el-table-column prop="name" label="产品名称" min-width="190" />
          <el-table-column prop="category" label="分类" width="105" />
          <el-table-column prop="orderCount" label="订单笔数" width="90" align="center" sortable />
          <el-table-column prop="amount" label="销售额" width="140" align="right" sortable>
            <template #default="{ row }">
              <span class="amount">{{ fmtMoney(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="cost" label="成本" width="130" align="right">
            <template #default="{ row }">
              <span class="mono">{{ fmtMoney(row.cost) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="profit" label="毛利" width="130" align="right" sortable>
            <template #default="{ row }">
              <span class="profit">{{ fmtMoney(row.profit) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="毛利率" width="120" align="right" sortable prop="profitRate">
            <template #default="{ row }">
              <span :class="row.profitRate >= 50 ? 'rate-good' : 'rate-normal'">{{ row.profitRate }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="customerCount" label="覆盖客户" width="105" align="center">
            <template #default="{ row }">{{ row.customerCount }} 家</template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import DonutChart from '@/components/charts/DonutChart.vue'
import RankBar from '@/components/charts/RankBar.vue'
import { getOrderPage } from '@/api/business'
import { getExtraDb } from '@/mock/extraDb'
import { formatMoney } from '@/utils/dict'

const PAGE_SIZE = 500
const loading = ref(false)
const range = ref(6)
const orders = ref([])
const extra = ref(getExtraDb())

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
    orders.value = await fetchAll(getOrderPage)
    extra.value = getExtraDb()
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

/** 从产品档案取成本率与分类：成本按档案中的「成本 / 标准价」比例折算到订单金额 */
const productMeta = computed(() => {
  const map = new Map()
  ;(extra.value.products || []).forEach((item) => {
    const price = Number(item.price) || 0
    const cost = Number(item.cost) || 0
    map.set(item.name, {
      costRate: price > 0 ? Math.min(cost / price, 1) : 0,
      category: item.categoryName,
      unit: item.unit
    })
  })
  return map
})

const productDetail = computed(() => {
  const bucket = new Map()
  validOrders.value.forEach((order) => {
    const key = order.productName || '未分类产品'
    const meta = productMeta.value.get(key)
    const current = bucket.get(key) || {
      name: key,
      category: meta?.category || '未分类零件',
      unit: meta?.unit || '件',
      orderCount: 0,
      amount: 0,
      cost: 0,
      customers: new Set()
    }
    current.orderCount += 1
    current.amount += Number(order.amount) || 0
    current.cost += Math.round((Number(order.amount) || 0) * (meta?.costRate ?? 0.3))
    if (order.customerName) current.customers.add(order.customerName)
    bucket.set(key, current)
  })

  return [...bucket.values()]
    .map((item) => {
      const profit = item.amount - item.cost
      return {
        name: item.name,
        category: item.category,
        unit: item.unit,
        orderCount: item.orderCount,
        amount: item.amount,
        cost: item.cost,
        profit,
        profitRate: item.amount ? ((profit / item.amount) * 100).toFixed(1) : '0.0',
        customerCount: item.customers.size
      }
    })
    .sort((a, b) => b.amount - a.amount)
})

const kpis = computed(() => {
  const list = productDetail.value
  const amount = totalAmount.value
  const cost = list.reduce((sum, item) => sum + item.cost, 0)
  const profit = amount - cost
  const best = [...list].sort((a, b) => b.amount - a.amount)[0]
  return [
    {
      label: '销售额',
      value: `¥${fmtShort(amount)}`,
      extra: `${validOrders.value.length} 笔有效订单`,
      tone: 'primary'
    },
    {
      label: '毛利额',
      value: `¥${fmtShort(profit)}`,
      extra: `毛利率 ${amount ? ((profit / amount) * 100).toFixed(1) : 0}%`,
      tone: 'success'
    },
    {
      label: '在售产品',
      value: `${list.length} 个`,
      extra: `产品档案 ${(extra.value.products || []).length} 条`,
      tone: 'info'
    },
    {
      label: '主推产品',
      value: best ? fmtShort(best.amount) : '—',
      extra: best ? best.name : '暂无销售',
      tone: 'violet'
    },
    {
      label: '客单价',
      value: `¥${fmtShort(validOrders.value.length ? amount / validOrders.value.length : 0)}`,
      extra: '平均每笔订单金额',
      tone: 'warning'
    }
  ]
})

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
  const list = productDetail.value
  if (list.length <= 6) return list.map((item) => ({ name: item.name, value: item.amount }))
  const rest = list.slice(5).reduce((sum, item) => sum + item.amount, 0)
  return [...list.slice(0, 5).map((item) => ({ name: item.name, value: item.amount })), { name: '其他产品', value: rest }]
})

const profitRank = computed(() =>
  [...productDetail.value]
    .sort((a, b) => b.profit - a.profit)
    .slice(0, 6)
    .map((item) => ({ name: item.name, value: item.profit, extra: `${item.profitRate}%` }))
)

const categoryRank = computed(() => {
  const bucket = new Map()
  productDetail.value.forEach((item) => {
    bucket.set(item.category, (bucket.get(item.category) || 0) + item.amount)
  })
  return [...bucket.entries()]
    .filter(([, value]) => value > 0)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
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

.kpi.tone-success .kpi-value {
  color: #22c08a;
}

.kpi.tone-info .kpi-value {
  color: #00b8d9;
}

.kpi.tone-violet .kpi-value {
  color: #8b5cf6;
}

.kpi.tone-warning .kpi-value {
  color: #f5a623;
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

.profit {
  font-weight: 600;
  color: #22c08a;
}

.rate-good {
  color: #22c08a;
  font-weight: 600;
}

.rate-normal {
  color: var(--crm-text-secondary);
}

@media (max-width: 1400px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .span-4 {
    grid-column: span 12;
  }
}
</style>
