<template>
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" class="problem-dialog"
    :close-on-click-modal="false" append-to-body @close="onClose">
    <el-form :model="currentTestCase" :rules="testCaseRules" ref="testCaseFormRef" label-width="80px">
      <el-form-item label="输入" prop="input">
        <el-input v-model="currentTestCase.input" type="textarea" :rows="5" placeholder="请输入测试输入" />
      </el-form-item>
      <el-form-item label="输出" prop="output">
        <el-input v-model="currentTestCase.output" type="textarea" :rows="5" placeholder="请输入期望输出" />
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
        <el-button @click="onClose" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="saveTestCase" :loading="savingTestCase" class="save-btn">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { TestCase } from '@/types/testCase'
import { createTestCase, updateTestCase } from '@/api/testCase'

const props = defineProps<{
  visible: boolean
  isEdit: boolean
  testCase: Partial<TestCase>
}>()

const emit = defineEmits(['update:visible', 'save'])

const dialogVisible = ref(props.visible)
const dialogTitle = ref(props.isEdit ? '编辑测试用例' : '添加测试用例')
const currentTestCase = ref<Partial<TestCase>>({})
const testCaseFormRef = ref<FormInstance>()
const savingTestCase = ref(false)

const testCaseRules: FormRules = {
  input: [{ required: true, message: '请输入测试输入', trigger: 'blur' }],
  output: [{ required: true, message: '请输入期望输出', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分数', trigger: 'blur' }],
}

watch(
  () => props.visible,
  async (newVal) => {
    dialogVisible.value = newVal
    if (newVal) {
      dialogTitle.value = props.isEdit ? '编辑测试用例' : '添加测试用例'
      currentTestCase.value = { ...props.testCase }
    }
  }
)

const onClose = () => {
  emit('update:visible', false)
}

const saveTestCase = async () => {
  if (!testCaseFormRef.value) return
  await testCaseFormRef.value.validate(async (valid) => {
    if (valid) {
      savingTestCase.value = true
      try {
        const payload = { ...currentTestCase.value }

        if (props.isEdit) {
          payload.id = currentTestCase.value.id!
          await updateTestCase(payload as TestCase)
          ElMessage.success('测试用例更新成功。')
        } else {
          await createTestCase(payload as TestCase)
          ElMessage.success('测试用例创建成功。')
        }
        emit('save')
        onClose()
      } catch (error) {
        console.error('Error saving test case:', error)
        ElMessage.error('保存测试用例失败。')
      } finally {
        savingTestCase.value = false
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
