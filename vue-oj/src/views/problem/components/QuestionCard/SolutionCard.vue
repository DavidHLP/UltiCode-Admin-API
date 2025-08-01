<template>
  <div class="solution-card">
    <div class="solution-container">
      <router-view
        :problem-id="problemId"
        @vote="handleVote"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { voteSolution } from '@/api/solution';
import type { Store } from 'pinia';
import type { SolutionVo } from '@/types/problem';

const route = useRoute();

// 从路由参数获取 problemId
const problemId = computed(() => Number(route.params.id));

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

// 处理投票
const handleVote = async (solution: SolutionVo, type: 'up' | 'down') => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    return;
  }

  try {
    // 调用投票API
    await voteSolution(solution.id, type);

    // 更新解决方案的投票数
    if (type === 'up') {
      solution.upvotes = (solution.upvotes || 0) + 1;
      ElMessage.success('点赞成功');
    } else {
      solution.downvotes = (solution.downvotes || 0) + 1;
      ElMessage.success('点踩成功');
    }
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
