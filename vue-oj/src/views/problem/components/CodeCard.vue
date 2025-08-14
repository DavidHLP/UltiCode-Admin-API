<template>
  <div ref="containerRef" class="code-card-container" :class="{ 'is-fullscreen': isFullscreen }">
    <HeaderComponent :icon="Document" title="代码编辑器">
      <template #right>
        <el-space wrap size="small" alignment="center">
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

            <el-tooltip content="自动换行" placement="bottom">
              <el-button text @click="toggleWrap">
                <el-icon><SwitchButton /></el-icon>
                <span class="btn-text">{{ wordWrap ? '换行开' : '换行关' }}</span>
              </el-button>
            </el-tooltip>

            <el-tooltip content="主题切换" placement="bottom">
              <el-button text @click="toggleTheme">
                <el-icon><component :is="isDark ? Moon : Sunny" /></el-icon>
              </el-button>
            </el-tooltip>

            <el-tooltip content="减小字号" placement="bottom">
              <el-button text @click="decreaseFont"><el-icon><ZoomOut /></el-icon></el-button>
            </el-tooltip>

            <el-tooltip content="增大字号" placement="bottom">
              <el-button text @click="increaseFont"><el-icon><ZoomIn /></el-icon></el-button>
            </el-tooltip>

            <el-tooltip content="格式化文档" placement="bottom">
              <el-button text @click="formatDocument"><el-icon><MagicStick /></el-icon></el-button>
            </el-tooltip>

            <el-tooltip content="复制代码" placement="bottom">
              <el-button text @click="copyCode"><el-icon><CopyDocument /></el-icon></el-button>
            </el-tooltip>

            <el-tooltip content="下载代码" placement="bottom">
              <el-button text @click="downloadCode"><el-icon><Download /></el-icon></el-button>
            </el-tooltip>

            <el-tooltip content="重置为模板" placement="bottom">
              <el-button text @click="resetCode"><el-icon><RefreshRight /></el-icon></el-button>
            </el-tooltip>

            <el-tooltip content="全屏" placement="bottom">
              <el-button text @click="toggleFullScreen"><el-icon><FullScreen /></el-icon></el-button>
            </el-tooltip>
        </el-space>
      </template>
    </HeaderComponent>
    <div class="main-content">
      <div ref="monacoEditorRef" class="editor-container"></div>
    </div>
    <div class="card-footer">
      <span>行 {{ cursorPosition.line }}, 列 {{ cursorPosition.column }}</span>
      <span>长度 {{ codeStats.chars }} | 行数 {{ codeStats.lines }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, shallowRef, computed, watchEffect, nextTick } from 'vue';
import * as monaco from 'monaco-editor';
import { Document, RefreshRight, FullScreen, CopyDocument, Download, MagicStick, Sunny, Moon, ZoomIn, ZoomOut, SwitchButton } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { toMonacoLanguage, LANGUAGE_OPTIONS } from '@/utils/languagetype';
import { getCodeTemplate } from '@/api/problem';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';
import HeaderComponent from './components/HeaderComponent.vue'

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

defineEmits<{
  (e: 'code-change', code: string, language: string): void;
  (e: 'submit', language: string, code: string): void;
}>();

const monacoEditorRef = ref<HTMLDivElement | null>(null);
const containerRef = ref<HTMLDivElement | null>(null);
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
const codeStats = ref({ chars: 0, lines: 1 });

// 体验增强：主题、自动换行、字号、全屏
const isDark = ref(false);
const wordWrap = ref(false);
const fontSize = ref(14);
const isFullscreen = ref(false);
// 顶部工具栏模式（已移除 tabs）

const extensionMap: Record<string, string> = {
  JAVA: 'java', PYTHON: 'py', C: 'c', CPP: 'cpp', JAVASCRIPT: 'js', TYPESCRIPT: 'ts', GO: 'go', RUST: 'rs',
  KOTLIN: 'kt', CSHARP: 'cs', RUBY: 'rb', PHP: 'php', SWIFT: 'swift'
};

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
      fontSize: fontSize.value,
      scrollBeyondLastLine: false,
      wordWrap: wordWrap.value ? 'on' : 'off',
    });

    editor.value.onDidChangeCursorPosition((e) => {
      cursorPosition.value = {
        line: e.position.lineNumber,
        column: e.position.column,
      };
    });
    editor.value.onDidChangeModelContent(() => {
      updateStats();
    });
    // 初始化统计
    updateStats();
    // 若初始无模板且提供了 problemId，则尝试拉取后端模板
    if (!initValue && props.problemId) {
      getCodeTemplate({ problemId: props.problemId, language: initLang })
        .then((tpl) => {
          if (editor.value) editor.value.setValue(tpl || '');
          nextTick(updateStats);
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
        nextTick(updateStats);
      } else if (props.problemId) {
        getCodeTemplate({ problemId: props.problemId, language: newLang })
          .then((tpl) => {
            if (editor.value) editor.value.setValue(tpl || '');
            nextTick(updateStats);
          })
          .catch(() => {
            ElMessage.error('获取代码模板失败');
          });
      } else {
        editor.value.setValue('');
        nextTick(updateStats);
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
      nextTick(updateStats);
    } else if (props.problemId) {
      getCodeTemplate({ problemId: props.problemId, language: lang })
        .then((tpl) => {
          if (editor.value) editor.value.setValue(tpl || '');
          nextTick(updateStats);
        })
        .catch(() => {
          ElMessage.error('获取代码模板失败');
        });
    } else {
      editor.value.setValue('');
      nextTick(updateStats);
    }
  }
};

const getCode = () => {
  return editor.value?.getValue();
};

// ============ 辅助能力 ============
const updateStats = () => {
  const val = editor.value?.getValue() ?? '';
  codeStats.value.chars = val.length;
  codeStats.value.lines = val ? val.split(/\r?\n/).length : 1;
};

const toggleTheme = () => {
  isDark.value = !isDark.value;
};

watchEffect(() => {
  monaco.editor.setTheme(isDark.value ? 'vs-dark' : 'vs');
});

const toggleWrap = () => {
  wordWrap.value = !wordWrap.value;
};

watch(wordWrap, (val) => {
  editor.value?.updateOptions({ wordWrap: val ? 'on' : 'off' });
});

watch(fontSize, (val) => {
  editor.value?.updateOptions({ fontSize: Math.min(24, Math.max(10, val)) });
});

const increaseFont = () => (fontSize.value = Math.min(24, fontSize.value + 1));
const decreaseFont = () => (fontSize.value = Math.max(10, fontSize.value - 1));

const formatDocument = async () => {
  try {
    await editor.value?.getAction('editor.action.formatDocument')?.run();
  } catch {
    ElMessage.warning('当前语言暂不支持自动格式化');
  }
};

const copyCode = async () => {
  const code = getCode() || '';
  try {
    await navigator.clipboard.writeText(code);
    ElMessage.success('已复制到剪贴板');
  } catch {
    // 兼容降级
    const ta = document.createElement('textarea');
    ta.value = code;
    document.body.appendChild(ta);
    ta.select();
    document.execCommand('copy');
    document.body.removeChild(ta);
    ElMessage.success('已复制到剪贴板');
  }
};

const downloadCode = () => {
  const code = getCode() || '';
  const lang = selectedLanguage.value;
  const ext = extensionMap[lang] || 'txt';
  const blob = new Blob([code], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `solution.${ext}`;
  a.click();
  URL.revokeObjectURL(url);
};

const toggleFullScreen = async () => {
  try {
    if (!isFullscreen.value) {
      await containerRef.value?.requestFullscreen?.();
      isFullscreen.value = true;
    } else {
      await document.exitFullscreen();
      isFullscreen.value = false;
    }
  } catch {
    // 部分环境不支持全屏 API，降级为样式全屏
    isFullscreen.value = !isFullscreen.value;
  }
};

// 暴露方法给父组件调用
defineExpose({ getCode, resetCode });
</script>

<style scoped>
@import '@/assets/styles/scrollbar.css';

/* 容器布局 */
.code-card-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 0;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.code-card-container.is-fullscreen {
  position: fixed;
  inset: 0;
  z-index: 2000;
  border-radius: 0;
}

/* Header 区域样式 */
.header-section {
  flex-shrink: 0;
  padding: 0 8px;
  display: block;
  background: linear-gradient(180deg, rgba(245, 247, 255, 0.9), rgba(255, 255, 255, 0.9));
  border-bottom: 1px solid #e5e7eb;
}

/* 工具栏样式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  min-height: 44px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #374151;
}

.title {
  font-size: 14px;
  font-weight: 600;
}

.toolbar-right :deep(.el-button) {
  padding: 6px 8px;
  border-radius: 8px;
}

/* 右侧工具 Tabs 样式 */
.lang-select {
  min-width: 180px;
}

/* Editor 操作按钮样式 */
.btn-text {
  font-size: 12px;
  color: #606266;
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
  padding: 8px 12px;
  flex-shrink: 0;
  font-size: 12px;
  color: #6b7280;
  background: linear-gradient(180deg, #ffffff, #f9fafb);
  border-top: 1px solid #e5e7eb;
}
</style>
