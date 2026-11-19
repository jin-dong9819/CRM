<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          部门管理
          <small>维护多级组织架构，部门信息会同步到员工档案与数据权限</small>
        </h2>
        <div class="header-actions">
          <el-button :icon="Sort" @click="toggleExpand">
            {{ expandAll ? '全部折叠' : '全部展开' }}
          </el-button>
          <el-button type="primary" :icon="Plus" @click="openCreate(null)">新增部门</el-button>
        </div>
      </div>

      <el-form :model="filters" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="部门名称 / 编码 / 负责人"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 110px">
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
          <span class="total-text">共 {{ matchedCount }} 个部门</span>
          <el-divider direction="vertical" />
          <span class="total-text">
            启用 {{ enabledCount }} 个 · 停用 {{ matchedCount - enabledCount }} 个
          </span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table
        :key="tableKey"
        v-loading="loading"
        :data="tableData"
        row-key="id"
        border
        :default-expand-all="expandAll"
        :tree-props="{ children: 'children' }"
        style="width: 100%"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="190">
          <template #default="{ row }">
            <span class="dept-name">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ row.deptName }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="deptCode" label="部门编码" width="110">
          <template #default="{ row }">
            <span class="mono">{{ row.deptCode || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="leader" label="负责人" width="90">
          <template #default="{ row }">{{ row.leader || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="120">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="部门邮箱" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="172" />
        <el-table-column label="操作" width="185" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCreate(row)">新增下级</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑部门' : '新增部门'"
      width="580px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            node-key="id"
            check-strictly
            default-expand-all
            placeholder="请选择上级部门"
            style="width: 100%"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" placeholder="如：华东销售二部" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门编码" prop="deptCode">
              <el-input v-model.trim="form.deptCode" placeholder="如：D112" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="leader">
              <el-input v-model="form.leader" placeholder="请输入负责人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="手机号或座机" maxlength="13" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门邮箱" prop="email">
              <el-input v-model="form.email" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number
                v-model="form.sort"
                :min="1"
                :max="999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
              <span class="form-tip">停用后不影响历史数据，仅用于标识</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-alert
          v-if="!isEdit"
          type="info"
          :closable="false"
          show-icon
          title="组织层级最多 5 级，创建后可继续用「新增下级」往下扩展。"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search, Sort, OfficeBuilding } from '@element-plus/icons-vue'
import { getDeptTree, createDept, updateDept, deleteDept, changeDeptStatus } from '@/api/system'

/* ---------- 列表 ---------- */
const loading = ref(false)
const treeData = ref([])
const tableKey = ref(0)
const expandAll = ref(true)

const filters = reactive({ keyword: '', status: '' })
const applied = reactive({ keyword: '', status: '' })

/** 部门树是整棵返回，筛选在前端做；命中节点会保留其祖先链路，但计数只算命中项 */
const filtered = computed(() => {
  const kw = applied.keyword.toLowerCase()
  const status = applied.status

  if (!kw && status === '') {
    const allCount = countNodes(treeData.value)
    return { nodes: treeData.value, count: allCount, enabled: countEnabled(treeData.value) }
  }

  let count = 0
  let enabled = 0
  const walk = (nodes) => {
    const result = []
    for (const node of nodes) {
      const text = `${node.deptName || ''}${node.deptCode || ''}${node.leader || ''}`.toLowerCase()
      const hit = (!kw || text.includes(kw)) && (status === '' || node.status === status)
      if (hit) {
        count += 1
        if (node.status === 1) enabled += 1
        result.push(node)
        continue
      }
      const children = walk(node.children || [])
      if (children.length) result.push({ ...node, children })
    }
    return result
  }
  const nodes = walk(treeData.value)
  return { nodes, count, enabled }
})

const tableData = computed(() => filtered.value.nodes)
const matchedCount = computed(() => filtered.value.count)
const enabledCount = computed(() => filtered.value.enabled)

function countNodes(nodes) {
  return nodes.reduce((sum, node) => sum + 1 + countNodes(node.children || []), 0)
}

function countEnabled(nodes) {
  return nodes.reduce(
    (sum, node) => sum + (node.status === 1 ? 1 : 0) + countEnabled(node.children || []),
    0
  )
}

async function fetchData() {
  loading.value = true
  try {
    treeData.value = (await getDeptTree()) || []
  } catch {
    treeData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  applied.keyword = filters.keyword.trim()
  applied.status = filters.status
}

function handleReset() {
  filters.keyword = ''
  filters.status = ''
  handleSearch()
}

/** 通过重建 key 切换展开/折叠 */
function toggleExpand() {
  expandAll.value = !expandAll.value
  tableKey.value += 1
}

onMounted(fetchData)

/* ---------- 新增 / 编辑 ---------- */
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()

const defaultForm = {
  parentId: 0,
  deptName: '',
  deptCode: '',
  leader: '',
  phone: '',
  email: '',
  sort: 1,
  status: 1
}

const form = reactive({ ...defaultForm })

/** 上级部门候选：编辑时排除自己及自己的子树，避免形成环 */
const parentOptions = computed(() => {
  const excludeId = currentId.value
  const prune = (nodes) =>
    nodes
      .filter((node) => node.id !== excludeId)
      .map((node) => (node.children ? { ...node, children: prune(node.children) } : node))
  return [{ id: 0, deptName: '顶级部门', children: prune(treeData.value) }]
})

const rules = {
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [
    { pattern: /^[A-Za-z0-9-]{2,20}$/, message: '2~20 位字母、数字或短横线', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/, message: '电话格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

function resetForm() {
  Object.assign(form, defaultForm)
  currentId.value = null
  isEdit.value = false
  formRef.value?.clearValidate()
}

function openCreate(parent) {
  resetForm()
  form.parentId = parent ? parent.id : 0
  form.sort = (parent?.children?.length || treeData.value.length) + 1
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, defaultForm, {
    parentId: row.parentId,
    deptName: row.deptName,
    deptCode: row.deptCode || '',
    leader: row.leader || '',
    phone: row.phone || '',
    email: row.email || '',
    sort: row.sort ?? 1,
    status: row.status ?? 1
  })
  currentId.value = row.id
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      parentId: form.parentId ?? 0,
      deptName: form.deptName,
      deptCode: form.deptCode || null,
      leader: form.leader,
      phone: form.phone,
      email: form.email,
      sort: form.sort,
      status: form.status
    }
    if (isEdit.value) {
      await updateDept(currentId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createDept(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row) {
  const target = row.status
  try {
    await changeDeptStatus(row.id, target)
    ElMessage.success(target === 1 ? '部门已启用' : '部门已停用')
    fetchData()
  } catch {
    row.status = target === 1 ? 0 : 1
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除部门「${row.deptName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteDept(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 8px;
}

.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.dept-name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.dept-name .el-icon {
  color: #7c8da3;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--crm-text-secondary);
}
</style>
