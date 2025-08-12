<template>
  <div>
    <ManageComponent
      :loading="loading"
      :table-data="filteredSolutions"
      :title-icon="Document"
      add-button-text="添加题解"
      empty-text="暂无题解数据"
      search-placeholder="搜索题解标题或内容..."
      title="题解管理"
      @add="openAddSolutionDialog"
      @refresh="refreshData"
      @search="handleSearch"
    >
      <!-- 表格列定义 -->
      <template #table-columns>
        <el-table-column align="center" label="ID" prop="id" width="80">
          <template #default="scope">
            <el-tag class="id-tag" size="small" type="info"> #{{ scope.row.id }} </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="标题" min-width="200" prop="title">
          <template #default="scope">
            <span class="solution-title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column align="center" label="题目ID" prop="problemId" width="100" />
        <el-table-column align="center" label="用户ID" prop="userId" width="100" />

        <el-table-column align="center" label="语言" prop="language" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.language }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column align="center" label="状态" prop="status" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column align="center" fixed="right" label="操作" width="200">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                :icon="Edit"
                class="action-btn"
                plain
                size="small"
                type="primary"
                @click="openEditSolutionDialog(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                :icon="Delete"
                class="action-btn"
                plain
                size="small"
                type="danger"
                @click="handleDeleteSolution(scope.row.id)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </template>
    </ManageComponent>

    <!-- 题解表单对话框 -->
    <SolutionForm
      :is-edit="isEdit"
      :solution="currentSolution"
      :visible="dialogVisible"
      @save="handleSolutionSave"
      @update:visible="dialogVisible = $event"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import ManageComponent from '@/components/management/ManageComponent.vue'
import { deleteSolution, fetchSolutions } from '@/api/solution'
import type { Solution } from '@/types/solution.d'
import { ElMessage } from 'element-plus'
import { Delete, Document, Edit } from '@element-plus/icons-vue'
import SolutionForm from './components/SolutionForm.vue'

const solutions = ref<Solution[]>([])
const loading = ref(false)
const searchQuery = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentSolution = ref<Partial<Solution>>({})

const filteredSolutions = computed(() => {
  let result = solutions.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (solution) =>
        solution.title.toLowerCase().includes(query) ||
        solution.content.toLowerCase().includes(query),
    )
  }

  return result
})

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'Approved':
      return 'success'
    case 'Pending':
      return 'warning'
    case 'Rejected':
      return 'danger'
    default:
      return 'info'
  }
}

const handleSearch = (query: string) => {
  searchQuery.value = query
}

const refreshData = async () => {
  loading.value = true
  try {
    await getSolutions()
    ElMessage.success('数据刷新成功！')
  } catch (error) {
    console.error('Error refreshing data:', error)
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const getSolutions = async () => {
  loading.value = true
  try {
    solutions.value = await fetchSolutions()
  } catch (error) {
    console.error('Error fetching solutions:', error)
    ElMessage.error('获取题解列表失败')
  } finally {
    loading.value = false
  }
}

const openAddSolutionDialog = () => {
  isEdit.value = false
  currentSolution.value = {
    status: 'Pending',
  }
  dialogVisible.value = true
}

const openEditSolutionDialog = (solution: Solution) => {
  isEdit.value = true
  currentSolution.value = { ...solution }
  dialogVisible.value = true
}

const handleSolutionSave = () => {
  getSolutions()
}

const handleDeleteSolution = async (solutionId: number) => {
  try {
    await deleteSolution(solutionId)
    await getSolutions()
    ElMessage.success('题解删除成功。')
  } catch (error) {
    console.error('Error deleting solution:', error)
    ElMessage.error('删除题解失败。')
  }
}

onMounted(() => {
  getSolutions()
})
</script>

<style scoped>
.solution-title {
  font-weight: 500;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.id-tag {
  font-weight: 600;
  border-radius: 6px;
}
</style>
