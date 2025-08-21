<template>
  <div>
    <ManageComponent
      title="题解管理"
      :title-icon="Memo"
      search-placeholder="搜索标题、内容或语言"
      empty-text="暂无题解数据"
      :table-data="solutions"
      :loading="loading"
      :total="total"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      @search="applySearch"
      @refresh="refreshData"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    >
      <!-- 筛选器 -->
      <template #filters>
        <el-input
          v-model="problemIdText"
          placeholder="题目ID"
          clearable
          class="id-filter"
          @clear="applySearch"
          @input="applySearchDebounced"
        />

        <el-input
          v-model="userIdText"
          placeholder="用户ID"
          clearable
          class="id-filter"
          @clear="applySearch"
          @input="applySearchDebounced"
        />

        <el-select
          v-model="selectedStatus"
          placeholder="筛选状态"
          clearable
          class="status-filter"
          @change="onStatusChange"
        >
          <el-option label="待审核" :value="'PENDING'" />
          <el-option label="通过" :value="'APPROVED'" />
          <el-option label="不通过" :value="'REJECTED'" />
        </el-select>
      </template>

      <!-- 表格列 -->
      <template #table-columns>
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">#{{ row.id }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="problemId" label="题目ID" width="100" align="center" />

        <el-table-column label="作者" min-width="180">
          <template #default="{ row }">
            <div class="author-cell">
              <el-avatar
                :src="row.authorAvatar || defaultUserAvatar"
                size="small"
                class="mr-8"
              />
              <span>{{ row.authorUsername || '匿名用户' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="220" />

        <el-table-column prop="language" label="语言" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.language }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </template>
    </ManageComponent>
    <!-- 详情页已改为独立路由页面 -->
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Memo } from '@element-plus/icons-vue'
import ManageComponent from '@/components/management/ManageComponent.vue'
import { fetchSolutionManagementPage } from '@/api/solution'
import type { SolutionManagementCard, SolutionStatus } from '@/types/solution.d'
import type { PageResult } from '@/types/commons'

const router = useRouter()
const solutions = ref<SolutionManagementCard[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const selectedStatus = ref<SolutionStatus | undefined>(undefined)
const problemIdText = ref('')
const userIdText = ref('')

// 将环境变量读取放在脚本中，避免在模板中直接使用 import.meta
// 统一使用 .env/.d.ts 中声明的 VITE_DEFAULT_USER_AVATAR
const defaultUserAvatar: string = import.meta.env.VITE_DEFAULT_USER_AVATAR || ''

// 独立详情页，不再在列表页维护详情状态

const toNumberOrUndefined = (s: string) => {
  const v = Number((s || '').trim())
  if (!Number.isFinite(v) || v <= 0) return undefined
  return v
}

const getPagedSolutions = async () => {
  loading.value = true
  try {
    const pid = toNumberOrUndefined(problemIdText.value)
    const uid = toNumberOrUndefined(userIdText.value)
    // 当使用 ID 精确筛选时，禁用关键词搜索，避免条件污染
    const keywordParam = pid || uid ? undefined : (searchQuery.value || undefined)
    const res: PageResult<SolutionManagementCard> = await fetchSolutionManagementPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keywordParam,
      status: selectedStatus.value || undefined,
      problemId: pid,
      userId: uid
    })
    solutions.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
    ElMessage.error('获取题解列表失败')
  } finally {
    loading.value = false
  }
}

const applySearch = (query?: string) => {
  if (typeof query === 'string') {
    searchQuery.value = query
  }
  currentPage.value = 1
  void getPagedSolutions()
}

// 简易防抖，避免频繁请求
const debounce = <T extends (...args: unknown[]) => void>(fn: T, delay = 300) => {
  let timer: ReturnType<typeof setTimeout> | null = null
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
    }, delay)
  }
}

const applySearchDebounced = debounce(() => applySearch(), 300)

// 状态变更只触发刷新，不修改关键字，防止将状态字符串污染为 keyword
const onStatusChange = () => {
  currentPage.value = 1
  void getPagedSolutions()
}

const refreshData = async () => {
  loading.value = true
  try {
    await getPagedSolutions()
    ElMessage.success('数据刷新成功！')
  } catch {
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  void getPagedSolutions()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  void getPagedSolutions()
}

const getStatusTagType = (status: SolutionStatus) => {
  switch (status) {
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

const getStatusText = (status: SolutionStatus) => {
  switch (status) {
    case 'PENDING':
      return '待审核'
    case 'APPROVED':
      return '通过'
    case 'REJECTED':
      return '不通过'
    default:
      return status
  }
}

const openDetail = (row: SolutionManagementCard) => {
  router.push({ name: 'solution-detail', params: { id: row.id } })
}

onMounted(() => {
  void getPagedSolutions()
})
</script>

<style scoped>
.status-filter {
  width: 140px;
  margin-right: 10px;
}
.id-filter {
  width: 140px;
  margin-right: 10px;
}
.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.mr-8 {
  margin-right: 8px;
}

</style>
