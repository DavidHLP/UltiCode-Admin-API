<template>
  <div class="code-card">
    <div class="card-header">
      <div class="header-left">
        <div class="header-item">
          <el-icon>
            <CodIcon />
          </el-icon>
          <span>代码</span>
        </div>
      </div>
      <div class="header-center">
        <el-select v-model="selectedLanguage" size="small" class="lang-select">
          <el-option v-for="lang in availableLanguages" :key="lang"
            :label="lang.charAt(0).toUpperCase() + lang.slice(1)" :value="lang" />
        </el-select>
      </div>
      <div class="header-right">
        <el-icon>
          <Setting />
        </el-icon>
        <el-icon>
          <CollectionTag />
        </el-icon>
        <el-icon @click="resetCode">
          <Refresh />
        </el-icon>
        <el-icon>
          <FullScreen />
        </el-icon>
      </div>
    </div>
    <div ref="monacoEditorRef" class="editor-container"></div>
    <div class="card-footer">
      <span>{{ statusText }}</span>
      <span>行 {{ cursorPosition.line }}, 列 {{ cursorPosition.column }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, shallowRef } from 'vue';
import * as monaco from 'monaco-editor';
import {
  Setting,
  CollectionTag,
  Refresh,
  FullScreen,
} from '@element-plus/icons-vue';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';
import CodIcon from '@/assets/icon/CodIcon.vue';

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

const monacoEditorRef = ref<HTMLDivElement | null>(null);
const editor = shallowRef<monaco.editor.IStandaloneCodeEditor | null>(null);
const availableLanguages = ref(Object.keys(props.initialCode));
const selectedLanguage = ref(availableLanguages.value[0] || 'java');
const statusText = ref('已存储');
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

defineExpose({ getCode, resetCode });
</script>

<style scoped>
.code-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background-color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 48px;
  flex-shrink: 0;
  color: #595959;
}

.header-left,
.header-center,
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-center {
  flex-grow: 1;
  padding-left: 24px;
}

.header-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.lang-select {
  width: 100px;
}

:deep(.lang-select .el-input__wrapper) {
  box-shadow: none !important;
  background-color: transparent;
}

.header-right .el-icon {
  cursor: pointer;
}

.editor-container {
  flex-grow: 1;
  overflow: hidden;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  flex-shrink: 0;
  font-size: 12px;
  color: #8c8c8c;
}
</style>
