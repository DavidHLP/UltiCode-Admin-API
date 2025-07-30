<template>
  <div class="problem-content">
    <div class="problem-header">
      <h1>{{ problem.title }}</h1>
      <el-tag :type="difficultyType" size="small" effect="light">
        {{ TransformDifficulty(problem.difficulty) }}
      </el-tag>
    </div>
    <el-divider />
    <md-preview :model-value="problem.description" theme="light" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';
import type { Problem } from '@/types/problem';

const props = defineProps<{
  problem: Problem;
}>();

const difficultyType = computed(() => {
  switch (props.problem.difficulty) {
    case 'Easy':
      return 'success';
    case 'Medium':
      return 'warning';
    case 'Hard':
      return 'danger';
    default:
      return 'info';
  }
});

const TransformDifficulty = (difficulty: string) => {
  switch (difficulty) {
    case 'Easy':
      return '简单';
    case 'Medium':
      return '中等';
    case 'Hard':
      return '困难';
    default:
      return '未知';
  }
};
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
