<template>
  <div class="submission-card">
    <HeaderComponent :icon="Document" title="提交记录">
      <template #right>
        <el-space size="small" wrap>
          <el-button text size="small" @click="fetchSubmissions">
            <el-icon><RefreshRight /></el-icon>
            <span style="margin-left: 4px">刷新</span>
          </el-button>
          <el-button v-if="currentSubmission" text size="small" @click="currentSubmission = null">
            <el-icon><Back /></el-icon>
            <span style="margin-left: 4px">返回列表</span>
          </el-button>
        </el-space>
      </template>
    </HeaderComponent>
    <div v-loading="loading" class="submission-container">
      <template v-if="!currentSubmission">
        <el-empty v-if="submissions.length === 0 && !loading" description="暂无提交记录" />
        <SubmissionTable v-else :submissions="submissions" @row-click="handleRowClick" />
      </template>
      <SubmissionView v-else :submission="currentSubmission" @back="currentSubmission = null" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSubmissionsByProblemId } from '@/api/submission'
import type { Submission } from '@/types/problem'
import SubmissionTable from './components/SubmissionTable.vue'
import SubmissionView from './components/SubmissionView.vue'
import HeaderComponent from '../components/HeaderComponent.vue'
import { Document, RefreshRight, Back } from '@element-plus/icons-vue'

const route = useRoute()

// 从路由参数获取 problemId
const problemId = computed(() => Number(route.params.id))

const submissions = ref<Submission[]>([])
const currentSubmission = ref<Submission | null>(null)
const loading = ref(false)

// 获取提交记录
const fetchSubmissions = async () => {
  if (!problemId.value) return

  try {
    loading.value = true
    const res: Submission[] = await getSubmissionsByProblemId(problemId.value)
    submissions.value = res.map((submission: Submission) => ({
      ...submission,
      memoryUsed: submission.memoryUsed ? Number((submission.memoryUsed / 1024).toFixed(2)) : 0
    }))
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('获取提交记录失败')
  } finally {
    loading.value = false
  }
}

// 处理行点击
const handleRowClick = (submission: Submission) => {
  currentSubmission.value = submission
}

// 监听路由参数变化
watch(
  () => problemId.value,
  (newId) => {
    if (newId && !isNaN(newId)) {
      currentSubmission.value = null
      fetchSubmissions()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.submission-card {
  padding: 16px;
}
</style>
