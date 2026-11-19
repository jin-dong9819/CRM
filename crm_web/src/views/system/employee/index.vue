<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          员工管理
          <small>维护员工账号、岗位与所属部门，员工编号即登录账号</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增员工</el-button>
      </div>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="员工编号 / 姓名 / 手机号 / 邮箱"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="query.deptId"
            :data="deptTree"
            :props="treeProps"
            check-strictly
            clearable
            placeholder="全部"
            style="width: 170px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 名员工</span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column label="员工" width="200">
          <template #default="{ row }">
            <div class="emp-cell">
              <el-avatar :size="32" :src="row.avatar || undefined">{{ row.name?.charAt(0) }}</el-avatar>
              <div class="emp-meta">
                <div class="emp-name">{{ row.name }}</div>
                <div class="emp-no mono">{{ row.empNo }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="genderLabel" label="性别" width="60" align="center">
          <template #default="{ row }">{{ row.genderLabel || '未知' }}</template>
        </el-table-column>
        <el-table-column prop="deptName" label="所属部门" width="125" show-overflow-tooltip />
        <el-table-column prop="jobTitle" label="职位" width="115" show-overflow-tooltip>
          <template #default="{ row }">{{ row.jobTitle || '—' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="125" />
        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-tag
              v-for="name in splitRoles(row.roleNames)"
              :key="name"
              effect="plain"
              size="small"
              class="role-tag"
            >
              {{ name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.id === userStore.userInfo?.id"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="hireDate" label="入职日期" width="105">
          <template #default="{ row }">{{ row.hireDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="145" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
              <el-button link type="primary">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="reset">重置密码</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除员工</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 员工详情：把表里不常展示的字段（邮箱、备注、登录信息）集中在这里 -->
    <el-drawer v-model="detailVisible" title="员工详情" size="440px">
      <div class="detail-head">
        <el-avatar :size="56" :src="detail.avatar || undefined">{{ detail.name?.charAt(0) }}</el-avatar>
        <div class="detail-head-text">
          <div class="detail-name">
            {{ detail.name }}
            <el-tag :type="detail.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ detail.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </div>
          <div class="detail-sub">{{ detail.jobTitle || '未设置职位' }} · {{ detail.deptName || '未分配部门' }}</div>
        </div>
      </div>

      <div class="detail-section">基本信息</div>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="员工编号">{{ detail.empNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.genderLabel || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email || '—' }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detail.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ detail.jobTitle || '—' }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ detail.hireDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag
            v-for="name in splitRoles(detail.roleNames)"
            :key="name"
            effect="plain"
            size="small"
            class="role-tag"
          >
            {{ name }}
          </el-tag>
          <span v-if="!detail.roleNames">—</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">账号信息</div>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="账号状态">
          {{ detail.status === 1 ? '启用' : '停用' }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ detail.lastLoginTime || '从未登录' }}</el-descriptions-item>
        <el-descriptions-item label="登录 IP">{{ formatIp(detail.lastLoginIp) }}</el-descriptions-item>
        <el-descriptions-item label="密码更新">{{ detail.pwdUpdateTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">备注</div>
      <div class="detail-remark">{{ detail.remark || '暂无备注' }}</div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button @click="handleCommand('reset', detail)">重置密码</el-button>
        <el-button type="primary" @click="openEdit(detail)">编辑</el-button>
      </template>
    </el-drawer>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑员工' : '新增员工'"
      width="700px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工编号" prop="empNo">
              <el-input v-model.trim="form.empNo" placeholder="如 E1009" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="0">未知</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="treeProps"
                check-strictly
                default-expand-all
                placeholder="请选择部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位">
              <el-input v-model="form.jobTitle" placeholder="如：华东区域经理" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleIds">
              <el-select
                v-model="form.roleIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="可分配多个角色"
                style="width: 100%"
              >
                <el-option
                  v-for="item in roleOptions"
                  :key="item.id"
                  :label="item.roleName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker
                v-model="form.hireDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账号状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="头像">
              <div class="avatar-row">
                <el-avatar :size="36" :src="form.avatar || undefined">{{ form.name?.charAt(0) || '头' }}</el-avatar>
                <el-input v-model="form.avatar" placeholder="头像图片地址，留空则显示姓名首字" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert
          v-if="!isEdit"
          type="info"
          :closable="false"
          show-icon
          title="新建员工默认密码为 123456，请提醒员工首次登录后修改。"
        />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useTable } from '@/composables/useTable'
import { useUserStore } from '@/stores/user'
import {
  getEmployeePage,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  changeEmployeeStatus,
  resetEmployeePassword,
  getDeptTree,
  getAllRoles
} from '@/api/system'

const userStore = useUserStore()

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
} = useTable(getEmployeePage, { deptId: '', status: '' })

const dialogVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()
const deptTree = ref([])
const roleOptions = ref([])
const detail = ref({})
const treeProps = { label: 'deptName', value: 'id', children: 'children' }

const defaultForm = {
  empNo: '',
  name: '',
  gender: 1,
  phone: '',
  email: '',
  avatar: '',
  deptId: null,
  roleIds: [],
  jobTitle: '',
  hireDate: '',
  status: 1,
  remark: ''
}

const form = reactive({ ...defaultForm })

/** 后端返回的角色名用「、」拼接，这里拆成多个标签展示 */
const splitRoles = (names) => (names ? names.split('、') : [])

/** 本机登录时后端拿到的是 IPv6 回环地址，展示成 127.0.0.1 更直观 */
const formatIp = (ip) => (ip ? ip.replace('0:0:0:0:0:0:0:1', '127.0.0.1').replace('::1', '127.0.0.1') : '—')

const rules = {
  empNo: [
    { required: true, message: '请输入员工编号', trigger: 'blur' },
    { pattern: /^[A-Za-z]\d{3,9}$/, message: '员工编号格式如 E1009', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  roleIds: [{ type: 'array', required: true, message: '请至少选择一个角色', trigger: 'change' }]
}

async function loadOptions() {
  const [depts, roles] = await Promise.all([getDeptTree(), getAllRoles()])
  deptTree.value = depts || []
  roleOptions.value = roles || []
}

onMounted(loadOptions)

function resetForm() {
  Object.assign(form, defaultForm, { roleIds: [] })
  currentId.value = null
  isEdit.value = false
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

function openEdit(row) {
  Object.assign(form, defaultForm, row, {
    roleIds: [...(row.roleIds || [])],
    hireDate: row.hireDate || ''
  })
  currentId.value = row.id
  isEdit.value = true
  detailVisible.value = false
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      empNo: form.empNo.toUpperCase(),
      name: form.name,
      gender: form.gender,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar || '',
      deptId: form.deptId,
      roleIds: form.roleIds,
      jobTitle: form.jobTitle,
      hireDate: form.hireDate || null,
      status: form.status,
      remark: form.remark
    }
    if (isEdit.value) {
      await updateEmployee(currentId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createEmployee(payload)
      ElMessage.success('新增成功，初始密码 123456')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row) {
  try {
    await changeEmployeeStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用该员工账号' : '已停用该员工账号')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

function handleCommand(cmd, row) {
  if (cmd === 'reset') return handleResetPassword(row)
  if (cmd === 'delete') return handleDelete(row)
}

async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `将「${row.name}（${row.empNo}）」的登录密码重置为 123456？`,
      '重置密码',
      { type: 'warning', confirmButtonText: '重置', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await resetEmployeePassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除员工「${row.name}（${row.empNo}）」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteEmployee(row.id)
  ElMessage.success('删除成功')
  detailVisible.value = false
  fetchData()
}
</script>

<style scoped>
.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.emp-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 无头像时用姓名首字兜底：浅蓝底 + 主色字，避免白字落在浅灰底上看不见 */
.emp-cell :deep(.el-avatar),
.detail-head :deep(.el-avatar),
.avatar-row :deep(.el-avatar) {
  background: #e8eefc;
  color: var(--el-color-primary);
  font-weight: 600;
  flex-shrink: 0;
}

.emp-meta {
  line-height: 1.35;
}

.emp-name {
  font-size: 14px;
  color: var(--crm-text-primary);
}

.emp-no {
  font-size: 12px;
  color: var(--crm-text-secondary);
}

.role-tag {
  margin: 2px 4px 2px 0;
}

.detail-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.detail-name {
  font-size: 17px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.detail-section {
  margin: 18px 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--crm-text-secondary);
}

.detail-remark {
  font-size: 13px;
  line-height: 1.7;
  color: var(--crm-text-primary);
  white-space: pre-wrap;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
</style>
