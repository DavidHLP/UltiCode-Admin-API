<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">
        <h1>CodeForge</h1>
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
          <el-button type="primary" class="login-btn" size="large" :loading="authStore.loading" @click.prevent="handleLogin">
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
import { useAuthStore, createAuthValidationRules, type LoginForm } from '@/stores/auth'

const authStore = useAuthStore()
const loginFormRef = ref()

const rememberMe = ref(false)
const loginForm = reactive<LoginForm>({
  username: '',
  password: ''
})

// 使用集中化的验证规则
const { loginRules } = createAuthValidationRules()

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  await authStore.handleLogin(loginForm)
}

const handleRegister = () => {
  authStore.navigateToRegister()
}
</script>

<style scoped>
@import './index.css';
</style>
