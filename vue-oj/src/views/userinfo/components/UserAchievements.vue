<template>
  <div class="user-achievements">
    <div class="section-title">成就徽章</div>
    <div class="achievements">
      <div
        v-for="achievement in achievements"
        :key="achievement.id"
        class="achievement-item"
      >
        <div
          class="achievement-icon"
          :style="{ background: achievement.color }"
        >
          {{ achievement.icon }}
        </div>
        <span class="achievement-text">
          {{ achievement.name }} {{ achievement.value }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Achievement } from '@/types/userinfo'

defineProps<{
  achievements: Achievement[]
}>()
</script>

<style scoped lang="scss">
.user-achievements {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #d0d7de;
    color: #24292f;
  }

  .achievement-item {
    display: flex;
    align-items: center;
    margin: 8px 0;
    color: #656d76;
    font-size: 14px;

    .achievement-icon {
      width: 16px;
      height: 16px;
      margin-right: 8px;
      border-radius: 3px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
    }

    .achievement-text {
      flex: 1;
    }
  }
}
</style>
