<template>
  <el-affix :offset="70">
    <div class="right-sidebar">
      <!-- 日历组件 -->
      <div class="calendar-widget">
        <div class="widget-title">学习日历</div>
        <div class="date-header">
          <div class="current-date">
            {{ formatSelectedDate() }}
          </div>
          <el-icon class="refresh-icon" @click="resetToToday">
            <Refresh />
          </el-icon>
        </div>
        <el-config-provider :locale="zhCn">
          <el-calendar v-model="calendarValue" class="custom-calendar">
            <template #date-cell="{ data }">
              <div
                :class="getCalendarDayClass(data.day)"
                class="calendar-day"
                :style="getCalendarDayStyle(data.day)"
              >
                {{ data.day.split('-').pop() }}
              </div>
            </template>
          </el-calendar>
        </el-config-provider>
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
  </el-affix>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { Refresh } from '@element-plus/icons-vue'
import { getSubmissionCalendar } from '@/api/problembank'
import type { SubmissionCalendar } from '@/types/questionbank'

// 定义接口
interface RecommendedQuestion {
  id: number
  title: string
  difficulty: string
}

// 路由
const router = useRouter()

// 响应式数据
const calendarValue = ref(new Date())
const submissionData = ref<SubmissionCalendar[]>([])

// 推荐题目数据
const recommendedQuestions = ref<RecommendedQuestion[]>([
  { id: 11, title: '爬楼梯', difficulty: '简单' },
  { id: 12, title: '买卖股票的最佳时机', difficulty: '简单' },
  { id: 13, title: '最大子序和', difficulty: '简单' },
  { id: 14, title: '合并两个有序链表', difficulty: '简单' },
  { id: 15, title: '有效的括号', difficulty: '简单' },
])

// 方法
onMounted(() => {
  fetchSubmissionCalendar()
})

const fetchSubmissionCalendar = async () => {
  try {
    submissionData.value = await getSubmissionCalendar()
  } catch (error) {
    console.error('获取提交日历失败', error)
  }
}

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

const getCalendarDayClass = (day: string) => {
  const today = new Date()
  const date = new Date(day)
  if (date.toDateString() === today.toDateString()) {
    return 'today'
  }
  return ''
}

const getCalendarDayStyle = (day: string) => {
  const data = submissionData.value.find((item) => item.date === day)
  if (!data) return {}

  const count = data.count
  let opacity = 0
  if (count > 0 && count <= 2) {
    opacity = 0.2
  } else if (count > 2 && count <= 5) {
    opacity = 0.4
  } else if (count > 5 && count <= 10) {
    opacity = 0.6
  } else if (count > 10) {
    opacity = 0.8
  }

  return {
    backgroundColor: `rgba(103, 194, 58, ${opacity})`,
    color: opacity > 0.5 ? '#fff' : 'inherit',
  }
}

const goToQuestion = (questionId: number) => {
  router.push(`/problem/${questionId}`)
}

const formatSelectedDate = () => {
  const selectedDate = calendarValue.value
  const year = selectedDate.getFullYear()
  const month = selectedDate.getMonth() + 1
  const day = selectedDate.getDate()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const weekDay = weekDays[selectedDate.getDay()]

  return `${year}年${month}月${day}日 星期${weekDay}`
}

const resetToToday = () => {
  calendarValue.value = new Date()
}
</script>

<style scoped>
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 日历头部样式 */
.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0 12px 0;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.calendar-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

/* 按钮组样式 */
:deep(.el-button-group) {
  display: flex;
  gap: 2px;
}

:deep(.el-button-group .el-button) {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  transition: all 0.2s ease;
}

:deep(.el-button-group .el-button:hover) {
  color: #409eff;
  border-color: #409eff;
  background-color: #f0f7ff;
}

/* 小部件通用样式 */
.calendar-widget,
.stats-widget,
.recommend-widget {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.calendar-widget:hover,
.stats-widget:hover,
.recommend-widget:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.widget-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.widget-title::before {
  content: '';
  width: 3px;
  height: 16px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  border-radius: 2px;
}

/* 日期头部容器 */
.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 当前日期样式 */
.current-date {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f0f7ff, #e6f7ff);
  border-radius: 8px;
  border: 1px solid #b3d8ff;
  flex: 1;
  text-align: center;
  margin-right: 12px;
}

/* 刷新图标样式 */
.refresh-icon {
  font-size: 16px;
  color: #409eff;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
  background: #f0f7ff;
  border: 1px solid #b3d8ff;
  flex-shrink: 0;
}

.refresh-icon:hover {
  color: #fff;
  background: #409eff;
  border-color: #409eff;
  transform: rotate(180deg);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.refresh-icon:active {
  transform: rotate(180deg) scale(0.95);
}

/* 日历样式 */
.custom-calendar {
  width: 100%;
}

:deep(.el-calendar__header) {
  display: none;
}

:deep(.el-calendar__body) {
  padding: 0;
}

:deep(.el-calendar-table) {
  width: 100%;
}

:deep(.el-calendar-table thead th) {
  padding: 8px 4px;
  color: #6b7280;
  font-weight: 500;
  font-size: 12px;
  text-align: center;
}

:deep(.el-calendar-table .el-calendar-day) {
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
  transition: all 0.2s ease;
  cursor: pointer;
}

.calendar-day:hover {
  background: #f0f7ff;
  color: #409eff;
}

.calendar-day.today {
  background: #409eff;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.calendar-day.active-day {
  background: #67c23a;
  color: #fff;
  font-weight: 600;
}

.calendar-day.partial-day {
  background: #e6f7ff;
  color: #409eff;
  font-weight: 500;
}

/* 统计小部件 */
.stats-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background: #f0f7ff;
  border-color: #409eff;
  transform: translateY(-1px);
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}

/* 推荐题目小部件 */
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.recommend-item:hover {
  background: #f0f7ff;
  border-color: #409eff;
  transform: translateX(4px);
}

.recommend-title {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  flex: 1;
  margin-right: 12px;
}

.recommend-item:hover .recommend-title {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .right-sidebar {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .right-sidebar {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .calendar-widget,
  .stats-widget,
  .recommend-widget {
    padding: 16px;
  }
}
</style>