<template>
  <div class="solution-card">
    <div class="solution-container">
      <router-view :problem-id="problemId" @vote="handleVote" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { voteSolution } from '@/api/solution'
import type { SolutionVo } from '@/types/problem'

const route = useRoute()

// 从路由参数获取 problemId
const problemId = computed(() => Number(route.params.id))
// 处理投票
const handleVote = async (solution: SolutionVo, type: 'up' | 'down') => {
  try {
    // 调用投票API
    await voteSolution(solution.id, type)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (error: any) {
    console.error('投票失败:', error)
    ElMessage.error(error.response?.data?.message || '投票失败，请稍后重试')
  }
}
</script>

<style scoped>
.solution-card {
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
}
</style>
