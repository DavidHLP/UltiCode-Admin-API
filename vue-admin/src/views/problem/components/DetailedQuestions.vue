<template>
  <div class="md-section" ref="mdSectionRef">
    <el-form-item label="题目描述" prop="description">
      <div class="md-card">
        <MdEditor
          :model-value="currentProblem.description"
          :theme="props.theme"
          placeholder="请输入题目描述"
        />
      </div>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Problem } from '@/types/problem'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const props = defineProps<{
  currentProblem: Partial<Problem>
  theme: 'light' | 'dark'
}>()

// 视口自适应高度，避免页面出现上下滚动
const mdSectionRef = ref<HTMLElement | null>(null)

</script>

<style scoped>
.md-card {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-fill-color-blank);
}

/* 去除 Markdown 表单项底部间距，避免多余高度引起外层滚动 */
.md-section :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>
