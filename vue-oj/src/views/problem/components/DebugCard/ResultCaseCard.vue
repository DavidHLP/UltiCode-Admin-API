<template>
  <div v-if="!submissionResult" class="result-placeholder">
    <el-empty description="暂无测试结果" :image-size="80" />
  </div>
  <div v-else class="result-details">
    <div class="result-header">
      <el-tag :type="statusTagType" size="large">{{ submissionResult.status }}</el-tag>
    </div>

    <div class="result-stats">
      <div class="stat-item">
        <span class="stat-label">执行用时:</span>
        <span class="stat-value">{{ submissionResult.timeUsed }} ms</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">内存消耗:</span>
        <span class="stat-value">{{ submissionResult.memoryUsed }} KB</span>
      </div>
    </div>

    <div v-if="submissionResult.compileInfo" class="compile-section">
      <h4>编译信息</h4>
      <pre class="compile-info">{{ submissionResult.compileInfo }}</pre>
    </div>

    <div v-if="submissionResult.judgeInfo" class="error-section">
      <h4>判题信息</h4>
      <pre class="error-message">{{ submissionResult.judgeInfo }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { SubmissionDetailVo } from '@/types/submission';

interface Props {
  submissionResult?: SubmissionDetailVo | null;
}

const props = defineProps<Props>();

const statusTagType = computed(() => {
  if (!props.submissionResult) return 'info';

  const s = String(props.submissionResult.status || '').toUpperCase();
  switch (s) {
    case 'ACCEPTED':
      return 'success';
    case 'WRONG_ANSWER':
      return 'danger';
    case 'TIME_LIMIT_EXCEEDED':
    case 'MEMORY_LIMIT_EXCEEDED':
      return 'warning';
    case 'RUNTIME_ERROR':
    case 'COMPILE_ERROR':
      return 'danger';
    default:
      return 'info';
  }
});
</script>

<style scoped>
.result-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
}

:deep(.el-empty) {
  padding: 40px;
}

:deep(.el-empty__description) {
  color: #64748b;
  font-size: 15px;
  font-weight: 500;
}

.result-details {
  padding: 16px;
  height: 100%;
  background: transparent;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-tag) {
  border-radius: 4px;
  padding: 2px 8px;
  font-weight: 500;
  font-size: 13px;
  border: 1px solid #dcdfe6;
  box-shadow: none;
}

.score-info {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  background: #f5f7fa;
  padding: 6px 10px;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.result-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.stat-item:hover {
  transform: none;
  box-shadow: none;
  border-color: #e5e7eb;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.compile-section,
.error-section {
  margin-top: 24px;
}

.compile-section h4,
.error-section h4 {
  font-size: 14px;
  color: #606266;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.compile-section h4::before {
  content: none;
  display: none;
}

.error-section h4::before {
  content: none;
  display: none;
}

.compile-info,
.error-message {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #374151;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 240px;
  overflow-y: auto;
}

.compile-info {
  background: #f5f7fa;
}

.error-message {
  background: #fff5f5;
  color: #dc2626;
}

/* 简化的滚动条样式 */
.compile-info::-webkit-scrollbar,
.error-message::-webkit-scrollbar {
  width: 4px;
}

.compile-info::-webkit-scrollbar-thumb,
.error-message::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 2px;
}

.compile-info::-webkit-scrollbar-track,
.error-message::-webkit-scrollbar-track {
  background: transparent;
}

/* 移除动画，保持简洁 */
</style>
