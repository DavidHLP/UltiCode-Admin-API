import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { AuthUser } from '@/types/auth'
import { getUserInfo, login, register, sendVerificationCode } from '@/api/auth'

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

  return {
    loginRules: {
      username: [{ required: true, trigger: 'blur', validator: validateUsername }],
      password: [{ required: true, trigger: 'blur', validator: validatePassword }],
    },
    getRegisterRules: (registerForm: RegisterForm) => ({
      username: [{ required: true, trigger: 'blur', validator: validateUsername }],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] },
      ],
      password: [{ required: true, trigger: 'blur', validator: validatePassword }],
      confirmPassword: [
        { required: true, trigger: 'blur', validator: validateConfirmPassword(registerForm) },
      ],
      code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
    }),
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
    () => 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  )
  const sendCodeText = computed(() => {
    return isSendingCode.value ? `${countdown.value}秒后重试` : '发送验证码'
  })

  // Token 管理
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 用户信息管理
  function setUser(newUser: AuthUser) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
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

  // 初始化认证状态
  function initAuth() {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')

    if (savedToken) {
      token.value = savedToken
    }

    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem('user')
      }
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

  return {
    // 状态
    token,
    user,
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
