<template>
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" class="problem-dialog"
    :close-on-click-modal="false" @close="onClose">
    <el-form :model="currentProblem" :rules="problemRules" ref="problemFormRef" label-position="top"
      class="problem-form">
      <el-tabs v-model="activeDialogTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form-item label="题目标题" prop="title">
            <el-input v-model="currentProblem.title" placeholder="请输入题目标题" />
          </el-form-item>
          <el-form-item label="题目描述" prop="description">
            <MdEditor v-model="currentProblem.description" :theme="theme" style="height: 400px"
              placeholder="请输入题目描述 (支持 Markdown)" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="难度" prop="difficulty">
                <el-select v-model="currentProblem.difficulty" placeholder="请选择难度" style="width: 100%">
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
          <el-form-item label="类别" prop="category">
            <el-select v-model="currentProblem.category" placeholder="请选择题目类别" style="width: 100%">
              <el-option v-for="category in categories" :key="category.category" :label="category.description"
                :value="category.category" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签" prop="tags">
            <el-select v-model="currentProblemTags" multiple filterable allow-create default-first-option
              placeholder="输入或选择标签" style="width: 100%">
            </el-select>
          </el-form-item>
        </el-tab-pane>

        <el-tab-pane label="执行限制" name="limits">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="时间限制 (ms)" prop="timeLimit">
                <el-input-number v-model="currentProblem.timeLimit" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="内存限制 (MB)" prop="memoryLimit">
                <el-input-number v-model="currentProblem.memoryLimit" :min="1" style="width: 100%" />
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
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import type { Problem, Category } from '@/types/problem'
import { createProblem, updateProblem, fetchCategories } from '@/api/problem'

const props = defineProps<{
  visible: boolean
  isEdit: boolean
  problem: Partial<Problem>
}>()

const emit = defineEmits(['update:visible', 'save'])

const dialogVisible = ref(props.visible)
const dialogTitle = ref(props.isEdit ? '编辑题目' : '添加题目')
const currentProblem = ref<Partial<Problem>>({})
const currentProblemTags = ref<string[]>([])
const problemFormRef = ref<FormInstance>()
const activeDialogTab = ref('basic')
const saving = ref(false)
const categories = ref<Category[]>([])
const theme = ref<'light' | 'dark'>('light')

const problemRules: FormRules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  timeLimit: [{ required: true, message: '请输入时间限制', trigger: 'blur' }],
  memoryLimit: [{ required: true, message: '请输入内存限制', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal
    if (newVal) {
      dialogTitle.value = props.isEdit ? '编辑题目' : '添加题目'
      currentProblem.value = { ...props.problem }
      currentProblemTags.value = props.problem.tags ? [...props.problem.tags] : []
      activeDialogTab.value = 'basic'
    }
  }
)

const onClose = () => {
  emit('update:visible', false)
}

const saveProblem = async () => {
  if (!problemFormRef.value) return
  await problemFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const problemToSave = { ...currentProblem.value, tags: currentProblemTags.value }
        if (props.isEdit) {
          problemToSave.id = props.problem.id!
          await updateProblem(problemToSave as Problem)
          ElMessage.success('题目更新成功。')
        } else {
          await createProblem(problemToSave as Problem)
          ElMessage.success('题目创建成功。')
        }
        emit('save')
        onClose()
      } catch (error) {
        console.error('Error saving problem:', error)
        ElMessage.error('保存题目失败。')
      } finally {
        saving.value = false
      }
    }
  })
}

const getCategories = async () => {
  try {
    categories.value = await fetchCategories()
  } catch (error) {
    console.error('Error fetching categories:', error)
    ElMessage.error('获取题目类别失败')
  }
}

onMounted(() => {
  getCategories()
})
</script>

<style scoped>
.problem-dialog .problem-form {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>
