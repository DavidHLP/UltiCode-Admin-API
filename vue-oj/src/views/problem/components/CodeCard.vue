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
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  padding-right: 16px;
}

.language-tabs {
  flex: 1;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.editor-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.editor-actions .el-button {
  font-size: 12px;
  padding: 4px 8px;
}

.editor-container {
  flex: 1;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  flex-shrink: 0;
  font-size: 12px;
  color: #666;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
}

.card-footer .el-button {
  border-radius: 6px;
  font-size: 14px;
  padding: 8px 16px;
}

/* Language tabs 样式 */
:deep(.language-tabs .el-tabs__header) {
  margin: 0;
  border-bottom: none;
}

:deep(.language-tabs .el-tabs__nav-wrap) {
  padding: 0 16px;
}

:deep(.language-tabs .el-tabs__item) {
  color: #666;
  font-size: 14px;
  padding: 0 16px;
  height: 40px;
  line-height: 40px;
}

:deep(.language-tabs .el-tabs__item.is-active) {
  color: #1890ff;
  font-weight: 500;
}

:deep(.language-tabs .el-tabs__active-bar) {
  background-color: #1890ff;
}

:deep(.language-tabs .el-tabs__content) {
  display: none;
}
</style>
