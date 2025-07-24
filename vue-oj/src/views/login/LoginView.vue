<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">
        <h1>CodeForge</h1>
        <p>在线编程评测系统</p>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password prefix-icon="Lock"
            size="large" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" class="forget-pwd" underline="never">
            忘记密码?
          </el-link>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" size="large" :loading="loading" @click.prevent="handleLogin">
            登 录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" underline="never" @click="handleRegister">
            立即注册
          </el-link>
        </div>
      </el-form>
    </div>

    <div class="login-footer-bottom">
      <p>© 2025 CodeForge 在线编程评测系统</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { login } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref()

const loading = ref(false)
const rememberMe = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const validateUsername = (rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度在3到20个字符之间'))
  } else {
    callback()
  }
}

const validatePassword = (rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
  } else {
    callback()
  }
}

const loginRules = {
  username: [
    { required: true, trigger: 'blur', validator: validateUsername }
  ],
  password: [
    { required: true, trigger: 'blur', validator: validatePassword }
  ]
}

const handleLogin = async () => {
  try {
    loading.value = true
    const token = await login(loginForm)
    authStore.setToken(token.token)

    ElMessage.success('登录成功')
    await router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
@import './index.css';
</style>
