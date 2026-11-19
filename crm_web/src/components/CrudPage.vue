<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">
          {{ config.title }}
          <small>{{ config.subtitle }}</small>
        </h2>
        <el-button v-if="!config.readonly" type="primary" :icon="Plus" @click="openCreate">
          {{ config.createText || '新增' }}
        </el-button>
      </div>

      <!-- 搜索区 -->
      <el-form :model="query" class="search-form" inline @submit.prevent>
        <el-form-item v-for="f in config.filters || []" :key="f.prop" :label="f.label">
          <el-select
            v-if="f.type === 'select'"
            v-model="query[f.prop]"
            placeholder="全部"
            clearable
            :style="{ width: f.width || '130px' }"
          >
            <el-option v-for="opt in f.options" :key="opt" :label="opt" :value="opt" />
          </el-select>
          <el-input
            v-else
            v-model="query[f.prop]"
            :placeholder="f.placeholder || '请输入'"
            clearable
            :style="{ width: f.width || '180px' }"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            :placeholder="config.keywordPlaceholder || '单号 / 客户 / 负责人'"
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
          <span class="total-text">共 {{ total }} 条记录</span>
          <el-tag v-if="config.hint" type="info" effect="plain" size="small">{{ config.hint }}</el-tag>
        </div>
        <el-button text :icon="Refresh" @click="reload">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="pageList" border stripe style="width: 100%" empty-text="暂无数据">
        <el-table-column
          v-for="col in config.columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :align="col.align || 'left'"
          :sortable="col.sortable === true"
        >
          <template #default="{ row }">
            <!-- 上下两行：主标题 + 副标题 -->
            <div v-if="col.type === 'sub'">
              <div class="cell-main">{{ row[col.prop] }}</div>
              <div v-if="col.subProp" class="cell-sub">{{ row[col.subProp] }}</div>
            </div>

            <!-- 标签 -->
            <el-tag
              v-else-if="col.type === 'tag'"
              :type="tagType(col, row)"
              effect="light"
              size="small"
            >
              {{ row[col.prop] }}
            </el-tag>

            <!-- 单号等等宽字体 -->
            <span v-else-if="col.type === 'mono'" class="mono">{{ row[col.prop] }}</span>

            <!-- 金额 -->
            <span v-else-if="col.type === 'money'" class="amount">{{ fmtMoney(row[col.prop]) }}</span>

            <!-- 满意度星级 -->
            <el-rate
              v-else-if="col.type === 'rate'"
              :model-value="Number(row[col.prop]) || 0"
              disabled
              size="small"
              disabled-void-color="#d8dee8"
            />

            <!-- 汇总（两字段拼接） -->
            <span v-else-if="col.type === 'join'">
              {{ joinText(col, row) }}
            </span>

            <span v-else>{{ row[col.prop] }}</span>
          </template>
        </el-table-column>

        <el-table-column v-if="!config.readonly" label="操作" :width="config.actionWidth || 105" fixed="right" align="center">
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
          @current-change="() => (query.page = $event)"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`${isEdit ? '编辑' : '新增'}${config.title}`"
      :width="config.dialogWidth || '680px'"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col v-for="field in config.formFields" :key="field.prop" :span="field.span || 12">
            <el-form-item :label="field.label" :prop="field.prop">
              <el-select
                v-if="field.type === 'select'"
                v-model="form[field.prop]"
                :placeholder="field.placeholder || '请选择'"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option v-for="opt in field.options" :key="opt" :label="opt" :value="opt" />
              </el-select>
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="form[field.prop]"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 100%"
              />
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="form[field.prop]"
                :min="0"
                :controls-position="'right'"
                style="width: 100%"
              />
              <el-input
                v-else-if="field.type === 'textarea'"
                v-model="form[field.prop]"
                type="textarea"
                :rows="3"
                maxlength="200"
                show-word-limit
                :placeholder="field.placeholder || '请输入'"
              />
              <el-input
                v-else
                v-model="form[field.prop]"
                :placeholder="field.placeholder || '请输入'"
                :disabled="field.disabled"
                :maxlength="field.maxlength"
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
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getExtraDb, persistExtra, nextExtraId, makeExtraNo } from '@/mock/extraDb'
import { formatMoney } from '@/utils/dict'

const props = defineProps({
  config: { type: Object, required: true }
})

const store = getExtraDb()
const tick = ref(0)
const loading = ref(false)

const defaultQuery = () => {
  const q = { page: 1, pageSize: 10, keyword: '' }
  ;(props.config.filters || []).forEach((f) => {
    q[f.prop] = ''
  })
  return q
}

const query = reactive(defaultQuery())

/**
 * 返回浅拷贝：演示数据存于普通对象，原地增删不会触发响应式，
 * 靠 tick 变化重算，且必须返回新数组引用才能让下游 computed 继续更新
 */
const rows = computed(() => {
  tick.value
  return [...(store[props.config.collection] || [])]
})

const filtered = computed(() => {
  const kw = String(query.keyword || '').trim().toLowerCase()
  const fields = props.config.searchFields || []
  return rows.value.filter((row) => {
    for (const f of props.config.filters || []) {
      if (!query[f.prop]) continue
      if (String(row[f.prop] ?? '') !== String(query[f.prop])) return false
    }
    if (!kw) return true
    return fields.some((field) => String(row[field] ?? '').toLowerCase().includes(kw))
  })
})

const total = computed(() => filtered.value.length)

const pageList = computed(() => {
  const start = (query.page - 1) * query.pageSize
  return filtered.value.slice(start, start + query.pageSize)
})

watch(
  () => props.config.collection,
  () => {
    Object.assign(query, defaultQuery())
  }
)

function reload() {
  loading.value = true
  tick.value += 1
  setTimeout(() => {
    loading.value = false
  }, 160)
}

function handleSearch() {
  query.page = 1
}

function handleReset() {
  Object.assign(query, defaultQuery())
}

function handleSizeChange(size) {
  query.pageSize = size
  query.page = 1
}

/* ---------- 单元格渲染 ---------- */

const fmtMoney = (value) => formatMoney(value)

function tagType(col, row) {
  return (col.tagMap && col.tagMap[row[col.prop]]) || 'info'
}

function joinText(col, row) {
  const parts = col.joinProps.map((p) => row[p]).filter((v) => v !== undefined && v !== null && v !== '')
  return parts.join(' / ')
}

/* ---------- 表单 ---------- */

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()
const form = reactive({})

const defaultForm = computed(() => {
  const obj = {}
  ;(props.config.formFields || []).forEach((field) => {
    obj[field.prop] = field.default ?? (field.type === 'number' ? 0 : '')
  })
  return obj
})

const rules = computed(() => {
  const r = {}
  ;(props.config.formFields || []).forEach((field) => {
    if (field.required) {
      r[field.prop] = [{ required: true, message: field.message || `请填写${field.label}`, trigger: field.type === 'input' ? 'blur' : 'change' }]
    }
  })
  return r
})

function resetForm() {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, defaultForm.value)
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
    const list = store[props.config.collection]
    if (isEdit.value) {
      const index = list.findIndex((item) => item.id === currentId.value)
      if (index !== -1) list[index] = { ...list[index], ...form, id: currentId.value }
      ElMessage.success('修改成功')
    } else {
      const noField = props.config.noField
      const row = { ...form, id: nextExtraId(list) }
      if (noField) row[noField] = makeExtraNo(props.config.noPrefix, list, noField)
      row.createTime = new Date().toLocaleString('zh-CN', { hour12: false })
      list.unshift(row)
      ElMessage.success('新增成功')
      query.page = 1
    }
    persistExtra()
    tick.value += 1
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除该条「${row[props.config.columns[0]?.prop] ?? ''}」记录吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  store[props.config.collection] = store[props.config.collection].filter((item) => item.id !== row.id)
  persistExtra()
  tick.value += 1
  ElMessage.success('删除成功')
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

.table-toolbar .left {
  align-items: center;
}
</style>
