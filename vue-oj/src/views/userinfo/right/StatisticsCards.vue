<template>
  <div class="stats-row">
    <div class="stat-card">
      <span class="stat-number">{{ statistics.contributionScore?.toLocaleString() }}</span>
      <div class="stat-label">贡献分数</div>
    </div>
    <div class="stat-card">
      <span class="stat-number">
        {{ statistics.globalRanking?.current?.toLocaleString() }}
        <span class="stat-meta">排名</span>
        {{ statistics.globalRanking?.total?.toLocaleString() }}
        <span class="stat-global">全球排行榜</span>
      </span>
      <div class="stat-label">全球排名</div>
    </div>
    <div class="stat-card">
      <span class="stat-number">{{ statistics.topPercentage }}%</span>
      <div class="stat-label">Top</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { StatisticsData } from '@/types/userinfo'

interface Props {
  statistics: StatisticsData
}

defineProps<Props>()
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #24292f;
  display: block;
  line-height: 1.2;
}

.stat-meta {
  color: #656d76;
  font-size: 12px;
  font-weight: normal;
}

.stat-global {
  color: #1f883d;
  font-size: 12px;
  font-weight: normal;
}

.stat-label {
  color: #656d76;
  font-size: 12px;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
