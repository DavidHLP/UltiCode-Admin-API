<template>
  <div class="user-bio">
    <div class="section-title">个人简介</div>
    <div class="bio-section">
      <div v-for="item in bio" :key="item.content" class="bio-item">
        <span class="bio-icon">{{ item.icon }}</span>
        {{ item.content }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { UserBioItem } from '@/types/userinfo'

defineProps<{
  bio: UserBioItem[]
}>()
</script>

<style scoped lang="scss">
.user-bio {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin: 24px 0 16px 0;
    padding-bottom: 8px;
    border-bottom: 1px solid #d0d7de;
    color: #24292f;
  }

  .bio-section {
    .bio-item {
      display: flex;
      align-items: center;
      margin: 8px 0;
      color: #656d76;
      font-size: 14px;

      .bio-icon {
        margin-right: 8px;
        width: 16px;
        text-align: center;
      }
    }
  }
}
</style>
