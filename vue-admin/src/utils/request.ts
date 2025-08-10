import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { Response } from '@/types/commons'
import router from '@/router'

// 创建一个 axios 实例
const service = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || '/', // API 的基础 URL
  timeout: 50000, // 请求超时时间（毫秒）
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    // 请求错误时做些什么
    console.log(error) // 用于调试
    return Promise.reject(error)
  },
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000,
      })
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res.data
    }
  },
  (error: AxiosError<Response>) => {
    // 响应错误统一处理
    console.log('错误: ' + error)
    const status = error?.response?.status as number | undefined
    const message = error?.response?.data?.message || error.message || '请求失败'

    if (status === 401) {
      const authStore = useAuthStore()
      // 清除本地认证信息
      authStore.clearAuthData()
      // 避免在登录页重复跳转
      const currentRoute = router.currentRoute.value
      if (currentRoute.name !== 'login') {
        const redirect = currentRoute.fullPath
        router.push({ name: 'login', query: { redirect } })
      }
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage({
        message,
        type: 'error',
        duration: 5 * 1000,
      })
    }

    return Promise.reject(error)
  },
)

// 类型安全的请求方法封装（命名导出）：保证调用方拿到的就是后端返回的 data 类型
export function requestData<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  // 通过第二个泛型参数指明拦截器处理后的返回值类型为 T
  return service.request<unknown, T>(config)
}

export default service
