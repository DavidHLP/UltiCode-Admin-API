<template>
  <div class="activity-section">
    <!-- 活动统计 -->
    <div class="activity-card">
      <div class="activity-title">
        <span>🌱</span>
        <span>贡献</span>
        <span class="activity-count">{{ heatmap.totalContributions }}</span>
        <span class="year-text">去年共提交</span>
      </div>
      <div class="activity-subtitle">
        过去一年共提交 {{ heatmap.yearContributions }} 次
      </div>
      
      <!-- 月份标签 -->
      <div class="month-labels">
        <span v-for="month in months" :key="month" class="month-label">{{ month }}</span>
      </div>

      <!-- 贡献热力图 -->
      <div class="contribution-heatmap">
        <div 
          v-for="contribution in heatmap.dailyContributions" 
          :key="contribution.date"
          class="contribution-day"
          :class="`level-${contribution.level}`"
          :title="`${contribution.date}: ${contribution.count} 次提交`"
        ></div>
      </div>

      <div class="heatmap-legend">
        <span class="legend-text">少</span>
        <div class="legend-dots">
          <div class="contribution-day level-0"></div>
          <div class="contribution-day level-1"></div>
          <div class="contribution-day level-2"></div>
          <div class="contribution-day level-3"></div>
          <div class="contribution-day level-4"></div>
        </div>
        <span class="legend-text">多</span>
      </div>
    </div>

    <!-- 右侧统计卡片 -->
    <div class="metric-cards">
      <div 
        v-for="card in metricCards" 
        :key="card.name"
        class="metric-card"
      >
        <div class="metric-icon" :style="{ backgroundColor: card.iconBgColor }">
          {{ card.icon }}
        </div>
        <div class="metric-value">{{ card.value }}</div>
        <div class="metric-description">{{ card.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ContributionHeatmap, MetricCard } from '@/types/userinfo'

interface Props {
  heatmap: ContributionHeatmap
  metricCards: MetricCard[]
}

defineProps<Props>()

const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
</script>

<style scoped>
.activity-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.activity-card {
  background: #ffffff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.activity-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
}

.activity-count {
  background: #1f883d;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.year-text {
  color: #cf222e;
  font-size: 12px;
  font-weight: normal;
}

.activity-subtitle {
  color: #656d76;
  font-size: 12px;
  margin-bottom: 16px;
}

.month-labels {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.month-label {
  font-size: 10px;
  color: #656d76;
}

.contribution-heatmap {
  display: grid;
  grid-template-columns: repeat(53, 1fr);
  gap: 2px;
  margin: 16px 0;
}

.contribution-day {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  border: 1px solid #d0d7de;
}

.contribution-day.level-0 { background: #ebedf0; }
.contribution-day.level-1 { background: #9be9a8; }
.contribution-day.level-2 { background: #40c463; }
.contribution-day.level-3 { background: #30a14e; }
.contribution-day.level-4 { background: #216e39; }

.heatmap-legend {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.legend-text {
  font-size: 12px;
  color: #656d76;
}

.legend-dots {
  display: flex;
  gap: 2px;
}

.metric-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-card {
  background: #ffffff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}

.metric-icon {
  width: 32px;
  height: 32px;
  margin: 0 auto 8px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.metric-value {
  font-size: 18px;
  font-weight: 600;
  color: #24292f;
}

.metric-description {
  font-size: 12px;
  color: #656d76;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .activity-section {
    grid-template-columns: 1fr;
  }
  
  .contribution-heatmap {
    grid-template-columns: repeat(26, 1fr);
  }
}
</style>
