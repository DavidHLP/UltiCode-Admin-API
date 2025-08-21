<template>
  <div class="activity-card">
    <div class="activity-tabs">
      <a 
        v-for="tab in tabs" 
        :key="tab.key"
        href="#" 
        class="activity-tab"
        :class="{ active: tab.active }"
        @click.prevent="$emit('tab-change', tab.key)"
      >
        {{ tab.icon }} {{ tab.name }}
      </a>
    </div>

    <ul class="activity-list">
      <li 
        v-for="activity in activities" 
        :key="activity.id"
        class="activity-item"
      >
        <span class="activity-icon">{{ activity.icon }}</span>
        <span class="activity-text">{{ activity.text }}</span>
        <span class="activity-time">{{ activity.relativeTime }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import type { ActivityItem, ActivityTab } from '@/types/userinfo'

interface Props {
  activities: ActivityItem[]
  tabs: ActivityTab[]
}

defineProps<Props>()
defineEmits<{
  'tab-change': [tabKey: string]
}>()
</script>

<style scoped>
.activity-card {
  background: #ffffff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.activity-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #d0d7de;
}

.activity-tab {
  padding: 8px 0;
  color: #656d76;
  text-decoration: none;
  font-size: 14px;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
}

.activity-tab.active {
  color: #24292f;
  border-bottom-color: #fd7e14;
}

.activity-tab:hover {
  color: #24292f;
}

.activity-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #d0d7de;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 16px;
  height: 16px;
  color: #656d76;
  flex-shrink: 0;
}

.activity-text {
  flex: 1;
  font-size: 14px;
  color: #24292f;
}

.activity-time {
  color: #656d76;
  font-size: 12px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .activity-tabs {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .activity-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .activity-time {
    font-size: 11px;
  }
}
</style>
