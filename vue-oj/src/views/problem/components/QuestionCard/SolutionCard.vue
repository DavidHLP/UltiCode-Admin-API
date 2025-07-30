<template>
  <div class="solution-card">
    <div v-if="!currentSolution">
      <SolutionTable :solutions="solutions" @row-click="handleViewSolution" />
    </div>
    <SolutionView v-else :solution="currentSolution" @back="currentSolution = null" @vote="handleVote" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import SolutionTable from './components/SolutionTable.vue';
import SolutionView from './components/SolutionView.vue';
import { getSolutionById, voteSolution } from '@/api/solution';
import type { SolutionCardVo, SolutionVo } from '@/types/problem';
import { useAuthStore } from '@/stores/auth';
import type { Store } from 'pinia';

const route = useRoute();
interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  permissions: string[];
}

const authStore = useAuthStore() as unknown as Store<
  'auth',
  AuthState,
  { isAuthenticated: boolean },
  { hasPermission(permission: string): boolean }
>;

const solutions = ref<SolutionCardVo[]>([]);
const currentSolution = ref<SolutionVo | null>(null);
const loading = ref(false);

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      currentSolution.value = null;
      solutions.value = [];
    }
  },
  { immediate: true }
);

// 查看题解详情
const handleViewSolution = async (solution: SolutionCardVo) => {
  try {
    loading.value = true;
    const res = await getSolutionById(solution.id);
    if (res) {
      currentSolution.value = res;
    } else {
      ElMessage.warning('未找到该题解');
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (error: any) {
    console.error('获取题解详情失败:', error);
    ElMessage.error(error.response?.data?.message || '获取题解详情失败');
  } finally {
    loading.value = false;
  }
};

// 投票
const handleVote = async (type: 'up' | 'down') => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    return;
  }

  if (!currentSolution.value) {
    ElMessage.warning('当前没有选中的题解');
    return;
  }

  const solutionId = currentSolution.value.id;

  try {
    await voteSolution(solutionId, type);

    // 更新UI
    if (type === 'up') {
      currentSolution.value.upvotes = (currentSolution.value.upvotes || 0) + 1;
      ElMessage.success('点赞成功');
    } else {
      currentSolution.value.downvotes = (currentSolution.value.downvotes || 0) + 1;
      ElMessage.success('点踩成功');
    }

    // 更新列表中的点赞数
    const index = solutions.value.findIndex(item => item.id === solutionId);
    if (index !== -1) {
      solutions.value[index].upvotes = currentSolution.value.upvotes;
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (error: any) {
    console.error('投票失败:', error);
    ElMessage.error(error.response?.data?.message || '投票失败，请稍后重试');
  }
};
</script>

<style scoped>
.solution-card {
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
}
</style>
