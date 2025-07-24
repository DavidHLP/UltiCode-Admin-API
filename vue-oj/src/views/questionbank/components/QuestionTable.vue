<template>
  <div>
    <!-- 题目统计信息 -->
    <div class="mb-4 text-sm text-gray-600">
      共 {{ totalQuestions }} 道题目，已加载 {{ displayedQuestions.length }} 道，已完成 {{ completedCount }} 道
    </div>

    <!-- 题目表格 -->
    <div>
      <el-table :data="displayedQuestions" v-loading="loading" stripe style="width: 100%" class="simple-table">
        <!-- 状态列 -->
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <div class="flex justify-center">
              <el-icon v-if="row.status === 'completed'" class="text-green-500 text-lg">
                <Check />
              </el-icon>
              <el-icon v-else-if="row.status === 'attempted'" class="text-yellow-500 text-lg">
                <Clock />
              </el-icon>
              <div v-else class="w-4 h-4 rounded-full border-2 border-gray-300"></div>
            </div>
          </template>
        </el-table-column>

        <!-- 题目标题列 -->
        <el-table-column prop="title" label="题目" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/problem/${row.id}`"
              class="text-gray-800 hover:text-blue-500 font-medium transition-colors cursor-pointer">
              {{ row.id }}. {{ row.title }}
            </router-link>
          </template>
        </el-table-column>

        <!-- 难度列 -->
        <el-table-column prop="difficulty" label="难度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficulty)" size="small" effect="light">
              {{ row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 通过率列 -->
        <el-table-column prop="passRate" label="通过率" width="100" align="center">
          <template #default="{ row }">
            <span class="text-gray-600">{{ row.passRate }}%</span>
          </template>
        </el-table-column>

        <!-- 提交次数列 -->
        <el-table-column prop="submitCount" label="提交" width="100" align="center">
          <template #default="{ row }">
            <span class="text-gray-500">{{ formatNumber(row.submitCount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 加载更多提示 -->
    <div v-if="hasMore && !loading" class="load-more-tip">
      <el-divider>
        <el-text type="info" size="small">滚动加载更多题目</el-text>
      </el-divider>
    </div>

    <!-- 加载完成提示 -->
    <div v-if="!hasMore && displayedQuestions.length > 0" class="load-complete-tip">
      <el-divider>
        <el-text type="success" size="small">✓ 已加载全部题目</el-text>
      </el-divider>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Check, Clock } from '@element-plus/icons-vue'

// 定义接口
interface Question {
  id: number
  title: string
  difficulty: string
  passRate: number
  submitCount: number
  status: 'completed' | 'attempted' | 'not-attempted'
}

// 定义属性
const props = defineProps<{
  questions: Question[]
  loading?: boolean
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'load-more'): void
  (e: 'questions-loaded', count: number): void
}>()

const router = useRouter()

// 无限滚动相关
const displayedQuestions = ref<Question[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)
const totalQuestions = ref(0)



// 计算属性
const completedCount = computed(() =>
  displayedQuestions.value.filter(q => q.status === 'completed').length
)

const infiniteScrollDisabled = computed(() => {
  return props.loading || !hasMore.value
})

// 方法
const getDifficultyType = (difficulty: string) => {
  switch (difficulty) {
    case '简单':
      return 'success'
    case '中等':
      return 'warning'
    case '困难':
      return 'danger'
    default:
      return 'info'
  }
}

const formatNumber = (num: number) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 初始化数据
const initializeData = () => {
  displayedQuestions.value = []
  currentPage.value = 1
  hasMore.value = true
  totalQuestions.value = props.questions.length

  // 初始加载第一页数据
  loadInitialData()
}

// 加载初始数据
const loadInitialData = () => {
  const startIndex = 0
  const endIndex = Math.min(pageSize.value, props.questions.length)
  const newQuestions = props.questions.slice(startIndex, endIndex)

  displayedQuestions.value = newQuestions
  hasMore.value = endIndex < props.questions.length

  emit('questions-loaded', displayedQuestions.value.length)
}

// 加载更多数据
const loadMore = () => {
  if (props.loading || !hasMore.value) return

  const startIndex = displayedQuestions.value.length
  const endIndex = Math.min(startIndex + pageSize.value, props.questions.length)
  const newQuestions = props.questions.slice(startIndex, endIndex)

  if (newQuestions.length > 0) {
    displayedQuestions.value.push(...newQuestions)
    hasMore.value = endIndex < props.questions.length

    emit('questions-loaded', displayedQuestions.value.length)
  } else {
    hasMore.value = false
  }
}

// 重置数据
const resetData = () => {
  initializeData()
}

// 监听题目数据变化，重新初始化
watch(() => props.questions, () => {
  initializeData()
}, { deep: true })

// 组件挂载时初始化数据
onMounted(() => {
  initializeData()
})

// 暴露方法给父组件
defineExpose({
  resetData,
  loadMore,
  displayedQuestions: displayedQuestions.value
})
</script>

<style scoped>
/* 表格样式优化 */
.simple-table {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background-color: #fafafa;
}

:deep(.el-table th.el-table__cell) {
  background-color: #fafafa;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  padding: 12px 8px;
}

:deep(.el-table td.el-table__cell) {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: #fafbfc;
}

:deep(.el-table__body tr:hover > td.el-table__cell) {
  background-color: #f9fafb !important;
}

/* 分页样式 */
:deep(.el-pagination) {
  --el-pagination-font-size: 14px;
  --el-pagination-bg-color: #fff;
  --el-pagination-text-color: #606266;
}

/* 链接样式 */
a {
  text-decoration: none;
}

a:hover {
  text-decoration: none;
}

/* 简约风格调整 */
.mb-4 {
  margin-bottom: 1rem;
}

.mt-6 {
  margin-top: 1.5rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-gray-600 {
  color: #6b7280;
}

.text-gray-500 {
  color: #9ca3af;
}

.text-gray-800 {
  color: #1f2937;
}

.text-green-500 {
  color: #10b981;
}

.text-yellow-500 {
  color: #f59e0b;
}

.font-medium {
  font-weight: 500;
}

.transition-colors {
  transition: color 0.2s ease;
}

.cursor-pointer {
  cursor: pointer;
}

.flex {
  display: flex;
}

.justify-center {
  justify-content: center;
}

.w-4 {
  width: 1rem;
}

.h-4 {
  height: 1rem;
}

.rounded-full {
  border-radius: 9999px;
}

.border-2 {
  border-width: 2px;
}

.border-gray-300 {
  border-color: #d1d5db;
}

.text-lg {
  font-size: 1.125rem;
}

/* 无限滚动提示样式 */
.load-more-tip {
  margin: 20px 0;
  text-align: center;
}

.load-more-tip :deep(.el-divider__text) {
  background-color: #f8f9fa;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 12px;
  color: #909399;
}

.load-complete-tip {
  margin: 20px 0;
  text-align: center;
}

.load-complete-tip :deep(.el-divider__text) {
  background-color: #f0f9ff;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 12px;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

/* 表格滚动样式优化 */
:deep(.el-table__body-wrapper) {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 3px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: #c1c1c1;
  border-radius: 3px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #a8a8a8;
}
</style>
