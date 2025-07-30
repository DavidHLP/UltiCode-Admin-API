<template>
  <div class="solution-table-container">
    <HeaderComponent :search-query="searchQuery" :current-sort="currentSort" @search-change="handleSearchChange"
      @sort-change="handleSortChange" />
    <MainComponent :search-query="searchQuery" :current-sort="currentSort" @solution-click="handleSolutionClick" />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import HeaderComponent from './SolutionTableComponent/HeaderComponent.vue'
import MainComponent from './SolutionTableComponent/MainComponent.vue'
import type { SolutionCardVo } from '@/types/problem';

// 定义事件
const emit = defineEmits<{
  (e: 'row-click', solution: SolutionCardVo): void;
}>()

// 状态管理
const searchQuery = ref('')
const currentSort = ref<'hot' | 'new'>('hot')

// 处理搜索变化
const handleSearchChange = (query: string) => {
  searchQuery.value = query
}

// 处理排序变化
const handleSortChange = (sort: string) => {
  currentSort.value = sort as 'hot' | 'new'
}

// 处理题解点击
const handleSolutionClick = (solution: SolutionCardVo) => {
  emit('row-click', solution)
}
</script>

<style scoped>
.solution-table-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
}
</style>
