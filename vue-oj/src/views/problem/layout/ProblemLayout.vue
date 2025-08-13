<template>
  <el-container class="problem-layout">
    <!-- Header 区域 -->
    <el-header class="problem-header" height="auto">
      <slot name="header">
        <el-row class="problem-header-row" align="middle" justify="space-between" :gutter="8">
          <el-col :span="8" class="header-left">
            <slot name="header-left" />
          </el-col>
          <el-col :span="8" class="header-center">
            <slot name="header-center" />
          </el-col>
          <el-col :span="8" class="header-right">
            <slot name="header-right" />
          </el-col>
        </el-row>
      </slot>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="problem-container">
      <Splitpanes class="problem-splitpanes" @resize="handleResize" :push-other-panes="false">
        <!-- 左侧题目描述面板 -->
        <Pane :size="leftPaneSize" :min-size="25" :max-size="75" class="question-pane">
          <slot name="question" />
        </Pane>

        <!-- 右侧工作区面板 -->
        <Pane :size="100 - leftPaneSize" :min-size="25" class="workspace-pane">
          <Splitpanes class="workspace-splitpanes" horizontal @resize="handleWorkspaceResize">
            <!-- 代码编辑器面板 -->
            <Pane :size="topPaneSize" :min-size="30" :max-size="80" class="code-pane">
              <slot name="code" />
            </Pane>

            <!-- 调试/结果面板 -->
            <Pane :size="100 - topPaneSize" :min-size="20" class="debug-pane">
              <slot name="debug" />
            </Pane>
          </Splitpanes>
        </Pane>
      </Splitpanes>
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'

// Props 定义
interface Props {
  initialLeftPaneSize?: number
  initialTopPaneSize?: number
  saveLayout?: boolean
  layoutKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  initialLeftPaneSize: 50,
  initialTopPaneSize: 60,
  saveLayout: true,
  layoutKey: 'problem-layout'
})

// 移除未使用的事件发射，精简组件逻辑

// 响应式状态
const leftPaneSize = ref(props.initialLeftPaneSize)
const topPaneSize = ref(props.initialTopPaneSize)

// 布局存储键名
const getStorageKey = (suffix: string) => `${props.layoutKey}-${suffix}`

// 加载保存的布局
const loadLayout = () => {
  if (!props.saveLayout) return

  try {
    const savedLeftSize = localStorage.getItem(getStorageKey('left-pane'))
    const savedTopSize = localStorage.getItem(getStorageKey('top-pane'))

    if (savedLeftSize) {
      leftPaneSize.value = Number(savedLeftSize)
    }
    if (savedTopSize) {
      topPaneSize.value = Number(savedTopSize)
    }
  } catch (error) {
    console.warn('Failed to load layout from localStorage:', error)
  }
}

// 保存布局
const saveLayout = () => {
  if (!props.saveLayout) return

  try {
    localStorage.setItem(getStorageKey('left-pane'), leftPaneSize.value.toString())
    localStorage.setItem(getStorageKey('top-pane'), topPaneSize.value.toString())
  } catch (error) {
    console.warn('Failed to save layout to localStorage:', error)
  }
}

// 处理主分割线调整
const handleResize = (event: Array<{ size: number }>) => {
  leftPaneSize.value = event[0].size
  saveLayout()
}

// 处理工作区分割线调整
const handleWorkspaceResize = (event: Array<{ size: number }>) => {
  topPaneSize.value = event[0].size
  saveLayout()
}

// 重置布局
const resetLayout = () => {
  leftPaneSize.value = props.initialLeftPaneSize
  topPaneSize.value = props.initialTopPaneSize
  saveLayout()
}

// 响应式布局调整
const handleWindowResize = () => {
  // 在小屏幕上自动调整布局
  if (window.innerWidth < 768) {
    leftPaneSize.value = 100 // 移动端默认全屏显示题目
  } else if (window.innerWidth < 1200) {
    leftPaneSize.value = Math.max(leftPaneSize.value, 40) // 平板端最小40%
  }
}

// 暴露方法给父组件
defineExpose({
  resetLayout,
  leftPaneSize,
  topPaneSize
})

// 生命周期
onMounted(() => {
  loadLayout()
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleWindowResize)
})
</script>

<style scoped>
.problem-layout {
  height: 98vh;
  background: #f5f7fa;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* Header 样式 */
.problem-header {
  flex-shrink: 0;
  background: #ffffff;
  z-index: 100;
}

/* 使用 Element Plus el-header + el-row 取代 .default-header */

.header-left,
.header-center,
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left {
  flex: 1;
  justify-content: flex-start;
}

.header-center {
  flex: 2;
  justify-content: center;
}

.header-right {
  flex: 1;
  justify-content: flex-end;
}

.problem-container {
  flex: 1;
  padding: 8px;
  box-sizing: border-box;
  overflow: hidden;
  min-height: 0;
}

.problem-splitpanes {
  height: 100%;
  overflow: hidden;
}

.workspace-splitpanes {
  height: 100%;
  overflow: hidden;
}

/* 面板容器样式 */
.workspace-pane {
  background: transparent;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.question-pane {
  background: #ffffff;
  overflow: hidden;
  /* 问题面板不滚动，交由子级内容容器控制 */
  position: relative;
}

.code-pane,
.debug-pane {
  background: #ffffff;
  overflow: auto;
  /* 代码与调试面板内部滚动 */
  position: relative;
}

/* 让代码编辑器插槽内容自适应填满高度，避免 Monaco 容器过小 */
.code-pane {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.code-pane>* {
  flex: 1;
  min-height: 0;
}

/* 分割线样式（简化） */
:deep(.splitpanes__splitter) {
  background: #ebeef5 !important;
  border: none !important;
  transition: background-color 0.2s ease;
}

/* 垂直分割线 */
:deep(.splitpanes--vertical > .splitpanes__splitter) {
  width: 6px !important;
  cursor: col-resize;
}

/* 水平分割线 */
:deep(.splitpanes--horizontal > .splitpanes__splitter) {
  height: 6px !important;
  cursor: row-resize;
}

/* 悬停与激活 */
:deep(.splitpanes__splitter:hover),
:deep(.splitpanes__splitter:active) {
  background: #dcdfe6 !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .problem-container {
    padding: 8px;
  }
}

@media (max-width: 768px) {
  .problem-layout {
    background: #f5f7fa;
  }

  .problem-header {
    background: rgba(255, 255, 255, 0.98);
  }

  .default-header {
    padding: 4px 12px;
    min-height: 36px;
  }

  .problem-container {
    padding: 4px;
  }

  .problem-splitpanes {
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  /* 移动端分割线调整 */
  :deep(.splitpanes--vertical > .splitpanes__splitter) {
    width: 6px !important;
  }

  :deep(.splitpanes--horizontal > .splitpanes__splitter) {
    height: 6px !important;
  }
}

@media (max-width: 480px) {
  .default-header {
    padding: 4px 12px;
    min-height: 40px;
  }

  .header-left,
  .header-center,
  .header-right {
    gap: 4px;
  }

  .problem-container {
    padding: 4px;
  }

  :deep(.splitpanes--vertical > .splitpanes__splitter) {
    width: 4px !important;
  }

  :deep(.splitpanes--horizontal > .splitpanes__splitter) {
    height: 4px !important;
  }
}
</style>
