<template>
  <div class="code-component">
    <div ref="codeRef">
      <pre><code :class="`language-${languageClass}`">{{ code }}</code></pre>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import hljs from 'highlight.js'

const props = defineProps({
  code: {
    type: String,
    required: true,
  },
  language: {
    type: String,
    default: 'plaintext',
  },
})

const codeRef = ref<HTMLElement | null>(null)

// 获取语言高亮类型
const languageClass = (() => {
  const langMap: Record<string, string> = {
    javascript: 'javascript',
    typescript: 'typescript',
    java: 'java',
    python: 'python',
    cpp: 'cpp',
    'c++': 'cpp',
  }
  return langMap[props.language.toLowerCase()] || 'plaintext'
})()

// 代码高亮
onMounted(() => {
  if (codeRef.value) {
    const blocks = codeRef.value.querySelectorAll('pre code')
    blocks.forEach((block) => {
      hljs.highlightElement(block as HTMLElement)
    })
  }
})
</script>

<style scoped>
.code-component {
  width: 100%;
  overflow-x: auto;
  overflow-y: auto;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  margin: 8px 0;
  max-height: 500px;
  /* 自��义滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(59, 130, 246, 0.4) rgba(0, 0, 0, 0.05);
}

/* 针对代码组件的自定义滚动条样式 */
.code-component::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.code-component::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.code-component::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.4);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.code-component::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.6);
}

.code-component::-webkit-scrollbar-corner {
  background: rgba(0, 0, 0, 0.05);
}

:deep(pre) {
  margin: 0;
  padding: 16px;
  background: transparent;
  border: none;
  font-size: 14px;
  line-height: 1.5;
  tab-size: 2;
  white-space: pre;
  word-spacing: normal;
  word-break: normal;
  word-wrap: normal;
}

:deep(code) {
  font-family: 'Fira Code', 'Source Code Pro', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #24292e;
}

:deep(.hljs) {
  background: transparent !important;
}
</style>
