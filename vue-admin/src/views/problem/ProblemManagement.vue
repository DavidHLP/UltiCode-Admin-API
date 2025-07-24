<template>
  <div class="problem-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2 class="page-title">
            <el-icon class="title-icon"><Memo /></el-icon>
            题目管理
          </h2>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            @click="openAddProblemDialog"
            :icon="Plus"
            class="add-btn"
            size="large"
          >
            添加题目
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-card class="search-card" shadow="never">
        <div class="search-form">
          <el-input
            v-model="searchQuery"
            placeholder="搜索题目标题或标签..."
            :prefix-icon="Search"
            class="search-input"
            clearable
            @input="handleSearch"
          />
          <el-select
            v-model="selectedDifficulty"
            placeholder="筛选难度"
            class="difficulty-filter"
            clearable
            @change="handleDifficultyFilter"
          >
            <el-option label="简单" value="Easy" />
            <el-option label="中等" value="Medium" />
            <el-option label="困难" value="Hard" />
          </el-select>
          <el-button :icon="Refresh" @click="refreshData" class="refresh-btn"> 刷新 </el-button>
        </div>
      </el-card>
    </div>

    <!-- 题目表格 -->
    <div class="table-section">
      <el-card class="table-card" shadow="never">
        <el-table
          :data="filteredProblems"
          class="modern-table"
          stripe
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
          v-loading="loading"
          @expand-change="handleExpandChange"
          row-key="id"
        >
          <!-- 展开行 -->
          <el-table-column type="expand">
            <template #default="props">
              <div class="problem-detail-view">
                <el-tabs type="border-card">
                  <el-tab-pane label="测试用例管理">
                    <div class="test-case-management">
                      <div class="test-case-header">
                        <h4>测试用例列表</h4>
                        <el-button
                          type="primary"
                          @click="openAddTestCaseDialog(props.row)"
                          :icon="Plus"
                          size="small"
                        >
                          添加用例
                        </el-button>
                      </div>
                      <el-table :data="props.row.testCases" size="small" stripe>
                        <el-table-column prop="id" label="ID" width="80" align="center" />
                        <el-table-column prop="inputContent" label="输入预览" show-overflow-tooltip />
                        <el-table-column prop="outputContent" label="输出预览" show-overflow-tooltip />
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
                            <el-button
                              @click="openEditTestCaseDialog(scope.row)"
                              :icon="Edit"
                              size="small"
                              type="primary"
                              plain
                            />
                            <el-button
                              @click="handleDeleteTestCase(props.row, scope.row.id)"
                              :icon="Delete"
                              size="small"
                              type="danger"
                              plain
                            />
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="题目详情">
                    <div class="problem-content-display">
                      <div v-if="props.row.description">
                        <h4>题目描述</h4>
                        <p>{{ props.row.description }}</p>
                      </div>
                      <div v-if="props.row.inputFormat">
                        <h4>输入格式</h4>
                        <p>{{ props.row.inputFormat }}</p>
                      </div>
                      <div v-if="props.row.outputFormat">
                        <h4>输出格式</h4>
                        <p>{{ props.row.outputFormat }}</p>
                      </div>
                      <div v-if="props.row.sampleInput">
                        <h4>样例输入</h4>
                        <pre>{{ props.row.sampleInput }}</pre>
                      </div>
                      <div v-if="props.row.sampleOutput">
                        <h4>样例输出</h4>
                        <pre>{{ props.row.sampleOutput }}</pre>
                      </div>
                      <div v-if="props.row.hint">
                        <h4>提示</h4>
                        <p>{{ props.row.hint }}</p>
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

          <el-table-column label="标签" min-width="150">
            <template #default="scope">
              <div class="tags-cell">
                <el-tag
                  v-for="tag in scope.row.tags"
                  :key="tag"
                  size="small"
                  class="problem-tag"
                >
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

          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <div class="action-buttons">
                <el-button
                  @click="openEditProblemDialog(scope.row)"
                  :icon="Edit"
                  size="small"
                  type="primary"
                  plain
                  class="action-btn"
                >
                  编辑
                </el-button>
                <el-button
                  @click="handleDeleteProblem(scope.row.id)"
                  :icon="Delete"
                  size="small"
                  type="danger"
                  plain
                  class="action-btn"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="filteredProblems.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无题目数据" />
        </div>
      </el-card>
    </div>

    <!-- 现代化题目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      class="problem-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        :model="currentProblem"
        :rules="problemRules"
        ref="problemFormRef"
        label-position="top"
        class="problem-form"
      >
        <el-tabs v-model="activeDialogTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-form-item label="题目标题" prop="title">
              <el-input v-model="currentProblem.title" placeholder="请输入题目标题" />
            </el-form-item>
            <el-form-item label="题目描述" prop="description">
              <el-input
                v-model="currentProblem.description"
                type="textarea"
                :rows="10"
                placeholder="请输入题目描述 (支持 Markdown)"
              />
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="难度" prop="difficulty">
                  <el-select
                    v-model="currentProblem.difficulty"
                    placeholder="请选择难度"
                    style="width: 100%"
                  >
                    <el-option label="简单" value="Easy" />
                    <el-option label="中等" value="Medium" />
                    <el-option label="困难" value="Hard" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="是否可见" prop="isVisible">
                  <el-switch v-model="currentProblem.isVisible" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="currentProblemTags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入或选择标签"
                style="width: 100%"
              >
              </el-select>
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="内容详情" name="content">
            <el-form-item label="输入格式" prop="inputFormat">
              <el-input
                v-model="currentProblem.inputFormat"
                type="textarea"
                :rows="4"
                placeholder="请输入输入格式"
              />
            </el-form-item>
            <el-form-item label="输出格式" prop="outputFormat">
              <el-input
                v-model="currentProblem.outputFormat"
                type="textarea"
                :rows="4"
                placeholder="请输入输出格式"
              />
            </el-form-item>
            <el-form-item label="样例输入" prop="sampleInput">
              <el-input
                v-model="currentProblem.sampleInput"
                type="textarea"
                :rows="4"
                placeholder="请输入样例输入"
              />
            </el-form-item>
            <el-form-item label="样例输出" prop="sampleOutput">
              <el-input
                v-model="currentProblem.sampleOutput"
                type="textarea"
                :rows="4"
                placeholder="请输入样例输出"
              />
            </el-form-item>
            <el-form-item label="提示" prop="hint">
              <el-input
                v-model="currentProblem.hint"
                type="textarea"
                :rows="4"
                placeholder="请输入提示"
              />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="执行限制" name="limits">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="时间限制 (ms)" prop="timeLimit">
                  <el-input-number
                    v-model="currentProblem.timeLimit"
                    :min="1"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="内存限制 (MB)" prop="memoryLimit">
                  <el-input-number
                    v-model="currentProblem.memoryLimit"
                    :min="1"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn"> 取消 </el-button>
          <el-button type="primary" @click="saveProblem" :loading="saving" class="save-btn">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加/编辑测试用例对话框 -->
    <el-dialog
      v-model="addEditTestCaseDialogVisible"
      :title="testCaseDialogTitle"
      width="600px"
      class="problem-dialog"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form
        :model="currentTestCase"
        :rules="testCaseRules"
        ref="testCaseFormRef"
        label-width="80px"
      >
        <el-form-item label="输入" prop="inputContent">
          <el-input
            v-model="currentTestCase.inputContent"
            type="textarea"
            :rows="5"
            placeholder="请输入测试输入"
          />
        </el-form-item>
        <el-form-item label="输出" prop="outputContent">
          <el-input
            v-model="currentTestCase.outputContent"
            type="textarea"
            :rows="5"
            placeholder="请输入期望输出"
          />
        </el-form-item>
        <el-form-item label="分数" prop="score">
          <el-input-number v-model="currentTestCase.score" :min="0" />
        </el-form-item>
        <el-form-item label="是否样例" prop="isSample">
          <el-switch v-model="currentTestCase.isSample" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addEditTestCaseDialogVisible = false" class="cancel-btn"
            >取消</el-button
          >
          <el-button
            type="primary"
            @click="saveTestCase"
            :loading="savingTestCase"
            class="save-btn"
          >
            {{ isEditTestCase ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  fetchProblems,
  createProblem,
  updateProblem,
  deleteProblem,
  getProblem,
} from '@/api/problem'
import { createTestCase, updateTestCase, deleteTestCase, getTestCaseContent } from '@/api/testCase'
import type { Problem } from '@/types/problem'
import type { TestCase } from '@/types/testCase'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, Memo } from '@element-plus/icons-vue'

const problems = ref<Problem[]>([])
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const selectedDifficulty = ref<string | undefined>()

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const currentProblem = ref<Partial<Problem>>({})
const currentProblemTags = ref<string[]>([])
const problemFormRef = ref<FormInstance>()
const activeDialogTab = ref('basic')

const addEditTestCaseDialogVisible = ref(false)
const isEditTestCase = ref(false)
const testCaseDialogTitle = ref('')
const currentTestCase = ref<Partial<TestCase>>({})
const testCaseFormRef = ref<FormInstance>()
const savingTestCase = ref(false)
const activeProblemForTestCases = ref<Partial<Problem>>({})

const problemRules: FormRules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  timeLimit: [{ required: true, message: '请输入时间限制', trigger: 'blur' }],
  memoryLimit: [{ required: true, message: '请输入内存限制', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

const testCaseRules: FormRules = {
  inputContent: [{ required: true, message: '请输入测试输入', trigger: 'blur' }],
  outputContent: [{ required: true, message: '请输入期望输出', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分数', trigger: 'blur' }],
}



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

const handleSearch = () => {}

const handleDifficultyFilter = () => {}

const refreshData = async () => {
  loading.value = true
  try {
    await getProblems()
    ElMessage.success('数据刷新成功！')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const getProblems = async () => {
  loading.value = true
  try {
    problems.value = await fetchProblems()
  } catch (error) {
    console.error('Error fetching problems:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

const openAddProblemDialog = () => {
  isEdit.value = false
  dialogTitle.value = '添加题目'
  currentProblem.value = { isVisible: true, timeLimit: 1000, memoryLimit: 128, difficulty: 'Easy' }
  currentProblemTags.value = []
  activeDialogTab.value = 'basic'
  dialogVisible.value = true
}

const openEditProblemDialog = (problem: Problem) => {
  isEdit.value = true
  dialogTitle.value = '编辑题目'
  currentProblem.value = { ...problem }
  currentProblemTags.value = problem.tags ? [...problem.tags] : []
  activeDialogTab.value = 'basic'
  dialogVisible.value = true
}

const saveProblem = async () => {
  if (!problemFormRef.value) return
  await problemFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const problemToSave = { ...currentProblem.value, tags: currentProblemTags.value }

        if (isEdit.value) {
          await updateProblem(problemToSave.id!, problemToSave as Problem)
          ElMessage.success('题目更新成功。')
        } else {
          await createProblem(problemToSave as Problem)
          ElMessage.success('题目创建成功。')
        }
        dialogVisible.value = false
        getProblems()
      } catch (error) {
        console.error('Error saving problem:', error)
        ElMessage.error('保存题目失败。')
      } finally {
        saving.value = false
      }
    }
  })
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

const handleExpandChange = async (row: Problem, expandedRows: Problem[]) => {
  const isExpanded = expandedRows.some((r) => r.id === row.id)
  if (isExpanded && !row.testCases) {
    try {
      const problemWithDetails = await getProblem(row.id)
      const targetProblem = problems.value.find((p) => p.id === row.id)
      if (targetProblem) {
        targetProblem.testCases = problemWithDetails.testCases
      }
    } catch (error) {
      ElMessage.error('获取测试用例失败')
    }
  }
}

const openAddTestCaseDialog = (problem: Problem) => {
  isEditTestCase.value = false
  testCaseDialogTitle.value = '添加测试用例'
  activeProblemForTestCases.value = problem
  currentTestCase.value = { problemId: problem.id, score: 10 }
  addEditTestCaseDialogVisible.value = true
}

const openEditTestCaseDialog = async (testCase: TestCase) => {
  isEditTestCase.value = true
  testCaseDialogTitle.value = '编辑测试用例'
  currentTestCase.value = { ...testCase }
  addEditTestCaseDialogVisible.value = true

  try {
    const content = await getTestCaseContent(testCase.id)
    currentTestCase.value.inputContent = content.inputContent
    currentTestCase.value.outputContent = content.outputContent
  } catch (error) {
    ElMessage.error('获取测试用例内容失败')
  }
}

const saveTestCase = async () => {
  if (!testCaseFormRef.value) return
  await testCaseFormRef.value.validate(async (valid) => {
    if (valid) {
      savingTestCase.value = true
      try {
        const payload = {
          ...currentTestCase.value,
          inputContent: currentTestCase.value.inputContent || '',
          outputContent: currentTestCase.value.outputContent || '',
        }

        if (isEditTestCase.value) {
          await updateTestCase(currentTestCase.value.id!, payload as TestCase)
          ElMessage.success('测试用例更新成功。')
        } else {
          await createTestCase(payload as TestCase)
          ElMessage.success('测试用例创建成功。')
        }
        addEditTestCaseDialogVisible.value = false
        const updatedProblem = await getProblem(activeProblemForTestCases.value.id!)
        const targetProblem = problems.value.find((p) => p.id === activeProblemForTestCases.value.id)
        if (targetProblem) {
          targetProblem.testCases = updatedProblem.testCases
        }
      } catch (error) {
        console.error('Error saving test case:', error)
        ElMessage.error('保存测试用例失败。')
      } finally {
        savingTestCase.value = false
      }
    }
  })
}

const handleDeleteTestCase = async (problem: Problem, testCaseId: number) => {
  try {
    await deleteTestCase(testCaseId)
    const updatedProblem = await getProblem(problem.id!)
    const targetProblem = problems.value.find((p) => p.id === problem.id)
    if (targetProblem) {
      targetProblem.testCases = updatedProblem.testCases
    }
    ElMessage.success('测试用例删除成功。')
  } catch (error) {
    console.error('Error deleting test case:', error)
    ElMessage.error('删除测试用例失败。')
  }
}

onMounted(() => {
  getProblems()
})
</script>

<style scoped>
@import './index.css';
.unit-label {
  margin-left: 10px;
}
</style>
''
