<template>
  <div class="debug-card-container">
    <HeaderComponent :icon="DataAnalysis" title="调试">
      <template #right>
        <el-button-group>
          <el-button :type="mainTab === 'test-cases' ? 'primary' : undefined" text @click="mainTab = 'test-cases'">
            <el-icon>
              <DocumentChecked />
            </el-icon>
            <span style="margin-left: 4px">测试用例</span>
          </el-button>
          <el-button :type="mainTab === 'test-results' ? 'primary' : undefined" text @click="mainTab = 'test-results'">
            <el-icon>
              <DataAnalysis />
            </el-icon>
            <span style="margin-left: 4px">测试结果</span>
          </el-button>
        </el-button-group>
      </template>
    </HeaderComponent>
    <div class="main-content">
      <TestCaseCard
        v-show="mainTab === 'test-cases'"
        :test-cases="props.testCases"
        @add-test-case="addTestCase"
      />
      <ResultCaseCard v-show="mainTab === 'test-results'" :submission-result="submissionResult" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { DataAnalysis, DocumentChecked } from '@element-plus/icons-vue'
import type { SubmissionDetailVo } from '@/types/submission'
import TestCaseCard from './DebugCard/TestCaseCard.vue'
import ResultCaseCard from './DebugCard/ResultCaseCard.vue'
import HeaderComponent from './components/HeaderComponent.vue'

interface Props {
  testCases: Array<{
    id: number
    inputs: Array<{ inputName: string; input: string }>
    output: string
    sample: boolean
    score: number
  }>
  submissionResult?: SubmissionDetailVo | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'addTestCase', testCase: {
    id: number
    inputs: Array<{ inputName: string; input: string }>
    output: string
    sample: boolean
    score: number
  }): void
}>()

const mainTab = ref('test-cases')

// 初始化第一个测试用例
watch(
  () => props.testCases,
  (newCases) => {
    // 确保每个测试用例都有有效的 inputs 数组
    if (newCases.some((tc) => !tc.inputs || !Array.isArray(tc.inputs))) {
      console.warn('Some test cases have invalid inputs format')
    }
  },
  { immediate: true, deep: true },
)

// 添加测试用例方法
const addTestCase = () => {
  emit('addTestCase', {
    id: Date.now(),
    inputs: [{ inputName: '', input: '' }],
    output: '',
    sample: true,
    score: 0,
  })
}

watch(
  () => props.submissionResult,
  (newVal) => {
    if (newVal) {
      mainTab.value = 'test-results'
    }
  },
)
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
/* 使用统一 HeaderComponent 样式，无需本地 header-section */

/* Main 内容区域 */
.main-content {
  flex: 1;
  background: transparent;
  overflow: hidden;
  min-height: 0;
}

/* 响应式：HeaderComponent 已内置适配，这里无需额外样式 */
</style>
