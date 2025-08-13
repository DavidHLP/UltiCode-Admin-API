<template>
  <div class="header-section">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-icon v-if="icon">
          <component :is="icon" />
        </el-icon>
        <span v-if="title" class="title">{{ title }}</span>
        <slot name="left" />
      </div>
      <div class="toolbar-right">
        <slot name="right" />
      </div>
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

interface Props {
  title?: string
  icon?: Component | string
}

defineProps<Props>()
</script>

<style scoped>
/* Header 容器样式，与各卡片统一风格 */
.header-section {
  flex-shrink: 0;
  padding: 0 8px;
  display: block;
  background: linear-gradient(180deg, rgba(245, 247, 255, 0.9), rgba(255, 255, 255, 0.9));
  border-bottom: 1px solid #e5e7eb;
}

/* 工具栏布局 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  min-height: 44px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #374151;
}

.title {
  font-size: 14px;
  font-weight: 600;
}

/* 右侧操作按钮的统一圆角与内边距 */
.toolbar-right :deep(.el-button) {
  padding: 6px 8px;
  border-radius: 8px;
}
</style>
