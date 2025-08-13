<template>
  <div class="question-card-container">
    <HeaderComponent :icon="Document" title="题目">
      <template #right>
        <el-button-group>
          <el-button :type="activeTab === 'description' ? 'primary' : undefined" text @click="go('description')">
            <el-icon><Document /></el-icon>
            <span style="margin-left: 4px">题目描述</span>
          </el-button>
          <el-button :type="activeTab === 'solution' ? 'primary' : undefined" text @click="go('solution')">
            <el-icon><Promotion /></el-icon>
            <span style="margin-left: 4px">题解</span>
          </el-button>
          <el-button :type="activeTab === 'submissions' ? 'primary' : undefined" text @click="go('submissions')">
            <el-icon><List /></el-icon>
            <span style="margin-left: 4px">提交记录</span>
          </el-button>
        </el-button-group>
      </template>
    </HeaderComponent>
    <div class="main-content">
      <router-view :problem="problem" :problem-id="problem?.id" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Promotion, List } from '@element-plus/icons-vue'
import type { ProblemDetailVo } from '@/types/problem'
import HeaderComponent from './components/HeaderComponent.vue'

defineProps<{
  problem: ProblemDetailVo | null
}>()

const route = useRoute()
const router = useRouter()
const activeTab = ref('description')

// 根据当前路由更新激活的标签页
const updateActiveTab = () => {
  const path = route.path
  if (path.includes('/solution')) {
    activeTab.value = 'solution'
  } else if (path.includes('/submissions')) {
    activeTab.value = 'submissions'
  } else {
    activeTab.value = 'description'
  }
}

// 头部按钮跳转
const go = (tab: 'description' | 'solution' | 'submissions') => {
  const problemId = route.params.id
  switch (tab) {
    case 'description':
      router.push({ name: 'problem-description', params: { id: problemId } })
      break
    case 'solution':
      router.push({ name: 'solution-list', params: { id: problemId } })
      break
    case 'submissions':
      router.push({ name: 'problem-submissions', params: { id: problemId } })
      break
  }
}

// 监听路由变化
watch(() => route.path, updateActiveTab, { immediate: true })

onMounted(() => {
  updateActiveTab()
})
</script>

<style scoped>
/* 容器布局 */
.question-card-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* 使用统一 HeaderComponent 样式，无需本地 header/tabs 样式 */

/* Main 内容区域 */
.main-content {
  flex: 1;
  background: #ffffff;
  overflow: hidden;
  min-height: 0;
}

/* 响应式：采用 HeaderComponent 默认适配，无需额外样式 */
</style>
