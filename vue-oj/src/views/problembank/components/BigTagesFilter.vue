<template>
  <div class="tag-container">
    <el-button v-for="tag in tags" :key="tag.category" :class="['tag-button', { active: activeTag === tag.category }]"
      @click="setActiveTag(tag.category)" round>
      <i :class="tag.icon" :style="{ color: tag.color, marginRight: '8px' }"></i>
      <span>{{ tag.description }}</span>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { QuestionBankQuery } from '@/types/questionbank'

const emit = defineEmits<{
  (e: 'category', category: QuestionBankQuery['category']): void;
}>();

const activeTag = ref<string>('');

const tags = ref([
  { description: '全部题目', category: '', icon: 'fas fa-bars', color: '#555555' },
  { description: '算法', category: 'ALGORITHMS', icon: 'fas fa-sitemap', color: '#ff9900' },
  { description: '数据库', category: 'DATABASE', icon: 'fas fa-database', color: '#007bff' },
  { description: 'Shell', category: 'SHELL', icon: 'fas fa-dollar-sign', color: '#28a745' },
  { description: '多线程', category: 'MULTI-THREADING', icon: 'fas fa-sync-alt', color: '#8a2be2' },
  { description: 'JavaScript', category: 'JAVASCRIPT', icon: 'fab fa-js', color: '#f7df1e' },
  { description: 'pandas', category: 'PANDAS', icon: 'fas fa-chart-line', color: '#61DAFB' },
]);

const setActiveTag = (tagName: string) => {
  activeTag.value = tagName;
  emit('category', tagName as QuestionBankQuery['category']);
};
</script>

<style scoped>
.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag-button {
  font-size: 14px;
  padding: 6px 12px;
  height: auto;
  transition: all 0.2s ease;
}

.tag-button.el-button {
  --el-button-text-color: #666666;
  --el-button-bg-color: #f5f5f5;
  --el-button-border-color: transparent;
  --el-button-hover-text-color: #409eff;
  --el-button-hover-bg-color: #ecf5ff;
  --el-button-hover-border-color: transparent;
}

.tag-button.active.el-button {
  --el-button-text-color: #ffffff;
  --el-button-bg-color: #409eff;
  --el-button-border-color: #409eff;
  --el-button-hover-text-color: #ffffff;
  --el-button-hover-bg-color: #337ecc;
  --el-button-hover-border-color: #337ecc;
}

.tag-button i {
  margin-right: 4px;
  font-size: 14px;
}
</style>
