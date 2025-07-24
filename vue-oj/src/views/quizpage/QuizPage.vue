<template>
  <div class="quiz-page">
    <Splitpanes class="default-theme" @resize="paneSize = $event[0].size">
      <Pane :size="paneSize">
        <div class="left-panel">
          <QuestionCard :quiz="quizData" />
        </div>
      </Pane>
      <Pane :size="100 - paneSize">
        <div class="right-panel">
          <Splitpanes horizontal class="default-theme">
            <Pane>
              <div class="code-section">
                <CodeCard ref="codeCardRef" :initial-code="quizData.initialCode" />
              </div>
            </Pane>
            <Pane>
              <div class="debug-section">
                <DebugCard :test-cases="quizData.testCases" />
              </div>
            </Pane>
          </Splitpanes>
        </div>
      </Pane>
    </Splitpanes>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Splitpanes, Pane } from 'splitpanes';
import 'splitpanes/dist/splitpanes.css';
import QuestionCard from './components/QuestionCard.vue';
import CodeCard from './components/CodeCard.vue';
import DebugCard from './components/DebugCard.vue';
import { twoSumMock } from './components/mock';
import type { Quiz } from './components/mock';

const quizData = ref<Quiz>(twoSumMock);
const codeCardRef = ref<InstanceType<typeof CodeCard> | null>(null);
const paneSize = ref(50);
</script>

<style scoped>
.quiz-page {
  display: flex;
  height: calc(100vh - 50px);
  /* Adjust based on your header's height */
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
