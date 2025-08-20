<template>
  <div class="user-profile">
    <div class="avatar">{{ profile.avatar }}</div>
    <div class="username">{{ profile.username }}</div>
    <div class="handle">{{ profile.handle }}</div>
    <div class="ranking">
      全站排名: {{ profile.ranking.toLocaleString() }}
    </div>
    <button class="follow-btn">+ 关注</button>
  </div>
</template>

<script setup lang="ts">
import type { UserProfile } from '@/types/userinfo'

defineProps<{
  profile: UserProfile
}>()
</script>

<style scoped lang="scss">
.user-profile {
  background: #ffffff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

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

  .ranking {
    margin-bottom: 16px;
    font-size: 14px;
    color: #24292f;
  }

  .follow-btn {
    width: 100%;
    padding: 8px 16px;
    background: #1a7f37;
    color: white;
    border: none;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
    
    &:hover {
      background: #2da44e;
    }
  }
}
</style>
