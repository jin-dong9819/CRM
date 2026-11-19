import axios from 'axios'
import { ElMessage } from 'element-plus'
import mockAdapter from '@/mock'

const USE_MOCK = String(import.meta.env.VITE_USE_MOCK) === 'true'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  // Mock 模式下用自定义 adapter 接管，真实模式下走浏览器 XHR
  adapter: USE_MOCK ? mockAdapter : undefined
})

// 请求拦截：统一携带 Token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('crm_token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：剥掉 { code, msg, data } 外壳，只把 data 交给业务层
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code !== 200) {
        ElMessage.error(res.msg || '请求失败')
        if (res.code === 401 || res.code === 403) {
          handleUnauthorized()
        }
        return Promise.reject(new Error(res.msg || '请求失败'))
      }
      return res.data
    }
    return res
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      handleUnauthorized()
    } else {
      ElMessage.error(error?.response?.data?.msg || error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  localStorage.removeItem('crm_token')
  localStorage.removeItem('crm_user')
  if (!location.pathname.startsWith('/login')) {
    location.replace(`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`)
  }
}

export default service
