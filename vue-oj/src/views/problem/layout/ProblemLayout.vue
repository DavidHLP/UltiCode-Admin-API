<template>
  <div class="problem-layout">
    <!-- Header 区域 -->
    <div class="problem-header">
      <slot name="header">
        <!-- 默认 header 内容 -->
        <div class="default-header">
          <div class="header-left">
            <slot name="header-left" />
          </div>
          <div class="header-center">
            <slot name="header-center" />
          </div>
          <div class="header-right">
            <slot name="header-right" />
          </div>
        </div>
      </slot>
    </div>

    <!-- 主要内容区域 -->
    <div class="problem-container">
      <Splitpanes class="problem-splitpanes" @resize="handleResize" :push-other-panes="false">
        <!-- 左侧题目描述面板 -->
        <Pane :size="leftPaneSize" :min-size="25" :max-size="75" class="question-pane">
          <div class="pane-content">
            <slot name="question" />
          </div>
        </Pane>

        <!-- 右侧工作区面板 -->
        <Pane :size="100 - leftPaneSize" :min-size="25" class="workspace-pane">
          <Splitpanes class="workspace-splitpanes" horizontal @resize="handleWorkspaceResize">
            <!-- 代码编辑器面板 -->
            <Pane :size="topPaneSize" :min-size="30" :max-size="80" class="code-pane">
              <div class="pane-content">
                <slot name="code" />
              </div>
            </Pane>

            <!-- 调试/结果面板 -->
            <Pane :size="100 - topPaneSize" :min-size="20" class="debug-pane">
              <div class="pane-content">
                <slot name="debug" />
              </div>
            </Pane>
          </Splitpanes>
        </Pane>
      </Splitpanes>
    </div>
  </div>
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

// Emits 定义
interface Emits {
  (e: 'layout-change', data: { leftPaneSize: number; topPaneSize: number }): void
  (e: 'pane-resize', data: { type: 'horizontal' | 'vertical'; sizes: number[] }): void
}

const emit = defineEmits<Emits>()

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

  emit('layout-change', {
    leftPaneSize: leftPaneSize.value,
    topPaneSize: topPaneSize.value
  })

  emit('pane-resize', {
    type: 'vertical',
    sizes: event.map(pane => pane.size)
  })
}

// 处理工作区分割线调整
const handleWorkspaceResize = (event: Array<{ size: number }>) => {
  topPaneSize.value = event[0].size
  saveLayout()

  emit('layout-change', {
    leftPaneSize: leftPaneSize.value,
    topPaneSize: topPaneSize.value
  })

  emit('pane-resize', {
    type: 'horizontal',
    sizes: event.map(pane => pane.size)
  })
}

// 重置布局
const resetLayout = () => {
  leftPaneSize.value = props.initialLeftPaneSize
  topPaneSize.value = props.initialTopPaneSize
  saveLayout()

  emit('layout-change', {
    leftPaneSize: leftPaneSize.value,
    topPaneSize: topPaneSize.value
  })
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
  height: 97vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* Header 样式 */
.problem-header {
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.default-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 16px;
  min-height: 40px;
}

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
  padding: 12px;
  box-sizing: border-box;
  overflow: hidden;
}

.problem-splitpanes {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.05);
}

.workspace-splitpanes {
  height: 100%;
  border-radius: 0 8px 8px 0;
  overflow: hidden;
}

/* 面板容器样式 */
.question-pane,
.workspace-pane,
.code-pane,
.debug-pane {
  background: transparent;
  overflow: hidden;
  position: relative;
}

.pane-content {
  height: 100%;
  width: 100%;
  overflow: hidden;
  border-radius: inherit;
}

.question-pane .pane-content {
  border-radius: 8px 0 0 8px;
}

.code-pane .pane-content {
  border-radius: 0 8px 0 0;
}

.debug-pane .pane-content {
  border-radius: 0 0 8px 0;
}

/* 优雅的分割线样式 */
:deep(.splitpanes__splitter) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: none !important;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  z-index: 10;
}

:deep(.splitpanes__splitter::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-radius: 2px;
  opacity: 0.6;
  transition: all 0.3s ease;
}

:deep(.splitpanes__splitter::after) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  opacity: 0;
  transition: all 0.3s ease;
}

/* 垂直分割线 */
:deep(.splitpanes--vertical > .splitpanes__splitter) {
  width: 8px !important;
  cursor: col-resize;
}

:deep(.splitpanes--vertical > .splitpanes__splitter::before) {
  width: 2px;
  height: 24px;
}

:deep(.splitpanes--vertical > .splitpanes__splitter::after) {
  width: 16px;
  height: 16px;
}

/* 水平分割线 */
:deep(.splitpanes--horizontal > .splitpanes__splitter) {
  height: 8px !important;
  cursor: row-resize;
}

:deep(.splitpanes--horizontal > .splitpanes__splitter::before) {
  width: 24px;
  height: 2px;
}

:deep(.splitpanes--horizontal > .splitpanes__splitter::after) {
  width: 16px;
  height: 16px;
}

/* 悬停效果 */
:deep(.splitpanes__splitter:hover) {
  background: rgba(255, 255, 255, 0.95) !important;
}

:deep(.splitpanes__splitter:hover::before) {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.2);
}

:deep(.splitpanes__splitter:hover::after) {
  opacity: 0.3;
  transform: translate(-50%, -50%) scale(1);
}

/* 激活状态 */
:deep(.splitpanes__splitter:active) {
  background: rgba(255, 255, 255, 1) !important;
}

:deep(.splitpanes__splitter:active::before) {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.5);
  background: linear-gradient(45deg, #4facfe, #00f2fe);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .problem-container {
    padding: 8px;
  }

  :deep(.splitpanes__splitter) {
    backdrop-filter: blur(5px);
  }
}

@media (max-width: 768px) {
  .problem-layout {
    background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
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
    padding: 2px 8px;
    min-height: 32px;
  }

  .header-left,
  .header-center,
  .header-right {
    gap: 4px;
  }

  .problem-container {
    padding: 2px;
  }

  .problem-splitpanes {
    border-radius: 6px;
  }

  /* 小屏幕分割线 */
  :deep(.splitpanes--vertical > .splitpanes__splitter) {
    width: 4px !important;
  }

  :deep(.splitpanes--horizontal > .splitpanes__splitter) {
    height: 4px !important;
  }

  :deep(.splitpanes__splitter::before) {
    opacity: 0.8;
  }
}

/* 暗色主题支持 */
@media (prefers-color-scheme: dark) {
  .problem-layout {
    background: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
  }

  .problem-header {
    background: rgba(26, 32, 44, 0.95);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .problem-splitpanes {
    background: rgba(0, 0, 0, 0.1);
  }

  :deep(.splitpanes__splitter) {
    background: rgba(0, 0, 0, 0.3) !important;
  }

  :deep(.splitpanes__splitter:hover) {
    background: rgba(0, 0, 0, 0.5) !important;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  :deep(.splitpanes__splitter) {
    background: #000 !important;
  }

  :deep(.splitpanes__splitter::before) {
    background: #fff;
    opacity: 1;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {

  :deep(.splitpanes__splitter),
  :deep(.splitpanes__splitter::before),
  :deep(.splitpanes__splitter::after) {
    transition: none;
  }
}
</style>
