<template>
  <div class="quiz-page">
    <Splitpanes class="default-theme" @resize="paneSize = $event[0].size">
      <Pane :size="paneSize">
        <div class="left-panel">
          <QuestionCard v-if="problem" :problem="problem" />
        </div>
      </Pane>
      <Pane :size="100 - paneSize">
        <div class="right-panel">
          <Splitpanes horizontal class="default-theme">
            <Pane>
              <div class="code-section">
                <CodeCard v-if="problem" ref="codeCardRef" :initial-code="problem.initialCode" @submit="handleSubmit" />
              </div>
            </Pane>
            <Pane>
              <div class="debug-section">
                <DebugCard v-if="problem" :test-cases="problem.testCases" :submission-result="submissionResult" />
              </div>
            </Pane>
          </Splitpanes>
        </div>
      </Pane>
    </Splitpanes>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Splitpanes, Pane } from 'splitpanes';
import 'splitpanes/dist/splitpanes.css';
import QuestionCard from './components/QuestionCard.vue';
import CodeCard from './components/CodeCard.vue';
import DebugCard from './components/DebugCard.vue';
import { getProblemById, submitCode, getSubmissionById } from '@/api/problem';
import type { Problem, Submission } from '@/types/problem';
import { ElMessage } from 'element-plus';

const route = useRoute();
const problem = ref<Problem | null>(null);
const codeCardRef = ref<InstanceType<typeof CodeCard> | null>(null);
const paneSize = ref(50);
const submissionResult = ref<Submission | null>(null);

const fetchProblem = async () => {
  const problemId = Number(route.params.id);
  if (isNaN(problemId)) return;

  try {
    problem.value = await getProblemById(problemId);
  } catch (error) {
    console.error('Failed to fetch problem:', error);
    ElMessage.error('题目加载失败');
  }
};

const handleSubmit = async (language: string, code: string) => {
  if (!problem.value) return;

  try {
    const submissionId = await submitCode({
      problemId: problem.value.id,
      sourceCode: code,
      language,
    });

    ElMessage.success('代码提交成功，正在判题...');
    pollSubmissionResult(submissionId);
  } catch (error) {
    console.error('Submission failed:', error);
    ElMessage.error('代码提交失败');
  }
};

const pollSubmissionResult = (submissionId: number) => {
  const interval = setInterval(async () => {
    try {
      const result = await getSubmissionById(submissionId);
      if (result.status !== 'PENDING' && result.status !== 'JUDGING') {
        submissionResult.value = result;
        clearInterval(interval);
        ElMessage.success('判题完成');
      }
    } catch (error) {
      console.error('Polling submission result failed:', error);
      clearInterval(interval);
      ElMessage.error('获取判题结果失败');
    }
  }, 2000);
};

onMounted(() => {
  fetchProblem();
});
</script>

<style scoped>
.quiz-page {
  display: flex;
  height: calc(100vh - 50px);
  background-color: #f7f8fa;
}

.left-panel,
.right-panel {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.right-panel {
  background-color: transparent;
}

.right-panel .splitpanes__pane {
  background-color: #ffffff;
  border-radius: 8px;
}

.code-section,
.debug-section {
  height: 100%;
  overflow: hidden;
  background-color: #ffffff;
  border-radius: 8px;
}

/* Style for the splitter */
:deep(.splitpanes__splitter) {
  background-color: #f7f8fa !important;
  border: none !important;
  box-sizing: border-box;
}

:deep(.splitpanes--vertical > .splitpanes__splitter) {
  width: 8px !important;
}

:deep(.splitpanes--horizontal > .splitpanes__splitter) {
  height: 8px !important;
}

.code-section {
  flex: 1;
  min-height: 0;
  /* Important for flexbox shrinking */
}

.debug-section {
  flex-shrink: 0;
  /* Let's give it a default height, maybe it can be resizable later */
  height: 300px;
}
</style>
