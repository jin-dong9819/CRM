<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          线索管理
          <small>记录潜在客户信息，跟进转化后成为正式客户</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增线索</el-button>
      </div>

      <!-- 搜索区 -->
      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="线索名称 / 公司 / 联系人 / 手机号"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="意向等级">
          <el-select v-model="query.level" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in LEAD_LEVEL" :key="item" :label="item + ' 类'" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in LEAD_STATUS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="query.source" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in LEAD_SOURCE" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 条线索</span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="leadNo" label="线索编号" width="115">
          <template #default="{ row }">
            <span class="mono">{{ row.leadNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="线索名称 / 公司" min-width="200">
          <template #default="{ row }">
            <div class="cell-main">{{ row.name }}</div>
            <div class="cell-sub">{{ row.company }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="85" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column label="意向" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="LEAD_LEVEL_TAG[row.level] || 'info'" effect="light" size="small">
              {{ row.level }} 类
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="负责人" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="LEAD_STATUS_TAG[row.status] || 'info'" effect="light" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextFollowTime" label="下次跟进" width="110" />
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

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑线索' : '新增线索'"
      width="680px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="线索编号">
              <el-input v-model="form.leadNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="线索名称" prop="name">
              <el-input v-model="form.name" placeholder="如：顺达汽修厂·玻璃水询价" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称" prop="company">
              <el-input v-model="form.company" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="form.contact" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="线索来源" prop="source">
              <el-select v-model="form.source" placeholder="请选择来源" style="width: 100%">
                <el-option v-for="item in LEAD_SOURCE" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="意向等级" prop="level">
              <el-radio-group v-model="form.level">
                <el-radio-button v-for="item in LEAD_LEVEL" :key="item" :value="item">
                  {{ item }} 类
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="owner">
              <el-input v-model="form.owner" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="item in LEAD_STATUS" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次跟进" prop="nextFollowTime">
              <el-date-picker
                v-model="form.nextFollowTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 100%"
              />
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
                placeholder="记录客户需求、沟通要点等"
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
import { getLeadPage, createLead, updateLead, deleteLead } from '@/api/business'
import {
  LEAD_STATUS,
  LEAD_STATUS_TAG,
  LEAD_SOURCE,
  LEAD_LEVEL,
  LEAD_LEVEL_TAG
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
} = useTable(getLeadPage, { status: '', source: '', level: '' })

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()

const defaultForm = {
  leadNo: '',
  name: '',
  company: '',
  contact: '',
  phone: '',
  source: '官网表单',
  level: 'B',
  owner: '',
  status: '未跟进',
  nextFollowTime: '',
  remark: ''
}

const form = reactive({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入线索名称', trigger: 'blur' }],
  company: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  source: [{ required: true, message: '请选择线索来源', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, defaultForm)
  currentId.value = null
  isEdit.value = false
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  isEdit.value = false
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
      await updateLead(currentId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createLead({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误已统一提示
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除线索「${row.name}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteLead(row.id)
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
