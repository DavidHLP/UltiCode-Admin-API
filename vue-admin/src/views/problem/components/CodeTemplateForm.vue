<template>
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" class="problem-dialog"
    :close-on-click-modal="false" append-to-body @close="onClose">
    <el-form :model="currentCodeTemplate" :rules="codeTemplateRules" ref="codeTemplateFormRef" label-width="120px">
      <el-form-item label="语言" prop="language">
        <el-input v-model="currentCodeTemplate.language" placeholder="请输入编程语言" />
      </el-form-item>
      <el-form-item label="解题模板" prop="solutionTemplate">
        <el-input v-model="currentCodeTemplate.solutionTemplate" type="textarea" :rows="10"
          placeholder="请输入解题模板" />
      </el-form-item>
      <el-form-item label="包装器模板" prop="mainWrapperTemplate">
        <el-input v-model="currentCodeTemplate.mainWrapperTemplate" type="textarea" :rows="10"
          placeholder="请输入包装器模板" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onClose" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="saveCodeTemplate" :loading="saving" class="save-btn">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { CodeTemplate } from '@/types/problem'
import { createCodeTemplate, updateCodeTemplate } from '@/api/problem'

const props = defineProps<{
  visible: boolean
  isEdit: boolean
  codeTemplate: Partial<CodeTemplate>
  problemId: number
}>()

const emit = defineEmits(['update:visible', 'save'])

const dialogVisible = ref(props.visible)
const dialogTitle = ref(props.isEdit ? '编辑代码模板' : '添加代码模板')
const currentCodeTemplate = ref<Partial<CodeTemplate>>({})
const codeTemplateFormRef = ref<FormInstance>()
const saving = ref(false)

const codeTemplateRules: FormRules = {
  language: [{ required: true, message: '请输入编程语言', trigger: 'blur' }],
  solutionTemplate: [{ required: true, message: '请输入解题模板', trigger: 'blur' }],
}

watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal
    if (newVal) {
      dialogTitle.value = props.isEdit ? '编辑代码模板' : '添加代码模板'
      currentCodeTemplate.value = { ...props.codeTemplate, problemId: props.problemId }
    }
  }
)

const onClose = () => {
  emit('update:visible', false)
}

const saveCodeTemplate = async () => {
  if (!codeTemplateFormRef.value) return
  await codeTemplateFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const payload = { ...currentCodeTemplate.value }
        if (props.isEdit) {
          await updateCodeTemplate(payload as CodeTemplate)
          ElMessage.success('代码模板更新成功。')
        } else {
          await createCodeTemplate(payload as CodeTemplate)
          ElMessage.success('代码模板创建成功。')
        }
        emit('save')
        onClose()
      } catch (error) {
        console.error('Error saving code template:', error)
        ElMessage.error('保存代码模板失败。')
      } finally {
        saving.value = false
      }
    }
  })
}
</script>

<style scoped>
.problem-dialog .el-form {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>
