<template>
  <div class="left-sidebar">

    <!-- 筛选和搜索区域 -->
    <div class="filter-section">
      <div class="filter-controls">
        <div class="filter-item">
          <el-select v-model="difficultyFilter" placeholder="难度" clearable class="filter-select">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </div>

        <div class="filter-item">
          <el-select v-model="statusFilter" placeholder="状态" clearable class="filter-select">
            <el-option label="未开始" value="not-attempted" />
            <el-option label="尝试过" value="attempted" />
            <el-option label="已通过" value="completed" />
          </el-select>
        </div>

        <div class="filter-item">
          <el-select v-model="tagFilter" placeholder="标签" clearable multiple class="filter-select">
            <el-option v-for="tag in availableTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </div>

        <div class="search-item">
          <el-input v-model="searchQuery" placeholder="搜索题目、编号或内容" clearable @keyup.enter="handleSearch"
            class="search-input">
            <template #suffix>
              <el-button :icon="Search" @click="handleSearch" text class="search-btn" />
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 题目表格区域 -->
    <div class="content-section">
      <el-card class="table-card">
        <QuestionTable
          ref="questionTableRef"
          :questions="filteredQuestions"
          :loading="loading"
          @questions-loaded="handleQuestionsLoaded"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import QuestionTable from './QuestionTable.vue'

// 定义接口
interface Question {
  id: number
  title: string
  difficulty: string
  passRate: number
  submitCount: number
  status: 'completed' | 'attempted' | 'not-attempted'
}

// 定义事件
const emit = defineEmits<{
  (e: 'page-change', page: number, size: number): void
  (e: 'questions-loaded', count: number): void
}>()

// 组件引用
const questionTableRef = ref<InstanceType<typeof QuestionTable>>()

// 响应式数据
const searchQuery = ref('')
const difficultyFilter = ref('')
const statusFilter = ref('')
const tagFilter = ref<string[]>([])
const loading = ref(false)
const pageSize = ref(20)
const currentPage = ref(1)

// 可用标签
const availableTags = ref(['数组', '字符串', '动态规划', '二分查找', '贪心算法', '回溯', '树', '图'])

// 题目数据
const questions = ref<Question[]>([
  {
    id: 1,
    title: '两数之和',
    difficulty: '简单',
    passRate: 64.7,
    submitCount: 1234567,
    status: 'completed'
  },
  {
    id: 2,
    title: '两数相加',
    difficulty: '中等',
    passRate: 55.1,
    submitCount: 987654,
    status: 'attempted'
  },
  {
    id: 3,
    title: '无重复字符的最长子串',
    difficulty: '中等',
    passRate: 45.8,
    submitCount: 876543,
    status: 'not-attempted'
  },
  {
    id: 4,
    title: '寻找两个正序数组的中位数',
    difficulty: '困难',
    passRate: 41.4,
    submitCount: 765432,
    status: 'not-attempted'
  },
  {
    id: 5,
    title: '最长回文子串',
    difficulty: '中等',
    passRate: 43.7,
    submitCount: 654321,
    status: 'completed'
  },
  {
    id: 6,
    title: 'Z字形变换',
    difficulty: '中等',
    passRate: 39.7,
    submitCount: 543210,
    status: 'not-attempted'
  },
  {
    id: 7,
    title: '整数反转',
    difficulty: '中等',
    passRate: 35.6,
    submitCount: 432109,
    status: 'not-attempted'
  },
  {
    id: 8,
    title: '字符串转换整数 (atoi)',
    difficulty: '中等',
    passRate: 21.7,
    submitCount: 321098,
    status: 'not-attempted'
  },
  {
    id: 9,
    title: '回文数',
    difficulty: '简单',
    passRate: 58.1,
    submitCount: 210987,
    status: 'not-attempted'
  },
  {
    id: 10,
    title: '正则表达式匹配',
    difficulty: '困难',
    passRate: 30.9,
    submitCount: 109876,
    status: 'not-attempted'
  },
])

// 计算属性
const completedQuestions = computed(() =>
  questions.value.filter(q => q.status === 'completed').length
)

// 过滤后的题目列表
const filteredQuestions = computed(() => {
  let filtered = questions.value

  // 根据搜索关键词过滤
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim()
    filtered = filtered.filter(q =>
      q.title.toLowerCase().includes(query) ||
      q.id.toString().includes(query)
    )
  }

  // 根据难度过滤
  if (difficultyFilter.value) {
    filtered = filtered.filter(q => q.difficulty === difficultyFilter.value)
  }

  // 根据状态过滤
  if (statusFilter.value) {
    filtered = filtered.filter(q => q.status === statusFilter.value)
  }

  // 根据标签过滤（这里是示例，实际需要题目数据包含标签信息）
  if (tagFilter.value.length > 0) {
    // 暂时跳过，因为当前题目数据中没有标签信息
    // 在实际项目中，可以根据题目的标签字段进行过滤
  }

  return filtered
})

// 事件处理方法
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    ElMessage.success(`搜索到 ${filteredQuestions.value.length} 道题目`)
  } else {
    ElMessage.info('请输入搜索关键词')
  }
}

const handlePageChange = (page: number, size: number) => {
  emit('page-change', page, size)
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  console.log('每页大小改变:', size)
}

// 处理题目加载完成事件
const handleQuestionsLoaded = (count: number) => {
  emit('questions-loaded', count)
}

// 加载更多题目（供主页面调用）
const loadMoreQuestions = () => {
  console.log('LeftSidebar: 收到加载更多请求')
  if (questionTableRef.value && !loading.value) {
    loading.value = true
    questionTableRef.value.loadMore()
    // 模拟加载完成
    setTimeout(() => {
      loading.value = false
    }, 300)
  }
}

// 暴露方法给父组件
defineExpose({
  loadMoreQuestions,
  questionTableRef
})

// 生命周期
onMounted(() => {
  console.log('LeftSidebar 组件已加载')
})
</script>

<style scoped>
.left-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
}

/* 页面标题 */
.page-header {
  margin-bottom: 8px;
}

.page-title {
  font-size: 1.5rem;
  line-height: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-description {
  color: #4b5563;
  margin: 0;
  font-size: 0.875rem;
}

/* 筛选区域 */
.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.filter-controls {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-item {
  min-width: 120px;
}

.search-item {
  flex: 1;
  min-width: 200px;
}

.filter-select {
  width: 100%;
}

.search-input {
  width: 100%;
}

/* Element Plus 组件样式优化 */
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

/* 内容区域 */
.content-section {
  flex: 1;
}

.table-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .filter-controls {
    flex-direction: column;
    gap: 12px;
  }

  .filter-item,
  .search-item {
    width: 100%;
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .left-sidebar {
    gap: 16px;
  }

  .filter-section {
    padding: 16px;
  }

  .page-title {
    font-size: 1.25rem;
    line-height: 1.75rem;
  }
}
</style>
