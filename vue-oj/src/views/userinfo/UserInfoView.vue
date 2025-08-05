<template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <div v-if="user" class="profile-content">
        <el-row :gutter="20">
          <el-col :span="8" class="avatar-col">
            <el-avatar :size="120" :src="userAvatar" />
            <el-button class="mt-4" type="primary" @click="handleChangeAvatar">更换头像</el-button>
          </el-col>
          <el-col :span="16">
            <el-descriptions :column="1" border title="用户信息">
              <el-descriptions-item label="用户名">
                {{ user.username }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ user.email }}
              </el-descriptions-item>
              <el-descriptions-item label="角色">
                <el-tag>{{ user.role.roleName }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="用户ID">
                {{ user.userId }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </div>

      <div v-else class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

const user = computed(() => authStore.user)
const userAvatar = computed(() => authStore.userAvatar)

const handleChangeAvatar = () => {
  ElMessage.info('更换头像功能正在开发中')
}
</script>

<style scoped>
.user-profile-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 48px);
  /* 减去顶部导航栏高度 */
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
}

.profile-content {
  padding: 20px;
}

.avatar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.mt-4 {
  margin-top: 16px;
}

.loading-state {
  padding: 20px;
}
</style>
