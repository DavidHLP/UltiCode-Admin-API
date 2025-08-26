import axios, {
  type AxiosResponse,
  type InternalAxiosRequestConfig,
  type AxiosError,
} from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { Response } from '@/types/commons'
import router from '@/router'
import { handleError, RESPONSE_CODES } from './errorCodes'
// ===================== 防抖与去重支持 =====================
const PENDING_REQUESTS = new Set<string>()
const CONFIG_KEYS = new WeakMap<object, string>()
const AUTH_DEBOUNCE_MS = 1500
let lastAuthFailureAt = 0
let suppressRequestsUntil = 0

interface DuplicateError extends Error {
  __duplicate?: boolean
  __reqKey?: string
}

// 对象稳定排序，确保序列化稳定
const sortObject = (obj: unknown): unknown => {
  if (obj === null || typeof obj !== 'object') return obj
  if (Array.isArray(obj)) return obj.map((i) => sortObject(i))
  const sorted: Record<string, unknown> = {}
  const rec = obj as Record<string, unknown>
  Object.keys(rec)
    .sort()
    .forEach((k) => {
      sorted[k] = sortObject(rec[k])
    })
  return sorted
}

const stableStringify = (val: unknown) => {
  try {
    return JSON.stringify(sortObject(val))
  } catch {
    return String(val)
  }
}

const genReqKey = (config: InternalAxiosRequestConfig) => {
  const method = (config.method || 'get').toLowerCase()
  const url = config.url || ''
  const params = stableStringify(config.params)
  const data = stableStringify(config.data)
  return `${method}::${url}::${params}::${data}`
}

const addPending = (config: InternalAxiosRequestConfig) => {
  const key = genReqKey(config)
  CONFIG_KEYS.set(config, key)
  PENDING_REQUESTS.add(key)
}

const removePending = (config?: InternalAxiosRequestConfig) => {
  if (!config) return
  const key = CONFIG_KEYS.get(config)
  if (key) PENDING_REQUESTS.delete(key)
}

const shouldSuppressRequests = (url?: string | null) => {
  const now = Date.now()
  if (now < suppressRequestsUntil) {
    const white = ['/api/auth/login', '/api/auth/register', '/api/auth/send-code', '/api/auth/validate', 'login']
    if (url && white.some((w) => url.includes(w))) return false
    return true
  }
  return false
}
// 创建一个 axios 实例
const service = axios.create({
  baseURL: '/', // API 的基础 URL
  timeout: 50000, // 请求超时时间（毫秒）
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    // 全局抑制：认证失败后短时阻止重复请求风暴
    if (shouldSuppressRequests(config.url)) {
      const err: DuplicateError = new Error('认证失效，已阻止重复请求')
      err.__duplicate = true
      return Promise.reject(err)
    }
    // 去重：相同请求在进行中则丢弃新请求
    const key = genReqKey(config)
    if (PENDING_REQUESTS.has(key)) {
      const err: DuplicateError = new Error('重复请求已忽略')
      err.__duplicate = true
      err.__reqKey = key
      return Promise.reject(err)
    }
    addPending(config)
    return config
  },
  (error) => {
    // 请求错误时做些什么
    console.log(error) // 用于调试
    return Promise.reject(error)
  },
)

// 处理认证失败的统一逻辑
const handleAuthFailure = (code: number, message: string) => {
  const authStore = useAuthStore()
  const errorResult = handleError(code, message)
  const now = Date.now()
  if (now - lastAuthFailureAt < AUTH_DEBOUNCE_MS) {
    return
  }
  lastAuthFailureAt = now
  suppressRequestsUntil = now + AUTH_DEBOUNCE_MS

  // 清除认证信息
  if (errorResult.shouldClearAuth) {
    authStore.clearToken()
  }

  // 显示错误消息
  if (!errorResult.shouldHandleSilently) {
    ElMessage({
      message: errorResult.message,
      type: errorResult.shouldShowWarning ? 'warning' : 'error',
      duration: 3000,
    })
  }

  // 跳转到登录页
  if (errorResult.shouldRedirectToLogin) {
    setTimeout(() => {
      // 只有在非登录页面时才跳转
      if (router.currentRoute.value.name !== 'login') {
        router.push({
          name: 'login',
          query: {
            redirect: router.currentRoute.value.fullPath,
          },
        })
      }
    }, 100)
  }
}

// 响应拦截器
service.interceptors.response.use(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (response: AxiosResponse<Response<unknown>>): any => {
    // 移除 pending
    removePending(response.config as InternalAxiosRequestConfig)
    const res = response.data

    // 处理非成功响应
    if (res.code !== RESPONSE_CODES.SUCCESS) {
      const errorResult = handleError(res.code, res.message)

      // 如果是认证相关错误，使用认证失败处理逻辑
      if (errorResult.shouldClearAuth || errorResult.shouldRedirectToLogin) {
        handleAuthFailure(res.code, res.message)
      } else if (!errorResult.shouldHandleSilently) {
        // 处理其他业务错误
        ElMessage({
          message: errorResult.message,
          type: errorResult.shouldShowWarning ? 'warning' : 'error',
          duration: 5 * 1000,
        })
      }

      return Promise.reject(new Error(errorResult.message))
    }

    return res.data
  },
  (error: AxiosError) => {
    console.error('请求错误:', error)
    // 移除 pending
    const cfg = (error.config ?? error.response?.config) as
      | InternalAxiosRequestConfig
      | undefined
    removePending(cfg)

    // 静默处理重复请求产生的本地错误
    const dup = error as unknown as DuplicateError
    if (dup && dup.__duplicate) {
      return Promise.reject(error)
    }

    // 处理HTTP状态码错误
    if (error.response) {
      const resp = error.response as AxiosResponse<{ message?: string }>
      const { status, data } = resp
      const serverMessage = typeof data?.message === 'string' ? data.message : undefined
      const mergedMessage = serverMessage || error.message || '请求失败'

      // 使用统一错误处理
      const errorResult = handleError(status, mergedMessage)

      // 如果是认证相关错误，使用认证失败处理逻辑
      if (errorResult.shouldClearAuth || errorResult.shouldRedirectToLogin) {
        handleAuthFailure(status, mergedMessage)
      } else if (!errorResult.shouldHandleSilently) {
        // 处理其他HTTP错误
        ElMessage({
          message: errorResult.message,
          type: errorResult.shouldShowWarning ? 'warning' : 'error',
          duration: 5 * 1000,
        })
      }
    } else if (error.request) {
      // 网络错误
      ElMessage({
        message: '网络连接失败，请检查网络设置',
        type: 'error',
        duration: 5 * 1000,
      })
    } else {
      // 其他错误
      ElMessage({
        message: error.message || '请求失败',
        type: 'error',
        duration: 5 * 1000,
      })
    }

    return Promise.reject(error)
  },
)

export default service
