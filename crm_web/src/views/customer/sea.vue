<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          客户公海
          <small>长期未跟进或主动释放的客户沉淀在此，可重新领取跟进</small>
        </h2>
        <div class="header-actions">
          <el-button :icon="Refresh" @click="reload">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openRelease">释放客户到公海</el-button>
        </div>
      </div>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="客户名称 / 联系人 / 手机号"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="query.industry" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in CUSTOMER_INDUSTRY" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="释放原因">
          <el-select v-model="query.reason" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in SEA_REASON" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in SEA_STATUS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 家客户</span>
          <el-tag type="warning" effect="plain" size="small">待领取 {{ pendingCount }} 家</el-tag>
        </div>
      </div>

      <el-table :data="pageList" border stripe style="width: 100%" empty-text="暂无公海客户">
        <el-table-column prop="name" label="客户名称" min-width="160">
          <template #default="{ row }">
            <div class="cell-main">{{ row.name }}</div>
            <div class="cell-sub">{{ row.industry }}</div>
          </template>
        </el-table-column>
        <el-table-column label="客户等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="CUSTOMER_LEVEL_TAG[row.level] || 'info'" effect="light" size="small">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="90" />
        <el-table-column prop="phone" label="手机号" width="125">
          <template #default="{ row }">
            <span class="mono">{{ row.phone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="释放原因" min-width="130" />
        <el-table-column prop="releaseDate" label="释放日期" width="115" />
        <el-table-column prop="lastOwner" label="原负责人" width="95" />
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="SEA_STATUS_TAG[row.status] || 'info'" effect="light" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待领取'"
              link
              type="primary"
              @click="handleClaim(row)"
            >
              领取
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 释放到公海 -->
    <el-dialog v-model="dialogVisible" title="释放客户到公海" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="行业" prop="industry">
          <el-select v-model="form.industry" placeholder="请选择行业" style="width: 100%">
            <el-option v-for="item in CUSTOMER_INDUSTRY" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级" prop="level">
          <el-select v-model="form.level" style="width: 100%">
            <el-option v-for="item in CUSTOMER_LEVEL" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="释放原因" prop="reason">
          <el-select v-model="form.reason" style="width: 100%">
            <el-option v-for="item in SEA_REASON" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="原负责人">
          <el-input v-model="form.lastOwner" placeholder="请输入原负责人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认释放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getExtraDb, persistExtra, nextExtraId } from '@/mock/extraDb'
import {
  CUSTOMER_INDUSTRY,
  CUSTOMER_LEVEL,
  CUSTOMER_LEVEL_TAG,
  SEA_REASON,
  SEA_STATUS,
  SEA_STATUS_TAG
} from '@/utils/dict'

const store = getExtraDb()
const tick = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  industry: '',
  reason: '',
  status: ''
})

const rows = computed(() => {
  tick.value
  return [...(store.seaCustomers || [])]
})

const filtered = computed(() => {
  const kw = String(query.keyword || '').trim().toLowerCase()
  return rows.value.filter((row) => {
    if (query.industry && row.industry !== query.industry) return false
    if (query.reason && row.reason !== query.reason) return false
    if (query.status && row.status !== query.status) return false
    if (!kw) return true
    return ['name', 'contact', 'phone', 'industry'].some((f) =>
      String(row[f] ?? '').toLowerCase().includes(kw)
    )
  })
})

const total = computed(() => filtered.value.length)

const pageList = computed(() => {
  const start = (query.page - 1) * query.pageSize
  return filtered.value.slice(start, start + query.pageSize)
})

const pendingCount = computed(() => rows.value.filter((item) => item.status === '待领取').length)

function reload() {
  tick.value += 1
}

function handleSearch() {
  query.page = 1
}

function handleReset() {
  Object.assign(query, { page: 1, pageSize: query.pageSize, keyword: '', industry: '', reason: '', status: '' })
}

function handleSizeChange(size) {
  query.pageSize = size
  query.page = 1
}

async function handleClaim(row) {
  try {
    await ElMessageBox.confirm(`确认领取客户「${row.name}」？领取后将转入客户档案跟进。`, '领取确认', {
      type: 'info',
      confirmButtonText: '领取',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  row.status = '已领取'
  persistExtra()
  tick.value += 1
  ElMessage.success(`已领取「${row.name}」，请到客户档案继续跟进`)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定从公海删除「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  store.seaCustomers = store.seaCustomers.filter((item) => item.id !== row.id)
  persistExtra()
  tick.value += 1
  ElMessage.success('删除成功')
}

/* ---------- 释放到公海 ---------- */

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  name: '',
  industry: '互联网',
  level: '普通客户',
  contact: '',
  phone: '',
  reason: '长期未跟进',
  lastOwner: ''
})

const rules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  industry: [{ required: true, message: '请选择行业', trigger: 'change' }],
  level: [{ required: true, message: '请选择客户等级', trigger: 'change' }],
  reason: [{ required: true, message: '请选择释放原因', trigger: 'change' }]
}

function openRelease() {
  Object.assign(form, {
    name: '',
    industry: '互联网',
    level: '普通客户',
    contact: '',
    phone: '',
    reason: '长期未跟进',
    lastOwner: ''
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const d = new Date()
    const p = (v) => String(v).padStart(2, '0')
    store.seaCustomers.unshift({
      ...form,
      id: nextExtraId(store.seaCustomers),
      releaseDate: `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`,
      status: '待领取',
      createTime: d.toLocaleString('zh-CN', { hour12: false })
    })
    persistExtra()
    tick.value += 1
    dialogVisible.value = false
    ElMessage.success('已释放到公海')
    query.page = 1
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.table-toolbar .left {
  align-items: center;
}

.cell-main {
  font-size: 13px;
  color: var(--crm-text);
}

.cell-sub {
  font-size: 12px;
  color: var(--crm-text-secondary);
  margin-top: 2px;
}
</style>
