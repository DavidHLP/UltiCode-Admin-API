<template>
  <div class="solution-content">
    <div class="solution-header">
      <div class="header-left">
        <el-button link class="back-button" @click="handleBack">
          <el-icon>
            <ElIconBack />
          </el-icon>
          <span>返回</span>
        </el-button>
        <h1>{{ solution?.title || '加载中...' }}</h1>
      </div>
    </div>

    <div class="solution-meta" v-if="solution">
      <div class="author-info">
        <el-avatar :size="40" :src="solution.authorAvatar" />
        <div class="author-details">
          <span class="author-name">{{ solution.authorUsername }}</span>
          <span class="publish-time">{{ formatDate(solution.createdAt) }}</span>
        </div>
      </div>
    </div>
    <el-divider v-if="solution" />

    <div v-loading="loading" class="markdown-content">
      <template v-if="solution">
        <MdPreview :model-value="solution.content" :previewTheme="'github'" :theme="'light'" :preview="true" />
      </template>
    </div>

    <!-- 修复：将点赞组件移动到内容区域内，并调整样式 -->
    <div class="solution-actions" v-if="solution">
      <SolutionLikeComponent
        targetType="SOLUTION"
        :targetId="solution.id"
        :initial="{
          userAction: 'NONE',
          likeCount: solution.upvotes || 0,
          dislikeCount: solution.downvotes || 0,
          totalCount: (solution.upvotes || 0) + (solution.downvotes || 0),
        }"
        @changed="onLikeChanged"
        @error="onLikeError"
      />
    </div>

    <CommentComponent v-if="solution" :comments="comments" :solutionId="solution.id"
      @comment-submitted="fetchComments" />
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back as ElIconBack } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { getSolutionById } from '@/api/solution'
import type { SolutionVo } from '@/types/solution'
import type { SolutionCommentVo } from '@/types/solution'
import { config } from 'md-editor-v3'
import { lineNumbers } from '@codemirror/view'
import CommentComponent from '@/components/CommentComponent.vue'
import SolutionLikeComponent from '@/components/SolutionLikeComponent.vue'
import type { LikeDislikeRecordVo } from '@/types/like.d'

config({
  codeMirrorExtensions(_theme, extensions) {
    return [...extensions, lineNumbers()]
  },
})

const route = useRoute()
const router = useRouter()

const solution = ref<SolutionVo | null>(null)
const loading = ref(false)
const comments = ref<SolutionCommentVo[]>([])

// 获取题解详情
const fetchSolution = async (id: number) => {
  try {
    loading.value = true
    const data = await getSolutionById(id)
    if (data) {
      solution.value = data
      comments.value = data.solutionComments || []
    } else {
      ElMessage.warning('未找到该题解')
      router.push({ name: 'solution-list' })
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (error: any) {
    console.error('获取题解详情失败:', error)
    ElMessage.error(error.response?.data?.message || '获取题解详情失败')
    router.push({ name: 'solution-list' })
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  if (solution.value) {
    try {
      const data = await getSolutionById(solution.value.id)
      solution.value = data
      comments.value = data.solutionComments || []
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      console.error('获取评论失败:', error)
      ElMessage.error('获取评论失败');
    }
  }
};

// 监听路由参数变化
watch(
  () => route.params.solutionId,
  (newId) => {
    if (newId) {
      fetchSolution(Number(newId))
    }
  },
  { immediate: true }
)

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString()
}

const handleBack = () => {
  router.push({
    name: 'solution-list',
    params: { id: route.params.id }
  })
}

function onLikeChanged(v: LikeDislikeRecordVo) {
  if (!solution.value) return
  solution.value.upvotes = v.likeCount
  solution.value.downvotes = v.dislikeCount
}

function onLikeError(err: unknown) {
  // 简单提示，保留服务器错误提示由拦截器处理
  console.error('Like error:', err)
}

defineExpose({
  refresh: () => solution.value && fetchSolution(solution.value.id)
})
</script>

<style scoped>
@import '@/assets/styles/html.css';
@import '@/assets/styles/md.css';
@import '@/assets/styles/scrollbar.css';

.solution-content {
  padding: 24px;
  overflow: hidden;
  background: transparent;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  position: relative;
  /* 移除底部padding，因为点赞组件不再固定定位 */
}

.solution-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .back-icon {
    font-size: 20px;
    color: #6b7280;
    cursor: pointer;
    transition: color 0.3s ease;

    &:hover {
      color: #3b82f6;
    }
  }
}

.solution-meta {
  .author-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .author-details {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .author-name {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }

      .publish-time {
        font-size: 14px;
        color: #6b7280;
      }
    }
  }
}

/* 修复：点赞组件的新样式 */
.solution-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 16px 0 8px;
  padding: 0;
}

/* 极简风格：在本区域内将按钮样式弱化为透明背景、无边框 */
.solution-actions :deep(.el-button-group) {
  box-shadow: none;
}

.solution-actions :deep(.el-button) {
  background-color: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: #64748b;
  padding: 6px 10px;
}

.solution-actions :deep(.el-button:hover) {
  background-color: #f2f4f7 !important;
  color: #334155;
}

/* 激活态（primary）也保持透明，仅强调文字颜色与字重 */
.solution-actions :deep(.el-button--primary) {
  background-color: transparent !important;
  border: none !important;
  color: #2563eb !important;
  font-weight: 600;
}

.solution-actions :deep(.el-button--primary:hover) {
  background-color: rgba(37, 99, 235, 0.08) !important;
}

.solution-actions :deep(.el-icon) {
  margin-right: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .solution-content {
    padding: 16px;
  }

  .solution-actions {
    margin: 12px 0 4px;
    padding: 0;
  }
}

/* 如果您希望保留固定定位的选项，可以添加一个修饰类 */
.solution-actions.floating {
  position: fixed;
  bottom: 16px;
  right: 24px;
  z-index: 100;
  background-color: white;
  margin: 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

@media (max-width: 768px) {
  .solution-actions.floating {
    left: 50%;
    right: auto;
    transform: translateX(-50%);
    bottom: 12px;
  }
}
</style>
