<template>
  <div class="question-card">
    <el-tabs v-model="activeTab" class="question-tabs">
      <el-tab-pane label="题目描述" name="description">
        <div class="problem-content">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
            <h1>{{ quiz.title }}</h1>
            <el-tag :type="difficultyType" size="small" effect="light">{{ quiz.difficulty }}</el-tag>
          </div>
          <md-preview :modelValue="quiz.description" theme="light" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="题解" name="solution">
        <div class="p-4">暂无题解</div>
      </el-tab-pane>
      <el-tab-pane label="提交记录" name="submissions">
        <div class="p-4">暂无提交记录</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';
import type { Quiz } from './mock';

const props = defineProps<{
  quiz: Quiz;
}>();

const activeTab = ref('description');

const difficultyType = computed(() => {
  switch (props.quiz.difficulty) {
    case '简单':
      return 'success';
    case '中等':
      return 'warning';
    case '困难':
      return 'danger';
    default:
      return 'info';
  }
});
</script>

<style scoped>
.question-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff;
}

.question-tabs {
  display: flex;
  flex-direction: column;
  height: 100%;
}

:deep(.el-tabs__header) {
  padding: 0 20px;
  margin: 0;
  flex-shrink: 0;
}

:deep(.el-tabs__content) {
  flex-grow: 1;
  overflow-y: auto;
  padding: 0 20px;
}

.problem-content h1 {
  font-size: 20px;
  font-weight: 600;
  margin: 16px 0;
}

.md-preview {
  line-height: 1.8;
}

:deep(.md-editor-preview-wrapper h3) {
  font-size: 16px;
  margin-top: 20px;
  margin-bottom: 12px;
}

:deep(.md-editor-preview-wrapper pre > code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 14px;
}
</style>
