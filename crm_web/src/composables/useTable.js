import { reactive, ref, onMounted } from 'vue'

/**
 * 列表页通用逻辑：分页、搜索、加载状态
 * @param {(params: object) => Promise<{ list: any[], total: number }>} fetcher 分页接口
 * @param {object} defaultQuery 默认查询条件
 */
export function useTable(fetcher, defaultQuery = {}) {
  const initialQuery = { page: 1, pageSize: 10, keyword: '', ...defaultQuery }

  const loading = ref(false)
  const list = ref([])
  const total = ref(0)
  const query = reactive({ ...initialQuery })

  async function fetchData() {
    loading.value = true
    try {
      const data = await fetcher({ ...query })
      list.value = data?.list || []
      total.value = data?.total || 0
    } catch {
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  /** 点击查询：回到第一页 */
  function handleSearch() {
    query.page = 1
    fetchData()
  }

  /** 重置查询条件（保留每页条数） */
  function handleReset() {
    Object.assign(query, { ...initialQuery })
    fetchData()
  }

  function handlePageChange(page) {
    query.page = page
    fetchData()
  }

  function handleSizeChange(size) {
    query.pageSize = size
    query.page = 1
    fetchData()
  }

  onMounted(fetchData)

  return {
    loading,
    list,
    total,
    query,
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleSizeChange
  }
}
