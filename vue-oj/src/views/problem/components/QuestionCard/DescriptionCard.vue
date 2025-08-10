<template>
  <div v-loading="loading" class="problem-content">
    <div v-if="problem" class="problem-header">
      <h1>{{ problem.title }}</h1>
      <el-tag :type="difficultyType" size="small" effect="light">
        {{ TransformDifficulty(problem.difficulty) }}
      </el-tag>
    </div>
    <el-divider v-if="problem" />
    <template v-if="problem">
      <md-preview :model-value="problem.description" theme="light" />
    </template>
    <el-empty v-else-if="!loading" description="题目加载失败" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { MdPreview } from 'md-editor-v3';
import { ElMessage } from 'element-plus';
import 'md-editor-v3/lib/preview.css';
import type { Problem } from '@/types/problem';
import { getProblemById } from '@/api/problem';

const route = useRoute();
const problem = ref<Problem | null>(null);
const loading = ref(true);

// 获取题目详情
const fetchProblem = async () => {
  const problemId = Number(route.params.id);
  if (isNaN(problemId)) return;

  try {
    loading.value = true;
    const res = await getProblemById(problemId);
    problem.value = {
      id: res.id,
      title: res.title,
      description: res.description,
      difficulty: res.difficulty,
      initialCode: res.initialCode?.reduce((acc: any, curr: any) => {
        acc[curr.language] = curr.code;
        return acc;
      }, {}),
      testCases: res.testCases,
    };
  } catch (error) {
    console.error('Failed to fetch problem:', error);
    ElMessage.error('题目加载失败');
  } finally {
    loading.value = false;
  }
};

const difficultyType = computed(() => {
  if (!problem.value) return 'info';
  switch (problem.value.difficulty) {
    case 'EASY':
      return 'success';
    case 'MEDIUM':
      return 'warning';
    case 'HARD':
      return 'danger';
    default:
      return 'info';
  }
});

const TransformDifficulty = (difficulty: string) => {
  switch (difficulty) {
    case 'EASY':
      return '简单';
    case 'MEDIUM':
      return '中等';
    case 'HARD':
      return '困难';
    default:
      return '未知';
  }
};

// 监听路由参数变化
onMounted(() => {
  fetchProblem();
});
</script>

<style scoped>
@import '@/assets/styles/md.css';
@import '@/assets/styles/html.css';
@import '@/assets/styles/scrollbar.css';

.problem-content {
  padding: 24px;
  height: 100%;
  max-height: 100%;
  overflow-y: auto;
  background: transparent;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
