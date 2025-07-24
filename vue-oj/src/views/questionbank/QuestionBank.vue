'''<template>
  <div class="mx-auto max-w-[1200px] min-w-[820px] px-6 py-6">
    <!-- 主要内容区域 -->
    <div class="grid gap-6 grid-cols-3 lg:grid-cols-4">
      <!-- 左侧主内容 -->
      <div class="col-span-2 lg:col-span-3">
        <LeftSidebar
          ref="leftSidebarRef"
          @questions-loaded="handleQuestionsLoaded"
        />
      </div>

      <!-- 右侧边栏 -->
      <div class="col-span-1">
        <div class="space-y-6">
          <RightSidebar :completed-count="completedQuestions" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LeftSidebar from './components/LeftSidebar.vue'
import RightSidebar from './components/RightSidebar.vue'

const completedQuestions = ref(0)

const handleQuestionsLoaded = (count: number) => {
  // This is just an example, you might want to get the *total* completed count
  // from an API call instead of just the loaded count.
  completedQuestions.value = count
}
</script>

<style scoped>
/* 简约风格样式 */
.mb-6 {
  margin-bottom: 1.5rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.flex {
  display: flex;
}

.gap-4 {
  gap: 1rem;
}

.gap-6 {
  gap: 1.5rem;
}

.items-center {
  align-items: center;
}

.w-32 {
  width: 8rem;
}

.w-full {
  width: 100%;
}

.flex-1 {
  flex: 1 1 0%;
}

.grid {
  display: grid;
}

.grid-cols-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (min-width: 1024px) {
  .lg\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

.col-span-2 {
  grid-column: span 2 / span 2;
}

.col-span-1 {
  grid-column: span 1 / span 1;
}

@media (min-width: 1024px) {
  .lg\:col-span-3 {
    grid-column: span 3 / span 3;
  }
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

.max-w-\[1200px\] {
  max-width: 1200px;
}

.min-w-\[820px\] {
  min-width: 820px;
}

.px-6 {
  padding-left: 1.5rem;
  padding-right: 1.5rem;
}

.py-6 {
  padding-top: 1.5rem;
  padding-bottom: 1.5rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.font-semibold {
  font-weight: 600;
}

.text-gray-800 {
  color: #1f2937;
}

.text-gray-600 {
  color: #4b5563;
}

.drop-shadow-sm {
  filter: drop-shadow(0 1px 1px rgb(0 0 0 / 0.05));
}

.space-y-6> :not([hidden])~ :not([hidden]) {
  margin-top: 1.5rem;
}

/* Element Plus 组件样式优化 */
.simple-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

.search-btn {
  padding: 0 8px;
  margin-right: -8px;
  color: #909399;
  transition: color 0.2s;
}

.search-btn:hover {
  color: #409eff;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .grid-cols-3 {
    grid-template-columns: 1fr;
  }

  .col-span-2 {
    grid-column: span 1 / span 1;
  }

  .flex {
    flex-direction: column;
    gap: 0.75rem;
  }

  .w-32 {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .px-6 {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .py-6 {
    padding-top: 1rem;
    padding-bottom: 1rem;
  }

  .text-2xl {
    font-size: 1.25rem;
    line-height: 1.75rem;
  }
}
</style>''
