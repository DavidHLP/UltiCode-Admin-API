<template>
  <div class="solution-content">
    <div class="solution-header">
      <div class="header-left">
        <el-button type="text" link class="back-button" @click="handleBack">
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

    <div class="solution-footer" v-if="solution">
      <el-button-group>
        <el-button :type="userVote === 'up' ? 'primary' : 'default'" @click="handleVote('up')">
          <el-icon>
            <ElIconCaretTop />
          </el-icon>
          <span>赞同 {{ solution.upvotes || 0 }}</span>
        </el-button>
        <el-button :type="userVote === 'down' ? 'primary' : 'default'" @click="handleVote('down')">
          <el-icon>
            <ElIconCaretBottom />
          </el-icon>
          <span>反对 {{ solution.downvotes || 0 }}</span>
        </el-button>
      </el-button-group>
    </div>
    <CommentComponent v-if="solution" :comments="comments" :solutionId="solution.id"
      @comment-submitted="fetchComments" />
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, defineEmits } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back as ElIconBack, CaretTop as ElIconCaretTop, CaretBottom as ElIconCaretBottom } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { getSolutionById } from '@/api/solution'
import type { SolutionVo } from '@/types/solution'
import type { SolutionCommentVo } from '@/types/solution'
import { config } from 'md-editor-v3'
import { lineNumbers } from '@codemirror/view'
import CommentComponent from '@/components/CommentComponent.vue'

config({
  codeMirrorExtensions(_theme, extensions) {
    return [...extensions, lineNumbers()]
  },
})

const emit = defineEmits<{
  (e: 'vote', solution: SolutionVo, type: 'up' | 'down'): void;
}>()

const route = useRoute()
const router = useRouter()

const solution = ref<SolutionVo | null>(null)
const loading = ref(false)
const userVote = ref<'up' | 'down' | null>(null)
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

const handleVote = (type: 'up' | 'down') => {
  if (!solution.value) return

  // 这里直接触发父组件的投票处理
  emit('vote', solution.value, type)

  // 本地更新UI
  if (userVote.value === type) {
    // 取消投票
    userVote.value = null
    if (type === 'up') {
      solution.value.upvotes = Math.max(0, (solution.value.upvotes || 0) - 1)
    } else {
      solution.value.downvotes = Math.max(0, (solution.value.downvotes || 0) - 1)
    }
  } else {
    // 新投票或切换投票
    const oldVote = userVote.value
    userVote.value = type

    // 更新计数
    if (oldVote === 'up') {
      solution.value.upvotes = Math.max(0, (solution.value.upvotes || 0) - 1)
    } else if (oldVote === 'down') {
      solution.value.downvotes = Math.max(0, (solution.value.downvotes || 0) - 1)
    }

    if (type === 'up') {
      solution.value.upvotes = (solution.value.upvotes || 0) + 1
    } else {
      solution.value.downvotes = (solution.value.downvotes || 0) + 1
    }
  }
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

.solution-footer {
  position: fixed;
  bottom: 20px;
  right: 40px;
  z-index: 100;
  background-color: white;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
