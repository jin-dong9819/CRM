<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          订单管理
          <small>查看合同订单的付款与交付进度</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建订单</el-button>
      </div>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="订单号 / 客户 / 产品"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in ORDER_STATUS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款状态">
          <el-select v-model="query.payStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in PAY_STATUS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 笔订单</span>
          <el-divider direction="vertical" />
          <span class="total-text">
            当前页金额合计：<b class="amount">{{ formatMoney(pageAmount) }}</b>
          </span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="115">
          <template #default="{ row }">
            <span class="mono">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="150" show-overflow-tooltip />
        <el-table-column prop="productName" label="订购产品" min-width="170" show-overflow-tooltip />
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">
            <span class="amount">{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="付款状态" width="100">
          <template #default="{ row }">
            <el-tag :type="PAY_STATUS_TAG[row.payStatus] || 'info'" effect="light" size="small">
              {{ row.payStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="ORDER_STATUS_TAG[row.status] || 'info'" effect="light" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="下单日期" width="105" />
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
      :title="isEdit ? '编辑订单' : '新建订单'"
      width="680px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单编号">
              <el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
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
            <el-form-item label="订购产品" prop="productName">
              <el-select
                v-model="form.productName"
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入产品"
                style="width: 100%"
              >
                <el-option v-for="item in PRODUCTS" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单金额" prop="amount">
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
            <el-form-item label="下单日期" prop="orderDate">
              <el-date-picker
                v-model="form.orderDate"
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
          <el-col :span="12">
            <el-form-item label="付款状态" prop="payStatus">
              <el-select v-model="form.payStatus" style="width: 100%">
                <el-option v-for="item in PAY_STATUS" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="item in ORDER_STATUS" :key="item" :label="item" :value="item" />
              </el-select>
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
                placeholder="交付要求、发票信息等"
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
import { getOrderPage, createOrder, updateOrder, deleteOrder, getCustomerPage } from '@/api/business'
import { ORDER_STATUS, ORDER_STATUS_TAG, PAY_STATUS, PAY_STATUS_TAG, formatMoney } from '@/utils/dict'

const PRODUCTS = ['玻璃水 -25℃', '化油器清洗剂', '机油滤清器', '三滤保养套装', '无骨雨刮片（对装）', '防冻液 -35℃', '半合成机油 10W-40', '标准洗车']

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
} = useTable(getOrderPage, { status: '', payStatus: '' })

const pageAmount = computed(() => list.value.reduce((sum, row) => sum + Number(row.amount || 0), 0))

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()
const customerOptions = ref([])

const today = new Date()
const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

const defaultForm = {
  orderNo: '',
  customerId: null,
  customerName: '',
  productName: PRODUCTS[0],
  amount: 36000,
  payStatus: '未付款',
  status: '待审核',
  orderDate: todayStr,
  owner: '',
  remark: ''
}

const form = reactive({ ...defaultForm })

const rules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  productName: [{ required: true, message: '请选择订购产品', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入订单金额', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (Number(value) <= 0) callback(new Error('订单金额需大于 0'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  orderDate: [{ required: true, message: '请选择下单日期', trigger: 'change' }],
  payStatus: [{ required: true, message: '请选择付款状态', trigger: 'change' }],
  status: [{ required: true, message: '请选择订单状态', trigger: 'change' }]
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
  Object.assign(form, defaultForm, { orderDate: todayStr })
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
      await updateOrder(currentId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createOrder({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除订单「${row.orderNo}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteOrder(row.id)
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
