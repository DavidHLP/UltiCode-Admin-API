<template>
  <div class="user-projects">
    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key" 
        :class="['tab', { active: tab.active }]"
        @click="handleTabClick(tab)"
      >
        {{ tab.icon }} {{ tab.label }}
      </button>
    </div>

    <div class="projects-list">
      <div 
        v-for="project in projects" 
        :key="project.id" 
        class="project-item"
        @click="handleProjectClick(project)"
      >
        <div class="project-info">
          <h3 class="project-title">{{ project.title }}</h3>
          <div v-if="project.tags" class="project-tags">
            <span 
              v-for="tag in project.tags" 
              :key="tag" 
              class="tag"
            >
              {{ tag }}
            </span>
          </div>
          <div v-if="project.difficulty" class="project-difficulty">
            <span 
              :class="['difficulty-tag', getDifficultyClass(project.difficulty)]"
            >
              {{ getDifficultyText(project.difficulty) }}
            </span>
          </div>
        </div>
        <div class="project-time">{{ project.time }}</div>
      </div>
    </div>
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

const getDifficultyClass = (difficulty: 'easy' | 'medium' | 'hard') => {
  switch (difficulty) {
    case 'easy':
      return 'easy'
    case 'medium':
      return 'medium'
    case 'hard':
      return 'hard'
    default:
      return 'unknown'
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
      background: none;
      border: none;
      color: #656d76;
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
    background: #ffffff;
    border: 1px solid #d0d7de;
    border-radius: 6px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

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
          font-size: 16px;
          font-weight: 500;
          margin: 0 0 4px 0;
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

          .tag {
            background-color: #f6f8fa;
            border: 1px solid #d0d7de;
            color: #656d76;
            font-size: 11px;
            padding: 2px 6px;
            border-radius: 3px;
          }
        }

        .project-difficulty {
          margin-top: 4px;

          .difficulty-tag {
            font-size: 11px;
            padding: 2px 6px;
            border-radius: 3px;
            border: 1px solid;

            &.easy {
              background-color: #dcfce7;
              border-color: #16a34a;
              color: #15803d;
            }

            &.medium {
              background-color: #fef3c7;
              border-color: #d97706;
              color: #b45309;
            }

            &.hard {
              background-color: #fee2e2;
              border-color: #dc2626;
              color: #b91c1c;
            }

            &.unknown {
              background-color: #f3f4f6;
              border-color: #6b7280;
              color: #4b5563;
            }
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
