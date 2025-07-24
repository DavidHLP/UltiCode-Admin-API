<template>
  <div class="right-sidebar">
    <!-- 日历组件 -->
    <div class="calendar-widget">
      <div class="widget-title">学习日历</div>
      <el-calendar v-model="calendarValue" class="custom-calendar">
        <template #date-cell="{ data }">
          <div class="calendar-day" :class="getCalendarDayClass(data.day)">
            {{ data.day.split('-').pop() }}
          </div>
        </template>
      </el-calendar>
    </div>

    <!-- 学习统计 -->
    <div class="stats-widget">
      <div class="widget-title">学习统计</div>
      <div class="stats-content">
        <div class="stat-item">
          <div class="stat-label">本周完成</div>
          <div class="stat-value">{{ weeklyCompleted }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">本月完成</div>
          <div class="stat-value">{{ monthlyCompleted }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">总计完成</div>
          <div class="stat-value">{{ totalCompleted }}</div>
        </div>
      </div>
    </div>

    <!-- 推荐题目 -->
    <div class="recommend-widget">
      <div class="widget-title">推荐题目</div>
      <div class="recommend-list">
        <div
          v-for="item in recommendedQuestions"
          :key="item.id"
          class="recommend-item"
          @click="goToQuestion(item.id)"
        >
          <div class="recommend-title">{{ item.title }}</div>
          <el-tag :type="getDifficultyType(item.difficulty)" size="small">
            {{ item.difficulty }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 定义接口
interface RecommendedQuestion {
  id: number
  title: string
  difficulty: string
}

// 定义属性
const props = defineProps<{
  completedCount: number
}>()

// 路由
const router = useRouter()

// 响应式数据
const calendarValue = ref(new Date())
const weeklyCompleted = ref(5)
const monthlyCompleted = ref(23)

// 推荐题目数据
const recommendedQuestions = ref<RecommendedQuestion[]>([
  { id: 11, title: '爬楼梯', difficulty: '简单' },
  { id: 12, title: '买卖股票的最佳时机', difficulty: '简单' },
  { id: 13, title: '最大子序和', difficulty: '简单' },
  { id: 14, title: '合并两个有序链表', difficulty: '简单' },
  { id: 15, title: '有效的括号', difficulty: '简单' }
])

// 计算属性
const totalCompleted = computed(() => props.completedCount)

// 方法
const getDifficultyType = (difficulty: string) => {
  switch (difficulty) {
    case '简单': return 'success'
    case '中等': return 'warning'
    case '困难': return 'danger'
    default: return 'info'
  }
}

const getCalendarDayClass = (day: string) => {
  // 这里可以根据实际的学习记录来设置不同的样式
  const dayNum = parseInt(day.split('-').pop() || '0')
  const today = new Date().getDate()

  if (dayNum === today) return 'today'
  if (dayNum % 7 === 0) return 'active-day'
  if (dayNum % 5 === 0) return 'partial-day'
  return ''
}

const goToQuestion = (questionId: number) => {
  router.push(`/question/${questionId}`)
}
</script>

<style scoped>
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 小部件通用样式 */
.calendar-widget,
.stats-widget,
.recommend-widget {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.calendar-widget:hover,
.stats-widget:hover,
.recommend-widget:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.widget-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.widget-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 16px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  border-radius: 2px;
}

/* 日历小部件 */
.custom-calendar {
  width: 100%;
}

.custom-calendar :deep(.el-calendar__header) {
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}

.custom-calendar :deep(.el-calendar__body) {
  padding: 8px 0;
}

.custom-calendar :deep(.el-calendar-table .el-calendar-day) {
  height: 32px;
  padding: 0;
}

.calendar-day {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  border-radius: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

.calendar-day:hover {
  background: #f0f7ff;
  transform: scale(1.1);
}

.calendar-day.today {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 3px 12px rgba(64, 158, 255, 0.4);
  transform: scale(1.05);
}

.calendar-day.active-day {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.calendar-day.partial-day {
  background: linear-gradient(135deg, #e6f7ff, #f0f7ff);
  color: #409eff;
  font-weight: 600;
  border: 1px solid #b3d8ff;
}

/* 统计小部件 */
.stats-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  opacity: 0;
  transition: opacity 0.3s;
}

.stat-item:hover {
  background: #f0f7ff;
  border-color: #d4edda;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.stat-item:hover::before {
  opacity: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  background: linear-gradient(135deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 推荐题目小部件 */
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.recommend-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 0;
  height: 100%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  transition: width 0.3s;
}

.recommend-item:hover {
  background: #f0f7ff;
  border-color: #d4edda;
  transform: translateX(6px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

.recommend-item:hover::before {
  width: 3px;
}

.recommend-title {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  flex: 1;
  margin-right: 12px;
  line-height: 1.5;
  transition: color 0.3s;
}

.recommend-item:hover .recommend-title {
  color: #374151;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .right-sidebar {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .right-sidebar {
    grid-template-columns: 1fr;
  }
}
</style>
