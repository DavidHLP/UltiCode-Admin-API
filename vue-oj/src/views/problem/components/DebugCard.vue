<template>
  <div class="debug-card">
    <el-tabs v-model="mainTab" class="main-tabs">
      <!-- 测试用例标签页 -->
      <el-tab-pane name="test-cases">
        <template #label>
          <div class="tab-label">
            <el-icon>
              <DocumentChecked />
            </el-icon>
            <span>测试用例</span>
          </div>
        </template>
        <TestCaseCard :test-cases="props.testCases" @add-test-case="addTestCase" />
      </el-tab-pane>

      <!-- 测试结果标签页 -->
      <el-tab-pane name="test-results">
        <template #label>
          <div class="tab-label">
            <el-icon>
              <DataAnalysis />
            </el-icon>
            <span>测试结果</span>
          </div>
        </template>
        <ResultCaseCard :submission-result="submissionResult" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { DocumentChecked, DataAnalysis } from '@element-plus/icons-vue';
import type { TestCase, Submission } from '../../../types/problem';
import TestCaseCard from './DebugCard/TestCaseCard.vue';
import ResultCaseCard from './DebugCard/ResultCaseCard.vue';

interface Props {
  testCases: TestCase[];
  submissionResult?: Submission | null;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (
    e: 'addTestCase',
    testCase: TestCase
  ): void
}>();

const mainTab = ref('test-cases');

// 初始化第一个测试用例
watch(() => props.testCases, (newCases) => {
  console.log('newCases', newCases);
  // 确保每个测试用例都有有效的 inputs 数组
  if (newCases.some(tc => !tc.inputs || !Array.isArray(tc.inputs))) {
    console.warn('Some test cases have invalid inputs format');
  }
}, { immediate: true, deep: true });

// 添加测试用例方法
const addTestCase = () => {
  emit('addTestCase', {
    id: Date.now(),
    inputs: [{ inputName: '', input: '' }],
    output: '',
    sample: true,
    score: 0
  });
};

watch(() => props.submissionResult, (newVal) => {
  if (newVal) {
    mainTab.value = 'test-results';
  }
});
</script>

<style scoped>
.debug-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.main-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-tabs__header) {
  margin: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
  backdrop-filter: blur(10px);
}

:deep(.el-tabs__nav-wrap) {
  padding: 0 20px;
}

:deep(.el-tabs__item) {
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
  color: #64748b;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px 8px 0 0;
  margin: 0 2px;
}

:deep(.el-tabs__item:hover) {
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.05);
}

:deep(.el-tabs__item.is-active) {
  color: #3b82f6;
  font-weight: 600;
  background: rgba(59, 130, 246, 0.1);
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #3b82f6, #1d4ed8);
  height: 3px;
  border-radius: 2px;
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

:deep(.el-tab-pane) {
  height: 100%;
}

/* 滚动条样式 */
:deep(.el-tabs__content::-webkit-scrollbar) {
  width: 6px;
}

:deep(.el-tabs__content::-webkit-scrollbar-track) {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

:deep(.el-tabs__content::-webkit-scrollbar-thumb) {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
  transition: background 0.3s ease;
}

:deep(.el-tabs__content::-webkit-scrollbar-thumb:hover) {
  background: rgba(0, 0, 0, 0.3);
}
</style>
