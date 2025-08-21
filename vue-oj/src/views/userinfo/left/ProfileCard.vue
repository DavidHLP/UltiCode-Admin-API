<template>
  <div class="profile-card">
    <div class="profile-header">
      <div class="avatar">
        <template v-if="typeof userProfile.avatar === 'string'">
          {{ userProfile.avatar }}
        </template>
        <template v-else-if="userProfile.avatar.type === 'char'">
          {{ userProfile.avatar.value }}
        </template>
        <template v-else>
          <img :src="userProfile.avatar.value" alt="avatar" />
        </template>
      </div>
      <div class="username">{{ userProfile.username }}</div>
      <div class="handle">{{ userProfile.handle }}</div>
      <div class="follower-count">全球排名 {{ userProfile.globalRank?.toLocaleString() }}</div>
    </div>
    <el-button
      type="primary"
      class="follow-btn"
      @click="$emit('follow')"
    >
      <span v-if="!userProfile.isFollowing">+</span>
      <span v-else>✓</span>
      {{ userProfile.isFollowing ? '已关注' : '关注' }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import type { UserProfile } from '@/types/userinfo'

interface Props {
  userProfile: UserProfile
}

defineProps<Props>()
defineEmits<{
  follow: []
}>()
</script>

<style scoped>
.profile-card {
  background: #ffffff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.profile-header {
  text-align: center;
  margin-bottom: 16px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b6b, #4ecdc4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin: 0 auto 12px;
}

.avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-size: 20px;
  font-weight: 600;
  color: #24292f;
  margin-bottom: 4px;
}

.handle {
  color: #656d76;
  font-size: 14px;
  margin-bottom: 8px;
}

.follower-count {
  color: #656d76;
  font-size: 14px;
}

.follow-btn {
  width: 100%;
  margin: 16px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.follow-btn:hover {
  opacity: 0.9;
}
</style>
