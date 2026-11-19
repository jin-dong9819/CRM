import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏是否折叠
  const collapsed = ref(localStorage.getItem('crm_collapsed') === '1')

  function toggleSidebar() {
    collapsed.value = !collapsed.value
    localStorage.setItem('crm_collapsed', collapsed.value ? '1' : '0')
  }

  return { collapsed, toggleSidebar }
})
