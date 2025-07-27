<template>
  <div>
    <ManageComponent title="题目管理" :title-icon="Memo" add-button-text="添加题目" search-placeholder="搜索题目标题或标签..."
      empty-text="暂无题目数据" :table-data="filteredProblems" :loading="loading" @add="openAddProblemDialog"
      @search="handleSearch" @refresh="refreshData">
      <!-- 自定义筛选器 -->
      <template #filters>
        <el-select v-model="selectedDifficulty" placeholder="筛选难度" class="difficulty-filter" clearable
          @change="handleDifficultyFilter">
          <el-option label="简单" value="Easy" />
          <el-option label="中等" value="Medium" />
          <el-option label="困难" value="Hard" />
        </el-select>
        <el-select v-model="selectedCategory" placeholder="筛选类别" class="category-filter" clearable
          @change="handleCategoryFilter">
          <el-option v-for="category in categories" :key="category.category" :label="category.description"
            :value="category.category" />
        </el-select>
        <el-button type="primary" @click="openAddProblemDialog" :icon="Plus" size="default"
          class="add-problem-btn">添加题目</el-button>
      </template>

      <!-- 表格列定义 -->
      <template #table-columns>
        <el-table-column type="expand">
          <template #default="props">
            <div class="problem-detail-view">
              <el-tabs type="border-card">
                <el-tab-pane label="测试用例管理" :lazy="true">
                  <template #label>
                    <span @click="loadTestCases(props.row)">测试用例管理</span>
                  </template>
                  <div class="test-case-management">
                    <div class="test-case-header">
                      <h4>测试用例列表</h4>
                      <el-button type="primary" @click="openAddTestCaseDialog(props.row)" :icon="Plus" size="small">
                        添加用例
                      </el-button>
                    </div>
                    <el-table :data="props.row.testCases" size="small" stripe>
                      <el-table-column prop="id" label="ID" width="80" align="center" />
                      <el-table-column prop="input" label="输入预览" show-overflow-tooltip />
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
                          <el-button @click="openEditTestCaseDialog(props.row, scope.row)" :icon="Edit" size="small"
                            type="primary" plain />
                          <el-button @click="handleDeleteTestCase(props.row, scope.row.id)" :icon="Delete" size="small"
                            type="danger" plain />
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="题目详情">
                  <div class="problem-content-display">
                    <div v-if="props.row.description">
                      <h4>题目描述</h4>
                      <MdPreview :modelValue="props.row.description" />
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </template>
        </el-table-column>

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
              {{ scope.row.difficulty }}
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

        <el-table-column prop="timeLimit" label="时间限制" width="120" align="center">
          <template #default="scope">
            <span>{{ scope.row.timeLimit }}ms</span>
          </template>
        </el-table-column>

        <el-table-column prop="memoryLimit" label="内存限制" width="120" align="center">
          <template #default="scope">
            <span>{{ scope.row.memoryLimit }}MB</span>
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

    <!-- 题目表单对话框 -->
    <ProblemForm :visible="dialogVisible" :is-edit="isEdit" :problem="currentProblem"
      @update:visible="dialogVisible = $event" @save="handleProblemSave" />

    <!-- 测试用例表单对话框 -->
    <TestCaseForm :visible="addEditTestCaseDialogVisible" :is-edit="isEditTestCase" :test-case="currentTestCase"
      @update:visible="addEditTestCaseDialogVisible = $event" @save="handleTestCaseSave" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import ManageComponent from '@/components/management/ManageComponent.vue'
import { fetchProblems, deleteProblem, fetchCategories, getTestCasesByProblemId } from '@/api/problem'
import { deleteTestCase } from '@/api/testCase'
import type { Problem, Category } from '@/types/problem'
import type { TestCase } from '@/types/testCase'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Memo } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import ProblemForm from './components/ProblemForm.vue'
import TestCaseForm from './components/TestCaseForm.vue'

const problems = ref<Problem[]>([])
const loading = ref(false)
const searchQuery = ref('')
const selectedDifficulty = ref<string | undefined>()
const selectedCategory = ref<string | undefined>()
const categories = ref<Category[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentProblem = ref<Partial<Problem>>({})

const addEditTestCaseDialogVisible = ref(false)
const isEditTestCase = ref(false)
const currentTestCase = ref<Partial<TestCase>>({})
const activeProblemForTestCases = ref<Partial<Problem>>({})

const filteredProblems = computed(() => {
  let result = problems.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (problem) =>
        problem.title.toLowerCase().includes(query) ||
        (problem.tags && problem.tags.some((tag) => tag.toLowerCase().includes(query)))
    )
  }

  if (selectedDifficulty.value) {
    result = result.filter((problem) => problem.difficulty === selectedDifficulty.value)
  }

  if (selectedCategory.value) {
    result = result.filter((problem) => problem.category === selectedCategory.value)
  }

  return result
})

const getDifficultyTagType = (difficulty: string) => {
  switch (difficulty) {
    case 'Easy':
      return 'success'
    case 'Medium':
      return 'warning'
    case 'Hard':
      return 'danger'
    default:
      return 'info'
  }
}

const getCategoryDescription = (category: string) => {
  const foundCategory = categories.value.find((cat) => cat.category === category)
  return foundCategory ? foundCategory.description : category
}

const loadTestCases = async (problem: Problem) => {
  if (!problem.testCases) {
    try {
      const testCases = await getTestCasesByProblemId(problem.id!)
      problem.testCases = testCases
    } catch (error) {
      console.error('Error fetching test cases:', error)
      ElMessage.error('获取测试用例失败')
    }
  }
}

const handleSearch = (query: string) => {
  searchQuery.value = query
}

const handleDifficultyFilter = () => { }

const handleCategoryFilter = () => { }

const refreshData = async () => {
  loading.value = true
  try {
    await getProblems()
    ElMessage.success('数据刷新成功！')
  } catch (error) {
    console.error('Error refreshing data:', error)
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const getProblems = async () => {
  loading.value = true
  try {
    problems.value = await fetchProblems()
    console.log(problems.value)
  } catch (error) {
    console.error('Error fetching problems:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

const getCategories = async () => {
  try {
    categories.value = await fetchCategories()
  } catch (error) {
    console.error('Error fetching categories:', error)
    ElMessage.error('获取题目类别失败')
  }
}

const openAddProblemDialog = () => {
  isEdit.value = false
  currentProblem.value = {
    isVisible: true,
    timeLimit: 1000,
    memoryLimit: 128,
    difficulty: 'Easy',
    category: '',
  }
  dialogVisible.value = true
}

const openEditProblemDialog = (problem: Problem) => {
  isEdit.value = true
  currentProblem.value = { ...problem }
  dialogVisible.value = true
}

const handleProblemSave = () => {
  getProblems()
}

const handleDeleteProblem = async (problemId: number) => {
  try {
    await deleteProblem(problemId)
    getProblems()
    ElMessage.success('题目删除成功。')
  } catch (error) {
    console.error('Error deleting problem:', error)
    ElMessage.error('删除题目失败。')
  }
}

const openAddTestCaseDialog = (problem: Problem) => {
  isEditTestCase.value = false
  activeProblemForTestCases.value = problem
  currentTestCase.value = { problemId: problem.id, score: 10 }
  addEditTestCaseDialogVisible.value = true
}

const openEditTestCaseDialog = (problem: Problem, testCase: TestCase) => {
  isEditTestCase.value = true
  activeProblemForTestCases.value = problem
  currentTestCase.value = { ...testCase }
  addEditTestCaseDialogVisible.value = true
}

const handleTestCaseSave = async () => {
  const problemId = activeProblemForTestCases.value.id!;
  const problemWithDetails = await getTestCasesByProblemId(problemId);
  const targetProblem = problems.value.find((p) => p.id === problemId);
  if (targetProblem) {
    targetProblem.testCases = problemWithDetails;
  }
}

const handleDeleteTestCase = async (problem: Problem, testCaseId: number) => {
  try {
    await deleteTestCase(testCaseId)
    const updatedTestCases = await getTestCasesByProblemId(problem.id!)
    const targetProblem = problems.value.find((p) => p.id === problem.id)
    if (targetProblem) {
      targetProblem.testCases = updatedTestCases
    }
    ElMessage.success('测试用例删除成功。')
  } catch (error) {
    console.error('Error deleting test case:', error)
    ElMessage.error('删除测试用例失败。')
  }
}

onMounted(() => {
  getProblems()
  getCategories()
})
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
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.problem-detail-view {
  padding: 20px;
  background-color: #fcfcfc;
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

.problem-content-display h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #333;
}

.problem-content-display p,
.problem-content-display pre {
  margin-top: 0;
  margin-bottom: 15px;
  line-height: 1.6;
  color: #555;
}

.problem-content-display pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.problem-tag {
  border-radius: 6px;
  font-weight: 500;
}
</style>
