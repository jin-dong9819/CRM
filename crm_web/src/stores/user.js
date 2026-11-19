import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, logoutApi, getProfileApi } from '@/api/auth'

const TOKEN_KEY = 'crm_token'
const USER_KEY = 'crm_user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLogin = computed(() => !!token.value)
  const displayName = computed(() => userInfo.value?.name || userInfo.value?.empNo || '未登录')

  function setToken(value) {
    token.value = value || ''
    if (value) localStorage.setItem(TOKEN_KEY, value)
    else localStorage.removeItem(TOKEN_KEY)
  }

  function setUserInfo(info) {
    userInfo.value = info || null
    if (info) localStorage.setItem(USER_KEY, JSON.stringify(info))
    else localStorage.removeItem(USER_KEY)
  }

  /** 员工编号 + 密码登录 */
  async function login(form) {
    const data = await loginApi(form)
    setToken(data.token)
    setUserInfo(data.user)
    return data
  }

  /** 刷新页面时补齐用户信息 */
  async function fetchProfile() {
    const data = await getProfileApi()
    setUserInfo(data)
    return data
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      setToken('')
      setUserInfo(null)
    }
  }

  return { token, userInfo, isLogin, displayName, login, fetchProfile, logout, setToken, setUserInfo }
})
