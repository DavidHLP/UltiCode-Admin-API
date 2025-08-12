<template>
  <div class="code-card-container">
    <div class="header-section">
      <div class="lang-select-row">
        <div class="lang-label">
          <el-icon>
            <Document />
          </el-icon>
          <span>选择语言</span>
        </div>
        <el-select
          v-model="selectedLanguage"
          class="lang-select"
          size="small"
          placeholder="选择语言"
          filterable
        >
          <el-option
            v-for="lang in availableLanguages"
            :key="lang"
            :label="displayNameMap[lang] || lang"
            :value="lang"
          />
        </el-select>
      </div>
    </div>
    <div class="main-content">
      <div ref="monacoEditorRef" class="editor-container"></div>
    </div>
    <div class="card-footer">
      <span>行 {{ cursorPosition.line }}, 列 {{ cursorPosition.column }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, shallowRef, computed } from 'vue';
import * as monaco from 'monaco-editor';
import { Document } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { toMonacoLanguage, LANGUAGE_OPTIONS } from '@/utils/languagetype';
import { getCodeTemplate } from '@/api/problem';
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
  problemId?: number;
}>();

const emit = defineEmits<{
  (e: 'code-change', code: string, language: string): void;
  (e: 'submit', language: string, code: string): void;
}>();

const monacoEditorRef = ref<HTMLDivElement | null>(null);
const editor = shallowRef<monaco.editor.IStandaloneCodeEditor | null>(null);
// 使用统一的语言选项，值为后端枚举名
const availableLanguages = computed(() => LANGUAGE_OPTIONS.map(o => o.name));
const displayNameMap = computed<Record<string, string>>(
  () => Object.fromEntries(LANGUAGE_OPTIONS.map(o => [o.name, o.display]))
);
// 默认优先使用 initialCode 的首个语言
const preferredLang = Object.keys(props.initialCode || {})[0] || 'JAVA';
const selectedLanguage = ref(preferredLang);
const cursorPosition = ref({ line: 1, column: 1 });

onMounted(() => {
  if (monacoEditorRef.value) {
    const initLang = selectedLanguage.value;
    const monacoLang = toMonacoLanguage(initLang);
    const initValue = props.initialCode[initLang] ?? '';
    editor.value = monaco.editor.create(monacoEditorRef.value, {
      value: initValue,
      language: monacoLang,
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
    // 若初始无模板且提供了 problemId，则尝试拉取后端模板
    if (!initValue && props.problemId) {
      getCodeTemplate({ problemId: props.problemId, language: initLang })
        .then((tpl) => {
          if (editor.value) editor.value.setValue(tpl || '');
        })
        .catch(() => {
          ElMessage.error('获取代码模板失败');
        });
    }
  }
});

watch(selectedLanguage, (newLang) => {
  if (editor.value) {
    const model = editor.value.getModel();
    if (model) {
      const monacoLang = toMonacoLanguage(newLang);
      monaco.editor.setModelLanguage(model, monacoLang);
      const localTpl = props.initialCode[newLang] ?? '';
      if (localTpl) {
        editor.value.setValue(localTpl);
      } else if (props.problemId) {
        getCodeTemplate({ problemId: props.problemId, language: newLang })
          .then((tpl) => {
            if (editor.value) editor.value.setValue(tpl || '');
          })
          .catch(() => {
            ElMessage.error('获取代码模板失败');
          });
      } else {
        editor.value.setValue('');
      }
    }
  }
});

const resetCode = () => {
  if (editor.value) {
    const lang = selectedLanguage.value;
    const localTpl = props.initialCode[lang] ?? '';
    if (localTpl) {
      editor.value.setValue(localTpl);
    } else if (props.problemId) {
      getCodeTemplate({ problemId: props.problemId, language: lang })
        .then((tpl) => {
          if (editor.value) editor.value.setValue(tpl || '');
        })
        .catch(() => {
          ElMessage.error('获取代码模板失败');
        });
    } else {
      editor.value.setValue('');
    }
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

// 暴露submitCode方法给父组件调用

defineExpose({ getCode, resetCode, submitCode });
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
  padding: 8px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 语言选择行 */
.lang-select-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
}

.lang-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.lang-label .el-icon {
  font-size: 16px;
}

.lang-select {
  min-width: 180px;
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
