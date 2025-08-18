<template>
  <el-card class="user-profile-card">
    <div class="avatar">{{ profile.avatar }}</div>
    <div class="username">{{ profile.username }}</div>
    <div class="handle">{{ profile.handle }}</div>
    <div class="followers-info">
      全站排名: {{ profile.ranking.toLocaleString() }}
    </div>
    <el-button class="follow-btn" type="success">+ 关注</el-button>
  </el-card>
</template>

<script setup lang="ts">
import type { UserProfile } from '@/types/userinfo'

defineProps<{
  profile: UserProfile
}>()
</script>

<style scoped lang="scss">
.user-profile-card {
  :deep(.el-card__body) {
    padding: 16px;
    background: #ffffff;
    border: 1px solid #d0d7de;
    border-radius: 6px;
  }

  .avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    margin-bottom: 16px;
    background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 32px;
    font-weight: bold;
  }

  .username {
    font-size: 24px;
    font-weight: 600;
    color: #24292f;
    margin-bottom: 4px;
  }

  .handle {
    color: #656d76;
    margin-bottom: 16px;
  }

  .followers-info {
    margin-bottom: 16px;
    font-size: 14px;
    color: #24292f;
  }

  .follow-btn {
    width: 100%;
    background: #1a7f37;
    color: white;
    border: none;
    font-weight: 500;
    
    &:hover, &:focus {
      background: #2da44e;
    }
  }
}
</style>
