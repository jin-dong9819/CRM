<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          商机管理
          <small>跟踪销售漏斗，掌握每个项目的阶段与预计成交时间</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增商机</el-button>
      </div>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="商机名称 / 编号 / 客户"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="query.stage" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in OPP_STAGE" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 个商机</span>
          <el-divider direction="vertical" />
          <span class="total-text">
            当前页金额合计：<b class="amount">{{ formatMoney(pageAmount) }}</b>
          </span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="oppNo" label="商机编号" width="115">
          <template #default="{ row }">
            <span class="mono">{{ row.oppNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商机名称" min-width="210" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户" min-width="130" show-overflow-tooltip />
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">
            <span class="amount">{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="阶段" width="100">
          <template #default="{ row }">
            <el-tag :type="OPP_STAGE_TAG[row.stage] || 'info'" effect="light" size="small">
              {{ row.stage }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="赢率" width="130">
          <template #default="{ row }">
            <el-progress
              :percentage="Number(row.probability) || 0"
              :stroke-width="8"
              :color="progressColor(row.probability)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="expectDate" label="预计成交" width="105" />
        <el-table-column prop="owner" label="负责人" width="80" />
        <el-table-column label="操作" width="105" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商机' : '新增商机'"
      width="680px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商机编号">
              <el-input v-model="form.oppNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商机名称" prop="name">
              <el-input v-model="form.name" placeholder="如：顺达汽修厂·易损件年度供货" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联客户" prop="customerId">
              <el-select
                v-model="form.customerId"
                filterable
                placeholder="请选择客户"
                style="width: 100%"
                @change="onCustomerChange"
              >
                <el-option
                  v-for="item in customerOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商机金额" prop="amount">
              <el-input-number
                v-model="form.amount"
                :min="0"
                :step="1000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售阶段" prop="stage">
              <el-select v-model="form.stage" style="width: 100%" @change="onStageChange">
                <el-option v-for="item in OPP_STAGE" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赢率(%)" prop="probability">
              <el-slider v-model="form.probability" :step="5" show-input :show-input-controls="false" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计成交" prop="expectDate">
              <el-date-picker
                v-model="form.expectDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="owner">
              <el-input v-model="form.owner" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                maxlength="200"
                show-word-limit
                placeholder="项目背景、竞品情况等"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useTable } from '@/composables/useTable'
import {
  getOpportunityPage,
  createOpportunity,
  updateOpportunity,
  deleteOpportunity,
  getCustomerPage
} from '@/api/business'
import { OPP_STAGE, OPP_STAGE_TAG, formatMoney } from '@/utils/dict'

const {
  loading,
  list,
  total,
  query,
  fetchData,
  handleSearch,
  handleReset,
  handlePageChange,
  handleSizeChange
} = useTable(getOpportunityPage, { stage: '' })

const pageAmount = computed(() => list.value.reduce((sum, row) => sum + Number(row.amount || 0), 0))

const progressColor = (value) => {
  const num = Number(value) || 0
  if (num >= 100) return '#2ba471'
  if (num >= 60) return '#e6a23c'
  if (num > 0) return '#2f6bff'
  return '#c0c7d0'
}

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()
const customerOptions = ref([])

const defaultForm = {
  oppNo: '',
  name: '',
  customerId: null,
  customerName: '',
  amount: 50000,
  stage: '初步接洽',
  probability: 10,
  expectDate: '',
  owner: '',
  remark: ''
}

const form = reactive({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入商机名称', trigger: 'blur' }],
  customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入商机金额', trigger: 'blur' }],
  stage: [{ required: true, message: '请选择销售阶段', trigger: 'change' }],
  expectDate: [{ required: true, message: '请选择预计成交日期', trigger: 'change' }]
}

/** 阶段变化时给出默认赢率 */
function onStageChange(value) {
  const map = { 初步接洽: 10, 需求确认: 30, 方案报价: 50, 商务谈判: 70, 赢单: 100, 输单: 0 }
  form.probability = map[value] ?? form.probability
}

function onCustomerChange(id) {
  const target = customerOptions.value.find((item) => item.id === id)
  form.customerName = target?.name || ''
}

async function loadCustomers() {
  if (customerOptions.value.length) return
  const data = await getCustomerPage({ page: 1, pageSize: 200 })
  customerOptions.value = data?.list || []
}

function resetForm() {
  Object.assign(form, defaultForm)
  currentId.value = null
  isEdit.value = false
  formRef.value?.clearValidate()
}

async function openCreate() {
  resetForm()
  await loadCustomers()
  dialogVisible.value = true
}

async function openEdit(row) {
  Object.assign(form, defaultForm, row)
  currentId.value = row.id
  isEdit.value = true
  await loadCustomers()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateOpportunity(currentId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createOpportunity({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商机「${row.name}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteOpportunity(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}
</style>
