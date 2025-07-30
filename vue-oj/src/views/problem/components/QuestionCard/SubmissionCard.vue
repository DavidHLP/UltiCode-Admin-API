<template>
  <div class="submission-card">
    <el-table :data="submissions" style="width: 100%">
      <el-table-column label="状态" prop="status">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="语言" prop="language" />
      <el-table-column label="执行用时" prop="timeUsed" />
      <el-table-column label="内存消耗" prop="memoryUsed" />
      <el-table-column label="提交时间" prop="createdAt">
        <template #default="{ row }">
          {{ new Date(row.createdAt).toLocaleString() }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, onMounted, ref } from 'vue'
import { getSubmissionsByProblemId } from '@/api/submission'
import type { Submission } from '@/types/problem'

const props = defineProps<{
  problemId: number
}>()

const submissions = ref<Submission[]>([])

const fetchSubmissions = async () => {
  try {
    submissions.value = await getSubmissionsByProblemId(props.problemId)
  } catch (error) {
    console.error('Failed to fetch submissions:', error)
  }
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'Accepted':
      return 'success'
    case 'Wrong Answer':
      return 'danger'
    default:
      return 'warning'
  }
}

onMounted(() => {
  fetchSubmissions()
})
</script>

<style scoped>
.submission-card {
  padding: 16px;
}
</style>
