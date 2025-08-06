<template>
  <el-table :data="submissions" style="width: 100%">
    <el-table-column label="状态" prop="status">
      <template #default="{ row }">
        <el-tag :type="getStatusTagType(row.status)">{{ row.status }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="语言" prop="language" />
    <el-table-column label="执行用时" prop="timeUsed">
      <template #default="{ row }"> {{ row.timeUsed }} ms </template>
    </el-table-column>
    <el-table-column label="内存消耗" prop="memoryUsed">
      <template #default="{ row }"> {{ row.memoryUsed }} KB </template>
    </el-table-column>
    <el-table-column label="提交时间" prop="createdAt">
      <template #default="{ row }">
        {{ formatDate(row.createdAt) }}
      </template>
    </el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue'
import type { Submission } from '@/types/problem'
import { getStatusTagType } from '@/utils/status'

defineProps<{
  submissions: Submission[]
}>()

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}
</script>
