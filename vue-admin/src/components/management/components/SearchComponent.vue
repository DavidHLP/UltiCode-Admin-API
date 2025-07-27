<template>
  <div class="search-container">
    <div class="search-input">
      <el-input v-model="searchQuery" :placeholder="placeholder" clearable @keyup.enter="$emit('search', searchQuery)">
        <template #append>
          <el-button :icon="Search" @click="$emit('search', searchQuery)">
            搜索
          </el-button>
        </template>
      </el-input>
    </div>
    <div class="filter-actions">
      <slot name="filters"></slot>
      <el-button :icon="Refresh" @click="$emit('refresh')">
        刷新
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh } from '@element-plus/icons-vue'

defineProps<{
  placeholder?: string
  modelValue?: string
}>()

defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'search', query: string): void
  (e: 'refresh'): void
}>()

const searchQuery = defineModel<string>('modelValue', { default: '' })
</script>

<style>
.search-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md);
  background-color: var(--bg-secondary);
  border-radius: var(--border-radius-medium);
  box-shadow: var(--shadow-light);
}

.search-input {
  flex: 1;
  max-width: 500px;
}

.filter-actions {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}
</style>
