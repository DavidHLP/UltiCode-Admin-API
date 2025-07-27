<template>
  <div class="top-bar-container">
    <div class="search-section">
      <el-input v-model="searchQuery" placeholder="搜索题目" class="search-input" @keyup.enter="handleSearch"
        @input="handleSearch" clearable>
        <template #prefix>
          <el-icon>
            <Search />
          </el-icon>
        </template>
      </el-input>
      <el-popover ref="sortPopoverRef" placement="bottom-end" :width="220" trigger="click" popper-class="sort-popover">
        <template #reference>
          <el-button :icon="Sort" circle class="icon-button" />
        </template>
        <SortComponent ref="sortComponentRef" @sort-change="handleSortChange" />
      </el-popover>
      <!-- TODO 后续实现筛选器 -->
      <!-- <el-button :icon="Filter" circle class="icon-button" @click="toggleFilter" /> -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Search, Sort } from '@element-plus/icons-vue';
// TODO 后续实现筛选器后使用Icon
// import { Filter } from '@/assets/icon/Filter.vue';
import SortComponent from './SearchComponent/SortComponent.vue';
import type { PopoverInstance } from 'element-plus';

const emit = defineEmits<{
  (e: 'search', query: string): void;
  (e: 'sort', option: string): void;
}>();

const searchQuery = ref('');
const sortPopoverRef = ref<PopoverInstance | null>(null);
const sortComponentRef = ref<InstanceType<typeof SortComponent> | null>(null);

const handleSearch = () => {
  emit('search', searchQuery.value);
};

const handleSortChange = (option: string) => {
  emit('sort', option);
  // 点击外部时Popover会自动关闭
};

// TODO 后续实现筛选器
// const toggleFilter = () => {
// };
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

<style>
.sort-popover {
  padding: 0 !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
}
</style>
