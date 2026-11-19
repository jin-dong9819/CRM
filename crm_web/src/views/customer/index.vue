<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          客户管理
          <small>维护已建档客户，查看负责人与合作状态</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增客户</el-button>
      </div>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="客户名称 / 编号 / 联系人 / 电话"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="query.level" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in CUSTOMER_LEVEL" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="query.industry" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in CUSTOMER_INDUSTRY" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in CUSTOMER_STATUS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 个客户</span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="custNo" label="客户编号" width="115">
          <template #default="{ row }">
            <span class="mono">{{ row.custNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户名称 / 行业" min-width="170">
          <template #default="{ row }">
            <div class="cell-main">{{ row.name }}</div>
            <div class="cell-sub">{{ row.industry }}</div>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="CUSTOMER_LEVEL_TAG[row.level] || 'info'" effect="light" size="small">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="85" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="address" label="所在地区" min-width="130" show-overflow-tooltip />
        <el-table-column prop="owner" label="负责人" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="CUSTOMER_STATUS_TAG[row.status] || 'info'" effect="light" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="建档时间" width="165" />
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
      :title="isEdit ? '编辑客户' : '新增客户'"
      width="680px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户编号">
              <el-input v-model="form.custNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入客户公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-select v-model="form.industry" placeholder="请选择行业" style="width: 100%">
                <el-option v-for="item in CUSTOMER_INDUSTRY" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户等级" prop="level">
              <el-select v-model="form.level" style="width: 100%">
                <el-option v-for="item in CUSTOMER_LEVEL" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="form.contact" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="手机号或座机" maxlength="13" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="owner">
              <el-input v-model="form.owner" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合作状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="item in CUSTOMER_STATUS" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所在地区">
              <el-input v-model="form.address" placeholder="如：上海市浦东新区" />
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
                placeholder="客户偏好、合作历史等"
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useTable } from '@/composables/useTable'
import { getCustomerPage, createCustomer, updateCustomer, deleteCustomer } from '@/api/business'
import {
  CUSTOMER_LEVEL,
  CUSTOMER_LEVEL_TAG,
  CUSTOMER_STATUS,
  CUSTOMER_STATUS_TAG,
  CUSTOMER_INDUSTRY
} from '@/utils/dict'

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
} = useTable(getCustomerPage, { level: '', status: '', industry: '' })

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()

const defaultForm = {
  custNo: '',
  name: '',
  industry: '互联网',
  level: '普通客户',
  contact: '',
  phone: '',
  address: '',
  owner: '',
  status: '跟进中',
  remark: ''
}

const form = reactive({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  industry: [{ required: true, message: '请选择所属行业', trigger: 'change' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/, message: '电话格式不正确', trigger: 'blur' }
  ],
  level: [{ required: true, message: '请选择客户等级', trigger: 'change' }],
  status: [{ required: true, message: '请选择合作状态', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, defaultForm)
  currentId.value = null
  isEdit.value = false
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, defaultForm, row)
  currentId.value = row.id
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateCustomer(currentId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createCustomer({ ...form })
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
    await ElMessageBox.confirm(`确定删除客户「${row.name}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteCustomer(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.cell-main {
  font-size: 13px;
  color: var(--crm-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-sub {
  font-size: 12px;
  color: var(--crm-text-secondary);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
