<template>
  <div class="user-info">
    <aside class="sidebar">
      <UserProfile :profile="userInfo.profile" />
      <UserBio :bio="userInfo.bio" />
      <UserAchievements :achievements="userInfo.achievements" />
      <UserLanguages :languages="userInfo.languages" />
      <UserSkillsRadar :skills="userInfo.skills" />
    </aside>

    <main class="main-content">
      <UserStats :stats="userInfo.stats" />
      <UserActivityChart :activity="userInfo.activity" />
      <UserProjects
        :projects="userInfo.projects"
        :tabs="userInfo.tabs"
        @tab-change="handleTabChange"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { mockUserInfoData } from '@/mock/userinfo'
import UserProfile from '@/views/userinfo/components/UserProfile.vue'
import UserBio from '@/views/userinfo/components/UserBio.vue'
import UserAchievements from '@/views/userinfo/components/UserAchievements.vue'
import UserLanguages from '@/views/userinfo/components/UserLanguages.vue'
import UserSkillsRadar from '@/views/userinfo/components/UserSkillsRadar.vue'
import UserStats from '@/views/userinfo/components/UserStats.vue'
import UserActivityChart from '@/views/userinfo/components/UserActivityChart.vue'
import UserProjects from '@/views/userinfo/components/UserProjects.vue'

// 使用模拟数据
const userInfo = ref(mockUserInfoData)

const handleTabChange = (key: string) => {
  // 更新激活状态
  userInfo.value.tabs.forEach((tab) => {
    tab.active = tab.key === key
  })

  // 根据选中的标签更新项目列表
  // 这里可以调用相应的API获取不同类型的数据
  console.log('Tab changed to:', key)
}
</script>

<style scoped lang="scss">
.user-info {
  display: flex;
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
  gap: 24px;
  color: #24292f;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans', sans-serif;
  font-size: 14px;
  line-height: 1.5;
  min-height: 100vh;

  .sidebar {
    width: 296px;
    flex-shrink: 0;
    align-self: flex-start;

    > * + * {
      margin-top: 16px;
    }
  }

  .main-content {
    flex: 1;
    min-width: 0;
    align-self: flex-start;

    > * + * {
      margin-top: 24px;
    }
  }

  // 响应式设计
  @media (max-width: 768px) {
    flex-direction: column;
    padding: 16px 12px;

    .sidebar {
      width: 100%;
      order: 2;
    }

    .main-content {
      order: 1;
    }
  }
}
</style>
