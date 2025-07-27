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
        <TestCaseCard :test-cases="sampleTestCases" @add-test-case="addTestCase" />
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
import { ref, computed, watch } from 'vue';
import { DocumentChecked, DataAnalysis } from '@element-plus/icons-vue';
import type { TestCase, Submission } from '../../../types/problem';
import TestCaseCard from './DebugCard/TestCaseCard.vue';
import ResultCaseCard from './DebugCard/ResultCaseCard.vue';

interface Props {
  testCases: TestCase[];
  submissionResult?: Submission;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (
    e: 'addTestCase',
    testCase: TestCase
  ): void
}>();

const mainTab = ref('test-cases');

const sampleTestCases = computed(() => {
  console.log('sampleTestCases', props.testCases);
  return props.testCases.filter(testCase => testCase.sample);
});

// 初始化第一个测试用例
watch(sampleTestCases, (newCases) => {
  if (newCases.length > 0) {
    // 可以在这里添加初始化逻辑
  }
}, { immediate: true });

// 添加测试用例方法
const addTestCase = () => {
  emit('addTestCase', {
    id: Date.now(),
    input: '',
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
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.main-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

:deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e8e8e8;
}

:deep(.el-tabs__nav-wrap) {
  padding: 0 16px;
}

:deep(.el-tabs__item) {
  color: #666;
  font-size: 14px;
  padding: 0 16px;
  height: 40px;
  line-height: 40px;
}

:deep(.el-tabs__item.is-active) {
  color: #1890ff;
  font-weight: 500;
}

:deep(.el-tabs__active-bar) {
  background-color: #1890ff;
}

:deep(.el-tabs__content) {
  flex: 1;
  padding: 0;
  overflow-y: auto;
}
</style>
