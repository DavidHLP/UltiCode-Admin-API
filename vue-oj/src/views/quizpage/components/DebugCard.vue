<template>
  <div class="debug-card">
    <el-tabs v-model="mainTab" class="main-tabs">
      <el-tab-pane name="test-cases">
        <template #label>
          <div class="tab-label">
            <el-icon><Select /></el-icon>
            <span>测试用例</span>
          </div>
        </template>
        <div class="case-content">
          <div class="case-tabs-container">
            <el-tabs v-model="activeTestCaseName" type="card" editable class="case-tabs" @edit="handleTabsEdit">
              <el-tab-pane v-for="(testCase, index) in localTestCases" :key="index" :label="testCase.name"
                :name="testCase.name" />
            </el-tabs>
          </div>
          <div class="test-case-details">
            <div v-for="(value, key) in activeTestCase?.inputs" :key="key" class="input-item">
              <div class="input-label">{{ key }} =</div>
              <el-input v-model="activeTestCase.inputs[key]" class="input-value" />
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane name="test-results">
        <template #label>
          <div class="tab-label">
            <el-icon>
              <MoreFilled />
            </el-icon>
            <span>测试结果</span>
          </div>
        </template>
        <div class="result-placeholder">暂无结果</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Select, MoreFilled } from '@element-plus/icons-vue';
import type { Quiz } from './mock';

const props = defineProps<{
  testCases: Quiz['testCases'];
}>();

const mainTab = ref('test-cases');
const activeTestCaseName = ref(props.testCases[0]?.name || '');

const localTestCases = ref(JSON.parse(JSON.stringify(props.testCases)));

watch(() => props.testCases, (newVal) => {
  localTestCases.value = JSON.parse(JSON.stringify(newVal));
}, { deep: true });

const activeTestCase = computed(() => {
  return localTestCases.value.find((tc: Quiz['testCases'][number]) => tc.name === activeTestCaseName.value);
});

const handleTabsEdit = (targetName: string | number | undefined, action: 'remove' | 'add') => {
  const targetNameStr = targetName?.toString(); // 转换为字符串
  if (action === 'add') {
    const newCaseName = `Case ${localTestCases.value.length + 1}`;
    localTestCases.value.push({
      name: newCaseName,
      inputs: { ...props.testCases[0]?.inputs, },
    });
    activeTestCaseName.value = newCaseName;
  } else if (action === 'remove') {
    if (localTestCases.value.length === 1) {
      return;
    }
    const tabs = localTestCases.value;
    let activeName = activeTestCaseName.value;
    if (activeName === targetNameStr) {
      tabs.forEach((tab: Quiz['testCases'][number], index: number) => {
        if (tab.name === targetNameStr) {
          const nextTab = tabs[index + 1] || tabs[index - 1];
          if (nextTab) {
            activeName = nextTab.name;
          }
        }
      });
    }

    activeTestCaseName.value = activeName;
    localTestCases.value = tabs.filter((tab: Quiz['testCases'][number]) => tab.name !== targetNameStr);
  }
};
</script>

<style scoped>
.debug-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background-color: #fff;
}

.main-tabs {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.el-tabs__header) {
  padding: 0 16px;
  margin: 0;
}

:deep(.el-tabs__item) {
  color: #595959;
}

:deep(.el-tabs__item.is-active) {
  color: #262626;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__content) {
  flex-grow: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.case-content {
  padding-top: 8px;
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
  background-color: #f7f7f7;
  margin-right: 8px;
  padding: 0 12px !important;
  height: 28px;
  line-height: 28px;
  color: #595959;
}

:deep(.case-tabs .el-tabs__item.is-active) {
  background-color: #e6e6e6;
  color: #262626;
}

.add-case-btn {
  margin-left: 4px;
}

.test-case-details {
  padding: 20px 0;
}

.input-item {
  margin-bottom: 16px;
}

.input-label {
  font-size: 14px;
  color: #595959;
  margin-bottom: 8px;
}

.input-value :deep(.el-input__wrapper) {
  background-color: #f7f7f7;
  box-shadow: none;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 14px;
  color: #262626;
  padding: 1px 12px;
}

.input-value :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.result-placeholder {
  padding: 20px;
  text-align: center;
  color: #8c8c8c;
}

.card-footer {
  flex-shrink: 0;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #8c8c8c;
  font-size: 14px;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.footer-item:hover {
  color: #595959;
}
</style>
