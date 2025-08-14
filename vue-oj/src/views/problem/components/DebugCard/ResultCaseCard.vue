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
  padding: 24px;
  height: 100%;
  background: transparent;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid rgba(59, 130, 246, 0.1);
}

:deep(.el-tag) {
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 600;
  font-size: 14px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.score-info {
  font-size: 16px;
  color: #1e293b;
  font-weight: 600;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(29, 78, 216, 0.1));
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.2);
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
  gap: 8px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.5);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.compile-section,
.error-section {
  margin-top: 24px;
}

.compile-section h4,
.error-section h4 {
  font-size: 16px;
  color: #1e293b;
  margin: 0 0 16px 0;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.compile-section h4::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 2px;
}

.error-section h4::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border-radius: 2px;
}

.compile-info,
.error-message {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(226, 232, 240, 0.5);
  border-radius: 12px;
  padding: 20px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #374151;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.compile-info {
  border-left: 4px solid #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.error-message {
  border-left: 4px solid #ef4444;
  background: rgba(239, 68, 68, 0.05);
  color: #dc2626;
}

/* 滚动条样式 */
.compile-info::-webkit-scrollbar,
.error-message::-webkit-scrollbar {
  width: 6px;
}

.compile-info::-webkit-scrollbar-track,
.error-message::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.compile-info::-webkit-scrollbar-thumb {
  background: rgba(16, 185, 129, 0.3);
  border-radius: 3px;
}

.error-message::-webkit-scrollbar-thumb {
  background: rgba(239, 68, 68, 0.3);
  border-radius: 3px;
}

.compile-info::-webkit-scrollbar-thumb:hover {
  background: rgba(16, 185, 129, 0.5);
}

.error-message::-webkit-scrollbar-thumb:hover {
  background: rgba(239, 68, 68, 0.5);
}

/* 动画效果 */
.result-details {
  animation: fadeInUp 0.6s ease-out;
}

.stat-item {
  animation: slideInUp 0.6s ease-out;
}

.stat-item:nth-child(1) {
  animation-delay: 0.1s;
}

.stat-item:nth-child(2) {
  animation-delay: 0.2s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
