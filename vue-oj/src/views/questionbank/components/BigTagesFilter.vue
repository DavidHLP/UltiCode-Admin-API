<template>
  <div class="tag-container">
    <el-button
      v-for="tag in tags"
      :key="tag.name"
      :class="['tag-button', { active: activeTag === tag.name }]"
      @click="setActiveTag(tag.name)"
      round
    >
      <i :class="tag.icon" :style="{ color: tag.color, marginRight: '8px' }"></i>
      <span>{{ tag.name }}</span>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const activeTag = ref('全部题目');

// TODO: 后续后端实现更多功能后前端开放
const tags = ref([
  { name: '全部题目', icon: 'fas fa-bars', color: '#555555' },
  { name: '算法', icon: 'fas fa-sitemap', color: '#ff9900' },
  // { name: '数据库', icon: 'fas fa-database', color: '#007bff' },
  // { name: 'Shell', icon: 'fas fa-dollar-sign', color: '#28a745' },
  // { name: '多线程', icon: 'fas fa-sync-alt', color: '#8a2be2' },
  // { name: 'JavaScript', icon: 'fab fa-js', color: '#f7df1e' },
  // { name: 'pandas', icon: 'fas fa-chart-line', color: '#61DAFB' },
]);

const setActiveTag = (tagName: string) => {
  activeTag.value = tagName;
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

