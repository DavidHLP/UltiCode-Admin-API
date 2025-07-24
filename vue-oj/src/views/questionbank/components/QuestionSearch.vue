<template>
  <div class="top-bar-container">
    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索题目"
        class="search-input"
        @keyup.enter="handleSearch"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button :icon="Sort" circle class="icon-button" @click="toggleSort" />
      <el-button :icon="Filter" circle class="icon-button" @click="toggleFilter" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Search, Sort, Filter } from '@element-plus/icons-vue';

const emit = defineEmits<{
  (e: 'search', query: string): void;
  (e: 'sort-toggle'): void;
  (e: 'filter-toggle'): void;
  (e: 'status-change', status: string): void;
}>();

const searchQuery = ref('');
const statusFilter = ref('answered');

const handleSearch = () => {
  emit('search', searchQuery.value);
};

const toggleSort = () => {
  emit('sort-toggle');
};

const toggleFilter = () => {
  emit('filter-toggle');
};

const handleStatusChange = () => {
  emit('status-change', statusFilter.value);
};
</script>

<style scoped>
.top-bar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.search-input.el-input {
  max-width: 300px;
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.2s ease;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.icon-button.el-button {
  width: 32px;
  height: 32px;
  font-size: 16px;
  color: #666666;
}

.icon-button.el-button:hover {
  color: #409eff;
  background-color: #ecf5ff;
}
</style>
