<template>
  <div class="code-card">
    <HeaderCard v-model:model-value="selectedLanguage" :available-languages="availableLanguages"
      @reset-code="resetCode" />
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
import HeaderCard from './CodeCard/HeaderCard.vue';
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
const selectedLanguage = ref(availableLanguages.value[0] || 'java');
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
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background-color: #fff;
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
