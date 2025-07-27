<template>
  <div class="header-container">
    <div class="header-title">
      <el-icon v-if="icon" class="header-icon">
        <component :is="icon" />
      </el-icon>
      <span>{{ title }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

interface Props {
  title?: string
  icon?: Component
  showAddButton?: boolean
  addButtonText?: string
  addButtonIcon?: Component
}

defineProps<Props>()
</script>

<style>
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md);
  background: var(--primary-gradient);
  border-radius: var(--border-radius-medium);
  color: var(--text-white);
  box-shadow: var(--shadow-light);
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 1.5rem;
  font-weight: 600;
}

.header-icon {
  margin-right: var(--spacing-sm);
  font-size: 1.8rem;
}
</style>
