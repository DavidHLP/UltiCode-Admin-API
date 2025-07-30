<template>
  <div class="test-case-management">
    <div class="test-case-header">
      <h4>测试用例列表</h4>
      <el-button type="primary" @click="$emit('add')" :icon="Plus" size="small">
        添加用例
      </el-button>
    </div>
    <el-table :data="testCases" size="small" stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="inputs" label="输入预览" min-width="200">
        <template #default="{ row }">
          <div v-if="row.inputs && row.inputs.length > 0">
            <el-popover placement="top-start" :width="300" trigger="hover">
              <template #reference>
                <el-tag type="info" size="small" class="input-preview-tag">
                  {{ row.inputs.length }} 个输入项
                </el-tag>
              </template>
              <el-descriptions :column="1" border size="small" class="input-descriptions">
                <el-descriptions-item v-for="(input, idx) in row.inputs" :key="idx"
                  :label="input.inputName || `输入 ${idx + 1}`" label-class-name="input-label">
                  <div class="input-value">
                    {{ input.input }}
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </el-popover>
          </div>
          <span v-else class="text-gray-400">无输入</span>
        </template>
      </el-table-column>
      <el-table-column prop="output" label="输出预览" show-overflow-tooltip />
      <el-table-column prop="isSample" label="是否样例" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.isSample ? 'success' : 'info'" size="small">
            {{ scope.row.isSample ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分数" width="100" align="center" />
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
import type { TestCase } from '@/types/testCase';

defineProps({
  testCases: {
    type: Array as () => TestCase[],
    required: true,
    default: () => []
  }
});

defineEmits(['add', 'edit', 'delete']);
</script>

<style scoped>
.test-case-management {
  padding: 10px;
}

.test-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

/* 输入预览样式 */
.input-preview-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.input-preview-tag:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

:deep(.input-descriptions) {
  margin: 5px 0;
  max-width: 100%;
}

:deep(.input-descriptions .el-descriptions__label) {
  width: 100px;
  background-color: #f5f7fa;
  font-weight: 600;
  color: #606266;
}

:deep(.input-descriptions .el-descriptions__content) {
  padding: 8px 12px;
  word-break: break-all;
  white-space: pre-wrap;
}

:deep(.input-descriptions .el-descriptions__cell) {
  padding: 0 !important;
}

:deep(.input-descriptions .el-descriptions__label) {
  padding: 8px 12px !important;
}

.input-value {
  font-family: 'Courier New', Courier, monospace;
  background-color: #f8f9fa;
  padding: 4px 8px;
  border-radius: 3px;
  border-left: 3px solid #7354af;
  margin: 2px 0;
  word-break: break-all;
}

.text-gray-400 {
  color: #c0c4cc;
}
</style>
