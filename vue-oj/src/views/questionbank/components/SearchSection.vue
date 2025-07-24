<template>
  <div class="search-section">
    <div class="search-container">
      <el-input
        v-model="searchQuery"
        placeholder="搜索题目"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 定义事件
const emit = defineEmits<{
  search: [query: string]
}>()

// 响应式数据
const searchQuery = ref('')

// 方法
const handleSearch = () => {
  emit('search', searchQuery.value)
  ElMessage.success('搜索完成')
}
</script>

<style scoped>
.search-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-container {
  display: flex;
  gap: 16px;
  align-items: center;
  max-width: 600px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style>
