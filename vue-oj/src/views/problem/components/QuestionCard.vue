<template>
  <div class="question-card-container">
    <div class="header-section">
      <el-tabs v-model="activeTab" class="question-tabs" @tab-click="handleTabChange">
        <el-tab-pane name="description">
          <template #label>
            <div class="tab-label">
              <el-icon>
                <Document />
              </el-icon>
              <span>题目描述</span>
            </div>
          </template>
        </el-tab-pane>
        <el-tab-pane name="solution">
          <template #label>
            <div class="tab-label">
              <el-icon>
                <Promotion />
              </el-icon>
              <span>题解</span>
            </div>
          </template>
        </el-tab-pane>
        <el-tab-pane name="submissions">
          <template #label>
            <div class="tab-label">
              <el-icon>
                <List />
              </el-icon>
              <span>提交记录</span>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="main-content">
      <router-view :problem="problem" :problem-id="problem?.id" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Promotion, List } from '@element-plus/icons-vue'
import type { Problem } from '@/types/problem'
import type { TabsPaneContext } from 'element-plus'

defineProps<{
  problem: Problem | null
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

// 处理标签切换
const handleTabChange = (pane: TabsPaneContext) => {
  const problemId = route.params.id
  const tabName = pane.paneName as string
  switch (tabName) {
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

/* Header 区域样式 */
.header-section {
  flex-shrink: 0;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

/* Tab 样式优化 */
.question-tabs {
  --el-tabs-header-height: 48px;
}

.question-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.question-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0 16px;
}

.question-tabs :deep(.el-tabs__item) {
  height: 48px;
  line-height: 48px;
  padding: 0 16px;
  color: #606266;
  font-weight: 400;
  border: none;
  transition: all 0.2s ease;
}

.question-tabs :deep(.el-tabs__item:hover) {
  color: #409eff;
}

.question-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 500;
}

.question-tabs :deep(.el-tabs__active-bar) {
  height: 2px;
  background-color: #409eff;
}

/* Tab 标签内容样式 */
.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.tab-label .el-icon {
  font-size: 16px;
}

/* Main 内容区域 */
.main-content {
  flex: 1;
  background: #ffffff;
  overflow: hidden;
  min-height: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .question-tabs :deep(.el-tabs__nav-wrap) {
    padding: 0 12px;
  }

  .question-tabs :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 13px;
  }

  .tab-label {
    gap: 4px;
    font-size: 13px;
  }

  .tab-label .el-icon {
    font-size: 14px;
  }
}
</style>
