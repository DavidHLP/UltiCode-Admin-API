<template>
  <div class="submission-content">
    <div class="submission-header">
      <div class="header-left">
        <el-icon class="back-icon" @click="handleBack">
          <ElIconBack />
        </el-icon>
        <h1>提交详情 #{{ submission.id }}</h1>
      </div>
      <el-tag :type="getStatusTagType(submission.status)">
        {{ submission.status }}
      </el-tag>
    </div>

    <div class="submission-meta">
      <div class="meta-item">
        <span class="meta-label">执行用时</span>
        <span class="meta-value">{{ submission.timeUsed }}ms</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">内存消耗</span>
        <span class="meta-value">{{ submission.memoryUsed }}KB</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">编程语言</span>
        <span class="meta-value">{{ submission.language }}</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">提交时间</span>
        <span class="meta-value">{{ formatDate(submission.createdAt) }}</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="submission-tabs">
      <el-tab-pane label="代码" name="code">
        <CodeComponent :code="submission.sourceCode" :language="submission.language" />
      </el-tab-pane>
      <el-tab-pane v-if="submission.compileInfo" label="编译信息" name="compile">
        <ErrorCodeComponent :message="submission.compileInfo" type="compile" />
      </el-tab-pane>
      <el-tab-pane v-if="submission.errorMessage" label="错误信息" name="error">
        <ErrorCodeComponent :message="submission.errorMessage" type="error" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts" setup>
import { defineEmits, defineProps, ref } from 'vue'
import { Back as ElIconBack } from '@element-plus/icons-vue'
import CodeComponent from '@/components/CodeComponent.vue'
import ErrorCodeComponent from '@/components/ErrorCodeComponent.vue'
import type { Submission } from '@/types/problem'

const activeTab = ref('code')

// 定义 props 和 emits
const { submission } = defineProps<{
  submission: Submission
}>()

const emit = defineEmits<{
  (e: 'back'): void
}>()

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'Accepted':
      return 'success'
    case 'Wrong Answer':
    case 'Time Limit Exceeded':
    case 'Memory Limit Exceeded':
    case 'Runtime Error':
    case 'Compile Error':
      return 'danger'
    default:
      return 'warning'
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

const handleBack = () => {
  emit('back')
}
</script>

<style scoped>
@import '@/assets/styles/scrollbar.css';
.submission-content {
  height: calc(100vh - 12px);
  overflow-y: auto;
  overflow-x: hidden;
  background-color: transparent;
  padding: 24px;
  box-sizing: border-box;
  /* 确保滚动条可见性 */
  scrollbar-width: thin;
  scrollbar-color: rgba(59, 130, 246, 0.4) rgba(0, 0, 0, 0.05);
}

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 0 16px 0;
  border-bottom: 1px solid var(--el-border-color-light);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.back-icon {
  cursor: pointer;
  font-size: 20px;
  color: #606266;
  transition: color 0.3s;
}

.back-icon:hover {
  color: #409eff;
}

h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.submission-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: #909399;
}

.meta-value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.submission-tabs {
  margin-top: 20px;
  flex: 1;
  overflow: hidden;
}

/* 为tabs内容区域添加滚动条样式 */
.submission-tabs :deep(.el-tab-pane) {
  overflow: auto;
  max-height: calc(100vh - 300px);
}

/* 针对代码标签页的特殊处理 */
.submission-tabs :deep(.el-tab-pane) .code-component {
  max-height: none;
  height: auto;
}
</style>
