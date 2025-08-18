<template>
  <div class="user-projects">
    <div class="tabs">
      <a 
        v-for="tab in tabs" 
        :key="tab.key" 
        :class="['tab', { active: tab.active }]"
        @click="handleTabClick(tab)"
      >
        {{ tab.icon }} {{ tab.label }}
      </a>
    </div>

    <el-card class="projects-list">
      <div 
        v-for="project in projects" 
        :key="project.id" 
        class="project-item"
        @click="handleProjectClick(project)"
      >
        <div class="project-info">
          <a class="project-title">{{ project.title }}</a>
          <div v-if="project.tags" class="project-tags">
            <el-tag 
              v-for="tag in project.tags" 
              :key="tag" 
              size="small"
              effect="dark"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div v-if="project.difficulty" class="project-difficulty">
            <el-tag 
              :type="getDifficultyType(project.difficulty)"
              size="small"
            >
              {{ getDifficultyText(project.difficulty) }}
            </el-tag>
          </div>
        </div>
        <div class="project-time">{{ project.time }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { ProjectItem } from '@/types/userinfo'

interface TabItem {
  key: string
  label: string
  icon: string
  active: boolean
}

defineProps<{
  projects: ProjectItem[]
  tabs: TabItem[]
}>()

const emit = defineEmits<{
  tabChange: [key: string]
}>()

const handleTabClick = (tab: TabItem) => {
  emit('tabChange', tab.key)
}

const handleProjectClick = (project: ProjectItem) => {
  ElMessage.info(`点击了题目：${project.title}`)
}

const getDifficultyType = (difficulty: 'easy' | 'medium' | 'hard') => {
  switch (difficulty) {
    case 'easy':
      return 'success'
    case 'medium':
      return 'warning'
    case 'hard':
      return 'danger'
    default:
      return 'info'
  }
}

const getDifficultyText = (difficulty: 'easy' | 'medium' | 'hard') => {
  switch (difficulty) {
    case 'easy':
      return '简单'
    case 'medium':
      return '中等'
    case 'hard':
      return '困难'
    default:
      return '未知'
  }
}
</script>

<style scoped lang="scss">
.user-projects {
  .tabs {
    display: flex;
    gap: 24px;
    border-bottom: 1px solid #d0d7de;
    margin-bottom: 24px;
    padding-bottom: 8px;

    .tab {
      color: #656d76;
      text-decoration: none;
      padding: 4px 0;
      border-bottom: 2px solid transparent;
      cursor: pointer;
      font-size: 14px;
      transition: all 0.2s;

      &:hover {
        color: #24292f;
      }

      &.active {
        color: #24292f;
        border-bottom-color: #fd7e14;
      }
    }
  }

  .projects-list {
    :deep(.el-card__body) {
      padding: 0;
      background: #ffffff;
      border: 1px solid #d0d7de;
      border-radius: 6px;
    }

    .project-item {
      padding: 16px;
      border-bottom: 1px solid #d0d7de;
      display: flex;
      justify-content: space-between;
      align-items: center;
      cursor: pointer;
      transition: background-color 0.2s;

      &:hover {
        background-color: rgba(56, 139, 253, 0.05);
      }

      &:last-child {
        border-bottom: none;
      }

      .project-info {
        flex: 1;

        .project-title {
          color: #0969da;
          text-decoration: none;
          font-weight: 500;
          margin-bottom: 4px;
          display: block;
          cursor: pointer;

          &:hover {
            text-decoration: underline;
          }
        }

        .project-tags {
          margin: 8px 0;
          display: flex;
          gap: 4px;
          flex-wrap: wrap;

          :deep(.el-tag) {
            background-color: #f6f8fa;
            border-color: #d0d7de;
            color: #656d76;
            font-size: 11px;
          }
        }

        .project-difficulty {
          margin-top: 4px;

          :deep(.el-tag) {
            font-size: 11px;
          }
        }
      }

      .project-time {
        color: #656d76;
        font-size: 12px;
        flex-shrink: 0;
      }
    }
  }
}
</style>
