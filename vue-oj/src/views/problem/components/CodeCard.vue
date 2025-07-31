<template>
  <div class="code-card-container">
    <div class="header-section">
      <el-tabs v-model="selectedLanguage" class="code-tabs">
        <el-tab-pane v-for="lang in availableLanguages" :key="lang" :name="lang">
          <template #label>
            <div class="tab-label">
              <el-icon>
                <Document />
              </el-icon>
              <span>{{ lang.charAt(0).toUpperCase() + lang.slice(1) }}</span>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>
      <div class="editor-actions">
        <el-button size="small" @click="resetCode">
          <template #icon><el-icon>
              <Refresh />
            </el-icon></template>
          重置
        </el-button>
        <el-button size="small">
          <template #icon><el-icon>
              <FullScreen />
            </el-icon></template>
          全屏
        </el-button>
      </div>
    </div>
    <div class="main-content">
      <div ref="monacoEditorRef" class="editor-container"></div>
    </div>
    <div class="card-footer">
      <el-button type="primary" @click="submitCode">提交</el-button>
      <span>行 {{ cursorPosition.line }}, 列 {{ cursorPosition.column }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, shallowRef, computed } from 'vue';
import * as monaco from 'monaco-editor';
import { Document, Refresh, FullScreen } from '@element-plus/icons-vue';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';

self.MonacoEnvironment = {
  getWorker(label: string) {
    if (label === 'typescript' || label === 'javascript') {
      return new tsWorker();
    }
    return new editorWorker();
  },
};

const props = defineProps<{
  initialCode: { [language: string]: string };
}>();

const emit = defineEmits<{
  (e: 'code-change', code: string, language: string): void;
  (e: 'submit', language: string, code: string): void;
}>();

const monacoEditorRef = ref<HTMLDivElement | null>(null);
const editor = shallowRef<monaco.editor.IStandaloneCodeEditor | null>(null);
const availableLanguages = computed(() => Object.keys(props.initialCode));
const selectedLanguage = ref(availableLanguages.value[0] || 'JAVA');
const cursorPosition = ref({ line: 1, column: 1 });

onMounted(() => {
  if (monacoEditorRef.value) {
    editor.value = monaco.editor.create(monacoEditorRef.value, {
      value: props.initialCode[selectedLanguage.value],
      language: selectedLanguage.value,
      theme: 'vs',
      automaticLayout: true,
      minimap: {
        enabled: false,
      },
      fontSize: 14,
      scrollBeyondLastLine: false,
    });

    editor.value.onDidChangeCursorPosition((e) => {
      cursorPosition.value = {
        line: e.position.lineNumber,
        column: e.position.column,
      };
    });
  }
});

watch(selectedLanguage, (newLang) => {
  if (editor.value) {
    const model = editor.value.getModel();
    if (model) {
      monaco.editor.setModelLanguage(model, newLang);
      editor.value.setValue(props.initialCode[newLang]);
    }
  }
});

const resetCode = () => {
  if (editor.value) {
    editor.value.setValue(props.initialCode[selectedLanguage.value]);
  }
};

const getCode = () => {
  return editor.value?.getValue();
};

const submitCode = () => {
  const code = getCode();
  if (code) {
    emit('code-change', code, selectedLanguage.value);
    emit('submit', selectedLanguage.value, code);
  }
};

defineExpose({ getCode, resetCode });
</script>

<style scoped>
/* 容器布局 */
.code-card-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Header 区域样式 */
.header-section {
  flex-shrink: 0;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 20px;
}

/* Tab 样式优化 */
.code-tabs {
  --el-tabs-header-height: 48px;
  flex: 1;
}

.code-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.code-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0 16px;
}

.code-tabs :deep(.el-tabs__item) {
  height: 48px;
  line-height: 48px;
  padding: 0 16px;
  color: #606266;
  font-weight: 400;
  border: none;
  transition: all 0.2s ease;
}

.code-tabs :deep(.el-tabs__item:hover) {
  color: #409eff;
}

.code-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 500;
}

.code-tabs :deep(.el-tabs__active-bar) {
  height: 2px;
  background-color: #409eff;
}

/* Tab 标签内容样式 */
.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.tab-label .el-icon {
  font-size: 16px;
}

/* Editor 操作按钮样式 */
.editor-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.editor-actions .el-button {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  background: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266;
  transition: all 0.2s ease;
}

.editor-actions .el-button:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
}

/* Main 内容区域 */
.main-content {
  flex: 1;
  background: #ffffff;
  overflow: hidden;
  min-height: 0;
}

.editor-container {
  height: 100%;
  overflow: hidden;
  position: relative;
}

/* Footer 区域样式 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  flex-shrink: 0;
  font-size: 13px;
  color: #909399;
  background: #ffffff;
  border-top: 1px solid #e4e7ed;
}

.card-footer .el-button {
  border-radius: 4px;
  font-size: 14px;
  font-weight: 400;
  padding: 8px 20px;
}
</style>
