<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="800px"
    class="solution-dialog"
    :close-on-click-modal="false"
    @close="onClose"
  >
    <el-form
      :model="currentSolution"
      :rules="solutionRules"
      ref="solutionFormRef"
      label-position="top"
      class="solution-form"
    >
      <el-form-item label="标题" prop="title">
        <el-input v-model="currentSolution.title" placeholder="请输入题解标题" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <MdEditor
          v-model="currentSolution.content"
          :theme="theme"
          style="height: 400px"
          placeholder="请输入题解内容 (支持 Markdown)"
        />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="语言" prop="language">
            <el-input v-model="currentSolution.language" placeholder="例如: Java, Python" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="currentSolution.status" placeholder="请选择状态" style="width: 100%">
              <el-option label="待审核" value="Pending" />
              <el-option label="已通过" value="Approved" />
              <el-option label="已拒绝" value="Rejected" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false" class="cancel-btn"> 取消 </el-button>
        <el-button type="primary" @click="saveSolution" :loading="saving" class="save-btn">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import type { Solution } from '@/types/solution'
import { createSolution, updateSolution } from '@/api/solution'

const props = defineProps<{
  visible: boolean
  isEdit: boolean
  solution: Partial<Solution>
}>()

const emit = defineEmits(['update:visible', 'save'])

const dialogVisible = ref(props.visible)
const dialogTitle = ref(props.isEdit ? '编辑题解' : '添加题解')
const currentSolution = ref<Partial<Solution>>({})
const solutionFormRef = ref<FormInstance>()
const saving = ref(false)
const theme = ref<'light' | 'dark'>('light')

const solutionRules: FormRules = {
  title: [{ required: true, message: '请输入题解标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入题解内容', trigger: 'blur' }],
  language: [{ required: true, message: '请输入编程语言', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal
    if (newVal) {
      dialogTitle.value = props.isEdit ? '编辑题解' : '添加题解'
      currentSolution.value = { ...props.solution }
    }
  }
)

const onClose = () => {
  emit('update:visible', false)
}

const saveSolution = async () => {
  if (!solutionFormRef.value) return
  await solutionFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const solutionToSave = { ...currentSolution.value }
        if (props.isEdit) {
          solutionToSave.id = props.solution.id!
          await updateSolution(solutionToSave as Solution)
          ElMessage.success('题解更新成功。')
        } else {
          await createSolution(solutionToSave as Solution)
          ElMessage.success('题解创建成功。')
        }
        emit('save')
        onClose()
      } catch (error) {
        console.error('Error saving solution:', error)
        ElMessage.error('保存题解失败。')
      } finally {
        saving.value = false
      }
    }
  })
}
</script>

<style scoped>
.solution-dialog .solution-form {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>
