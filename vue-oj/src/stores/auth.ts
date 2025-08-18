import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormRules, FormItemRule } from 'element-plus'
import type { AuthUser } from '@/types/auth'
import { getUserInfo, login, register, sendVerificationCode } from '@/api/auth'

// 存储键名
const STORAGE_KEYS = {
  TOKEN: 'oj_auth_token',
  USER: 'oj_auth_user',
  EXPIRE_TIME: 'oj_auth_expire_time',
} as const

// Token 有效期（毫秒）
const TOKEN_EXPIRE_DAYS = 7
const TOKEN_EXPIRE_MS = TOKEN_EXPIRE_DAYS * 24 * 60 * 60 * 1000

// 登录表单类型
export interface LoginForm {
  username: string
  password: string
}

// 注册表单类型
export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  email: string
  code: string
}

// 表单验证规则
export const createAuthValidationRules = () => {
  const validateUsername = (rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (!value) {
      callback(new Error('请输入用户名'))
    } else if (value.length < 3 || value.length > 20) {
      callback(new Error('用户名长度在3到20个字符之间'))
    } else {
      callback()
    }
  }

  const validatePassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (!value) {
      callback(new Error('请输入密码'))
    } else if (value.length < 6) {
      callback(new Error('密码长度不能小于6位'))
    } else {
      callback()
    }
  }

  const validateConfirmPassword = (registerForm: RegisterForm) => {
    return (rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (!value) {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
  }

  const loginRules: FormRules = {
    username: [{ required: true, trigger: 'blur', validator: validateUsername } as FormItemRule],
    password: [{ required: true, trigger: 'blur', validator: validatePassword } as FormItemRule],
  }

  const getRegisterRules = (registerForm: RegisterForm): FormRules => ({
    username: [{ required: true, trigger: 'blur', validator: validateUsername } as FormItemRule],
    email: [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' } as FormItemRule,
      { type: 'email' as const, message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] } as FormItemRule,
    ],
    password: [{ required: true, trigger: 'blur', validator: validatePassword } as FormItemRule],
    confirmPassword: [
      { required: true, trigger: 'blur', validator: validateConfirmPassword(registerForm) } as FormItemRule,
    ],
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' } as FormItemRule],
  })

  return {
    loginRules,
    getRegisterRules,
  }
}

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  // 基础状态
  const token = ref<string | null>(null)
  const user = ref<AuthUser | null>(null)
  const loading = ref(false)

  // 注册相关状态
  const isSendingCode = ref(false)
  const countdown = ref(60)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '用户')
  const userAvatar = computed(
    () => 'https://prettyavatars.com/api/pixel-art/100ng',
  )
  const sendCodeText = computed(() => {
    return isSendingCode.value ? `${countdown.value}秒后重试` : '发送验证码'
  })

  // 安全的本地存储操作
  const safeSetStorage = (key: string, value: string) => {
    try {
      localStorage.setItem(key, value)
    } catch (error) {
      console.error('本地存储写入失败:', error)
      // 存储空间不足时，尝试清理过期数据
      if (error instanceof DOMException && error.name === 'QuotaExceededError') {
        clearStaleData()
        try {
          localStorage.setItem(key, value)
        } catch (e) {
          console.error('清理后存储仍然失败:', e)
          throw new Error('存储空间不足，请清理浏览器缓存后重试')
        }
      } else {
        throw error
      }
    }
  }

  // 清理过期数据
  const clearStaleData = () => {
    const now = Date.now()
    Object.keys(localStorage).forEach((key) => {
      if (key.startsWith('oj_') && !key.endsWith('_expire')) return

      try {
        const expireTime = localStorage.getItem(`${key}_expire`)
        if (expireTime && Number(expireTime) < now) {
          localStorage.removeItem(key)
          localStorage.removeItem(`${key}_expire`)
        }
      } catch (e) {
        console.error(`清理存储项 ${key} 时出错:`, e)
      }
    })
  }

  // Token 管理
  function setToken(newToken: string) {
    const expireTime = Date.now() + TOKEN_EXPIRE_MS
    token.value = newToken

    try {
      safeSetStorage(STORAGE_KEYS.TOKEN, newToken)
      safeSetStorage(STORAGE_KEYS.EXPIRE_TIME, expireTime.toString())
    } catch (error) {
      console.error('保存认证信息失败:', error)
      ElMessage.error('保存登录信息失败，部分功能可能受限')
    }
  }

  function clearToken() {
    token.value = null
    user.value = null

    try {
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER)
      localStorage.removeItem(STORAGE_KEYS.EXPIRE_TIME)
    } catch (error) {
      console.error('清除认证信息失败:', error)
    }
  }

  // 用户信息管理
  function setUser(newUser: AuthUser | null) {
    if (!newUser) {
      user.value = null
      localStorage.removeItem(STORAGE_KEYS.USER)
      return
    }

    // 验证用户数据
    if (!newUser.userId || !newUser.username) {
      console.error('无效的用户数据:', newUser)
      throw new Error('用户数据不完整')
    }

    user.value = newUser

    try {
      safeSetStorage(STORAGE_KEYS.USER, JSON.stringify(newUser))
    } catch (error) {
      console.error('保存用户信息失败:', error)
      throw error
    }
  }

  async function fetchUserInfo() {
    if (token.value && !user.value) {
      try {
        const userInfo = await getUserInfo()
        setUser(userInfo)
      } catch (error) {
        console.error('获取用户信息失败:', error)
        clearToken() // 如果获取失败，可能是无效的token，清除状态
      }
    }
  }

  // 验证token是否过期
  const isTokenExpired = (expireTime: string | null): boolean => {
    if (!expireTime) return true
    const expireTimestamp = Number(expireTime)
    return isNaN(expireTimestamp) || Date.now() > expireTimestamp
  }

  // 初始化认证状态
  function initAuth() {
    try {
      const savedToken = localStorage.getItem(STORAGE_KEYS.TOKEN)
      const savedUser = localStorage.getItem(STORAGE_KEYS.USER)
      const savedExpireTime = localStorage.getItem(STORAGE_KEYS.EXPIRE_TIME)

      // 检查token是否过期
      if (isTokenExpired(savedExpireTime)) {
        clearToken()
        return
      }

      if (savedToken) {
        token.value = savedToken
      }

      if (savedUser) {
        try {
          const parsedUser = JSON.parse(savedUser) as AuthUser
          // 验证用户数据完整性
          if (parsedUser && parsedUser.userId && parsedUser.username) {
            user.value = parsedUser
          } else {
            console.error('用户数据不完整:', parsedUser)
            clearToken()
          }
        } catch (error) {
          console.error('解析用户信息失败:', error)
          clearToken()
        }
      }
    } catch (error) {
      console.error('初始化认证状态失败:', error)
      clearToken()
    }
  }

  // 登录功能
  async function handleLogin(loginForm: LoginForm) {
    try {
      loading.value = true
      const response = await login(loginForm)
      setToken(response.token)
      await fetchUserInfo()

      ElMessage.success('登录成功')
      await router.push('/')
      return true
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('用户名或密码错误')
      return false
    } finally {
      loading.value = false
    }
  }

  // 注册功能
  async function handleRegister(registerForm: RegisterForm) {
    try {
      loading.value = true
      await register(registerForm)
      ElMessage.success('注册成功！即将跳转到登录页面')
      setTimeout(() => {
        router.push('/login')
      }, 2000)
      return true
    } catch (error: unknown) {
      console.error('注册失败:', error)

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const message = (error as any)?.response?.data?.message || '注册失败，请检查您的输入'
      ElMessage.error(message)
      return false
    } finally {
      loading.value = false
    }
  }

  // 发送验证码
  async function handleSendCode(email: string) {
    if (!email) {
      ElMessage.error('请先输入邮箱地址')
      return false
    }

    isSendingCode.value = true
    try {
      await sendVerificationCode(email)
      ElMessage.success('验证码已发送，请注意查收')

      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          isSendingCode.value = false
          countdown.value = 60
        }
      }, 1000)

      return true
    } catch (error) {
      console.error('发送验证码失败:', error)
      ElMessage.error('发送验证码失败，请稍后重试')
      isSendingCode.value = false
      return false
    }
  }

  // 登出功能
  function handleLogout() {
    clearToken()
    ElMessage.success('已退出登录')
    router.push('/login')
  }

  // 用户操作处理
  function handleUserAction(command: string) {
    switch (command) {
      case 'profile':
        router.push('/profile')
        break
      case 'settings':
        router.push('/settings')
        break
      case 'logout':
        handleLogout()
        break
    }
  }

  // 导航功能
  function navigateToLogin() {
    router.push('/login')
  }

  function navigateToRegister() {
    router.push('/register')
  }

  // 监听token变化，自动同步到localStorage
  watch(token, (newToken) => {
    if (!newToken) {
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.EXPIRE_TIME)
    }
  })

  // 监听用户信息变化，自动同步到localStorage
  watch(
    user,
    (newUser) => {
      if (!newUser) {
        localStorage.removeItem(STORAGE_KEYS.USER)
      }
    },
    { deep: true },
  )

  // 初始化认证状态
  initAuth()

  // 添加页面可见性变化事件，当页面重新获得焦点时检查认证状态
  if (typeof window !== 'undefined') {
    window.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible' && token.value) {
        fetchUserInfo().catch(console.error)
      }
    })
  }

  return {
    // 状态
    token: computed({
      get: () => token.value,
      set: (value) => {
        if (value === null) {
          clearToken()
        } else {
          setToken(value)
        }
      },
    }),
    user: computed({
      get: () => user.value,
      set: (value) => setUser(value),
    }),
    loading,
    isSendingCode,
    countdown,

    // 计算属性
    isLoggedIn,
    username,
    userAvatar,
    sendCodeText,

    // 基础方法
    setToken,
    clearToken,
    setUser,
    fetchUserInfo,
    initAuth,

    // 认证功能
    handleLogin,
    handleRegister,
    handleSendCode,
    handleLogout,
    handleUserAction,

    // 导航功能
    navigateToLogin,
    navigateToRegister,
  }
})
