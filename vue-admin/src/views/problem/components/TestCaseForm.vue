<template>
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" class="problem-dialog"
    :close-on-click-modal="false" append-to-body @close="onClose">
    <el-form :model="currentTestCase" :rules="testCaseRules" ref="testCaseFormRef" label-width="80px">
      <el-form-item v-for="(input, index) in currentTestCase.inputs" :key="index" :label="`输入 ${index + 1}`"
        :prop="'inputs.' + index + '.input'" :rules="{ required: true, message: '输入值不能为空', trigger: 'blur' }">
        <div class="input-group">
          <el-input v-model="currentTestCase.inputs[index].inputName" placeholder="输入名称" class="input-name" />
          <el-input v-model="currentTestCase.inputs[index].input" placeholder="输入值" class="input-value" />
          <el-button @click="removeInput(index)" :icon="Delete" />
        </div>
      </el-form-item>
      <el-form-item>
        <el-button @click="addInput" type="primary" plain>添加输入</el-button>
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
import type { TestCase, InputDto } from '@/types/testCase'
import { createTestCase, updateTestCase } from '@/api/testCase'
import { Delete } from '@element-plus/icons-vue'

const props = defineProps<{
  visible: boolean
  isEdit: boolean
  testCase: Partial<TestCase>
}>()

const emit = defineEmits(['update:visible', 'save'])

const dialogVisible = ref(props.visible)
const dialogTitle = ref(props.isEdit ? '编辑测试用例' : '添加测试用例')
interface TestCaseFormData {
  id?: number;
  problemId: number;
  inputs: InputDto[];
  output: string;
  score: number;
  isSample: boolean;
  createdAt?: string;
}

const currentTestCase = ref<TestCaseFormData>({
  inputs: [{ input: '', inputName: '' }],
  score: 0,
  isSample: false,
  problemId: props.testCase?.problemId || 0,
  output: props.testCase?.output || ''
})
const testCaseFormRef = ref<FormInstance>()
const savingTestCase = ref(false)

const testCaseRules: FormRules = {
  output: [{ required: true, message: '请输入期望输出', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分数', trigger: 'blur' }],
}

watch(
  () => props.visible,
  async (newVal) => {
    dialogVisible.value = newVal
    if (newVal) {
      dialogTitle.value = props.isEdit ? '编辑测试用例' : '添加测试用例'
      currentTestCase.value = {
        ...props.testCase,
        problemId: props.testCase.problemId || 0,
        output: props.testCase.output || '',
        score: props.testCase.score || 0,
        isSample: props.testCase.isSample || false,
        inputs: props.testCase.inputs && props.testCase.inputs.length > 0
          ? [...props.testCase.inputs]
          : [{ input: '', inputName: '' }],
      }
    }
  }
)

const addInput = () => {
  if (!currentTestCase.value.inputs) {
    currentTestCase.value.inputs = []
  }
  currentTestCase.value.inputs.push({ input: '', inputName: '' })
}

const removeInput = (index: number) => {
  if (currentTestCase.value.inputs && currentTestCase.value.inputs.length > 1) {
    currentTestCase.value.inputs.splice(index, 1)
  }
}

const onClose = () => {
  emit('update:visible', false)
}

const saveTestCase = async () => {
  if (!testCaseFormRef.value) return
  await testCaseFormRef.value.validate(async (valid) => {
    if (valid) {
      savingTestCase.value = true
      try {
        const payload: TestCase = {
          id: currentTestCase.value.id!,
          problemId: currentTestCase.value.problemId,
          output: currentTestCase.value.output,
          score: currentTestCase.value.score,
          isSample: currentTestCase.value.isSample,
          inputs: currentTestCase.value.inputs.map((i: InputDto) => ({
            input: i.input || '',
            inputName: i.inputName || ''
          })),
          createdAt: currentTestCase.value.createdAt || new Date().toISOString()
        }

        if (props.isEdit) {
          payload.id = currentTestCase.value.id!
          await updateTestCase(payload)
          ElMessage.success('测试用例更新成功。')
        } else {
          await createTestCase(payload)
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

.input-group {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.input-name {
  flex: 0 0 120px;
}

.input-value {
  flex: 1;
}
</style>
