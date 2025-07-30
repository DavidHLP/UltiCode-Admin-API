<template>
  <div class="code-card">
    <div class="code-header">
      <el-tabs v-model="selectedLanguage" class="language-tabs">
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
              <Setting />
            </el-icon></template>
          设置
        </el-button>
        <el-button size="small">
          <template #icon><el-icon>
              <FullScreen />
            </el-icon></template>
          全屏
        </el-button>
      </div>
    </div>
    <div ref="monacoEditorRef" class="editor-container"></div>
    <div class="card-footer">
      <el-button type="primary" @click="submitCode">提交</el-button>
      <span>行 {{ cursorPosition.line }}, 列 {{ cursorPosition.column }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, shallowRef, computed } from 'vue';
import * as monaco from 'monaco-editor';
import { Document, Refresh, Setting, FullScreen } from '@element-plus/icons-vue';
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
.code-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
  padding-right: 20px;
  backdrop-filter: blur(10px);
}

.language-tabs {
  flex: 1;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.editor-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.editor-actions .el-button {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(226, 232, 240, 0.6);
  color: #64748b;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.editor-actions .el-button:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  color: #3b82f6;
  transform: translateY(-1px);
}

.editor-container {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  flex-shrink: 0;
  font-size: 13px;
  color: #64748b;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.9), rgba(255, 255, 255, 0.9));
  border-top: 1px solid rgba(226, 232, 240, 0.5);
  backdrop-filter: blur(10px);
}

.card-footer .el-button {
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  padding: 10px 24px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border: none;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.card-footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
}

/* Language tabs 样式 */
:deep(.language-tabs .el-tabs__header) {
  margin: 0;
  border-bottom: none;
}

:deep(.language-tabs .el-tabs__nav-wrap) {
  padding: 0 20px;
}

:deep(.language-tabs .el-tabs__item) {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
  border-radius: 8px 8px 0 0;
  margin: 0 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.language-tabs .el-tabs__item:hover) {
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.05);
}

:deep(.language-tabs .el-tabs__item.is-active) {
  color: #3b82f6;
  font-weight: 600;
  background: rgba(59, 130, 246, 0.1);
}

:deep(.language-tabs .el-tabs__active-bar) {
  background: linear-gradient(90deg, #3b82f6, #1d4ed8);
  height: 3px;
  border-radius: 2px;
}

:deep(.language-tabs .el-tabs__content) {
  display: none;
}

/* Monaco Editor 样式增强 */
:deep(.monaco-editor) {
  border-radius: 0;
}

:deep(.monaco-editor .margin) {
  background: rgba(248, 250, 252, 0.5) !important;
}

:deep(.monaco-editor .monaco-editor-background) {
  background: rgba(255, 255, 255, 0.8) !important;
}
</style>
