<template>
  <div>
    <!-- 题目表格 -->
    <div v-infinite-scroll="loadMore" :infinite-scroll-disabled="loading || !hasMore">
      <el-table v-loading="loading" :data="questions" :show-header="false" class="simple-table" stripe
        style="width: 100%" @row-click="handleRowClick">
        <!-- 状态列 -->
        <el-table-column align="center" label="状态" prop="status" width="70">
          <template #default="{ row }">
            <div class="status-icon">
              <span v-if="row.status === 'completed'" class="status-completed">✓</span>
              <span v-else-if="row.status === 'attempted'" class="status-attempted">◑</span>
              <span v-else class="status-not-attempted">-</span>
            </div>
          </template>
        </el-table-column>

        <!-- 题目标题列 -->
        <el-table-column label="题目" min-width="200" prop="title">
          <template #default="{ row }">
            <router-link :to="`/problem/${row.id}`" class="question-title">
              {{ row.id }}. {{ row.title }}
            </router-link>
          </template>
        </el-table-column>

        <el-table-column align="center" label="标签" prop="tags" width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" effect="light" size="" style="margin-right: 8px">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 难度列 -->
        <el-table-column align="center" label="难度" prop="difficulty" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficulty)" effect="light" size="">
              {{ getDifficultyChinese(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 通过率列 -->
        <el-table-column align="center" label="通过率" prop="passRate" width="100">
          <template #default="{ row }">
            <span class="pass-rate">{{ row.passRate.toFixed(1) }}%</span>
          </template>
        </el-table-column>

        <!-- 提交次数列 -->
        <el-table-column align="center" label="提交" prop="submissionCount" width="100">
          <template #default="{ row }">
            <span class="submission-count">{{ formatNumber(row.submissionCount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import type { Problem } from '@/types/problembank.d.ts'

defineProps<{
  questions: Problem[]
  loading: boolean
  hasMore: boolean
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'load-more'): void
}>()

// 路由实例
const router = useRouter()

// 方法
const getDifficultyType = (difficulty: string) => {
  switch (difficulty) {
    case 'EASY':
      return 'success'
    case 'MEDIUM':
      return 'warning'
    case 'HARD':
      return 'danger'
    default:
      return 'info'
  }
}

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + '千'
  }
  return num.toString()
}

// 将英文难度转换为中文
const getDifficultyChinese = (difficulty: string) => {
  const map: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难',
  }
  return map[difficulty]
}

const loadMore = () => {
  emit('load-more')
}

// 处理行点击事件
const handleRowClick = (row: Problem) => {
  router.push(`/problem/${row.id}`)
}
</script>

<style scoped>
/* 表格样式 */
.simple-table {
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

:deep(.el-table td.el-table__cell) {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

:deep(.el-table__body tr:hover > td.el-table__cell) {
  background-color: #fafafa;
  cursor: pointer;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: #fafafa;
}

/* 状态样式 */
.status-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 20px;
  height: 20px;
  margin: 0 auto;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.status-completed {
  font-size: 15px;
  color: #52c41a;
  background-color: #ffffff;
}

.status-attempted {
  font-size: 15px;
  color: #faad14;
  background-color: #ffffff;
}

.status-not-attempted {
  color: #d9d9d9;
  background-color: transparent;
  font-size: 16px;
}

/* 题目样式 */
.question-title {
  color: #262626;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.question-title:hover {
  color: #409eff;
}

/* 难度标签样式 */
:deep(.el-tag) {
  border: none;
  font-weight: 500;
  font-size: 12px;
}

/* 数据样式 */
.pass-rate {
  color: #595959;
  font-weight: 500;
}

.submission-count {
  color: #8c8c8c;
}

/* 加载提示 */
.load-more-tip {
  margin: 24px 0;
  text-align: center;
}

.load-more-tip :deep(.el-divider__text) {
  background-color: #f5f5f5;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 12px;
  color: #8c8c8c;
}
</style>
