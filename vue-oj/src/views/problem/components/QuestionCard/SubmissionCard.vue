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
        <template v-else>
          <SubmissionTable :submissions="submissions" @row-click="handleRowClick" />
          <div class="pager">
            <el-config-provider :locale="zhCn">
              <el-pagination
                background
                layout="prev, pager, next, sizes, total"
                :total="total"
                :current-page="page"
                :page-size="size"
                :page-sizes="[5, 10, 20, 50]"
                prev-text="上一页"
                next-text="下一页"
                @current-change="onPageChange"
                @size-change="onSizeChange"
              />
            </el-config-provider>
          </div>
        </template>
      </template>
      <SubmissionView v-else :submission="currentSubmission" @back="currentSubmission = null" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { fetchSubmissionPage, fetchSubmissionDetail } from '@/api/submission'
import type { SubmissionCardVo, SubmissionDetailVo } from '@/types/submission.d'
import SubmissionTable from './components/SubmissionTable.vue'
import SubmissionView from './components/SubmissionView.vue'
import HeaderComponent from '../components/HeaderComponent.vue'
import { Document, RefreshRight, Back } from '@element-plus/icons-vue'

const route = useRoute()

// 从路由参数获取 problemId
const problemId = computed(() => Number(route.params.id))

const submissions = ref<SubmissionCardVo[]>([])
const currentSubmission = ref<SubmissionDetailVo | null>(null)
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

// 获取提交记录
const fetchSubmissions = async () => {
  if (!problemId.value) return

  try {
    loading.value = true
    const res = await fetchSubmissionPage({ problemId: problemId.value, page: page.value, size: size.value })
    submissions.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('获取提交记录失败')
  } finally {
    loading.value = false
  }
}

// 处理行点击
const handleRowClick = async (row: SubmissionCardVo) => {
  try {
    loading.value = true
    const detail = await fetchSubmissionDetail(row.id)
    currentSubmission.value = detail
  } catch (e) {
    console.error('获取提交详情失败:', e)
    ElMessage.error('获取提交详情失败')
  } finally {
    loading.value = false
  }
}

const onPageChange = (p: number) => {
  page.value = p
  fetchSubmissions()
}

const onSizeChange = (s: number) => {
  size.value = s
  page.value = 1
  fetchSubmissions()
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
.submission-container .pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
