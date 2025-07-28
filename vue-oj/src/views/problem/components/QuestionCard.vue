<template>
  <div class="question-card">
    <el-tabs v-model="activeTab" class="question-tabs">
      <el-tab-pane name="description">
        <template #label>
          <div class="tab-label">
            <el-icon>
              <Document />
            </el-icon>
            <span>题目描述</span>
          </div>
        </template>
        <DescriptionCard :problem="problem" />
      </el-tab-pane>
      <el-tab-pane name="solution">
        <template #label>
          <div class="tab-label">
            <el-icon>
              <Promotion />
            </el-icon>
            <span>题解</span>
          </div>
        </template>
        <SolutionCard :solutions="solutions" />
      </el-tab-pane>
      <el-tab-pane name="submissions">
        <template #label>
          <div class="tab-label">
            <el-icon>
              <List />
            </el-icon>
            <span>提交记录</span>
          </div>
        </template>
        <SubmissionCard :problem-id="problem.id" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Document, Promotion, List } from '@element-plus/icons-vue';
import type { Problem } from '@/types/problem';
import SolutionCard from './QuestionCard/SolutionCard.vue';
import DescriptionCard from './QuestionCard/DescriptionCard.vue';
import SubmissionCard from './QuestionCard/SubmissionCard.vue';
import { getSolutionsByProblemId } from '@/api/problem';
import type { Solution } from '@/types/problem';

const props = defineProps<{
  problem: Problem;
}>();

const activeTab = ref('description');
const solutions = ref<Solution[]>([]);

const fetchSolutions = async () => {
  try {
    solutions.value = await getSolutionsByProblemId(props.problem.id);
  } catch (error) {
    console.error('Failed to fetch solutions:', error);
  }
};

onMounted(() => {
  if (props.problem) {
    fetchSolutions();
  }
});
</script>

<style scoped>
.question-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.question-tabs {
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
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

:deep(.el-tabs__nav-wrap) {
  padding: 0 16px;
}

:deep(.el-tabs__item) {
  padding: 0 16px;
  height: 40px;
  line-height: 40px;
}

:deep(.el-tabs__item.is-active) {
  color: #1890ff;
  font-weight: 500;
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
}
</style>
