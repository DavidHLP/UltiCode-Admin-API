<template>
  <div class="problem-view">
    <div class="problem-container">
      <Splitpanes class="problem-splitpanes" @resize="paneSize = $event[0].size">
        <Pane :size="paneSize" class="question-pane">
          <QuestionCard v-if="problem" :problem="problem" />
        </Pane>
        <Pane :size="100 - paneSize" class="workspace-pane">
          <Splitpanes class="workspace-splitpanes" horizontal>
            <Pane class="code-pane">
              <CodeCard v-if="problem" ref="codeCardRef" :initial-code="problem.initialCode" @submit="handleSubmit" />
            </Pane>
            <Pane class="debug-pane">
              <DebugCard v-if="problem" :submission-result="submissionResult" :test-cases="problem.testCases" />
            </Pane>
          </Splitpanes>
        </Pane>
      </Splitpanes>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import QuestionCard from './components/QuestionCard.vue'
import CodeCard from './components/CodeCard.vue'
import DebugCard from './components/DebugCard.vue'
import { getProblemById, submitCode } from '@/api/problem'
import { getSubmissionById } from '@/api/submission'
import type { Problem, Submission,ProblemVO} from '@/types/problem.d'
import { ElMessage } from 'element-plus'

const route = useRoute()
const problem = ref<Problem | null>(null)
const codeCardRef = ref<InstanceType<typeof CodeCard> | null>(null)
const paneSize = ref(50)
const submissionResult = ref<Submission | null>(null)

const fetchProblem = async () => {
  const problemId = Number(route.params.id)
  if (isNaN(problemId)) return

  try {
    const res = await getProblemById(problemId) as ProblemVO
    const initialCodeMap = res.initialCode.reduce((acc, curr) => {
      acc[curr.language] = curr.code
      return acc
    }, {} as { [key: string]: string })

    problem.value = {
      id: res.id,
      title: res.title,
      description: res.description,
      difficulty: res.difficulty,
      initialCode: initialCodeMap,
      testCases: res.testCases,
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

onMounted(() => {
  fetchProblem()
})
</script>

<style scoped>
.problem-view {
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
}

.problem-container {
  height: 100%;
  padding: 12px;
  box-sizing: border-box;
}

.problem-splitpanes {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.question-pane,
.workspace-pane,
.code-pane,
.debug-pane {
  background: transparent;
  overflow: hidden;
}

.workspace-splitpanes {
  height: 100%;
}

/* 优雅的分割线样式 */
:deep(.splitpanes__splitter) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: none !important;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

:deep(.splitpanes__splitter::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-radius: 2px;
  opacity: 0.6;
  transition: all 0.3s ease;
}

:deep(.splitpanes--vertical > .splitpanes__splitter) {
  width: 8px !important;
  cursor: col-resize;
}

:deep(.splitpanes--vertical > .splitpanes__splitter::before) {
  width: 2px;
  height: 24px;
}

:deep(.splitpanes--horizontal > .splitpanes__splitter) {
  height: 8px !important;
  cursor: row-resize;
}

:deep(.splitpanes--horizontal > .splitpanes__splitter::before) {
  width: 24px;
  height: 2px;
}

:deep(.splitpanes__splitter:hover) {
  background: rgba(255, 255, 255, 0.95) !important;
}

:deep(.splitpanes__splitter:hover::before) {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.2);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .problem-container {
    padding: 8px;
  }
}

@media (max-width: 768px) {
  .problem-view {
    background: #f8fafc;
  }

  .problem-container {
    padding: 4px;
  }

  .problem-splitpanes {
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

@media (max-width: 480px) {
  .problem-container {
    padding: 2px;
  }

  .problem-splitpanes {
    border-radius: 6px;
  }
}
</style>
