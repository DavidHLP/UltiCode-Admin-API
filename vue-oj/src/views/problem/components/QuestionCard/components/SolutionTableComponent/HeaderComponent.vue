<template>
  <div class="filter-toolbar-container">
    <div class="left-panel">
      <el-input :model-value="searchQuery" @update:model-value="handleSearchInput" placeholder="搜索"
        :prefix-icon="Search" clearable class="search-input" />
    </div>

    <div class="right-panel">
      <el-space :size="20">
        <el-dropdown @command="handleSortChange">
          <span class="dropdown-trigger">
            <el-icon :size="16">
              <Sort />
            </el-icon>
            <span>{{ currentSortLabel }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="hot" :class="{ 'is-active': currentSort === 'hot' }">
                热度最高
              </el-dropdown-item>
              <el-dropdown-item command="new" :class="{ 'is-active': currentSort === 'new' }">
                最新优先
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button :icon="Finished" text bg aria-label="切换视图" />
        <el-button type="primary" @click="handleAddSolution">
          <el-icon><Plus /></el-icon> 新增题解
        </el-button>
      </el-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Search, Sort, Finished, Plus } from '@element-plus/icons-vue'

// 定义 props
interface Props {
  searchQuery: string
  currentSort: string
}

const props = defineProps<Props>()

// 定义 emits
const emit = defineEmits<{
  searchChange: [query: string]
  sortChange: [sort: string]
  addSolution: []
}>()

// 定义排序选项的文本映射
const sortOptions: Record<string, string> = {
  hot: '热度最高',
  new: '最新优先',
}

// 计算属性，用于显示当前选择的排序文本
const currentSortLabel = computed(() => sortOptions[props.currentSort] || '热度最高')

// 处理搜索输入变化
const handleSearchInput = (value: string) => {
  emit('searchChange', value)
}

// 下拉菜单项点击事件处理
const handleSortChange = (command: string | number | object) => {
  if (typeof command === 'string') {
    emit('sortChange', command)
  }
}

// 处理新增题解按钮点击
const handleAddSolution = () => {
  emit('addSolution')
}
</script>

<style scoped>
.filter-toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.left-panel .search-input {
  width: 320px;
}

.right-panel {
  display: flex;
  align-items: center;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  /* 图标和文字的间距 */
  font-size: 14px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  outline: none;
  /* 移除点击时的轮廓 */
}

/* 自定义下拉菜单选中项的样式 */
.el-dropdown-menu__item.is-active {
  color: var(--el-color-primary);
  font-weight: bold;
}
</style>
