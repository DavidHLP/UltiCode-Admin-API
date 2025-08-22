<template>
  <div class="basic-component">
    <div class="sections">
      <el-card class="section-card pe-section-card" shadow="never">
        <template #header>
          <span class="section-title">基本信息</span>
        </template>

        <div class="form-grid">
          <div class="col-12">
            <el-form-item label="题目标题" prop="title">
              <el-input v-model="localProblem.title" placeholder="请输入题目标题" />
            </el-form-item>
          </div>

          <div class="col-6">
            <el-form-item label="类别" prop="category">
              <el-select
                v-model="localProblem.category"
                placeholder="请选择题目类别"
                style="width: 100%"
                clearable
                filterable
              >
                <el-option
                  v-for="c in CATEGORIES"
                  :key="c.category"
                  :label="c.description"
                  :value="c.category"
                />
              </el-select>
            </el-form-item>
          </div>

          <div class="col-6">
            <el-form-item label="难度" prop="difficulty" >
              <div class="inline-field">
                <el-select
                  v-model="localProblem.difficulty"
                  placeholder="请选择难度"
                  clearable
                  style="width: 80px"
                >
                  <el-option label="简单" value="EASY" />
                  <el-option label="中等" value="MEDIUM" />
                  <el-option label="困难" value="HARD" />
                </el-select>
                <el-tag
                  class="difficulty-preview"
                  size="small"
                  :type="getDifficultyTagType(localProblem.difficulty || '')"
                >
                  {{ getDifficultyChinese(localProblem.difficulty || '') }}
                </el-tag>
              </div>
            </el-form-item>
          </div>

          <div class="col-6">
            <el-form-item label="是否可见" prop="isVisible">
              <el-switch v-model="localProblem.isVisible" />
            </el-form-item>
          </div>
        </div>
      </el-card>

      <el-card class="section-card pe-section-card" shadow="never">
        <template #header>
          <span class="section-title">题目设置</span>
        </template>
        <div class="form-grid">
          <div class="col-6">
            <el-form-item label="题目类型" prop="problemType">
              <el-select
                v-model="localProblem.problemType"
                placeholder="请选择题目类型"
                style="width: 100%"
                clearable
              >
                <el-option label="ACM" value="ACM" />
                <el-option label="OI" value="OI" />
              </el-select>
            </el-form-item>
          </div>
          <div class="col-6">
            <el-form-item label="运行方法名称" prop="solutionFunctionName">
              <el-input v-model="localProblem.solutionFunctionName" placeholder="请输入题目运行方法" />
            </el-form-item>
          </div>
        </div>
      </el-card>

      <el-card class="section-card pe-section-card" shadow="never">
        <template #header>
          <span class="section-title">标签与限制</span>
        </template>

        <div class="form-grid">
          <div class="col-12">
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="tagsProxy"
                multiple
                filterable
                allow-create
                default-first-option
                collapse-tags
                collapse-tags-tooltip
                tag-type="primary"
                placeholder="输入或选择标签"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { Problem } from '@/types/problem'
import { CATEGORIES } from '@/types/problem.d'
import { getDifficultyChinese, getDifficultyTagType } from '@/utils/tag'

const props = defineProps<{
  currentProblem: Partial<Problem>
  currentProblemTags: string[]
}>()

// 避免直接修改 props，使用计算属性代理并向父组件派发更新事件
const emit = defineEmits<{
  (e: 'update:current-problem-tags', value: string[]): void
  (e: 'update:current-problem', value: Partial<Problem>): void
}>()

const tagsProxy = computed<string[]>({
  get: () => props.currentProblemTags,
  set: (val) => emit('update:current-problem-tags', val),
})

// 使用局部副本以避免直接修改 props，并通过 watch 同步父子数据
const localProblem = reactive<Partial<Problem>>({ ...props.currentProblem })

watch(
  () => props.currentProblem,
  (val) => {
    Object.assign(localProblem, val)
  },
  { deep: true, immediate: true },
)

watch(
  localProblem,
  (val) => {
    // 向父级同步最新的表单数据
    emit('update:current-problem', { ...val })
  },
  { deep: true },
)
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
.section-title {
  font-weight: 600;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: var(--pe-gap-lg);
}

.col-12 {
  grid-column: span 12;
}

.col-6 {
  grid-column: span 6;
}

@media (max-width: 768px) {
  .col-6 {
    grid-column: span 12;
  }
}

.inline-field {
  display: flex;
  align-items: center;
  gap: var(--pe-gap);
}

.difficulty-preview {
  min-width: 56px;
}
</style>
