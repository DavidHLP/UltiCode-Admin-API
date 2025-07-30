<template>
  <div class="debug-card-container">
    <div class="header-section">
      <el-tabs v-model="mainTab" class="debug-tabs">
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
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="main-content">
      <TestCaseCard :test-cases="props.testCases" @add-test-case="addTestCase" v-show="mainTab === 'test-cases'" />
      <ResultCaseCard :submission-result="submissionResult" v-show="mainTab === 'test-results'" />
    </div>
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
/* 容器布局 */
.debug-card-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Header 区域样式 */
.header-section {
  flex-shrink: 0;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

/* Tab 样式优化 */
.debug-tabs {
  --el-tabs-header-height: 48px;
}

.debug-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.debug-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0 16px;
}

.debug-tabs :deep(.el-tabs__item) {
  height: 48px;
  line-height: 48px;
  padding: 0 16px;
  color: #606266;
  font-weight: 400;
  border: none;
  transition: all 0.2s ease;
}

.debug-tabs :deep(.el-tabs__item:hover) {
  color: #409eff;
}

.debug-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 500;
}

.debug-tabs :deep(.el-tabs__active-bar) {
  height: 2px;
  background-color: #409eff;
}

/* Tab 标签内容样式 */
.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.tab-label .el-icon {
  font-size: 16px;
}

/* Main 内容区域 */
.main-content {
  flex: 1;
  background: #ffffff;
  overflow: hidden;
  min-height: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .debug-tabs :deep(.el-tabs__nav-wrap) {
    padding: 0 12px;
  }

  .debug-tabs :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 13px;
  }

  .tab-label {
    gap: 4px;
    font-size: 13px;
  }

  .tab-label .el-icon {
    font-size: 14px;
  }
}
</style>
