<template>
  <div class="code-template-view">
    <div class="code-template-header">
      <h4>代码模板列表</h4>
      <el-button type="primary" @click="$emit('add')" :icon="Plus" size="small">
        添加模板
      </el-button>
    </div>
    <el-table :data="codeTemplates" size="small" stripe>
      <el-table-column prop="language" label="语言" width="120" align="center" />
      <el-table-column prop="solutionTemplate" label="解题模板" show-overflow-tooltip />
      <el-table-column prop="mainWrapperTemplate" label="包装器模板" show-overflow-tooltip />
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button @click="$emit('edit', scope.row)" :icon="Edit" size="small" type="primary" plain />
          <el-button @click="$emit('delete', scope.row.id)" :icon="Delete" size="small" type="danger" plain />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits } from 'vue';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import type { CodeTemplate } from '@/types/problem';

defineProps({
  codeTemplates: {
    type: Array as () => CodeTemplate[],
    required: true,
    default: () => []
  }
});

defineEmits(['add', 'edit', 'delete']);
</script>

<style scoped>
.code-template-view {
  padding: 10px;
}

.code-template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
</style>
