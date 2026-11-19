<template>
  <div class="page-container">
    <div class="page-card" v-loading="loading">
      <div class="page-header">
        <h2 class="page-title">
          我的待办
          <small>按紧急程度汇总今日需要处理的事项，逐条跟进即可</small>
        </h2>
        <div class="header-actions">
          <el-radio-group v-model="filterType" size="default">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="today">今日到期</el-radio-button>
            <el-radio-button value="overdue">已逾期</el-radio-button>
          </el-radio-group>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>

      <!-- 汇总卡片 -->
      <div class="summary-grid">
        <div v-for="item in summary" :key="item.label" class="summary" :class="`tone-${item.tone}`">
          <el-icon class="s-icon"><component :is="item.icon" /></el-icon>
          <div class="s-body">
            <div class="s-value">{{ item.value }}</div>
            <div class="s-label">{{ item.label }}</div>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="todo-tabs">
        <el-tab-pane v-for="tab in tabs" :key="tab.key" :name="tab.key">
          <template #label>
            <span class="tab-label">
              {{ tab.title }}
              <el-badge v-if="tabRows(tab.key).length" :value="tabRows(tab.key).length" type="primary" />
            </span>
          </template>

          <el-table :data="tabRows(tab.key)" border stripe size="default" empty-text="暂无待办">
            <el-table-column label="类型" width="110">
              <template #default>
                <el-tag :type="tab.tag" effect="light" size="small">{{ tab.title }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="事项" min-width="220">
              <template #default="{ row }">
                <div class="cell-main">{{ row.title }}</div>
                <div class="cell-sub">{{ row.sub }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="owner" label="负责人" width="95" />
            <el-table-column label="截止日期" width="150">
              <template #default="{ row }">
                <span :class="{ overdue: row.overdue }">{{ row.deadline || '—' }}</span>
                <el-tag v-if="row.overdue" type="danger" effect="plain" size="small" class="gap-left">逾期</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="优先级" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="PRIORITY_TAG[row.priority] || 'info'" effect="light" size="small">
                  {{ row.priority }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" align="center" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="go(row.path)">去处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getLeadPage, getOpportunityPage, getOrderPage } from '@/api/business'
import { getExtraDb } from '@/mock/extraDb'

const PAGE_SIZE = 500
const router = useRouter()

const loading = ref(false)
const activeTab = ref('lead')
const filterType = ref('all')

const leads = ref([])
const opportunities = ref([])
const orders = ref([])
const extra = ref(getExtraDb())

const PRIORITY_TAG = { 高: 'danger', 中: 'warning', 低: 'info' }

const todayKey = () => {
  const d = new Date()
  const p = (v) => String(v).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

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
    const [leadList, oppList, orderList] = await Promise.all([
      fetchAll(getLeadPage),
      fetchAll(getOpportunityPage),
      fetchAll(getOrderPage)
    ])
    leads.value = leadList
    opportunities.value = oppList
    orders.value = orderList
    extra.value = getExtraDb()
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(load)

const go = (path) => router.push(path)

/* ---------- 待办构造 ---------- */

const FS = computed(() => {
  const today = todayKey()

  // 1. 线索跟进
  const leadTasks = leads.value
    .filter((item) => item.status === '未跟进' || item.status === '跟进中')
    .map((item) => ({
      title: `${item.company} · ${item.contact}`,
      sub: `${item.name}（${item.level} 类意向，来源 ${item.source}）`,
      owner: item.owner,
      deadline: item.nextFollowTime,
      priority: item.level === 'A' ? '高' : item.level === 'B' ? '中' : '低',
      overdue: item.nextFollowTime ? item.nextFollowTime < today : false,
      path: '/customer/lead'
    }))

  // 2. 商机推进
  const oppTasks = opportunities.value
    .filter((item) => item.stage !== '赢单' && item.stage !== '输单')
    .map((item) => ({
      title: item.name,
      sub: `${item.customerName} · 当前阶段 ${item.stage} · 预计金额 ¥${Number(item.amount).toLocaleString('zh-CN')}`,
      owner: item.owner,
      deadline: item.expectDate,
      priority: Number(item.amount) >= 500000 ? '高' : Number(item.amount) >= 200000 ? '中' : '低',
      overdue: item.expectDate ? item.expectDate < today : false,
      path: '/sales/opportunity'
    }))

  // 3. 待回款订单
  const payTasks = orders.value
    .filter((item) => item.status !== '已取消' && item.payStatus !== '已付款')
    .map((item) => ({
      title: `${item.customerName} · ${item.productName}`,
      sub: `订单 ${item.orderNo} · ${item.payStatus} · 金额 ¥${Number(item.amount).toLocaleString('zh-CN')}`,
      owner: item.owner,
      deadline: item.orderDate,
      priority: item.payStatus === '未付款' ? '高' : '中',
      overdue: item.orderDate ? item.orderDate < today : false,
      path: '/delivery/receivable'
    }))

  // 4. 售后工单
  const ticketTasks = (extra.value.tickets || [])
    .filter((item) => item.status === '待受理' || item.status === '处理中')
    .map((item) => ({
      title: item.title,
      sub: `${item.customerName} · ${item.ticketNo} · ${item.type}`,
      owner: item.assignee,
      deadline: String(item.createTime || '').slice(0, 10),
      priority: item.priority,
      overdue: String(item.createTime || '').slice(0, 10) < today && item.priority === '高',
      path: '/service/ticket'
    }))

  // 5. 待审核退换货
  const returnTasks = (extra.value.returns || [])
    .filter((item) => item.status === '待审核')
    .map((item) => ({
      title: `${item.customerName} · ${item.type}`,
      sub: `${item.returnNo} · 原因：${item.reason}`,
      owner: item.owner,
      deadline: String(item.createTime || '').slice(0, 10),
      priority: '中',
      overdue: false,
      path: '/sales/return'
    }))

  // 6. 逾期应收
  const ARTasks = (extra.value.receivables || [])
    .filter((item) => item.status === '已逾期')
    .map((item) => ({
      title: `${item.customerName} 应收款催收`,
      sub: `订单 ${item.orderNo} · 未收 ¥${Number(item.totalAmount - item.receivedAmount).toLocaleString('zh-CN')}`,
      owner: item.owner,
      deadline: item.dueDate,
      priority: '高',
      overdue: true,
      path: '/delivery/receivable'
    }))

  return {
    lead: leadTasks,
    opp: oppTasks,
    pay: payTasks,
    ticket: ticketTasks,
    ret: returnTasks,
    ar: ARTasks
  }
})

const tabs = [
  { key: 'lead', title: '线索跟进', tag: 'primary' },
  { key: 'opp', title: '商机推进', tag: 'warning' },
  { key: 'ar', title: '应收催收', tag: 'danger' },
  { key: 'pay', title: '待回款', tag: 'success' },
  { key: 'ticket', title: '售后工单', tag: 'info' },
  { key: 'ret', title: '退换审核', tag: 'info' }
]

function tabRows(key) {
  const today = todayKey()
  let list = FS.value[key] || []
  if (filterType.value === 'today') list = list.filter((item) => item.deadline === today)
  if (filterType.value === 'overdue') list = list.filter((item) => item.overdue)
  return list
}

const summary = computed(() => {
  const all = Object.values(FS.value).flat()
  const today = todayKey()
  return [
    {
      label: '待办总数',
      value: all.length,
      icon: 'List',
      tone: 'primary'
    },
    {
      label: '今日到期',
      value: all.filter((item) => item.deadline === today).length,
      icon: 'AlarmClock',
      tone: 'warning'
    },
    {
      label: '已逾期',
      value: all.filter((item) => item.overdue).length,
      icon: 'WarningFilled',
      tone: 'danger'
    },
    {
      label: '高优先级',
      value: all.filter((item) => item.priority === '高').length,
      icon: 'Flag',
      tone: 'violet'
    }
  ]
})
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 6px;
}

.summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 16px;
  border: 1px solid #eef1f8;
  border-radius: var(--crm-radius);
  background: linear-gradient(180deg, #fbfcff, #f6f8fd);
}

.s-icon {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  border-radius: 9px;
  font-size: 16px;
  color: #fff;
}

.summary.tone-primary .s-icon {
  background: linear-gradient(135deg, #2f6bff, #6b93ff);
}

.summary.tone-warning .s-icon {
  background: linear-gradient(135deg, #f5a623, #ffc36b);
}

.summary.tone-danger .s-icon {
  background: linear-gradient(135deg, #f2637b, #ff8fa3);
}

.summary.tone-violet .s-icon {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
}

.s-body {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.s-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--crm-text);
}

.s-label {
  font-size: 11px;
  color: #a3aebd;
}

.todo-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tab-label :deep(.el-badge__content) {
  transform: translateY(-6px);
}

.cell-main {
  font-size: 13px;
}

.cell-sub {
  font-size: 12px;
  color: var(--crm-text-secondary);
  margin-top: 2px;
}

.overdue {
  color: #f2637b;
  font-weight: 600;
}

.gap-left {
  margin-left: 6px;
}

@media (max-width: 1000px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
