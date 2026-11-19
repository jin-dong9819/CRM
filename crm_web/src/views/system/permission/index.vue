<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          权限管理
          <small>维护目录 / 菜单 / 按钮三级权限，权限标识用于后端鉴权与前端按钮控制</small>
        </h2>
        <div class="header-actions">
          <el-button :icon="Sort" @click="toggleExpand">
            {{ expandAll ? '全部折叠' : '全部展开' }}
          </el-button>
          <el-button type="primary" :icon="Plus" @click="openCreate(null)">新增权限</el-button>
        </div>
      </div>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ stats.total }} 项权限</span>
          <el-divider direction="vertical" />
          <span class="total-text">启用 {{ stats.enabled }} 项 · 停用 {{ stats.total - stats.enabled }} 项</span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table
        :key="tableKey"
        v-loading="loading"
        :data="treeData"
        row-key="id"
        border
        :default-expand-all="expandAll"
        :tree-props="{ children: 'children' }"
        style="width: 100%"
      >
        <el-table-column prop="permName" label="权限名称" min-width="210">
          <template #default="{ row }">
            <span class="perm-name">
              <el-icon><component :is="iconOf(row)" /></el-icon>
              <span>{{ row.permName }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="permCode" label="权限标识" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="mono">{{ row.permCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="88" align="center">
          <template #default="{ row }">
            <el-tag :type="PERM_TYPE_TAG[row.permType] || 'info'" effect="plain" size="small">
              {{ permTypeLabel(row.permType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由地址" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="mono">{{ row.path || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.component || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
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
      :title="isEdit ? '编辑权限' : '新增权限'"
      width="620px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="上级权限" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'permName', value: 'id', children: 'children' }"
            node-key="id"
            check-strictly
            default-expand-all
            placeholder="请选择上级权限"
            style="width: 100%"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="权限名称" prop="permName">
              <el-input v-model="form.permName" placeholder="如：权限管理" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限类型" prop="permType">
              <el-select v-model="form.permType" style="width: 100%">
                <el-option
                  v-for="item in PERM_TYPE"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="权限标识" prop="permCode">
              <el-input
                v-model.trim="form.permCode"
                placeholder="如：system:permission:list"
                maxlength="100"
              />
            </el-form-item>
          </el-col>

          <template v-if="form.permType !== 3">
            <el-col :span="12">
              <el-form-item label="路由地址">
                <el-input v-model.trim="form.path" placeholder="如：/system/permission" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="组件路径">
                <el-input v-model.trim="form.component" placeholder="如：system/permission/index" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="图标">
                <el-select
                  v-model="form.icon"
                  filterable
                  allow-create
                  clearable
                  placeholder="选择或输入图标名"
                  style="width: 100%"
                >
                  <el-option v-for="name in iconNames" :key="name" :label="name" :value="name">
                    <span class="icon-option">
                      <el-icon><component :is="name" /></el-icon>
                      <span>{{ name }}</span>
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </template>

          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number
                v-model="form.sort"
                :min="0"
                :max="999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-alert
          v-if="!isEdit"
          type="info"
          :closable="false"
          show-icon
          title="权限标识全局唯一，建议按「模块:资源:操作」命名，新增后可继续用「新增下级」往下扩展。"
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
import { Plus, Refresh, Sort } from '@element-plus/icons-vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import {
  getPermissionTreeAll,
  createPermission,
  updatePermission,
  deletePermission
} from '@/api/system'
import { PERM_TYPE, PERM_TYPE_TAG } from '@/utils/dict'

/* ---------- 列表 ---------- */
const loading = ref(false)
const treeData = ref([])
const tableKey = ref(0)
const expandAll = ref(true)

const iconNames = Object.keys(ElementPlusIcons)

const stats = computed(() => {
  let total = 0
  let enabled = 0
  const walk = (nodes) => {
    for (const node of nodes) {
      total += 1
      if (node.status === 1) enabled += 1
      if (node.children?.length) walk(node.children)
    }
  }
  walk(treeData.value)
  return { total, enabled }
})

function permTypeLabel(type) {
  return PERM_TYPE.find((item) => item.value === type)?.label || '未知'
}

function iconOf(row) {
  if (row.icon) return row.icon
  if (row.permType === 1) return 'Folder'
  if (row.permType === 3) return 'Pointer'
  return 'Document'
}

async function fetchData() {
  loading.value = true
  try {
    treeData.value = (await getPermissionTreeAll()) || []
  } catch {
    treeData.value = []
  } finally {
    loading.value = false
  }
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
  permName: '',
  permCode: '',
  permType: 2,
  path: '',
  component: '',
  icon: '',
  sort: 1,
  visible: 1,
  status: 1
}

const form = reactive({ ...defaultForm })

/** 上级候选：编辑时排除自己及自己的子树，避免形成环 */
const parentOptions = computed(() => {
  const excludeId = currentId.value
  const prune = (nodes) =>
    nodes
      .filter((node) => node.id !== excludeId)
      .map((node) => (node.children ? { ...node, children: prune(node.children) } : node))
  return [{ id: 0, permName: '顶级权限', children: prune(treeData.value) }]
})

const rules = {
  parentId: [{ required: true, message: '请选择上级权限', trigger: 'change' }],
  permName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permType: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
  permCode: [
    { required: true, message: '请输入权限标识', trigger: 'blur' },
    {
      pattern: /^[A-Za-z][A-Za-z0-9:_-]{1,99}$/,
      message: '字母开头，可包含字母、数字、冒号、下划线或短横线',
      trigger: 'blur'
    }
  ]
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
  // 目录下挂菜单，菜单下挂按钮
  if (parent) form.permType = parent.permType === 1 ? 2 : 3
  form.sort = (parent?.children?.length || treeData.value.length) + 1
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, defaultForm, {
    parentId: row.parentId ?? 0,
    permName: row.permName,
    permCode: row.permCode,
    permType: row.permType ?? 2,
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    sort: row.sort ?? 0,
    visible: row.visible ?? 1,
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
      permName: form.permName.trim(),
      permCode: form.permCode.trim(),
      permType: form.permType,
      path: form.permType === 3 ? null : form.path || null,
      component: form.permType === 3 ? null : form.component || null,
      icon: form.permType === 3 ? null : form.icon || null,
      sort: form.sort,
      visible: form.visible,
      status: form.status
    }
    if (isEdit.value) {
      await updatePermission(currentId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createPermission(payload)
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
    await ElMessageBox.confirm(
      `确定删除权限「${row.permName}」吗？删除后已授予该权限的角色会同步失效。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await deletePermission(row.id)
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

.perm-name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.perm-name .el-icon {
  color: #7c8da3;
}

.icon-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
