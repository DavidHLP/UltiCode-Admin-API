import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as authApi from '@/api/auth'
import type { LoginRequest, AuthUser } from '@/types/auth'

// 从 localStorage 初始化 state
const getInitialState = () => {
  const token = localStorage.getItem('token')
  const userString = localStorage.getItem('user')
  const user = userString ? (JSON.parse(userString) as AuthUser) : null
  return { token, user }
}

export const useAuthStore = defineStore('auth', () => {
  const { token: initialToken, user: initialUser } = getInitialState()

  const token = ref<string | null>(initialToken)
  const user = ref<AuthUser | null>(initialUser)
  const isLoading = ref(false)
  const router = useRouter()

  const isAuthenticated = computed(() => !!token.value)
  const userRoles = computed<string[]>(() => {
    const r = user.value?.role
    return r ? [r.roleName] : []
  })

  function setLoading(loading: boolean) {
    isLoading.value = loading
  }

  function setAuthData(newToken: string, newUser: AuthUser) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem('token', newToken)
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  function clearAuthData() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function login(loginData: LoginRequest, redirectPath?: string) {
    try {
      setLoading(true)
      const response = await authApi.login(loginData)
      // 先暂存 token，确保后续请求（如 /me）会带上 Authorization 头
      token.value = response.token
      localStorage.setItem('token', response.token)

      const user = await authApi.getUserInfo()
      // 再统一存储 token + user（重复写入 token 可接受，保证一致的持久化入口）
      setAuthData(response.token, user)

      // 支持从路由 query 中读取 redirect 参数
      const redirectFromQuery = router.currentRoute.value.query?.redirect as string | undefined
      const targetPath = redirectPath || redirectFromQuery || '/'
      await router.push(targetPath)

      return { success: true, message: '登录成功' }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      console.error('登录失败:', error)
      clearAuthData() // 确保登录失败时清除无效状态
      return {
        success: false,
        message: error.response?.data?.message || '登录失败，请检查凭据',
      }
    } finally {
      setLoading(false)
    }
  }

  async function logout() {
    try {
      setLoading(true)
      if (token.value) {
        // 即使API调用失败，也应继续执行登出流程
        await authApi.logout({ token: token.value }).catch((e) => {
          console.warn('登出API调用失败，但将继续清除本地状态:', e)
        })
      }
    } finally {
      clearAuthData()
      setLoading(false)
      // 确保重定向到登录页
      if (router.currentRoute.value.path !== '/login') {
        await router.push('/login')
      }
    }
  }
  function hasRole(role: string): boolean {
    return userRoles.value.includes(role)
  }

  return {
    token,
    user,
    isLoading,
    isAuthenticated,
    userRoles,
    login,
    logout,
    setAuthData,
    clearAuthData,
    setLoading,
    hasRole,
  }
})
