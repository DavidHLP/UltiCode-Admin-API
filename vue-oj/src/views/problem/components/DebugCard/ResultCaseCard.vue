<template>
  <div v-if="!submissionResult" class="result-placeholder">
    <el-empty description="暂无测试结果" :image-size="80" />
  </div>
  <div v-else class="result-details">
    <div class="result-header">
      <el-tag :type="statusTagType" size="large">{{ submissionResult.status }}</el-tag>
      <div class="score-info">得分: {{ submissionResult.score }}/100</div>
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

    <div v-if="submissionResult.errorMessage" class="error-section">
      <h4>错误信息</h4>
      <pre class="error-message">{{ submissionResult.errorMessage }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Submission } from '../../../../types/problem';

interface Props {
  submissionResult?: Submission;
}

const props = defineProps<Props>();

const statusTagType = computed(() => {
  if (!props.submissionResult) return 'info';

  switch (props.submissionResult.status) {
    case 'Accepted':
      return 'success';
    case 'Wrong Answer':
      return 'danger';
    case 'Time Limit Exceeded':
      return 'warning';
    case 'Memory Limit Exceeded':
      return 'warning';
    case 'Runtime Error':
      return 'danger';
    case 'Compile Error':
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
  height: 200px;
}

.result-details {
  padding: 16px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.score-info {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.result-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.compile-section,
.error-section {
  margin-top: 16px;
}

.compile-section h4,
.error-section h4 {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.compile-info,
.error-message {
  background-color: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #666;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 200px;
  overflow-y: auto;
}

.error-message {
  background-color: #fff2f0;
  border-color: #ffccc7;
  color: #a8071a;
}
</style>
