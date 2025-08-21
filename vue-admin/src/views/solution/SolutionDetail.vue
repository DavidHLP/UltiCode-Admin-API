<template>
  <div class="solution-detail-page" v-loading="loading">
    <div class="header">
      <div class="title-row">
        <h2 class="title">{{ detail?.title || '题解详情' }}</h2>
        <div class="actions">
          <el-button @click="goBack">返回</el-button>
          <template v-if="status === 'PENDING'">
            <el-button type="success" :loading="actionLoading" @click="handleAccept">设为通过</el-button>
            <el-button type="danger" plain :loading="actionLoading" @click="handleReject">设为不通过</el-button>
          </template>
          <template v-else-if="status === 'APPROVED'">
            <el-button type="danger" plain :loading="actionLoading" @click="handleReject">设为不通过</el-button>
          </template>
          <template v-else-if="status === 'REJECTED'">
            <el-button type="success" :loading="actionLoading" @click="handleAccept">设为通过</el-button>
          </template>
        </div>
      </div>
      <div class="meta">
        <el-tag type="info" size="small">ID #{{ detail?.id }}</el-tag>
        <span class="dot" />
        <span>题目ID：{{ detail?.problemId }}</span>
        <span class="dot" />
        <span>用户ID：{{ detail?.userId }}</span>
        <span class="dot" />
        <el-tag size="small">{{ detail?.language }}</el-tag>
        <span class="dot" />
        <el-tag :type="getStatusTagType(status)" size="small">{{ getStatusText(status) }}</el-tag>
      </div>
    </div>

    <SolutionApprovalSteps :status="status" />

    <div class="content">
      <div class="section-title">内容</div>
      <div class="markdown-content">
        <MdPreview :model-value="contentPreview" :previewTheme="'github'" :theme="'light'" :preview="true" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SolutionApprovalSteps from '@/views/solution/components/SolutionApprovalSteps.vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { fetchSolutionDetail, acceptSolution, rejectSolution } from '@/api/solution'
import type { SolutionStatus } from '@/types/solution.d'

const route = useRoute()
const router = useRouter()

const id = computed(() => Number(route.params.id))
const loading = ref(false)
const actionLoading = ref(false)

interface Detail {
  id: number
  problemId: number
  userId: number
  title: string
  content: string
  language: string
  status?: SolutionStatus
}
const detail = ref<Detail | null>(null)
const status = ref<SolutionStatus>('PENDING')
const contentPreview = ref('')

const loadDetail = async () => {
  loading.value = true
  try {
    const d = await fetchSolutionDetail(id.value)
    detail.value = {
      id: d.id,
      problemId: d.problemId,
      userId: d.userId,
      title: d.title,
      content: d.content,
      language: d.language,
      status: d.status,
    }
    // 以后端返回为准；若后端未返回 status，可保持默认 PENDING
    status.value = (d.status as SolutionStatus) || 'PENDING'
    contentPreview.value = d.content || ''
  } catch (e) {
    console.error(e)
    ElMessage.error('获取题解详情失败')
  } finally {
    loading.value = false
  }
}

const handleAccept = async () => {
  if (!detail.value) return
  actionLoading.value = true
  try {
    await acceptSolution(detail.value.id)
    status.value = 'APPROVED'
    ElMessage.success('已设为通过')
  } catch (e) {
    console.error(e)
    ElMessage.error('设置为通过失败')
  } finally {
    actionLoading.value = false
  }
}

const handleReject = async () => {
  if (!detail.value) return
  actionLoading.value = true
  try {
    await rejectSolution(detail.value.id)
    status.value = 'REJECTED'
    ElMessage.success('已设为不通过')
  } catch (e) {
    console.error(e)
    ElMessage.error('设置为不通过失败')
  } finally {
    actionLoading.value = false
  }
}

const goBack = () => {
  router.back()
}

const getStatusTagType = (s: SolutionStatus) => {
  switch (s) {
    case 'PENDING':
      return 'warning'
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (s: SolutionStatus) => {
  switch (s) {
    case 'PENDING':
      return '待审核'
    case 'APPROVED':
      return '通过'
    case 'REJECTED':
      return '不通过'
    default:
      return s
  }
}

onMounted(() => {
  void loadDetail()
})
</script>

<style scoped>
.solution-detail-page {
  padding: 16px;
}
.header .title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  margin: 0;
}
.actions {
  display: flex;
  gap: 12px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #656d76;
  margin-top: 8px;
}
.dot::before {
  content: '·';
  margin: 0 4px;
  color: #d0d7de;
}
.content {
  margin-top: 20px;
}
.content .section-title {
  margin: 12px 0;
  font-weight: 600;
}
</style>
