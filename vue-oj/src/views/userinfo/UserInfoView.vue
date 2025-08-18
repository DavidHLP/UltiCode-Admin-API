<template>
  <div class="user-info-page">
    <el-container class="container">
      <!-- 侧边栏 -->
      <el-aside class="sidebar">
        <UserProfile :profile="userInfo.profile" />
        <UserBio :bio="userInfo.bio" />
        <UserAchievements :achievements="userInfo.achievements" />
        <UserLanguages :languages="userInfo.languages" />
        <UserSkillsRadar :skills="userInfo.skills" />
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <UserStats :stats="userInfo.stats" />
        <UserActivityChart :activity="userInfo.activity" />
        <UserProjects
          :projects="userInfo.projects"
          :tabs="userInfo.tabs"
          @tab-change="handleTabChange"
        />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { mockUserInfoData } from '@/mock/userinfo'
import UserProfile from '@/components/userinfo/UserProfile.vue'
import UserBio from '@/components/userinfo/UserBio.vue'
import UserAchievements from '@/components/userinfo/UserAchievements.vue'
import UserLanguages from '@/components/userinfo/UserLanguages.vue'
import UserSkillsRadar from '@/components/userinfo/UserSkillsRadar.vue'
import UserStats from '@/components/userinfo/UserStats.vue'
import UserActivityChart from '@/components/userinfo/UserActivityChart.vue'
import UserProjects from '@/components/userinfo/UserProjects.vue'

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
.user-info-page {
  background-color: #ffffff;
  color: #24292f;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans', Helvetica, Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  min-height: 100vh;
  padding: 0;

  .container {
    max-width: 1280px;
    margin: 0 auto;
    padding: 32px 24px;
    gap: 24px;
    background-color: #ffffff;

    :deep(.el-container) {
      background-color: transparent;
    }
  }

  .sidebar {
    width: 296px !important;
    flex-shrink: 0;

    :deep(.el-aside) {
      background-color: transparent;
    }

    // 确保所有侧边栏组件有合适的间距
    & > * {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .main-content {
    flex: 1;
    min-width: 0;
    padding-left: 24px;

    :deep(.el-main) {
      background-color: transparent;
      padding: 0;
    }
  }

  // Element Plus 亮色主题样式
  :deep(.el-card) {
    background-color: #ffffff;
    border: 1px solid #d0d7de;
    border-radius: 6px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    .el-card__body {
      background-color: #ffffff;
      color: #24292f;
    }
  }

  :deep(.el-button) {
    &.el-button--success {
      background-color: #1a7f37;
      border-color: #1a7f37;

      &:hover,
      &:focus {
        background-color: #2da44e;
        border-color: #2da44e;
      }
    }
  }

  :deep(.el-tag) {
    background-color: #f6f8fa;
    border-color: #d0d7de;
    color: #656d76;
  }

  :deep(.el-progress-circle) {
    .el-progress__text {
      color: #24292f !important;
    }
  }

  :deep(.el-tooltip__popper) {
    background-color: #24292f;
    border: 1px solid #d0d7de;
    color: #ffffff;

    .el-tooltip__arrow::before {
      background-color: #24292f;
      border: 1px solid #d0d7de;
    }
  }

  // 响应式设计
  @media (max-width: 768px) {
    .container {
      flex-direction: column;
      padding: 16px 12px;
    }

    .sidebar {
      width: 100% !important;
      order: 2;
    }

    .main-content {
      padding-left: 0;
      order: 1;
    }
  }
}

// 全局样式调整 - 亮色主题
:deep(.el-popper) {
  background-color: #24292f !important;
  border-color: #d0d7de !important;
  color: #ffffff !important;
}

:deep(.el-tooltip__popper.is-dark) {
  background-color: #24292f !important;
  color: #ffffff !important;
}
</style>
