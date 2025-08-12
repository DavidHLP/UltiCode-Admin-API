<template>
  <div class="detailed-questions" ref="mdSectionRef">
    <div class="sections">
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">题目设置</span>
        </template>
        <div class="form-grid">
          <div class="col-6">
            <el-form-item label="题目类型" prop="problemType">
              <el-select v-model="problemTypeProxy" placeholder="请选择题目类型" style="width: 100%">
                <el-option
                  v-for="item in problemTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="col-6">
            <el-form-item label="运行方法名称" prop="solutionFunctionName">
              <el-input v-model="solutionFunctionNameProxy" placeholder="请输入题目运行方法" />
            </el-form-item>
          </div>
        </div>
      </el-card>

      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">题目描述</span>
        </template>
        <div class="md-card">
          <MdEditor
            v-model="descriptionProxy"
            :theme="props.theme"
            placeholder="请输入题目描述"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Problem } from '@/types/problem'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const props = defineProps<{
  currentProblem: Partial<Problem>
  theme: 'light' | 'dark'
}>()

const emit = defineEmits<{
  (e: 'update:current-problem', value: Partial<Problem>): void
}>()

// 视口自适应高度，避免页面出现上下滚动
const mdSectionRef = ref<HTMLElement | null>(null)

const problemTypeOptions = [
  { value: 'ACM', label: 'ACM' },
  { value: 'OI', label: 'OI' },
]

// 与父组件同步的双向绑定代理
const problemTypeProxy = computed<Problem['problemType'] | undefined>({
  get: () => props.currentProblem.problemType,
  set: (val) => emit('update:current-problem', { ...props.currentProblem, problemType: val as Problem['problemType'] }),
})

const solutionFunctionNameProxy = computed<string>({
  get: () => props.currentProblem.solutionFunctionName || '',
  set: (val) => emit('update:current-problem', { ...props.currentProblem, solutionFunctionName: (val ?? '') }),
})

const descriptionProxy = computed<string>({
  get: () => props.currentProblem.description || '',
  set: (val) => emit('update:current-problem', { ...props.currentProblem, description: (val ?? '') }),
})
</script>

<style scoped>
.sections {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.section-card :deep(.el-card__header) {
  padding: 10px 12px;
}
.section-title {
  font-weight: 600;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 16px;
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
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-fill-color-blank);
}

/* 去除表单项底部间距，避免多余高度引起外层滚动 */
.detailed-questions :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>
