<template>
  <div>
    <ManageComponent title="题目管理" :title-icon="Memo" add-button-text="添加题目" search-placeholder="搜索题目标题或标签..."
      empty-text="暂无题目数据" :table-data="problems" :loading="loading" @add="openAddProblemDialog" @search="handleSearch"
      @refresh="refreshData" :total="total" v-model:current-page="currentPage" v-model:page-size="pageSize"
      @size-change="handleSizeChange" @current-change="handleCurrentChange">
      <!-- 自定义筛选器 -->
      <template #filters>
        <el-select v-model="selectedDifficulty" placeholder="筛选难度" class="difficulty-filter" clearable
          @change="handleDifficultyFilter">
          <el-option label="简单" value="EASY" />
          <el-option label="中等" value="MEDIUM" />
          <el-option label="困难" value="HARD" />
        </el-select>
        <el-select v-model="selectedCategory" placeholder="筛选类别" class="category-filter" clearable
          @change="handleCategoryFilter">
          <el-option v-for="category in CATEGORIES" :key="category.category" :label="category.description"
            :value="category.category" />
        </el-select>
        <el-button type="primary" @click="openAddProblemDialog" :icon="Plus" size="default"
          class="add-problem-btn">添加题目</el-button>
      </template>

      <!-- 表格列定义 -->
      <template #table-columns>

        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="scope">
            <el-tag type="info" size="small" class="id-tag"> #{{ scope.row.id }} </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="题目标题" min-width="200">
          <template #default="scope">
            <span class="problem-title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="difficulty" label="难度" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getDifficultyTagType(scope.row.difficulty)" size="small">
              {{ getDifficultyChinese(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="category" label="类别" width="120" align="center">
          <template #default="scope">
            <el-tag type="info" size="small">
              {{ getCategoryDescription(scope.row.category) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="标签" min-width="150">
          <template #default="scope">
            <div class="tags-cell">
              <el-tag v-for="tag in scope.row.tags" :key="tag" size="small" class="problem-tag">
                {{ tag }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button @click="openEditProblemDialog(scope.row)" :icon="Edit" size="small" type="primary" plain
                class="action-btn">
                编辑
              </el-button>
              <el-button @click="handleDeleteProblem(scope.row.id)" :icon="Delete" size="small" type="danger" plain
                class="action-btn">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </template>
    </ManageComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ManageComponent from '@/components/management/ManageComponent.vue'
import { fetchProblemPage, deleteProblem } from '@/api/problem'
import type { Problem } from '@/types/problem.d'
import { CATEGORIES } from '@/types/problem.d'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Memo } from '@element-plus/icons-vue'
import { getDifficultyTagType, getDifficultyChinese } from '@/utils/tag'

const problems = ref<Problem[]>([])
const router = useRouter()
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const selectedDifficulty = ref<string | undefined>()
const selectedCategory = ref<string | undefined>()

const getCategoryDescription = (category: string) => {
  const foundCategory = CATEGORIES.find((cat) => cat.category === category)
  return foundCategory ? foundCategory.description : category
}



const handleSearch = (query: string) => {
  searchQuery.value = query
  currentPage.value = 1
  void getPagedProblems()
}

const handleDifficultyFilter = () => {
  currentPage.value = 1
  void getPagedProblems()
}

const handleCategoryFilter = () => {
  currentPage.value = 1
  void getPagedProblems()
}

const refreshData = async () => {
  loading.value = true
  try {
    await getPagedProblems()
    ElMessage.success('数据刷新成功！')
  } catch (error) {
    console.error('Error refreshing data:', error)
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const getPagedProblems = async () => {
  loading.value = true
  try {
    const pageRes = await fetchProblemPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value || undefined,
      difficulty: selectedDifficulty.value,
      category: selectedCategory.value,
    })
    problems.value = pageRes.records
    total.value = pageRes.total
  } catch (error) {
    console.error('Error fetching problems:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}


const openAddProblemDialog = () => {
  router.push({ name: 'problem-new' })
}

const openEditProblemDialog = (problem: Problem) => {
  router.push({ name: 'problem-edit', params: { id: problem.id } })
}

// 表单保存回调由页面内处理；返回列表后可手动刷新

const handleDeleteProblem = async (problemId: number) => {
  try {
    await deleteProblem(problemId)
    void getPagedProblems()
    ElMessage.success('题目删除成功。')
  } catch (error) {
    console.error('Error deleting problem:', error)
    ElMessage.error('删除题目失败。')
  }
}
// 测试用例与代码模板的增删改查已迁移至 ProblemEditor 页面

onMounted(() => {
  void getPagedProblems()
})

// 分页事件
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  void getPagedProblems()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  void getPagedProblems()
}
</script>

<style scoped>
.difficulty-filter,
.category-filter {
  width: 120px;
}

.add-problem-btn {
  margin-left: 10px;
}

.id-tag {
  font-weight: 600;
  border-radius: 6px;
}

.problem-title {
  font-weight: 500;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  margin: 0 2px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.problem-detail-view {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin: 5px 0;
}

.test-case-management {
  padding: 10px;
}

.test-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.problem-content-display {
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  min-height: 100px;
}

.problem-content-display h4 {
  margin-top: 0;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.problem-tag {
  margin: 2px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-btn {
  margin: 0 2px;
}

.id-tag {
  font-weight: bold;
  min-width: 40px;
  justify-content: center;
}

.problem-title {
  font-weight: 500;
  color: #303133;
}

.difficulty-filter,
.category-filter {
  width: 120px;
  margin-right: 10px;
}

.add-problem-btn {
  margin-left: auto;
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
