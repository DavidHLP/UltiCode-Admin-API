<template>
  <div class="page testcase-editor pe-page">
    <header class="toolbar pe-toolbar" v-if="!showEditor">
      <span class="title">测试用例</span>
      <span class="spacer" />
      <el-button :disabled="!problemId" :icon="Plus" type="primary" @click="openCreate">新增</el-button>
    </header>

    <el-card v-if="!showEditor" class="pe-section-card" shadow="never">
      <el-empty v-if="!problemId" description="请输入题目ID" />
      <el-skeleton v-else-if="loading" :rows="4" animated />
      <div v-else>
        <el-empty v-if="!list.length" description="暂无数据" />
        <div v-else class="desc-list">
          <section v-for="(row, idx) in list" :key="row.id" class="desc-block">
            <div class="desc-header">
              <el-tag type="info">#{{ idx + 1 }}</el-tag>
              <span class="desc-id">ID: {{ row.id }}</span>
              <span class="spacer" />
              <el-button size="small" :icon="Edit" text type="primary" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm :title="`确认删除测试用例 #${row.id} 吗？`" @confirm="onDelete(row)">
                <template #reference>
                  <el-button size="small" :icon="Delete" text type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>

            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="分值">
                {{ row.testCaseOutput.score }}
              </el-descriptions-item>
              <el-descriptions-item label="样例">
                <el-tag :type="row.testCaseOutput.isSample ? 'success' : 'info'">{{ row.testCaseOutput.isSample ? '是' : '否' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="输出类型">
                {{ row.testCaseOutput.outputType || '-' }}
              </el-descriptions-item>

              <el-descriptions-item label="输入" :span="3">
                <div class="inputs-details">
                  <div v-for="(inp, i2) in row.testCaseInput" :key="i2" class="input-detail">
                    <div class="input-meta">
                      <el-tag type="info">#{{ i2 + 1 }}</el-tag>
                      <span class="input-name">{{ inp.testCaseName || '-' }}</span>
                      <el-tag v-if="inp.inputType" size="small" effect="plain" type="info">{{ inp.inputType }}</el-tag>
                    </div>
                    <el-popover width="520" placement="top-start" trigger="click">
                      <template #reference>
                        <pre class="output-preview clickable pe-mono">{{ preview(inp.inputContent) }}</pre>
                      </template>
                      <div class="popover-output">
                        <div class="popover-actions">
                          <el-button :icon="CopyDocument" text @click="copyOutput(inp.inputContent)">复制</el-button>
                        </div>
                        <pre class="full-output pe-mono">{{ inp.inputContent }}</pre>
                      </div>
                    </el-popover>
                  </div>
                </div>
              </el-descriptions-item>

              <el-descriptions-item label="期望输出" :span="3">
                <el-popover width="520" placement="top-start" trigger="click">
                  <template #reference>
                    <pre class="output-preview clickable pe-mono">{{ preview(row.testCaseOutput.output) }}</pre>
                  </template>
                  <div class="popover-output">
                    <div class="popover-actions">
                      <el-button :icon="CopyDocument" text @click="copyOutput(row.testCaseOutput.output)">复制</el-button>
                    </div>
                    <pre class="full-output pe-mono">{{ row.testCaseOutput.output }}</pre>
                  </div>
                </el-popover>
              </el-descriptions-item>
            </el-descriptions>
          </section>
        </div>
      </div>
    </el-card>

    <!-- 子页面编辑器 -->
    <TestcaseEditor
      v-else
      :mode="editorMode"
      :problem-id="problemId!"
      :editing-row="editingRow"
      :lock-format="!!formatTemplate"
      :format-template="formatTemplate"
      @saved="onSaved"
      @cancel="onCancelEdit"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { TestCase } from '@/types/testCase.d'
import { deleteTestCase, fetchTestCasesByProblemId } from '@/api/testCase.ts'
import TestcaseEditor from './TestcaseEditor.vue'
import { Plus, Edit, Delete, CopyDocument } from '@element-plus/icons-vue'

const route = useRoute()

const problemId = ref<number | null>(null)
const loading = ref(false)
const list = ref<TestCase[]>([])

// 从现有测试用例中提取“格式”：输入参数的名称与类型、输出类型
type CaseFormat = {
  outputType?: string
  inputs: { inputType?: string; testCaseName: string }[]
}
const formatTemplate = ref<CaseFormat | null>(null)

const showEditor = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const editingRow = ref<TestCase | null>(null)

// 子页面负责表单与校验

const preview = (s: string, max = 120): string => {
  if (!s) return ''
  const oneLine = String(s).replace(/\n/g, ' \\n ')
  return oneLine.length > max ? oneLine.slice(0, max) + '…' : oneLine
}

const copyOutput = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text || '')
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    console.error(e)
    ElMessage.error('复制失败')
  }
}

const loadList = async () => {
  if (!problemId.value) return
  loading.value = true
  try {
    list.value = await fetchTestCasesByProblemId(problemId.value)
    // 基于最新列表提取格式（以第一条为准）
    if (list.value.length) {
      const first = list.value[0]
      const sorted = [...first.testCaseInput].sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
      formatTemplate.value = {
        outputType: first.testCaseOutput?.outputType,
        inputs: sorted.map((it) => ({
          inputType: it.inputType,
          testCaseName: it.testCaseName || ''
        }))
      }
    } else {
      formatTemplate.value = null
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载测试用例失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  if (!problemId.value) return ElMessage.warning('请先输入题目ID')
  editorMode.value = 'create'
  editingRow.value = null
  showEditor.value = true
}

const openEdit = (row: TestCase) => {
  editorMode.value = 'edit'
  editingRow.value = row
  showEditor.value = true
}

// 子页面保存/取消回调
const onSaved = async () => {
  showEditor.value = false
  await loadList()
}
const onCancelEdit = () => {
  showEditor.value = false
}

const onDelete = async (row: TestCase) => {
  try {
    await deleteTestCase(row.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (e) {
    console.error(e)
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  const idParam = route.params.id || route.query.problemId
  const parsed = Number(idParam)
  if (Number.isFinite(parsed) && parsed > 0) problemId.value = parsed
  if (problemId.value) await loadList()
})
</script>

<style scoped>
.page.testcase-editor {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap);
}
.toolbar {
  position: sticky;
  top: 0;
  z-index: 5;
  padding: var(--pe-gap-sm) 0;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--pe-border);
}
.title {
  font-size: 16px;
  font-weight: 600;
  margin-left: var(--pe-gap-sm);
}
.spacer {
  flex: 1;
}
.inputs {
  display: flex;
  flex-wrap: wrap;
  gap: var(--pe-gap-sm);
}
.inputs-details {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap-sm);
}
.input-detail {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap-sm);
  padding: var(--pe-gap);
  border: 1px dashed var(--pe-border);
  border-radius: var(--pe-radius-sm);
  background: var(--pe-bg-soft);
}
.input-meta {
  display: flex;
  align-items: center;
  gap: var(--pe-gap-sm);
}
.input-name {
  font-weight: 500;
}
.desc-list {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap);
}
.desc-header {
  display: flex;
  align-items: center;
  gap: var(--pe-gap);
  margin-bottom: var(--pe-gap-sm);
}
.desc-id {
  color: var(--el-text-color-secondary);
}
.output-preview {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  background: var(--pe-bg-soft);
  border: 1px dashed var(--pe-border);
  padding: var(--pe-gap-sm) var(--pe-gap);
  border-radius: var(--pe-radius-sm);
}
.clickable {
  cursor: pointer;
}
.popover-output {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap-sm);
}
.popover-actions {
  display: flex;
  justify-content: flex-end;
}
.full-output {
  margin: 0;
  max-height: 40vh;
  overflow: auto;
  background: var(--pe-bg-subtle);
  border: 1px solid var(--pe-border);
  padding: var(--pe-card-pad);
  border-radius: var(--pe-radius-md);
}
.input-list {
  display: flex;
  flex-direction: column;
  gap: var(--pe-gap-sm);
}
.input-item {
  display: grid;
  grid-template-columns: 1fr 1fr 2fr auto;
  gap: var(--pe-gap-sm);
  align-items: start;
}
</style>
