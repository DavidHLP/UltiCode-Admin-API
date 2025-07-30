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
  padding: 24px;
  height: 100%;
  background: transparent;
}

.case-tabs-container {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
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
  border-radius: 8px;
  background: rgba(248, 250, 252, 0.8);
  margin-right: 8px;
  padding: 0 16px !important;
  height: 36px;
  line-height: 36px;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

:deep(.case-tabs .el-tabs__item:hover) {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  border-color: rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

:deep(.case-tabs .el-tabs__item.is-active) {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.test-case-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-section,
.output-section {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 15px;
  color: #1e293b;
  margin-bottom: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border-radius: 2px;
}

.input-textarea :deep(.el-textarea__inner),
.output-textarea :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(226, 232, 240, 0.5);
  border-radius: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #374151;
  resize: none;
  padding: 16px;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.input-textarea :deep(.el-textarea__inner):focus,
.output-textarea :deep(.el-textarea__inner):focus {
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1), 0 4px 12px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-1px);
}

.input-textarea :deep(.el-textarea__inner)::placeholder,
.output-textarea :deep(.el-textarea__inner)::placeholder {
  color: #9ca3af;
  font-style: italic;
}

/* 添加一些微妙的动画效果 */
.input-section,
.output-section {
  animation: fadeInUp 0.6s ease-out;
}

.input-section {
  animation-delay: 0.1s;
}

.output-section {
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
</style>
