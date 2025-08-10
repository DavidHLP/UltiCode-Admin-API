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
                <el-tab-pane label="测试用例管理">
                  <TestCaseView :test-cases="props.row.testCases" @add="openAddTestCaseDialog(props.row)"
                    @edit="(testCase) => openEditTestCaseDialog(props.row, testCase)"
                    @delete="(testCaseId) => handleDeleteTestCase(props.row, testCaseId)" />
                </el-tab-pane>
                <el-tab-pane label="代码模板管理">
                  <CodeTemplateView :code-templates="props.row.codeTemplates" @add="openAddCodeTemplateDialog(props.row)"
                    @edit="(codeTemplate) => openEditCodeTemplateDialog(props.row, codeTemplate)"
                    @delete="(codeTemplateId) => handleDeleteCodeTemplate(props.row, codeTemplateId)" />
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

    <!-- 代码模板表单对话框（仅在有有效 problemId 时渲染） -->
    <CodeTemplateForm
      v-if="addEditCodeTemplateDialogVisible && activeProblemForCodeTemplates.id"
      :visible="addEditCodeTemplateDialogVisible"
      :is-edit="isEditCodeTemplate"
      :code-template="currentCodeTemplate"
      :problem-id="Number(activeProblemForCodeTemplates.id)"
      @update:visible="addEditCodeTemplateDialogVisible = $event"
      @save="handleCodeTemplateSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import ManageComponent from '@/components/management/ManageComponent.vue'
import { fetchProblems, deleteProblem, fetchCategories, getTestCasesByProblemId, getCodeTemplatesByProblemId, deleteCodeTemplate } from '@/api/problem'
import { deleteTestCase } from '@/api/testCase'
import type { Problem, Category, CodeTemplate } from '@/types/problem'
import type { TestCase } from '@/types/testCase'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Memo } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import ProblemForm from './components/ProblemForm.vue'
import TestCaseForm from './components/TestCaseForm.vue'
import TestCaseView from './components/TestCaseView.vue'
import CodeTemplateForm from './components/CodeTemplateForm.vue'
import CodeTemplateView from './components/CodeTemplateView.vue'

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

const addEditCodeTemplateDialogVisible = ref(false)
const isEditCodeTemplate = ref(false)
const currentCodeTemplate = ref<Partial<CodeTemplate>>({})
const activeProblemForCodeTemplates = ref<Partial<Problem>>({})

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
    const baseProblems = await fetchProblems()
    // 预取每个题目的测试用例与代码模板，取消懒加载
    await Promise.all(
      baseProblems.map(async (p) => {
        try {
          const [testCases, codeTemplates] = await Promise.all([
            getTestCasesByProblemId(p.id!),
            getCodeTemplatesByProblemId(p.id!),
          ])
          p.testCases = testCases
          p.codeTemplates = codeTemplates
        } catch (e) {
          console.error('预加载题目详情失败: ', p.id, e)
        }
      })
    )
    problems.value = baseProblems
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

const openAddCodeTemplateDialog = (problem: Problem) => {
  isEditCodeTemplate.value = false
  activeProblemForCodeTemplates.value = problem
  currentCodeTemplate.value = { problemId: problem.id }
  addEditCodeTemplateDialogVisible.value = true
}

const openEditCodeTemplateDialog = (problem: Problem, codeTemplate: CodeTemplate) => {
  isEditCodeTemplate.value = true
  activeProblemForCodeTemplates.value = problem
  currentCodeTemplate.value = { ...codeTemplate }
  addEditCodeTemplateDialogVisible.value = true
}

const handleCodeTemplateSave = async () => {
  const problemId = activeProblemForCodeTemplates.value.id!;
  const problemWithDetails = await getCodeTemplatesByProblemId(problemId);
  const targetProblem = problems.value.find((p) => p.id === problemId);
  if (targetProblem) {
    targetProblem.codeTemplates = problemWithDetails;
  }
}

const handleDeleteCodeTemplate = async (problem: Problem, codeTemplateId: number) => {
  try {
    await deleteCodeTemplate(codeTemplateId)
    const updatedCodeTemplates = await getCodeTemplatesByProblemId(problem.id!)
    const targetProblem = problems.value.find((p) => p.id === problem.id)
    if (targetProblem) {
      targetProblem.codeTemplates = updatedCodeTemplates
    }
    ElMessage.success('代码模板删除成功。')
  } catch (error) {
    console.error('Error deleting code template:', error)
    ElMessage.error('删除代码模板失败。')
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
