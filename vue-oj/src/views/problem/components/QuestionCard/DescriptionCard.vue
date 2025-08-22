<template>
  <div v-loading="loading" class="problem-content">
    <div v-if="problem" class="problem-header">
      <h1>{{ problem.title }}</h1>
      <el-tag :type="difficultyType" size="small" effect="light">
        {{ difficultyLabel }}
      </el-tag>
    </div>
    <el-divider v-if="problem" />
    <template v-if="problem">
      <!-- 题目描述（结构化） -->
      <section class="section">
        <h2 class="section-title">题目描述</h2>
        <md-preview :model-value="structured.description" theme="light" />
      </section>

      <!-- 示例列表（结构化） -->
      <section v-if="structured.examples && structured.examples.length" class="section">
        <h2 class="section-title">示例</h2>
        <div class="examples">
          <div v-for="(ex, idx) in structured.examples" :key="idx" class="example-item">
            <div class="example-header">示例 {{ idx + 1 }}</div>
            <div class="io-row">
              <span class="io-label">输入：</span>
              <pre class="io-code">{{ ex.input }}</pre>
            </div>
            <div class="io-row">
              <span class="io-label">输出：</span>
              <pre class="io-code">{{ ex.output }}</pre>
            </div>
            <div v-if="ex.explanation" class="explanation">
              <span class="io-label">解释：</span>
              <md-preview :model-value="ex.explanation" theme="light" />
            </div>
          </div>
        </div>
      </section>

      <!-- 提示（结构化） -->
      <section v-if="structured.hints" class="section">
        <h2 class="section-title">提示</h2>
        <md-preview :model-value="structured.hints" theme="light" />
      </section>
    </template>
    <el-empty v-else-if="!loading" description="题目加载失败" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import { ElMessage } from 'element-plus'
import 'md-editor-v3/lib/preview.css'
import type { ProblemDetailVo } from '@/types/problem'
import { getProblemDetailVoById } from '@/api/problem'
import { getDifficultyTagType, getDifficultyChinese } from '@/utils/tag'

const route = useRoute()
const problem = ref<ProblemDetailVo | null>(null)
const loading = ref(true)

// 结构化描述类型
interface ProblemExample {
  input: string
  output: string
  explanation?: string
}
interface StructuredProblem {
  description: string
  examples: ProblemExample[]
  hints: string
}

// 解析结构化描述：支持 JSON；否则回退为旧版 Markdown 文本
const parseStructuredDescription = (description: string): StructuredProblem => {
  if (!description) {
    return { description: '', examples: [], hints: '' }
  }
  try {
    const parsed = JSON.parse(description)
    if (parsed && typeof parsed === 'object' && 'description' in parsed) {
      return {
        description: parsed.description || '',
        examples: Array.isArray(parsed.examples) ? parsed.examples : [],
        hints: parsed.hints || '',
      }
    }
  } catch {
    // 非 JSON，按旧格式处理
  }
  return { description, examples: [], hints: '' }
}

const structured = computed<StructuredProblem>(() =>
  parseStructuredDescription(problem.value?.description || ''),
)

// 获取题目详情
const fetchProblem = async () => {
  const problemId = Number(route.params.id)
  if (isNaN(problemId)) return

  try {
    loading.value = true
    const res = await getProblemDetailVoById(problemId)
    problem.value = {
      id: res.id,
      title: res.title,
      description: res.description,
      difficulty: res.difficulty,
    }
  } catch (error) {
    console.error('Failed to fetch problem:', error)
    ElMessage.error('题目加载失败')
  } finally {
    loading.value = false
  }
}

const difficultyType = computed(() => getDifficultyTagType(problem.value?.difficulty))
const difficultyLabel = computed(() => getDifficultyChinese(problem.value?.difficulty))

// 监听路由参数变化
onMounted(() => {
  fetchProblem()
})
</script>

<style scoped>
@import '@/assets/styles/md.css';
@import '@/assets/styles/html.css';
@import '@/assets/styles/scrollbar.css';

.problem-content {
  padding: 24px;
  height: 100%;
  max-height: 100%;
  overflow-y: auto;
  background: transparent;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 结构化内容样式 */
.section {
  margin-bottom: 24px;
}
.section-title {
  font-weight: 600;
  font-size: 16px;
  margin: 0 0 12px;
}

.examples {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.example-item {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
}
.example-header {
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.io-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 6px 0;
}
.io-label {
  color: var(--el-text-color-regular);
  min-width: 40px;
}
.io-code {
  margin: 0;
  padding: 8px 10px;
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  max-width: 100%;
  overflow-x: auto;
  white-space: pre-wrap;
}
.explanation {
  margin-top: 8px;
}
</style>
