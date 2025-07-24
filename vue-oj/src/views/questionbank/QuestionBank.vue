<template>
  <div class="mx-auto max-w-[1200px] min-w-[820px] px-6 py-6">
    <!-- 主要内容区域 -->
    <div class="grid gap-6 grid-cols-3 lg:grid-cols-4">
      <!-- 左侧主内容 -->
      <div class="col-span-2 lg:col-span-3">
        <LeftSidebar
          ref="leftSidebarRef"
          @page-change="handlePageChange"
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
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 导入子组件
import LeftSidebar from './components/LeftSidebar.vue'
import RightSidebar from './components/RightSidebar.vue'

// 组件引用
const leftSidebarRef = ref<InstanceType<typeof LeftSidebar>>()

// 计算属性 - 从 LeftSidebar 组件中获取完成数量
const completedQuestions = ref(2) // 示例数据，实际应该从 LeftSidebar 传递

// 无限滚动相关状态
const loading = ref(false)
const hasMore = ref(true)
const isLoadingMore = ref(false)

// 事件处理方法
const handlePageChange = (page: number, size: number) => {
  console.log('分页改变:', page, size)
  // 这里可以添加分页逻辑或者发送到后端
}

// 处理题目加载完成事件
const handleQuestionsLoaded = (count: number) => {
  console.log('已加载题目数量:', count)
  completedQuestions.value = count // 更新完成数量
}

// 页面滚动监听器
const handleScroll = () => {
  if (isLoadingMore.value || !hasMore.value) return
  
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 当滚动到距离底部100px时触发加载
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadMore()
  }
}

// 无限滚动加载更多
const loadMore = () => {
  console.log('触发页面级无限滚动加载')
  if (isLoadingMore.value || !hasMore.value) return
  
  isLoadingMore.value = true
  
  if (leftSidebarRef.value) {
    // 调用 LeftSidebar 组件的加载更多方法
    leftSidebarRef.value.loadMoreQuestions()
  }
  
  // 模拟加载完成（实际应该在数据加载完成后设置）
  setTimeout(() => {
    isLoadingMore.value = false
  }, 500)
}

// 生命周期
onMounted(() => {
  console.log('题库页面已加载')
  // 添加页面滚动监听器
  window.addEventListener('scroll', handleScroll, { passive: true })
  // 这里可以添加数据初始化逻辑，比如从API获取题目数据
})

// 组件卸载时移除滚动监听器
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
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
</style>
