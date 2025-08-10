<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card-wrapper">
      <div class="login-card">
        <!-- 头部标题 -->
        <div class="card-header">
          <div class="logo-section">
            <div class="logo-icon">
              <el-icon size="32"><Platform /></el-icon>
            </div>
            <h1 class="title">CodeForge</h1>
          </div>
        </div>

        <!-- 登录表单 -->
        <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          @submit.prevent="handleLogin"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
              class="form-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              size="large"
              class="form-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              @click="handleLogin"
              :loading="authStore.isLoading"
              size="large"
              class="login-button"
            >
              <span v-if="!authStore.isLoading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部信息 -->
        <div class="card-footer">
          <p class="copyright">© 2025 SpringOJ. All rights reserved.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { LoginRequest } from '@/types/auth'
import { User, Lock, Platform } from '@element-plus/icons-vue'

defineOptions({ name: 'LoginView' })

const authStore = useAuthStore()
const loginFormRef = ref<FormInstance>()

const loginForm = ref<LoginRequest>({
  username: '',
  password: '',
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 表单验证
    await loginFormRef.value.validate()

    const result = await authStore.login(loginForm.value)

    if (result.success) {
      ElMessage.success(result.message)
    } else {
      ElMessage.error(result.message)
    }
  } catch (validationError) {
    // ElMessage 会在验证失败时自动提示，这里只需捕获异常防止程序崩溃
    console.log('表单验证失败', validationError)
  }
}
</script>

<style scoped>
@import './index.css';
</style>
