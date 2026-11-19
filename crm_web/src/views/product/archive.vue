<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          产品档案
          <small>易损件与养护用品的编码、适用车型与进销价格</small>
        </h2>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增产品</el-button>
      </div>

      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="编码 / 名称 / 品牌 / 型号 / 车型"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="产品种类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in topCategories" :key="item.id" :label="item.categoryName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="query.brand" placeholder="全部" clearable filterable style="width: 130px">
            <el-option v-for="item in brands" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="在售" :value="1" />
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <div class="left">
          <span class="total-text">共 {{ total }} 个产品</span>
          <el-divider direction="vertical" />
          <span class="total-text">当前页均价：<b class="amount">{{ formatMoney(avgPrice) }}</b></span>
        </div>
        <el-button text :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="productCode" label="编码" width="95">
          <template #default="{ row }">
            <span class="mono">{{ row.productCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="产品名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="90" />
        <el-table-column prop="categoryName" label="种类" width="120" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="mono">{{ row.model || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fitment" label="适用车型" min-width="150" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="65" align="center" />
        <el-table-column label="标准价" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">{{ formatMoney(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成本" width="95" align="right">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">
              {{ row.status === 1 ? '在售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="105" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="fetchData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑产品' : '新增产品'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品编码">
              <el-input v-model="form.productCode" placeholder="留空自动生成" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="如：玻璃水 -25℃" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择" filterable style="width: 100%">
                <el-option
                  v-for="item in allCategories"
                  :key="item.id"
                  :label="item.parentId === 0 ? item.categoryName : `　└ ${item.categoryName}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-select v-model="form.brand" placeholder="请选择" filterable clearable style="width: 100%">
                <el-option v-for="item in brands" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号/零件号">
              <el-input v-model="form.model" placeholder="如 BW-25-2L / LX1780" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in PRODUCT_UNIT" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准价">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用车型">
              <el-input v-model="form.fitment" placeholder="如 朗逸/宝来 1.6L，或「通用」" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="在售" :value="1" />
                <el-option label="停售" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规格参数">
              <el-input
                v-model="form.spec"
                type="textarea"
                :rows="2"
                maxlength="200"
                show-word-limit
                placeholder="容量、香型、材质、包装数量等"
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useTable } from '@/composables/useTable'
import {
  getProductPage,
  createProduct,
  updateProduct,
  deleteProduct,
  getProductCategories
} from '@/api/business'
import { formatMoney, PRODUCT_UNIT, PRODUCT_BRAND } from '@/utils/dict'

const brands = PRODUCT_BRAND
const allCategories = ref([])
const topCategories = computed(() => allCategories.value.filter((item) => item.parentId === 0))

const { loading, list, total, query, fetchData, handleSearch, handleReset, handleSizeChange } = useTable(
  getProductPage,
  { keyword: '', categoryId: null, brand: '', status: null }
)

const avgPrice = computed(() => {
  if (!list.value.length) return 0
  const sum = list.value.reduce((acc, item) => acc + (Number(item.price) || 0), 0)
  return sum / list.value.length
})

async function loadCategories() {
  try {
    const data = await getProductCategories()
    allCategories.value = data || []
  } catch {
    allCategories.value = []
  }
}

onMounted(loadCategories)

/* ---------- 表单 ---------- */

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()
const form = reactive({})

const rules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }]
}

function resetForm() {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, {
    productCode: '',
    productName: '',
    categoryId: null,
    brand: '通用',
    model: '',
    spec: '',
    fitment: '通用',
    unit: '瓶',
    price: 0,
    costPrice: 0,
    status: 1
  })
  currentId.value = null
  isEdit.value = false
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  resetForm()
  Object.assign(form, row)
  currentId.value = row.id
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = { ...form }
    if (isEdit.value) {
      await updateProduct(currentId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createProduct(payload)
      ElMessage.success('新增成功')
      query.page = 1
    }
    dialogVisible.value = false
    await fetchData()
  } catch {
    // 错误提示已由请求拦截器统一弹出，这里只保持弹窗不关闭
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除产品「${row.productName}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.page > 1) query.page -= 1
    await fetchData()
  } catch {
    // 错误提示已由请求拦截器统一处理
  }
}
</script>

<style scoped>
.total-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
