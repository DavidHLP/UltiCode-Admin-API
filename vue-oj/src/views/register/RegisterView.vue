<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-logo">
        <h1>创建您的 CodeForge 账户</h1>
      </div>

      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="register-form" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>

        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="邮箱地址" prefix-icon="Message" size="large" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码" show-password prefix-icon="Lock" size="large" />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" show-password prefix-icon="Lock" size="large" />
        </el-form-item>

        <el-form-item prop="code">
          <el-row :gutter="10" style="width: 100%;">
            <el-col :span="16">
              <el-input v-model="registerForm.code" placeholder="邮箱验证码" prefix-icon="Key" size="large" />
            </el-col>
            <el-col :span="8">
              <el-button :disabled="authStore.isSendingCode" class="send-code-btn" size="large" @click="handleSendCode">
                {{ authStore.sendCodeText }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="register-btn" size="large" :loading="authStore.loading" @click.prevent="handleRegister">
            注 册
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <el-link type="primary" underline="never" @click="handleLogin">
            立即登录
          </el-link>
        </div>
      </el-form>
    </div>

    <div class="register-footer-bottom">
      <p>© 2025 CodeForge 在线编程评测系统</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { type FormInstance } from 'element-plus'
import { useAuthStore, createAuthValidationRules, type RegisterForm } from '@/stores/auth'

const authStore = useAuthStore()
const registerFormRef = ref<FormInstance>()

const registerForm = reactive<RegisterForm>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  code: ''
})

// 使用集中化的验证规则
const { getRegisterRules } = createAuthValidationRules()
const registerRules = computed(() => getRegisterRules(registerForm))

const handleSendCode = async () => {
  const emailField = await registerFormRef.value?.validateField('email').catch(() => false)
  if (!emailField) return
  
  await authStore.handleSendCode(registerForm.email)
}

const handleRegister = async () => {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  await authStore.handleRegister(registerForm)
}

const handleLogin = () => {
  authStore.navigateToLogin()
}
</script>

<style scoped>
@import './index.css';
</style>
