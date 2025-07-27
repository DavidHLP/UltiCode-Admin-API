<template>
  <div class="sort-component">
    <div class="sort-header">
      <span class="sort-title">排序方式</span>
      <el-button size="small" type="primary" link @click="resetSort">
        重置
      </el-button>
    </div>

    <div class="sort-options">
      <div v-for="option in sortOptions" :key="option.value" class="sort-option"
        :class="{ 'active': sortOption === option.value, 'locked': option.locked }"
        @click="handleSortChange(option.value)">
        <span class="option-label">{{ option.label }}</span>
        <el-icon v-if="option.locked" class="lock-icon">
          <Lock />
        </el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import { Lock } from '@element-plus/icons-vue';

const emit = defineEmits<{
  (e: 'sort-change', option: string): void;
}>();

const sortOption = ref('default');

const sortOptions = [
  { value: 'default', label: '默认排序', locked: false },
  { value: 'id', label: '题目编号', locked: false },
  { value: 'difficulty', label: '难度', locked: false },
  { value: 'acceptance', label: '通过率', locked: false },
  { value: 'number', label: '题号', locked: false },
  { value: 'recent_submit', label: '最近提交时间', locked: false },
];

const handleSortChange = (option: string) => {
  const selectedOption = sortOptions.find(opt => opt.value === option);
  if (selectedOption && !selectedOption.locked) {
    sortOption.value = option;
    emit('sort-change', option);
  }
};

const resetSort = () => {
  sortOption.value = 'default';
  emit('sort-change', 'default');
};

// 向父组件暴露获取当前排序状态的方法
defineExpose({
  getSortState: () => ({
    option: sortOption.value
  })
});
</script>

<style scoped>
.sort-component {
  padding: 12px;
  min-width: 150px;
}

.sort-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.sort-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.sort-options {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sort-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  transition: all 0.2s ease;
}

.sort-option:hover:not(.locked) {
  background-color: #f5f5f5;
}

.sort-option.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.sort-option.locked {
  color: #ccc;
  cursor: not-allowed;
}

.option-label {
  flex: 1;
}

.option-actions {
  display: flex;
  gap: 4px;
}

.option-actions .el-button {
  padding: 4px;
  width: 24px;
  height: 24px;
  min-width: 24px;
}

.option-actions .el-button :deep(.el-icon) {
  font-size: 12px;
}

.lock-icon {
  font-size: 12px;
  color: #ffa500;
}
</style>
