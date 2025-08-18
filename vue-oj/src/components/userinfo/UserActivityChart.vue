<template>
  <el-card class="activity-chart">
    <div class="chart-header">
      <span>过去一年共提交 {{ totalSubmissions }} 次</span>
      <span class="chart-subtitle">
        最少贡献: {{ minContribution }} 最多贡献: {{ maxContribution }} 过去一年
      </span>
    </div>

    <div class="month-labels">
      <span v-for="month in months" :key="month">{{ month }}</span>
    </div>

    <div class="activity-grid">
      <el-tooltip
        v-for="(day, index) in activity" 
        :key="index"
        :content="`${day.date}: ${day.count} 次提交`"
        placement="top"
      >
        <div 
          :class="['activity-day', `level-${day.level}`]"
          @click="handleDayClick(day)"
        />
      </el-tooltip>
    </div>

    <div class="legend">
      <span class="legend-label">较少</span>
      <div class="legend-colors">
        <div class="legend-color level-0"></div>
        <div class="legend-color level-1"></div>
        <div class="legend-color level-2"></div>
        <div class="legend-color level-3"></div>
        <div class="legend-color level-4"></div>
      </div>
      <span class="legend-label">较多</span>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { ActivityDay } from '@/types/userinfo'

const props = defineProps<{
  activity: ActivityDay[]
}>()

const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

const totalSubmissions = computed(() => {
  return props.activity.reduce((total, day) => total + day.count, 0)
})

const minContribution = computed(() => {
  const nonZeroCounts = props.activity.filter(day => day.count > 0).map(day => day.count)
  return nonZeroCounts.length > 0 ? Math.min(...nonZeroCounts) : 0
})

const maxContribution = computed(() => {
  return Math.max(...props.activity.map(day => day.count))
})

const handleDayClick = (day: ActivityDay) => {
  if (day.count > 0) {
    ElMessage.success(`${day.date} 提交了 ${day.count} 次`)
  }
}
</script>

<style scoped lang="scss">
.activity-chart {
  :deep(.el-card__body) {
    padding: 16px;
    background: #ffffff;
    border: 1px solid #d0d7de;
    border-radius: 6px;
  }

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    color: #24292f;
    font-size: 14px;

    .chart-subtitle {
      color: #656d76;
      font-size: 12px;
    }
  }

  .month-labels {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 16px;
    font-size: 12px;
    color: #656d76;
    margin-bottom: 8px;
    text-align: center;
  }

  .activity-grid {
    display: grid;
    grid-template-columns: repeat(53, 1fr);
    gap: 3px;
    margin: 16px 0;

    .activity-day {
      width: 11px;
      height: 11px;
      border-radius: 2px;
      background: #ebedf0;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        outline: 1px solid #656d76;
      }

      &.level-0 {
        background: #ebedf0;
      }

      &.level-1 {
        background: #9be9a8;
      }

      &.level-2 {
        background: #40c463;
      }

      &.level-3 {
        background: #30a14e;
      }

      &.level-4 {
        background: #216e39;
      }
    }
  }

  .legend {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 16px;

    .legend-label {
      font-size: 12px;
      color: #656d76;
    }

    .legend-colors {
      display: flex;
      gap: 2px;

      .legend-color {
        width: 10px;
        height: 10px;
        border-radius: 2px;

        &.level-0 {
          background: #ebedf0;
        }

        &.level-1 {
          background: #9be9a8;
        }

        &.level-2 {
          background: #40c463;
        }

        &.level-3 {
          background: #30a14e;
        }

        &.level-4 {
          background: #216e39;
        }
      }
    }
  }
}
</style>
