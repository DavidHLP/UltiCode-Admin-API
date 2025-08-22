<template>
  <div class="detailed-questions" ref="mdSectionRef">
    <div class="sections">

      <!-- 题目描述部分 -->
      <el-card class="section-card pe-section-card" shadow="never">
        <template #header>
          <span class="section-title">题目描述</span>
        </template>
        <div class="md-card">
          <MdEditor v-model="problemDescriptionProxy" :theme="props.theme"
            placeholder="请输入题目描述内容，如：给你一个数组nums，求最大子数组的和..." />
        </div>
      </el-card>

      <!-- 示例部分 -->
      <el-card class="section-card pe-section-card" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">示例</span>
            <el-button type="primary" size="small" @click="addExample">
              <el-icon>
                <Plus />
              </el-icon>
              添加示例
            </el-button>
          </div>
        </template>
        <div class="examples-container">
          <div v-for="(example, index) in examplesProxy" :key="index" class="example-item">
            <div class="example-header">
              <span class="example-title">示例 {{ index + 1 }}：</span>
              <el-button type="danger" size="small" text @click="removeExample(index)" v-if="examplesProxy.length > 1">
                <el-icon>
                  <Delete />
                </el-icon>
                删除
              </el-button>
            </div>
            <div class="example-content">
              <div class="example-field">
                <label class="field-label">输入：</label>
                <el-input v-model="example.input" placeholder="如：nums = [1,2,3,4]" autosize type="textarea" />
              </div>
              <div class="example-field">
                <label class="field-label">输出：</label>
                <el-input v-model="example.output" placeholder="如：10" autosize type="textarea" />
              </div>
              <div class="example-field" v-if="example.explanation !== undefined">
                <label class="field-label">解释（支持 Markdown）：</label>
                <MdEditor
                  v-model="example.explanation"
                  :theme="props.theme"
                  placeholder="在这里编写解释内容，支持Markdown格式，如：\n- 逐步说明推导过程\n- 给出关键公式或反例"
                />
              </div>
              <el-button v-else type="primary" text size="small" @click="addExplanation(index)">
                <el-icon>
                  <Plus />
                </el-icon>
                添加解释
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 提示/约束条件部分 -->
      <el-card class="section-card pe-section-card" shadow="never">
        <template #header>
          <span class="section-title">提示</span>
        </template>
        <div class="md-card">
          <MdEditor v-model="hintsProxy" :theme="props.theme"
            placeholder="请输入约束条件和提示，如：\n* 1 <= nums.length <= 10^5\n* -10^4 <= nums[i] <= 10^4" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { Problem } from '@/types/problem'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

// 示例数据结构
interface ProblemExample {
  input: string
  output: string
  explanation?: string
}

// 结构化题目数据
interface StructuredProblem {
  description: string
  examples: ProblemExample[]
  hints: string
}

const props = defineProps<{
  currentProblem: Partial<Problem>
  theme: 'light' | 'dark'
}>()

const emit = defineEmits<{
  (e: 'update:current-problem', value: Partial<Problem>): void
}>()

// 视口自适应高度，避免页面出现上下滚动
const mdSectionRef = ref<HTMLElement | null>(null)

// 题目设置移动至 BasicComponent.vue

// 解析结构化数据的工具函数
const parseStructuredDescription = (description: string): StructuredProblem => {
  if (!description) {
    return {
      description: '',
      examples: [{ input: '', output: '' }],
      hints: ''
    }
  }

  try {
    // 尝试解析为JSON格式的结构化数据
    const parsed = JSON.parse(description)
    if (parsed.description !== undefined) {
      return {
        description: parsed.description || '',
        examples: parsed.examples || [{ input: '', output: '' }],
        hints: parsed.hints || ''
      }
    }
  } catch {
    // 如果不是JSON，视为普通文本描述
  }

  // 兼容旧格式：将整个description作为题目描述
  return {
    description: description,
    examples: [{ input: '', output: '' }],
    hints: ''
  }
}

const stringifyStructuredDescription = (structured: StructuredProblem): string => {
  return JSON.stringify(structured, null, 2)
}

// 与父组件同步的双向绑定代理（题目设置字段已移至 BasicComponent）

// 本地结构化状态，避免仅依赖父组件回传导致的视图延迟
const localStructured = ref<StructuredProblem>(
  parseStructuredDescription(props.currentProblem.description || '')
)

// 父组件description变化时，同步到本地，并刷新序列化缓存
const lastSerialized = ref(stringifyStructuredDescription(localStructured.value))
watch(
  () => props.currentProblem.description,
  (desc) => {
    const parsed = parseStructuredDescription(desc || '')
    localStructured.value = parsed
    lastSerialized.value = stringifyStructuredDescription(parsed)
  }
)

// 监听本地结构化对象的深度变化，同步回父组件，避免频繁无效 emit
watch(
  localStructured,
  (val) => {
    const next = stringifyStructuredDescription(val)
    if (next !== lastSerialized.value) {
      lastSerialized.value = next
      emit('update:current-problem', { ...props.currentProblem, description: next })
    }
  },
  { deep: true }
)

// 题目描述代理
const problemDescriptionProxy = computed<string>({
  get: () => localStructured.value.description,
  set: (val) => {
    localStructured.value = { ...localStructured.value, description: val || '' }
  }
})

// 示例列表代理
const examplesProxy = computed<ProblemExample[]>({
  get: () => localStructured.value.examples,
  set: (val) => {
    localStructured.value = { ...localStructured.value, examples: val }
  }
})

// 提示代理
const hintsProxy = computed<string>({
  get: () => localStructured.value.hints,
  set: (val) => {
    localStructured.value = { ...localStructured.value, hints: val || '' }
  }
})

// 示例操作函数
const addExample = () => {
  const newExamples = [...examplesProxy.value, { input: '', output: '' } as ProblemExample]
  examplesProxy.value = newExamples
}

const removeExample = (index: number) => {
  if (examplesProxy.value.length > 1) {
    const newExamples = examplesProxy.value.filter((_, i) => i !== index)
    examplesProxy.value = newExamples
  }
}

const addExplanation = (index: number) => {
  const newExamples = [...examplesProxy.value]
  newExamples[index] = { ...newExamples[index], explanation: '' }
  examplesProxy.value = newExamples
}
</script>

<style scoped>
.sections {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap);
}

.section-card :deep(.el-card__header) {
  padding: var(--pe-card-header-pad);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.section-title {
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: var(--pe-gap-lg);
}

.col-6 {
  grid-column: span 6;
}

@media (max-width: 768px) {
  .col-6 {
    grid-column: span 12;
  }
}

.md-card {
  border: 1px solid var(--pe-border);
  border-radius: var(--pe-radius-md);
  overflow: hidden;
  background: var(--pe-bg-blank);
}

/* 示例相关样式 */
.examples-container {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap-lg);
}

.example-item {
  border: 1px solid var(--pe-border-light);
  border-radius: var(--pe-radius-md);
  padding: var(--pe-card-pad);
  background: var(--pe-bg-subtle);
}

.example-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.example-title {
  font-weight: 600;
  color: var(--el-color-primary);
}

.example-content {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap);
}

.example-field {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap-sm);
}

.field-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

/* 去除表单项底部间距，避免多余高度引起外层滚动 */
.detailed-questions :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>
