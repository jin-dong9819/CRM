<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          产品分类
          <small>按系统与部位划分两级目录：产品种类 → 零件分组</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate(0)">新增分类</el-button>
      </div>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">
            共 {{ totalCount }} 个分类（{{ topCount }} 个一级 / {{ totalCount - topCount }} 个二级）
          </span>
        </div>
        <el-button text :icon="Refresh" @click="loadData">刷新</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tree"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        border
        style="width: 100%"
      >
        <el-table-column prop="categoryName" label="分类名称" min-width="260">
          <template #default="{ row }">
            <span :class="{ 'is-top': row.parentId === 0 }">{{ row.categoryName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="层级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.parentId === 0 ? 'primary' : 'info'" effect="plain" size="small">
              {{ row.parentId === 0 ? '一级' : '二级' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="190" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.parentId === 0" link type="primary" @click="openCreate(row.id)">加子类</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" style="width: 100%" :disabled="lockParent">
            <el-option label="无（作为一级分类）" :value="0" />
            <el-option
              v-for="item in topCategories"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
              :disabled="isEdit && item.id === currentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="如：清洁清洗 / 清洗剂" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="1" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getProductCategories,
  createProductCategory,
  updateProductCategory,
  deleteProductCategory
} from '@/api/business'

const loading = ref(false)
const rawList = ref([])

const totalCount = computed(() => rawList.value.length)
const topCount = computed(() => rawList.value.filter((item) => item.parentId === 0).length)
const topCategories = computed(() => rawList.value.filter((item) => item.parentId === 0))

const tree = computed(() => {
  const map = new Map()
  rawList.value.forEach((item) => map.set(item.id, { ...item, children: [] }))
  const roots = []
  map.forEach((node) => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId).children.push(node)
    } else {
      roots.push(node)
    }
  })
  map.forEach((node) => {
    if (!node.children.length) delete node.children
  })
  return roots
})

async function loadData() {
  loading.value = true
  try {
    const data = await getProductCategories()
    rawList.value = data || []
  } catch {
    rawList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

/* ---------- 表单 ---------- */

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const lockParent = ref(false)
const currentId = ref(null)
const formRef = ref()
const form = reactive({})

const rules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

function openCreate(parentId) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { parentId, categoryName: '', sort: 1, status: 1 })
  currentId.value = null
  isEdit.value = false
  lockParent.value = false
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEdit(row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { ...row })
  currentId.value = row.id
  isEdit.value = true
  lockParent.value = false
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      parentId: form.parentId ?? 0,
      categoryName: form.categoryName,
      sort: form.sort,
      status: form.status
    }
    if (isEdit.value) {
      await updateProductCategory(currentId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createProductCategory(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.categoryName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteProductCategory(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch {
    // 分类下有子类或产品时后端会拒绝，提示已由拦截器弹出
  }
}
</script>

<style scoped>
.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.is-top {
  font-weight: 600;
}
</style>
