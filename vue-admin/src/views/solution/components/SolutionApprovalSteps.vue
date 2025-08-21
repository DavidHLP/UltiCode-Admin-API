<template>
  <div class="approval-steps">
    <el-steps :active="active" align-center finish-status="success">
      <el-step title="提交" description="作者已提交题解" />
      <el-step title="待审核" description="管理员审核中" />
      <el-step :title="finalTitle" :description="finalDesc" />
    </el-steps>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import type { SolutionStatus } from '@/types/solution.d'

const props = defineProps<{ status: SolutionStatus }>()

const active = computed(() => {
  switch (props.status) {
    case 'PENDING':
      return 2
    case 'APPROVED':
    case 'REJECTED':
      return 3
    default:
      return 1
  }
})

const finalTitle = computed(() => (props.status === 'APPROVED' ? '通过' : props.status === 'REJECTED' ? '不通过' : '完成'))
const finalDesc = computed(() => (props.status === 'APPROVED' ? '审核通过' : props.status === 'REJECTED' ? '审核不通过' : '等待处理'))
</script>

<style scoped>
.approval-steps {
  margin: 12px 0 16px 0;
}
</style>
