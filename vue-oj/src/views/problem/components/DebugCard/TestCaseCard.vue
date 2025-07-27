<template>
  <div class="case-content">
    <div class="case-tabs-container">
      <el-tabs v-model="activeTestCaseName" type="card" editable class="case-tabs" @edit="handleTabsEdit">
        <el-tab-pane v-for="(testCase, index) in localTestCases" :key="index" :label="testCase.name"
          :name="testCase.name" />
      </el-tabs>
    </div>
    <div class="test-case-details" v-if="activeTestCase">
      <div class="input-section">
        <div class="section-title">输入</div>
        <el-input v-model="activeTestCase.input" type="textarea" :autosize="{ minRows: 2, maxRows: 2 }"
          class="input-textarea" placeholder="请输入测试用例输入数据" />
      </div>

      <div class="output-section">
        <div class="section-title">输出</div>
        <el-input v-model="activeTestCase.output" type="textarea" :autosize="{ minRows: 2, maxRows: 2 }"
          class="output-textarea" placeholder="请输入期望输出结果" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { TestCase } from '@/types/problem.ts';

// 扩展测试用例类型以包含名称
interface TestCaseWithNames extends TestCase {
  name: string;
}

interface Props {
  testCases: TestCase[];
}

const props = defineProps<Props>();

const activeTestCaseName = ref(`Case 1`);

// 创建本地测试用例副本
const localTestCases = ref<TestCaseWithNames[]>(props.testCases.map((tc, index) => ({
  ...tc,
  name: `Case ${index + 1}`
})));

// 监听props变化
watch(() => props.testCases, (newVal) => {
  localTestCases.value = newVal.map((tc, index) => ({
    ...tc,
    name: `Case ${index + 1}`
  }));
  // 如果当前激活的测试用例不存在了，设置为第一个
  if (localTestCases.value.length > 0 &&
    !localTestCases.value.find(tc => tc.name === activeTestCaseName.value)) {
    activeTestCaseName.value = localTestCases.value[0].name;
  }
}, { deep: true });

// 计算当前激活的测试用例
const activeTestCase = computed(() => {
  return localTestCases.value.find(tc => tc.name === activeTestCaseName.value);
});

// 处理标签页编辑事件
const handleTabsEdit = (targetName: string | number | undefined, action: 'remove' | 'add') => {
  const targetNameStr = targetName?.toString() || '';

  if (action === 'add') {
    const newCaseName = `Case ${localTestCases.value.length + 1}`;
    const newCase: TestCase & { name: string } = {
      id: Date.now(), // 临时ID
      name: newCaseName,
      input: '',
      output: '',
      sample: true,
      score: 0
    };
    localTestCases.value.push(newCase);
    activeTestCaseName.value = newCaseName;
  } else if (action === 'remove' && localTestCases.value.length > 1) {
    // 确保至少保留一个测试用例
    const tabs = localTestCases.value;
    let activeName = activeTestCaseName.value;

    if (activeName === targetNameStr) {
      // 如果删除的是当前激活的标签页，切换到相邻的标签页
      const index = tabs.findIndex(tab => tab.name === targetNameStr);
      if (index !== -1) {
        const nextTab = tabs[index + 1] || tabs[index - 1];
        if (nextTab) {
          activeName = nextTab.name;
        }
      }
    }

    activeTestCaseName.value = activeName;
    localTestCases.value = tabs.filter(tab => tab.name !== targetNameStr);
  }
};
</script>

<style scoped>
.case-content {
  padding: 16px;
}

.case-tabs-container {
  display: flex;
  align-items: center;
}

.case-tabs {
  flex-grow: 1;
}

:deep(.case-tabs .el-tabs__header) {
  border-bottom: none;
  padding: 0;
  margin: 0;
}

:deep(.case-tabs .el-tabs__nav) {
  border: none !important;
}

:deep(.case-tabs .el-tabs__item) {
  border: none !important;
  border-radius: 4px;
  background-color: #f5f5f5;
  margin-right: 8px;
  padding: 0 12px !important;
  height: 28px;
  line-height: 28px;
  color: #666;
  font-size: 13px;
}

:deep(.case-tabs .el-tabs__item.is-active) {
  background-color: #1890ff;
  color: #fff;
}

.test-case-details {
  display: flex;
  flex-direction: column;
  margin-left: 12px;
}

.input-section,
.output-section {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.input-textarea :deep(.el-textarea__inner),
.output-textarea :deep(.el-textarea__inner) {
  background-color: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  resize: none;
  padding: 12px;
}

.input-textarea :deep(.el-textarea__inner):focus,
.output-textarea :deep(.el-textarea__inner):focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}
</style>
