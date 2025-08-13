<template>
  <div class="solution-table-container">
    <HeaderComponent
      :search-query="searchQuery"
      :current-sort="currentSort"
      @search-change="handleSearchChange"
      @sort-change="handleSortChange"
      @add-solution="handleAddSolution"
    />
    <MainComponent
      ref="mainComponentRef"
      :search-query="searchQuery"
      :current-sort="currentSort"
      @solution-click="handleSolutionClick"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import HeaderComponent from './SolutionTableComponent/HeaderComponent.vue'
import MainComponent from './SolutionTableComponent/MainComponent.vue'
import type { SolutionCardVo } from '@/types/solution';

const router = useRouter()

// 定义属性
const props = defineProps<{
  problemId: number
}>()

// 暴露刷新方法给父组件
const refresh = () => {
  if (mainComponentRef.value?.refresh) {
    return mainComponentRef.value.refresh()
  }
  return Promise.resolve()
}

defineExpose({
  refresh
})

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
  router.push({
    name: 'solution-detail',
    params: { solutionId: solution.id }
  })
}

// 处理添加题解按钮点击
const handleAddSolution = () => {
  router.push({
    name: 'solution-add',
    params: { problemId: props.problemId }
  })
}

const mainComponentRef = ref()

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
