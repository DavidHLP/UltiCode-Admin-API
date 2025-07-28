import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as authApi from '@/api/auth'
import type { LoginRequest } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const isLoading = ref(false)
  const router = useRouter()

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = null
    localStorage.removeItem('token')
  }

  function setLoading(loading: boolean) {
    isLoading.value = loading
  }

  // 检查是否已登录
  const isAuthenticated = computed(() => !!token.value)

  // 登录函数
  async function login(loginData: LoginRequest, redirectPath?: string) {
    try {
      setLoading(true)
      const response = await authApi.login(loginData)
      setToken(response.token)

      // 登录成功后重定向
      const targetPath = redirectPath || '/'
      await router.push(targetPath)

      return { success: true, message: '登录成功' }
    } catch (error: any) {
      console.error('登录失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '登录失败，请检查用户名和密码',
      }
    } finally {
      setLoading(false)
    }
  }

  // 登出函数
  async function logout() {
    try {
      setLoading(true)
      if (token.value) {
        await authApi.logout({ token: token.value })
      }
    } catch (error) {
      console.error('登出API调用失败:', error)
    } finally {
      clearToken()
      setLoading(false)
      await router.push('/login')
    }
  }

  return {
    token,
    isLoading,
    isAuthenticated,
    setToken,
    clearToken,
    setLoading,
    login,
    logout,
  }
})
