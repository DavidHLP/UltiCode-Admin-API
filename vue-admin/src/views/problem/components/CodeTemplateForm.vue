<template>
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="1000px" :close-on-click-modal="false" append-to-body
    :close-on-press-escape="false"
    @close="onClose" :fullscreen="isFullscreen">
    <div class="form-container">
      <div class="form-group">
        <label class="form-label">语言</label>
        <el-select v-model="currentCodeTemplate.language" placeholder="请选择编程语言" style="width: 100%">
          <el-option
            v-for="lang in supportedLanguages"
            :key="lang.value"
            :label="lang.label"
            :value="lang.value"
          />
        </el-select>
      </div>

      <div class="form-group">
        <div class="editor-header">
          <span class="editor-title">解题模板</span>
          <div class="editor-actions">
            <button class="btn-text" @click="resetCode('solutionTemplate')">
              <el-icon><Refresh /></el-icon> 重置
            </button>
            <button class="btn-text" @click="toggleFullscreen('solution')">
              <el-icon><FullScreen /></el-icon> 全屏
            </button>
          </div>
        </div>
        <div ref="solutionEditorRef" class="code-editor"></div>
      </div>

      <div class="form-group">
        <div class="editor-header">
          <span class="editor-title">包装器模板</span>
          <div class="editor-actions">
            <button class="btn-text" @click="resetCode('mainWrapperTemplate')">
              <el-icon><Refresh /></el-icon> 重置
            </button>
            <button class="btn-text" @click="toggleFullscreen('wrapper')">
              <el-icon><FullScreen /></el-icon> 全屏
            </button>
          </div>
        </div>
        <div ref="wrapperEditorRef" class="code-editor"></div>
      </div>
    </div>

    <template #footer>
      <button class="btn btn-default" @click="onClose">取消</button>
      <button class="btn btn-primary" @click="saveCodeTemplate" :disabled="saving">
        <span v-if="saving">处理中...</span>
        <span v-else>{{ isEdit ? '更新' : '创建' }}</span>
      </button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, shallowRef, nextTick } from 'vue'
import type { Ref } from 'vue'
import { ElMessage, ElSelect, ElOption } from 'element-plus'
import { Refresh, FullScreen } from '@element-plus/icons-vue'
import * as monaco from 'monaco-editor'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'
import type { CodeTemplate } from '@/types/problem'
import { createCodeTemplate, updateCodeTemplate } from '@/api/problem'

// 配置 monaco workers
self.MonacoEnvironment = {
  getWorker(_, label) {
    if (label === 'typescript' || label === 'javascript') {
      return new tsWorker()
    }
    return new editorWorker()
  },
}

// 支持的编程语言列表
const supportedLanguages = [
  { label: 'Java', value: 'java' },
  { label: 'Python', value: 'python' },
  { label: 'C++', value: 'cpp' },
  { label: 'JavaScript', value: 'javascript' },
  { label: 'TypeScript', value: 'typescript' },
  { label: 'Go', value: 'go' },
  { label: 'C', value: 'c' },
  { label: 'C#', value: 'csharp' },
  { label: 'Rust', value: 'rust' },
  { label: 'Kotlin', value: 'kotlin' },
]

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
const saving = ref(false)
const isFullscreen = ref(false)
const fullscreenEditor = ref('')

// 编辑器相关引用
const solutionEditorRef = ref<HTMLElement | null>(null)
const wrapperEditorRef = ref<HTMLElement | null>(null)
const solutionEditor = shallowRef<monaco.editor.IStandaloneCodeEditor | null>(null)
const wrapperEditor = shallowRef<monaco.editor.IStandaloneCodeEditor | null>(null)

// 初始化编辑器
const initEditor = (editorRef: Ref<HTMLElement | null>, value: string, language: string) => {
  if (!editorRef.value) return null

  const editor = monaco.editor.create(editorRef.value, {
    value: value || '',
    language: language || 'plaintext',
    theme: 'vs',
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 14,
    scrollBeyondLastLine: false,
    lineNumbers: 'on',
    renderWhitespace: 'selection',
    formatOnPaste: true,
    formatOnType: true,
    tabSize: 2,
  })

  // 添加窗口大小变化监听，确保编辑器正确布局
  window.addEventListener('resize', () => {
    editor.layout()
  })

  return editor
}

// 重置代码
const resetCode = (field: 'solutionTemplate' | 'mainWrapperTemplate') => {
  const defaultValue = field === 'solutionTemplate' ? '' : ''
  if (field === 'solutionTemplate' && solutionEditor.value) {
    solutionEditor.value.setValue(defaultValue)
  } else if (field === 'mainWrapperTemplate' && wrapperEditor.value) {
    wrapperEditor.value.setValue(defaultValue)
  }
}

// 切换全屏
const toggleFullscreen = (editorType: 'solution' | 'wrapper') => {
  isFullscreen.value = !isFullscreen.value
  fullscreenEditor.value = isFullscreen.value ? editorType : ''

  // 在下一次 DOM 更新后调整编辑器布局
  nextTick(() => {
    if (editorType === 'solution' && solutionEditor.value) {
      solutionEditor.value.layout()
    } else if (editorType === 'wrapper' && wrapperEditor.value) {
      wrapperEditor.value.layout()
    }
  })
}

// 监听语言变化，更新编辑器语言
watch(() => currentCodeTemplate.value.language, (newLang) => {
  if (newLang && solutionEditor.value) {
    const model = solutionEditor.value.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, newLang)
    }
  }

  if (newLang && wrapperEditor.value) {
    const model = wrapperEditor.value.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, newLang)
    }
  }
})

watch(
  () => props.visible,
  async (newVal) => {
    dialogVisible.value = newVal
    if (newVal) {
      dialogTitle.value = props.isEdit ? '编辑代码模板' : '添加代码模板'
      currentCodeTemplate.value = { ...props.codeTemplate, problemId: props.problemId }

      // 等待 DOM 更新后初始化编辑器
      await nextTick()

      // 销毁旧的编辑器实例
      solutionEditor.value?.dispose()
      wrapperEditor.value?.dispose()

      // 初始化新的编辑器实例
      solutionEditor.value = initEditor(
        solutionEditorRef,
        currentCodeTemplate.value.solutionTemplate || '',
        currentCodeTemplate.value.language || 'java'
      )

      wrapperEditor.value = initEditor(
        wrapperEditorRef,
        currentCodeTemplate.value.mainWrapperTemplate || '',
        currentCodeTemplate.value.language || 'java'
      )

      // 监听编辑器内容变化，更新表单数据
      solutionEditor.value?.onDidChangeModelContent(() => {
        if (solutionEditor.value) {
          currentCodeTemplate.value.solutionTemplate = solutionEditor.value.getValue()
        }
      })

      wrapperEditor.value?.onDidChangeModelContent(() => {
        if (wrapperEditor.value) {
          currentCodeTemplate.value.mainWrapperTemplate = wrapperEditor.value.getValue()
        }
      })
    } else {
      // 关闭对话框时销毁编辑器实例
      solutionEditor.value?.dispose()
      wrapperEditor.value?.dispose()
      solutionEditor.value = null
      wrapperEditor.value = null
    }
  },
  { immediate: true }
)

// 组件卸载时清理资源
onMounted(() => {
  return () => {
    solutionEditor.value?.dispose()
    wrapperEditor.value?.dispose()
  }
})

const onClose = () => {
  emit('update:visible', false)
}

const saveCodeTemplate = async () => {
  if (!currentCodeTemplate.value.language) {
    ElMessage.warning('请选择编程语言')
    return
  }

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
</script>

<style scoped>
.form-container {
  padding: 12px 16px;
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.form-select {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  background-color: #fff;
  transition: border-color 0.2s;
  margin-bottom: 16px;
}

.form-select:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.editor-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.code-editor {
  height: 250px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  margin-top: 8px;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.btn-text {
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  padding: 4px 8px;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.btn-text:hover {
  color: #66b1ff;
}

.btn-default {
  background: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

.btn-default:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.btn-primary {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.btn-primary:hover {
  background: #66b1ff;
  border-color: #66b1ff;
}

.btn-primary:disabled {
  background: #a0cfff;
  border-color: #a0cfff;
  cursor: not-allowed;
}
</style>
