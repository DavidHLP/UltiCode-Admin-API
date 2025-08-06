import axios, { type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { Response } from '@/types/commons'
// 创建一个 axios 实例
const service = axios.create({
  baseURL: '/', // API 的基础 URL
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
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (response: AxiosResponse<Response<any>>) => {
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
  (error) => {
    // 响应错误时做些什么
    console.log('错误: ' + error) // 用于调试
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 5 * 1000, // 消息显示时长（毫秒）
    })
    return Promise.reject(error)
  },
)

export default service
