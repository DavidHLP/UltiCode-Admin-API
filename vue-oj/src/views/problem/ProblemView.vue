<template>
  <ProblemLayout
    ref="problemLayoutRef"
    :initial-left-pane-size="50"
    :initial-top-pane-size="60"
    :save-layout="true"
    layout-key="problem-view"
  >
    <!-- Header 插槽 -->
    <template #header-left>
      <el-breadcrumb class="header-breadcrumb" :separator-icon="Right">
        <el-breadcrumb-item :to="{ path: '/problems' }">
          <el-icon>
            <ArrowLeft />
          </el-icon>
          <span style="margin-left: 4px">返回</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item>题目详情</el-breadcrumb-item>
      </el-breadcrumb>
    </template>

    <template #header-center>
      <div v-if="problem" class="problem-title-section">
        <el-button
          type="primary"
          size="default"
          @click="handleSubmitCode"
          :loading="isSubmitting"
          class="submit-button"
        >
          <el-icon>
            <SubmitIcon />
          </el-icon>
          {{ isSubmitting ? '提交中...' : '提交' }}
        </el-button>
      </div>
    </template>

    <template #header-right>
      <!-- <div class="header-actions">
        <el-space size="small" alignment="center">
          <el-tooltip content="运行样例" placement="bottom">
            <el-button text :loading="isRunning" @click="handleRun">
              <el-icon>
                <VideoPlay />
              </el-icon>
              <span class="btn-text">运行</span>
            </el-button>
          </el-tooltip>

          <el-tooltip content="重置代码" placement="bottom">
            <el-button text :disabled="isSubmitting || isRunning" @click="handleRetry">
              <el-icon>
                <RefreshLeft />
              </el-icon>
              <span class="btn-text">重试</span>
            </el-button>
          </el-tooltip>
        </el-space>
      </div> -->
    </template>

    <!-- 题目描述插槽 -->
    <template #question>
      <QuestionCardRouter v-if="problem" :problem="problem" />
    </template>

    <!-- 代码编辑器插槽 -->
    <template #code>
      <CodeCard
        v-if="problem"
        ref="codeCardRef"
        :initial-code="initialCodeMap"
        :problem-id="problem.id"
        @submit="handleSubmit"
      />
    </template>

    <!-- 调试面板插槽 -->
    <template #debug>
      <DebugCard
        v-if="problem"
        :submission-result="submissionResult"
        :test-cases="clientTestCases"
      />
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
import { getProblemDetailVoById } from '@/api/problem'
import { submitCode as submitCodeApi } from '@/api/submit'
import { fetchSubmissionDetail } from '@/api/submission'
import type { ProblemDetailVo } from '@/types/problem'
import type { SubmissionDetailVo } from '@/types/submission'
import { ElMessage } from 'element-plus'
import { fetchTestCasesByProblemId } from '@/api/testCase'
import type { TestCaseVo } from '@/types/testCase'

const route = useRoute()
const problem = ref<ProblemDetailVo | null>(null)
const codeCardRef = ref<InstanceType<typeof CodeCard> | null>(null)
const submissionResult = ref<SubmissionDetailVo | null>(null)
const problemLayoutRef = ref<InstanceType<typeof ProblemLayout> | null>(null)
const isSubmitting = ref(false)
const isRunning = ref(false)

// 新类型：代码模板映射与测试用例（前端展示结构）
const initialCodeMap = ref<{ [key: string]: string }>({})
const clientTestCases = ref<
  Array<{
    id: number
    inputs: Array<{ inputName: string; input: string }>
    output: string
    sample: boolean
    score: number
  }>
>([])

const mapTestCaseVoToClient = (vo: TestCaseVo) => {
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
      initialCode?: Array<{ language: string; code: string }>
    }

    // 先渲染题目基本信息，测试用例随后单独拉取
    problem.value = {
      id: res.id,
      title: res.title,
      description: res.description,
      difficulty: res.difficulty,
    }

    // 构建代码模板映射，传递给 CodeCard
    initialCodeMap.value = Array.isArray(res.initialCode)
      ? res.initialCode.reduce(
          (acc, curr) => {
            acc[curr.language] = curr.code
            return acc
          },
          {} as { [key: string]: string },
        )
      : {}

    // 拉取测试用例并映射为前端模型
    try {
      const serverTestCases = (await fetchTestCasesByProblemId(problemId)) as TestCaseVo[]
      const mapped = Array.isArray(serverTestCases)
        ? serverTestCases.map(mapTestCaseVoToClient)
        : []
      clientTestCases.value = mapped
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
  const trimmed = (code || '').trim()
  if (!trimmed) {
    ElMessage.warning('代码不能为空')
    return
  }

  try {
    isSubmitting.value = true
    const submissionId = await submitCodeApi({
      problemId: problem.value.id,
      language,
      sourceCode: trimmed,
    })
    ElMessage.success('代码提交成功，正在判题...')
    pollSubmissionResult(submissionId)
  } catch (error) {
    console.error('Submission failed:', error)
    ElMessage.error('代码提交失败')
  } finally {
    isSubmitting.value = false
  }
}

const pollSubmissionResult = (submissionId: number) => {
  const interval = setInterval(async () => {
    try {
      const result = await fetchSubmissionDetail(submissionId)
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

// 处理提交代码：触发 CodeCard 暴露的 submit()，由 handleSubmit 统一处理提交流程
const handleSubmitCode = () => {
  if (!codeCardRef.value || !problem.value) {
    ElMessage.warning('请等待页面加载完成')
    return
  }
  try {
    codeCardRef.value.submit()
  } catch (error) {
    console.error('Submit trigger failed:', error)
    ElMessage.error('提交失败，请重试')
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
    // 展开下方调试面板，便于查看结果
    const topRef = (problemLayoutRef.value as unknown as { topPaneSize: Ref<number> } | undefined)
      ?.topPaneSize
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
/* ProblemView 只保留与业务相关的最小样式，布局交由 ProblemLayout 控制 */

/* Header 相关样式 */
.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

/* 使用 Element Plus el-breadcrumb 代替自定义面包屑链接 */

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
