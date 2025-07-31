<template>
  <div class="solution-content">
    <div class="solution-header">
      <div class="header-left">
        <el-icon class="back-icon" @click="handleBack">
          <ElIconBack />
        </el-icon>
        <h1>{{ solution.title }}</h1>
      </div>
    </div>

    <div class="solution-meta">
      <div class="author-info">
        <el-avatar :size="40" :src="solution.authorAvatar" />
        <div class="author-details">
          <span class="author-name">{{ solution.authorUsername }}</span>
          <span class="publish-time">{{ formatDate(solution.createdAt) }}</span>
        </div>
      </div>
    </div>
    <el-divider />
    <md-preview :model-value="solution.content" theme="light" />

    <div class="solution-footer">
      <el-button-group>
        <el-button :type="userVote === 'up' ? 'primary' : 'default'" @click="handleVote('up')">
          <el-icon>
            <ElIconCaretTop />
          </el-icon>
          <span>赞同 {{ solution.upvotes }}</span>
        </el-button>
        <el-button :type="userVote === 'down' ? 'primary' : 'default'" @click="handleVote('down')">
          <el-icon>
            <ElIconCaretBottom />
          </el-icon>
          <span>反对 {{ solution.downvotes }}</span>
        </el-button>
      </el-button-group>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import {
  Back as ElIconBack,
  CaretBottom as ElIconCaretBottom,
  CaretTop as ElIconCaretTop,
} from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import type { SolutionVo } from '@/types/problem'

defineProps<{
  solution: SolutionVo & { viewCount?: number }
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'vote', type: 'up' | 'down'): void
}>()

const userVote = ref<'up' | 'down' | null>(null)

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

const handleBack = () => {
  emit('back')
}

const handleVote = (type: 'up' | 'down') => {
  if (userVote.value === type) {
    // 取消投票
    userVote.value = null
    // 这里可以再发一个请求到后端取消投票，如果后端支持的话
  } else {
    userVote.value = type
    emit('vote', type)
  }
}
</script>

<style scoped>
@import '@/assets/styles/html.css';
@import '@/assets/styles/md.css';
@import '@/assets/styles/scrollbar.css';

.solution-content {
  padding: 24px;
  height: calc(100vh - 12px);
  overflow: auto;
  background: transparent;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  position: relative;
}

.solution-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .back-icon {
    font-size: 20px;
    color: #6b7280;
    cursor: pointer;
    transition: color 0.3s ease;

    &:hover {
      color: #3b82f6;
    }
  }
}

.solution-meta {
  .author-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .author-details {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .author-name {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }

      .publish-time {
        font-size: 14px;
        color: #6b7280;
      }
    }
  }
}

.solution-footer {
  position: fixed;
  bottom: 20px;
  right: 40px;
  z-index: 100;
  background-color: white;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
