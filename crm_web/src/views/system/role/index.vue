<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          角色管理
          <small>定义角色编码与数据范围，员工绑定角色后生效</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增角色</el-button>
      </div>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="角色名称 / 编码"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 个角色</span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="roleCode" label="角色编码" width="180">
          <template #default="{ row }">
            <span class="mono">{{ row.roleCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column label="数据范围" width="160">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ row.dataScopeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="175" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              type="primary"
              :disabled="row.roleCode === 'ADMIN'"
              @click="openAssign(row)"
            >
              分配权限
            </el-button>
            <el-button link type="danger" :disabled="row.roleCode === 'ADMIN'" @click="handleDelete(row)">
              删除
            </el-button>
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
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model.trim="form.roleCode"
            placeholder="大写字母 + 下划线，如 SALES_LEAD"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="如：销售主管" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" style="width: 100%">
            <el-option
              v-for="item in DATA_SCOPE"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="该角色的职责说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限 -->
    <el-dialog
      v-model="permVisible"
      :title="`分配权限 · ${currentRole?.roleName || ''}`"
      width="560px"
      :close-on-click-modal="false"
    >
      <div class="perm-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>勾选该角色可访问的菜单与操作；勾选上级会连带选中下级。</span>
      </div>
      <div v-loading="permLoading" class="perm-tree-wrap">
        <el-tree
          ref="treeRef"
          :data="permTree"
          show-checkbox
          node-key="id"
          default-expand-all
          :props="{ label: 'permName', children: 'children' }"
          @check="syncCheckedCount"
        />
      </div>
      <template #footer>
        <span class="checked-count">已选 {{ checkedCount }} 项</span>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSaving" @click="handleAssignSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, InfoFilled } from '@element-plus/icons-vue'
import { useTable } from '@/composables/useTable'
import {
  getRolePage,
  createRole,
  updateRole,
  deleteRole,
  getPermissionTree,
  getRolePermissions,
  assignRolePermissions
} from '@/api/system'
import { DATA_SCOPE } from '@/utils/dict'

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
} = useTable(getRolePage)

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()

const defaultForm = {
  roleCode: '',
  roleName: '',
  dataScope: 4,
  remark: ''
}

const form = reactive({ ...defaultForm })

/* ---------- 分配权限 ---------- */
const permVisible = ref(false)
const permLoading = ref(false)
const permSaving = ref(false)
const permTree = ref([])
const currentRole = ref(null)
const treeRef = ref()
const checkedCount = ref(0)

async function openAssign(row) {
  currentRole.value = row
  permVisible.value = true
  permLoading.value = true
  try {
    if (!permTree.value.length) {
      permTree.value = (await getPermissionTree()) || []
    }
    const ids = (await getRolePermissions(row.id)) || []
    await nextTick()
    treeRef.value?.setCheckedKeys(ids, false)
    syncCheckedCount()
  } finally {
    permLoading.value = false
  }
}

function syncCheckedCount() {
  checkedCount.value = treeRef.value?.getCheckedKeys().length || 0
}

async function handleAssignSave() {
  if (!currentRole.value) return
  permSaving.value = true
  try {
    // 只提交全选中的节点，半选的父级由子级隐式表示
    const ids = treeRef.value?.getCheckedKeys() || []
    await assignRolePermissions(currentRole.value.id, ids)
    ElMessage.success('权限已保存')
    permVisible.value = false
    fetchData()
  } finally {
    permSaving.value = false
  }
}

const rules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]{1,29}$/, message: '仅支持大写字母、数字和下划线', trigger: 'blur' }
  ],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }]
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
    const payload = { ...form, roleCode: form.roleCode.toUpperCase() }
    if (isEdit.value) {
      await updateRole(currentId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createRole(payload)
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
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.perm-tip {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: #f7f9ff;
  border: 1px solid #dce6ff;
  color: #5b6b82;
  font-size: 12px;
  line-height: 1.6;
}

.perm-tip .el-icon {
  color: #2f6bff;
  margin-top: 2px;
}

.perm-tree-wrap {
  max-height: 380px;
  overflow: auto;
  border: 1px solid var(--crm-border);
  border-radius: 8px;
  padding: 8px 4px;
}

.checked-count {
  float: left;
  line-height: 32px;
  font-size: 12px;
  color: var(--crm-text-secondary);
}
</style>
