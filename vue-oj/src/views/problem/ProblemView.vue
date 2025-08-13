<template>
  <ProblemLayout ref="problemLayoutRef" :initial-left-pane-size="50" :initial-top-pane-size="60" :save-layout="true"
    layout-key="problem-view" @layout-change="handleLayoutChange" @pane-resize="handlePaneResize">

    <!-- Header 插槽 -->
    <template #header-left>
      <div class="header-breadcrumb">
        <router-link to="/problems" class="breadcrumb-link">
          <el-icon>
            <ArrowLeft />
          </el-icon>
          题目列表
        </router-link>
        <el-icon class="breadcrumb-separator">
          <Right />
        </el-icon>
        <span class="current-problem">题目详情</span>
      </div>
    </template>

    <template #header-center>
      <div v-if="problem" class="problem-title-section">
        <el-button type="primary" size="default" @click="handleSubmitCode" :loading="isSubmitting"
          class="submit-button">
          <el-icon>
            <SubmitIcon />
          </el-icon>
          {{ isSubmitting ? '提交中...' : '提交' }}
        </el-button>
      </div>
    </template>

    <template #header-right>
      <div class="header-actions">
        <el-space size="small" alignment="center">
          <el-tooltip content="运行样例" placement="bottom">
            <el-button text :loading="isRunning" @click="handleRun">
              <el-icon><VideoPlay /></el-icon>
              <span class="btn-text">运行</span>
            </el-button>
          </el-tooltip>

          <el-tooltip content="重置代码" placement="bottom">
            <el-button text :disabled="isSubmitting || isRunning" @click="handleRetry">
              <el-icon><RefreshLeft /></el-icon>
              <span class="btn-text">重试</span>
            </el-button>
          </el-tooltip>
        </el-space>
      </div>
    </template>

    <!-- 题目描述插槽 -->
    <template #question>
      <QuestionCardRouter v-if="problem" :problem="problem" />
    </template>

    <!-- 代码编辑器插槽 -->
    <template #code>
      <CodeCard v-if="problem" ref="codeCardRef" :initial-code="problem.initialCode" :problem-id="problem.id" @submit="handleSubmit" />
    </template>

    <!-- 调试面板插槽 -->
    <template #debug>
      <DebugCard v-if="problem" :submission-result="submissionResult" :test-cases="problem.testCases" />
    </template>
  </ProblemLayout>
</template>

<script lang="ts" setup>
import { onMounted, ref, type Ref } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Right } from '@element-plus/icons-vue'
import { VideoPlay, RefreshLeft } from '@element-plus/icons-vue'
import SubmitIcon from '@/assets/icon/SubmitIcon.vue'
import ProblemLayout from './layout/ProblemLayout.vue'
import QuestionCardRouter from './components/QuestionCard.vue'
import CodeCard from './components/CodeCard.vue'
import DebugCard from './components/DebugCard.vue'
import { getProblemDetailVoById, submitCode } from '@/api/problem'
import { getSubmissionById } from '@/api/submission'
import type { Problem, Submission, TestCase } from '@/types/problem'
import type { ProblemDetailVo } from '@/types/problem.d'
import { ElMessage } from 'element-plus'
import { fetchTestCasesByProblemId } from '@/api/testCase'
import type { TestCaseVo } from '@/types/testCase'

const route = useRoute()
const problem = ref<Problem | null>(null)
const codeCardRef = ref<InstanceType<typeof CodeCard> | null>(null)
const submissionResult = ref<Submission | null>(null)
const problemLayoutRef = ref<InstanceType<typeof ProblemLayout> | null>(null)
const isSubmitting = ref(false)
const isRunning = ref(false)

// 布局相关状态
const layoutState = ref({
  leftPaneSize: 50,
  topPaneSize: 60
})

// 将后端的 TestCaseVo 转换为前端 DebugCard 使用的 TestCase 模型
const mapTestCaseVoToClient = (vo: TestCaseVo): TestCase => {
  const inputs = Array.isArray(vo.testCaseInputs)
    ? [...vo.testCaseInputs]
        .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
        .map((inp) => ({ inputName: inp.testCaseName, input: inp.inputContent }))
    : []

  const output = vo.testCaseOutput?.output ?? ''
  const sample = Boolean(vo.testCaseOutput?.isSample)
  const score = Number(vo.testCaseOutput?.score ?? 0)

  return {
    id: Number(vo.id),
    inputs,
    output,
    sample,
    score,
  }
}

const fetchProblem = async () => {
  const problemId = Number(route.params.id)
  if (isNaN(problemId)) return

  try {
    const res = (await getProblemDetailVoById(problemId)) as ProblemDetailVo & {
      // 兼容后端未下发时的可选字段
      initialCode?: Array<{ language: string; code: string }>
    }

    const initialCodeMap = Array.isArray(res.initialCode)
      ? res.initialCode.reduce((acc, curr) => {
          acc[curr.language] = curr.code
          return acc
        }, {} as { [key: string]: string })
      : {}

    // 先渲染题目基本信息，测试用例随后单独拉取
    problem.value = {
      id: res.id,
      title: res.title,
      description: res.description,
      difficulty: res.difficulty,
      initialCode: initialCodeMap,
      testCases: [],
    }

    // 拉取测试用例并映射为前端模型
    try {
      const serverTestCases = (await fetchTestCasesByProblemId(problemId)) as TestCaseVo[]
      const mapped: TestCase[] = Array.isArray(serverTestCases)
        ? serverTestCases.map(mapTestCaseVoToClient)
        : []
      if (problem.value) {
        problem.value.testCases = mapped
      }
    } catch (e) {
      // 若后端返回404（无样例），静默为空数组
      console.warn('Failed to fetch test cases or none exists:', e)
    }
  } catch (error) {
    console.error('Failed to fetch problem:', error)
    ElMessage.error('题目加载失败')
  }
}

const handleSubmit = async (language: string, code: string) => {
  if (!problem.value) return

  try {
    const submissionId = await submitCode({
      problemId: problem.value.id,
      sourceCode: code,
      language,
    })

    ElMessage.success('代码提交成功，正在判题...')
    pollSubmissionResult(submissionId)
  } catch (error) {
    console.error('Submission failed:', error)
    ElMessage.error('代码提交失败')
  }
}

const pollSubmissionResult = (submissionId: number) => {
  const interval = setInterval(async () => {
    try {
      const result = await getSubmissionById(submissionId)
      if (result.status !== 'PENDING' && result.status !== 'JUDGING') {
        submissionResult.value = result
        clearInterval(interval)
        ElMessage.success('判题完成')
      }
    } catch (error) {
      console.error('Polling submission result failed:', error)
      clearInterval(interval)
      ElMessage.error('获取判题结果失败')
    }
  }, 2000)
}

// 布局变化处理
const handleLayoutChange = (data: { leftPaneSize: number; topPaneSize: number }) => {
  layoutState.value = data
  console.log('Layout changed:', data)
}

// 面板调整处理
const handlePaneResize = (data: { type: 'horizontal' | 'vertical'; sizes: number[] }) => {
  console.log('Pane resized:', data)
}



// 处理提交代码
const handleSubmitCode = async () => {
  if (!codeCardRef.value || !problem.value) {
    ElMessage.warning('请等待页面加载完成')
    return
  }

  try {
    isSubmitting.value = true
    // 调用CodeCard的submitCode方法
    codeCardRef.value.submitCode()
  } catch (error) {
    console.error('Submit failed:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    // 延迟重置加载状态，给用户反馈
    setTimeout(() => {
      isSubmitting.value = false
    }, 1000)
  }
}

onMounted(() => {
  fetchProblem()
})

// 运行样例：触发提交并展开调试区
const handleRun = async () => {
  if (!codeCardRef.value || !problem.value) {
    ElMessage.warning('请等待页面加载完成')
    return
  }
  try {
    isRunning.value = true
    // 触发一次提交以运行样例（兼容当前提交流程）
    codeCardRef.value.submitCode()
    // 展开下方调试面板，便于查看结果
    const topRef = (problemLayoutRef.value as unknown as { topPaneSize: Ref<number> } | undefined)?.topPaneSize
    if (topRef && typeof topRef.value === 'number') {
      topRef.value = 45
    }
    ElMessage.success('已触发运行，稍后显示结果')
  } catch (e) {
    console.error('Run failed:', e)
    ElMessage.error('运行失败，请重试')
  } finally {
    setTimeout(() => (isRunning.value = false), 800)
  }
}

// 重试：重置代码并清空结果
const handleRetry = () => {
  if (!codeCardRef.value) {
    ElMessage.warning('编辑器尚未就绪')
    return
  }
  try {
    codeCardRef.value.resetCode()
    submissionResult.value = null
    ElMessage.success('已重置代码并清空结果')
  } catch (e) {
    console.error('Retry failed:', e)
    ElMessage.error('操作失败，请重试')
  }
}
</script>

<style scoped>
/* ProblemView 现在主要负责业务逻辑，样式由 ProblemLayout 组件处理 */

/* 如果需要覆盖 ProblemLayout 的某些样式，可以在这里添加 */
:deep(.problem-layout) {
  /* 确保布局组件正确显示 */
  min-height: 100vh;
}

/* 确保组件内容正确显示 */
:deep(.pane-content) {
  display: flex;
  flex-direction: column;
}

/* 针对特定组件的样式调整 */
:deep(.question-pane .pane-content) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

:deep(.code-pane .pane-content) {
  background: rgba(255, 255, 255, 0.98);
}

:deep(.debug-pane .pane-content) {
  background: rgba(255, 255, 255, 0.95);
}

/* Header 相关样式 */
.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 4px;
}

.breadcrumb-link:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.breadcrumb-separator {
  color: #c0c4cc;
  font-size: 12px;
}

.current-problem {
  color: #303133;
  font-weight: 500;
}

.problem-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 100%;
  justify-content: center;
}

.problem-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.difficulty-tag {
  flex-shrink: 0;
  font-weight: 500;
}

.submit-button {
  flex-shrink: 0;
  margin-left: 8px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.submit-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.submit-button:active {
  transform: translateY(0);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-text {
  font-size: 12px;
  color: #606266;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .problem-title {
    font-size: 16px;
    max-width: 150px;
  }

  .problem-title-section {
    gap: 8px;
  }

  .submit-button {
    font-size: 12px;
    padding: 6px 12px;
    margin-left: 4px;
  }

  .header-breadcrumb {
    font-size: 12px;
  }

  .breadcrumb-link {
    padding: 2px 4px;
  }

  .header-actions {
    gap: 4px;
  }

  :deep(.pane-content) {
    background: rgba(255, 255, 255, 0.98) !important;
  }
}

@media (max-width: 480px) {
  .problem-title-section {
    gap: 6px;
    flex-wrap: wrap;
  }

  .problem-title {
    font-size: 14px;
    max-width: 120px;
  }

  .submit-button {
    font-size: 11px;
    padding: 4px 8px;
    margin-left: 2px;
  }

  .header-actions .el-button {
    padding: 4px 8px;
  }
}
</style>
