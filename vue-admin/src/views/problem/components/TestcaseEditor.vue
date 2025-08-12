<template>
  <div class="editor-wrapper">
    <header class="toolbar">
      <span class="title">{{ mode === 'create' ? '新增测试用例' : '编辑测试用例' }}</span>
      <span class="spacer" />
      <el-button :icon="Close" text @click="onCancel">取消</el-button>
      <el-button :loading="saving" :icon="Check" type="primary" @click="onSubmit">保存</el-button>
    </header>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-row :gutter="12">
        <el-col :xs="24" :md="12">
          <el-card shadow="never">
            <template #header>
              <span>输出设置</span>
            </template>
            <el-form-item label="分值" prop="testCaseOutput.score">
              <el-input-number v-model="form.testCaseOutput.score" :max="100" :min="1" :step="1" />
            </el-form-item>
            <el-form-item label="是否为样例" prop="testCaseOutput.isSample">
              <el-switch v-model="form.testCaseOutput.isSample" />
            </el-form-item>
            <el-form-item label="输出类型">
              <el-select v-model="form.testCaseOutput.outputType" clearable filterable placeholder="请选择输出类型">
                <el-option v-for="opt in outputTypeOptions" :key="opt" :label="opt" :value="opt" />
              </el-select>
            </el-form-item>
            <el-form-item label="期望输出" prop="testCaseOutput.output">
              <el-input
                v-model="form.testCaseOutput.output"
                class="mono"
                :autosize="{ minRows: 4, maxRows: 12 }"
                placeholder="请输入期望输出"
                type="textarea"
              />
            </el-form-item>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12">
          <el-card shadow="never">
            <template #header>
              <span>输入参数</span>
            </template>
            <el-form-item label="输入参数" required>
              <div class="input-list">
                <div v-for="(inp, idx) in form.testCaseInput" :key="idx" class="input-item">
                  <el-form-item :prop="`testCaseInput.${idx}.inputType`" label-width="0">
                    <el-select v-model="inp.inputType" clearable filterable placeholder="类型">
                      <el-option v-for="opt in inputTypeOptions" :key="opt" :label="opt" :value="opt" />
                    </el-select>
                  </el-form-item>
                  <el-form-item :prop="`testCaseInput.${idx}.testCaseName`" :rules="[{ required: true, message: '请输入输入名称', trigger: 'blur' }]" label-width="0">
                    <el-input v-model="inp.testCaseName" placeholder="输入名称，如 nums 或 n" />
                  </el-form-item>
                  <el-form-item :prop="`testCaseInput.${idx}.inputContent`" :rules="[{ required: true, message: '请输入输入内容', trigger: 'blur' }]" label-width="0">
                    <el-input
                      v-model="inp.inputContent"
                      class="mono"
                      :autosize="{ minRows: 2, maxRows: 10 }"
                      placeholder="输入内容，例如: [1,2,3] 或 5"
                      type="textarea"
                    />
                  </el-form-item>

                  <div class="op">
                    <el-tooltip content="在此处添加一项" placement="top">
                      <span>
                        <el-button :disabled="mode === 'edit'" circle :icon="Plus" size="small" @click="addInput(idx + 1)" />
                      </span>
                    </el-tooltip>
                    <el-tooltip content="删除该项" placement="top">
                      <span>
                        <el-button :disabled="mode === 'edit' || form.testCaseInput.length <= 1" circle :icon="Delete" size="small" type="danger" @click="removeInput(idx)" />
                      </span>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-alert
              v-if="mode === 'edit'"
              class="tip"
              type="info"
              :closable="false"
              show-icon
              title="编辑模式仅支持修改现有输入，不支持新增/删除"
            />
          </el-card>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, watchEffect } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { TestCase, TestCaseInput, TestCaseOutput } from '@/types/testCase.ts'
import { InputType, OutputType } from '@/types/testCase.d'
import { createTestCase, updateTestCase } from '@/api/testCase.ts'
import { Plus, Delete, Check, Close } from '@element-plus/icons-vue'

type Mode = 'create' | 'edit'
const props = defineProps<{ mode: Mode; problemId: number; editingRow?: TestCase | null }>()
const emit = defineEmits<{ (e: 'saved'): void; (e: 'cancel'): void }>()

const inputTypeOptions = InputType
const outputTypeOptions = OutputType

type TestCaseForm = {
  problemId: number
  testCaseOutput: TestCaseOutput
  testCaseInput: TestCaseInput[]
}

const formRef = ref<FormInstance>()
const saving = ref(false)

const newInput = (orderIndex: number): TestCaseInput => ({
  testCaseName: '',
  inputContent: '',
  inputType: undefined,
  orderIndex,
})

const form = ref<TestCaseForm>({
  problemId: props.problemId,
  testCaseOutput: {
    problemId: props.problemId,
    output: '',
    score: 10,
    isSample: false,
    outputType: undefined,
  },
  testCaseInput: [newInput(0)],
})

const rules: FormRules = {
  'testCaseOutput.output': [{ required: true, message: '请输入期望输出', trigger: 'blur' }],
  'testCaseOutput.score': [{ required: true, message: '请输入分值', trigger: 'change' }],
}

watchEffect(() => {
  if (props.mode === 'create') {
    form.value = {
      problemId: props.problemId,
      testCaseOutput: {
        problemId: props.problemId,
        output: '',
        score: 10,
        isSample: false,
        outputType: undefined,
      },
      testCaseInput: [newInput(0)],
    }
  } else if (props.mode === 'edit' && props.editingRow) {
    const row = props.editingRow
    const out: TestCaseOutput = row.testCaseOutput
    const sortedInputs = [...row.testCaseInput].sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
    form.value = {
      problemId: row.problemId,
      testCaseOutput: {
        id: out.id,
        problemId: row.problemId,
        output: out.output || '',
        score: out.score ?? 10,
        isSample: out.isSample ?? false,
        outputType: out.outputType,
      },
      testCaseInput: sortedInputs.map((it, idx) => ({
        id: it.id,
        testCaseOutputId: it.testCaseOutputId,
        testCaseName: it.testCaseName || '',
        inputContent: it.inputContent || '',
        inputType: it.inputType,
        orderIndex: idx,
      })),
    }
  }
})

const addInput = (insertIndex?: number) => {
  if (props.mode === 'edit') return
  const idx = insertIndex ?? form.value.testCaseInput.length
  form.value.testCaseInput.splice(idx, 0, newInput(idx))
  form.value.testCaseInput.forEach((it, i) => (it.orderIndex = i))
}

const removeInput = (idx: number) => {
  if (props.mode === 'edit') return
  if (form.value.testCaseInput.length <= 1) return ElMessage.warning('至少需要一个输入')
  form.value.testCaseInput.splice(idx, 1)
  form.value.testCaseInput.forEach((it, i) => (it.orderIndex = i))
}

const onSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (props.mode === 'create') {
      const payload: Partial<TestCase> = {
        problemId: props.problemId,
        testCaseOutput: form.value.testCaseOutput,
        testCaseInput: form.value.testCaseInput.map((it, idx) => ({
          testCaseName: it.testCaseName,
          inputContent: it.inputContent,
          inputType: it.inputType,
          orderIndex: idx,
        })),
      }
      await createTestCase(payload)
      ElMessage.success('创建成功')
    } else {
      if (!props.editingRow) return
      const hasNew = form.value.testCaseInput.some((it) => !it.id)
      if (hasNew) {
        ElMessage.warning('编辑模式暂不支持新增输入/删除输入，请仅修改已有项')
        return
      }
      const payload: Partial<TestCase> = {
        problemId: props.editingRow.problemId,
        testCaseOutput: {
          id: form.value.testCaseOutput.id!,
          problemId: props.editingRow.problemId,
          output: form.value.testCaseOutput.output!,
          score: form.value.testCaseOutput.score,
          isSample: form.value.testCaseOutput.isSample,
          outputType: form.value.testCaseOutput.outputType,
        },
        testCaseInput: form.value.testCaseInput.map((it, idx) => ({
          id: it.id!,
          testCaseOutputId: form.value.testCaseOutput.id!,
          testCaseName: it.testCaseName,
          inputContent: it.inputContent,
          inputType: it.inputType,
          orderIndex: idx,
        })),
      }
      await updateTestCase(payload)
      ElMessage.success('更新成功')
    }
    emit('saved')
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const onCancel = () => emit('cancel')
</script>

<style scoped>
.editor-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}
.spacer {
  flex: 1;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.input-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.input-item {
  display: grid;
  grid-template-columns: 160px 1fr 1fr auto;
  gap: 8px 12px;
  align-items: start;
  padding: 12px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
}
.op {
  display: flex;
  gap: 6px;
}
.tip {
  margin-top: 8px;
}

/* 等宽字体，便于输入/输出阅读 */
.mono :deep(textarea.el-textarea__inner) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 13px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .input-item {
    grid-template-columns: 1fr;
  }
}
</style>
