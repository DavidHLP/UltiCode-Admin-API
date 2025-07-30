<template>
  <div class="error-code-component">
    <div ref="errorRef" class="error-content" :data-type="type">
      <pre><code>{{ message }}</code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const { type } = defineProps({
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'error',
    validator: (value: string) => ['error', 'compile'].includes(value)
  }
});

const errorRef = ref<HTMLElement | null>(null);
</script>

<style scoped>
.error-code-component {
  width: 100%;
  overflow-x: auto;
  background-color: #ffebee;
  /* 更淡的红色背景 */
  border-radius: 6px;
  border: 1px solid #ffcdd2;
  /* 浅红色边框 */
  margin: 8px 0;
}

.error-content {
  padding: 16px;
  margin: 0;
  font-family: 'Fira Code', 'Source Code Pro', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #d32f2f;
  /* 深红色文字 */
  background-color: #ffebee;
  /* 淡红色背景 */
  border-radius: 4px;
}

.error-content[data-type="compile"] {
  color: #e65100;
  /* 深橙色文字 */
  background-color: #fff3e0;
  /* 淡橙色背景 */
  border-color: #ffe0b2;
  /* 浅橙色边框 */
}

error-content pre,
:deep(pre) {
  margin: 0;
  padding: 0;
  background: transparent;
  border: none;
  white-space: pre-wrap;
  word-break: break-word;
}

:deep(code) {
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  color: inherit;
}
</style>
