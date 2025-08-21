<template>
  <div class="developer-profile">
    <div v-if="loading" class="loading-container">
      <el-loading />
    </div>
    <div v-else-if="profile" class="container">
      <!-- 左侧边栏 -->
      <div class="sidebar">
        <ProfileCard :user-profile="profile.userProfile" @follow="handleFollow" />
        <InfoCard :profile="profile" />
      </div>

      <!-- 主要内容 -->
      <div class="main-content">
        <StatisticsCards :statistics="profile.statistics" />
        <ContributionChart :chart-data="profile.contributionChart" />
        <ActivitySection
          :heatmap="profile.contributionHeatmap"
          :metric-cards="profile.metricCards"
        />
        <RecentActivity
          :activities="profile.activities"
          :tabs="profile.activityTabs"
          @tab-change="handleTabChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { DeveloperProfile } from '@/types/userinfo'
import { getMockDeveloperProfile } from '@/mock/userinfo'
import ProfileCard from '@/views/userinfo/left/ProfileCard.vue'
import InfoCard from '@/views/userinfo/left/InfoCard.vue'
import StatisticsCards from '@/views/userinfo/right/StatisticsCards.vue'
import ContributionChart from '@/views/userinfo/right/ContributionChart.vue'
import ActivitySection from '@/views/userinfo/right/ActivitySection.vue'
import RecentActivity from '@/views/userinfo/right/RecentActivity.vue'

// 响应式数据
const profile = ref<DeveloperProfile | null>(null)
const loading = ref(true)

// 生命周期
onMounted(async () => {
  try {
    profile.value = await getMockDeveloperProfile()
  } catch (error) {
    console.error('加载开发者档案失败:', error)
  } finally {
    loading.value = false
  }
})

// 事件处理
const handleFollow = () => {
  if (!profile.value) return
  profile.value.userProfile.isFollowing = !profile.value.userProfile.isFollowing
}

const handleTabChange = (tabKey: string) => {
  if (!profile.value) return
  profile.value.activityTabs = profile.value.activityTabs.map(tab => ({
    ...tab,
    active: tab.key === tabKey
  }))
}
</script>

<style scoped>
.developer-profile {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
  background-color: #ffffff;
  color: #24292f;
  line-height: 1.5;
  min-height: 100vh;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  display: grid;
  grid-template-columns: 296px 1fr;
  gap: 24px;
}

.sidebar {
  position: sticky;
  top: 24px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

@media (max-width: 768px) {
  .container {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .sidebar {
    position: static;
  }
}
</style>
