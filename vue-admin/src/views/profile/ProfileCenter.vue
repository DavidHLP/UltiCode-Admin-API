<template>
  <div class="profile-center">
    <el-row :gutter="20">
      <el-col :md="8" :sm="24" :xs="24">
        <el-card shadow="hover" class="profile-card profile-left">
          <div class="profile-header">
            <el-avatar :icon="UserFilled" :size="72" class="avatar" />
            <div class="meta">
              <div class="name">{{ username || '未登录' }}</div>
              <div class="role" v-if="roleName">{{ roleName }}</div>
            </div>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <span class="label">用户名</span>
              <span class="value">{{ username || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色</span>
              <span class="value">
                <el-tag v-if="roleName" type="success" effect="dark">{{ roleName }}</el-tag>
                <span v-else>-</span>
              </span>
            </div>
          </div>
          <div class="actions">
            <el-button :loading="loading" type="primary" @click="handleRefresh">刷新信息</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :md="16" :sm="24" :xs="24">
        <el-card shadow="hover" class="profile-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
            </div>
          </template>
          <el-form :model="editForm" :rules="rules" label-width="88px" status-icon>
            <el-form-item label="用户名" prop="username">
              <el-input v-model="editForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" disabled>保存（待接入）</el-button>
              <el-button @click="resetEdit">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" class="profile-card">
          <template #header>
            <div class="card-header">
              <span>安全设置</span>
            </div>
          </template>
          <el-form :model="pwdForm" :rules="pwdRules" label-width="88px" status-icon>
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" disabled>修改密码（待接入）</el-button>
              <el-button @click="resetPwd">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import * as authApi from '@/api/auth'

const authStore = useAuthStore()

const loading = ref(false)
const user = computed(() => authStore.user)
const username = computed(() => user.value?.username ?? '')
const email = computed(() => user.value?.email ?? '')
const roleName = computed(() => user.value?.role?.roleName ?? '')

// 刷新用户信息
const handleRefresh = async () => {
  if (!authStore.token) {
    ElMessage.error('尚未登录或 token 无效')
    return
  }
  try {
    loading.value = true
    const newUser = await authApi.getUserInfo()
    authStore.setAuthData(authStore.token, newUser)
    ElMessage.success('已刷新')
  } catch (e) {
    console.error(e)
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

// 基本信息编辑表单（暂不提交，仅做展示）
const editForm = reactive({
  username: username.value,
  email: email.value,
})

watch([username, email], ([un, em]) => {
  editForm.username = un
  editForm.email = em
})

const rules: FormRules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 2, max: 32, message: '长度 2-32 个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
}

// 修改密码表单（占位 / 待接入）
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '不少于 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_: unknown, value: string, callback: (err?: Error) => void) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const resetEdit = () => {
  editForm.username = username.value
  editForm.email = email.value
}

const resetPwd = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}
</script>

<style scoped>
.profile-center {
  padding: 8px;
}

.profile-card + .profile-card {
  margin-top: 16px;
}

.profile-left .profile-header {
  display: flex;
  align-items: center;
}

.profile-left .avatar {
  margin-right: 12px;
  background: linear-gradient(135deg, #6a8dff, #57d2ff);
}

.profile-left .meta .name {
  font-size: 18px;
  font-weight: 600;
}

.profile-left .meta .role {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.info-list {
  display: grid;
  grid-template-columns: 1fr;
  row-gap: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.info-item .label {
  color: #909399;
}

.info-item .value {
  color: #303133;
}

.actions {
  margin-top: 12px;
  text-align: right;
}

.card-header {
  font-weight: 600;
}
</style>
