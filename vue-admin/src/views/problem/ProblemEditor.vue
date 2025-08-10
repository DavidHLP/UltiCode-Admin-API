<template>
  <div class="page problem-editor">
    <header class="editor-toolbar">
      <el-button text @click="goBack" :icon="Back">返回</el-button>
      <div class="title-area">
        <span class="title">{{ pageTitle }}</span>
        <el-tag v-if="isEdit" type="warning" size="small">编辑模式</el-tag>
        <el-tag v-else type="success" size="small">新建模式</el-tag>
      </div>
      <div class="actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProblem">{{
          isEdit ? '更新' : '创建'
        }}</el-button>
      </div>
    </header>

    <el-card class="editor-card" shadow="never">
      <el-form
        ref="problemFormRef"
        :model="currentProblem"
        :rules="problemRules"
        label-position="top"
        class="problem-form"
      >
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <BasicComponent
              :current-problem="currentProblem"
              :current-problem-tags="currentProblemTags"
              :categories="categories"
              @update:current-problem-tags="(v) => (currentProblemTags = v)"
            />
          </el-tab-pane>
          <el-tab-pane label="题目详细" name="info">
            <DetailedQuestions :current-problem="currentProblem" :theme="theme" />
          </el-tab-pane>
        </el-tabs>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import BasicComponent from './components/BasicComponent.vue'
import DetailedQuestions from './components/DetailedQuestions.vue'
import { Back } from '@element-plus/icons-vue'

import type { Problem, Category } from '@/types/problem'
import { getProblem, createProblem, updateProblem, fetchCategories } from '@/api/problem'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const pageTitle = computed(() => (isEdit.value ? '编辑题目' : '新建题目'))

const problemFormRef = ref<FormInstance>()
const activeTab = ref('basic')
const saving = ref(false)
const theme = ref<'light' | 'dark'>('light')
const isDirty = ref(false)
const originalSnapshot = ref<string>('')

const categories = ref<Category[]>([])

const currentProblem = ref<Partial<Problem>>({
  title: '',
  description: '',
  isVisible: true,
  timeLimit: 1000,
  memoryLimit: 128,
  difficulty: 'EASY',
  category: '',
  tags: [],
})
const currentProblemTags = ref<string[]>([])

// Markdown 自适应高度逻辑已下放至 DetailedQuestions 组件

const computeSnapshot = () =>
  JSON.stringify({ ...currentProblem.value, tags: currentProblemTags.value })
const markSnapshot = () => {
  originalSnapshot.value = computeSnapshot()
  isDirty.value = false
}

watch(
  [currentProblem, currentProblemTags],
  () => {
    if (!originalSnapshot.value) return
    isDirty.value = computeSnapshot() !== originalSnapshot.value
  },
  { deep: true },
)

const problemRules: FormRules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  timeLimit: [{ required: true, message: '请输入时间限制', trigger: 'blur' }],
  memoryLimit: [{ required: true, message: '请输入内存限制', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

const loadCategories = async () => {
  try {
    categories.value = await fetchCategories()
  } catch (e) {
    console.error('获取类别失败', e)
    ElMessage.error('获取题目类别失败')
  }
}

const loadProblemIfEdit = async () => {
  if (!isEdit.value) return
  const id = Number(route.params.id)
  if (!Number.isFinite(id)) return
  try {
    const p = await getProblem(id)
    currentProblem.value = { ...p }
    currentProblemTags.value = Array.isArray(p.tags) ? [...p.tags] : []
  } catch (e) {
    console.error('获取题目详情失败', e)
    ElMessage.error('获取题目详情失败')
  }
}

const confirmLeave = async (): Promise<boolean> => {
  if (!isDirty.value) return true
  try {
    await ElMessageBox.confirm('您有未保存的更改，确定离开吗？', '提示', { type: 'warning' })
    return true
  } catch {
    return false
  }
}

const goBack = async () => {
  if (await confirmLeave()) {
    router.push({ name: 'problems' })
  }
}

const saveProblem = async () => {
  if (!problemFormRef.value) return
  const valid = await problemFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const tags = Array.from(
      new Set((currentProblemTags.value || []).map((t) => t.trim()).filter(Boolean)),
    )
    const payload = { ...currentProblem.value, tags }
    if (isEdit.value) {
      payload.id = Number(route.params.id)
      await updateProblem(payload as Problem)
      ElMessage.success('题目更新成功。')
    } else {
      await createProblem(payload as Problem)
      ElMessage.success('题目创建成功。')
    }
    markSnapshot()
    await goBack()
  } catch (e) {
    console.error('保存题目失败', e)
    ElMessage.error('保存题目失败。')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const onKeydown = async (e: KeyboardEvent) => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
      e.preventDefault()
      if (!saving.value) await saveProblem()
    }
  }
  window.addEventListener('keydown', onKeydown)

  // 同步系统主题到编辑器
  const mq = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)')
  if (mq) {
    theme.value = mq.matches ? 'dark' : 'light'
    const listener = (evt: MediaQueryListEvent) => {
      theme.value = evt.matches ? 'dark' : 'light'
    }
    const anyMq = mq as unknown as Record<string, unknown>
    if (typeof anyMq.addEventListener === 'function') {
      // 新标准 API
      ;(mq as unknown as MediaQueryList).addEventListener(
        'change',
        listener as unknown as EventListener,
      )
      onBeforeUnmount(() => {
        ;(mq as unknown as MediaQueryList).removeEventListener(
          'change',
          listener as unknown as EventListener,
        )
      })
    } else if (typeof anyMq.addListener === 'function') {
      // 旧版 API（Safari/旧 Chromium）
      ;(anyMq.addListener as (cb: (e: MediaQueryListEvent) => void) => void)(listener)
      onBeforeUnmount(() => {
        ;(anyMq.removeListener as (cb: (e: MediaQueryListEvent) => void) => void)(listener)
      })
    }
  }

  await Promise.all([loadCategories(), loadProblemIfEdit()])
  markSnapshot()

  onBeforeUnmount(() => {
    window.removeEventListener('keydown', onKeydown)
  })
})

onBeforeRouteLeave((to, from, next) => {
  if (!isDirty.value) return next()
  ElMessageBox.confirm('您有未保存的更改，确定离开吗？', '提示', { type: 'warning' })
    .then(() => next())
    .catch(() => next(false))
})
</script>

<style scoped>
.page.problem-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 20px;
  max-width: 1080px;
  margin: 0 auto;
  overflow-y: hidden;
  /* 避免页面出现上下滚动 */
}

.editor-toolbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
}

.title-area {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 auto;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.actions {
  display: flex;
  gap: 8px;
  justify-self: end;
}

.problem-form {
  margin-top: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 16px;
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

.md-card {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-fill-color-blank);
}

/* 去除 Markdown 表单项底部间距，避免多余高度引起外层滚动 */
.md-section :deep(.el-form-item) {
  margin-bottom: 0;
}

.inline-field {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 150px;
}

.difficulty-preview {
  min-width: 56px;
}
</style>
