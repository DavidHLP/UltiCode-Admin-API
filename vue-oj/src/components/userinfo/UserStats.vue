<template>
  <div class="user-stats">
    <div class="stats-grid">
      <el-card v-for="stat in stats" :key="stat.type" class="stat-card">
        <div class="stat-header">
          <div class="stat-title">{{ stat.title }}</div>
          <div v-if="stat.percentage" class="stat-change">Top</div>
        </div>
        
        <!-- 热度卡片 -->
        <div v-if="stat.type === 'heat'" class="heat-stat">
          <div class="stat-number">{{ formatNumber(stat.value) }}</div>
          <div class="stat-change">{{ stat.change }}</div>
          <div class="top-percentage">{{ stat.percentage }}</div>
        </div>
        
        <!-- 解题数卡片 - 圆形进度条 -->
        <div v-else-if="stat.type === 'solved'" class="solved-stat">
          <div class="circular-progress">
            <el-progress 
              :percentage="Math.min((Number(stat.value) / 1000) * 100, 100)"
              type="circle"
              :width="120"
              :stroke-width="8"
              stroke-linecap="round"
              color="#fd7e14"
            />
            <div class="progress-text">{{ stat.value }}</div>
          </div>
          <div class="solved-label">已解题数</div>
        </div>
        
        <!-- 其他统计卡片 -->
        <div v-else class="normal-stat">
          <div class="stat-number">{{ formatNumber(stat.value) }}</div>
          <div class="stat-change">{{ stat.change }}</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { StatCard } from '@/types/userinfo'

defineProps<{
  stats: StatCard[]
}>()

const formatNumber = (value: string | number): string => {
  const num = Number(value)
  return num.toLocaleString()
}
</script>

<style scoped lang="scss">
.user-stats {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
  }

  .stat-card {
    :deep(.el-card__body) {
      padding: 16px;
      background: #ffffff;
      border: 1px solid #d0d7de;
      border-radius: 6px;
    }

    .stat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .stat-title {
        font-size: 14px;
        color: #656d76;
      }

      .stat-change {
        font-size: 12px;
        color: #656d76;
      }
    }

    .heat-stat {
      .stat-number {
        font-size: 24px;
        font-weight: 600;
        color: #24292f;
        margin-bottom: 8px;
      }

      .stat-change {
        font-size: 12px;
        color: #656d76;
        margin-bottom: 16px;
      }

      .top-percentage {
        font-size: 32px;
        font-weight: 600;
        color: #24292f;
        text-align: center;
      }
    }

    .solved-stat {
      .circular-progress {
        position: relative;
        display: flex;
        justify-content: center;
        margin-bottom: 16px;

        :deep(.el-progress-circle) {
          .el-progress__text {
            display: none;
          }
        }

        .progress-text {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          font-size: 24px;
          font-weight: 600;
          color: #24292f;
        }
      }

      .solved-label {
        text-align: center;
        color: #656d76;
        font-size: 14px;
      }
    }

    .normal-stat {
      .stat-number {
        font-size: 28px;
        font-weight: 600;
        color: #24292f;
        margin-bottom: 8px;
      }

      .stat-change {
        font-size: 12px;
        color: #656d76;
      }
    }
  }
}
</style>
