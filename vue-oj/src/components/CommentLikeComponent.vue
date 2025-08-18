<template>
  <div class="comment-like">
    <el-button
      link
      size="small"
      :class="{ active: userAction === 'LIKE' }"
      :disabled="pending"
      @click="handleLike"
    >
      <el-icon><ElIconCaretTop /></el-icon>
      <span class="count">{{ likeCount }}</span>
    </el-button>
    <span class="divider">/</span>
    <el-button
      link
      size="small"
      :class="{ active: userAction === 'DISLIKE' }"
      :disabled="pending"
      @click="handleDislike"
    >
      <el-icon><ElIconCaretBottom /></el-icon>
      <span class="count">{{ dislikeCount }}</span>
    </el-button>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CaretTop as ElIconCaretTop, CaretBottom as ElIconCaretBottom } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getStatus, getStats, takeAction, cancelAction } from '@/api/like'
import type { LikeDislikeRecordVo, TargetType, UserAction } from '@/types/like'

const props = defineProps<{
  targetType: TargetType
  targetId: number
  initial?: LikeDislikeRecordVo
}>()

const emit = defineEmits<{
  (e: 'changed', v: LikeDislikeRecordVo): void
  (e: 'error', err: unknown): void
}>()

const auth = useAuthStore()
const pending = ref(false)
const state = ref<LikeDislikeRecordVo | null>(props.initial ?? null)

const userId = computed(() => auth.user?.userId)
const isLoggedIn = computed(() => !!auth.user)

const userAction = computed<UserAction>(() => state.value?.userAction ?? 'NONE')
const likeCount = computed(() => state.value?.likeCount ?? 0)
const dislikeCount = computed(() => state.value?.dislikeCount ?? 0)

async function refresh() {
  try {
    if (isLoggedIn.value && userId.value) {
      const res = await getStatus({ userId: userId.value, targetType: props.targetType, targetId: props.targetId })
      state.value = res
    } else {
      const res = await getStats({ targetType: props.targetType, targetId: props.targetId })
      state.value = { ...res, userAction: 'NONE' }
    }
    emit('changed', state.value!)
  } catch (err) {
    emit('error', err)
  }
}

async function ensureLogin(): Promise<boolean> {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再进行操作')
    auth.navigateToLogin()
    return false
  }
  return true
}

async function handleLike() {
  if (!(await ensureLogin())) return
  if (!userId.value) return
  if (pending.value) return
  pending.value = true
  try {
    const same = userAction.value === 'LIKE'
    const res = same
      ? await cancelAction({ userId: userId.value, targetType: props.targetType, targetId: props.targetId })
      : await takeAction({ userId: userId.value, targetType: props.targetType, targetId: props.targetId, actionType: 'LIKE' })
    state.value = res
    emit('changed', res)
  } catch (err) {
    emit('error', err)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    pending.value = false
  }
}

async function handleDislike() {
  if (!(await ensureLogin())) return
  if (!userId.value) return
  if (pending.value) return
  pending.value = true
  try {
    const same = userAction.value === 'DISLIKE'
    const res = same
      ? await cancelAction({ userId: userId.value, targetType: props.targetType, targetId: props.targetId })
      : await takeAction({ userId: userId.value, targetType: props.targetType, targetId: props.targetId, actionType: 'DISLIKE' })
    state.value = res
    emit('changed', res)
  } catch (err) {
    emit('error', err)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    pending.value = false
  }
}

onMounted(() => { void refresh() })
watch([() => props.targetType, () => props.targetId], () => { void refresh() })
watch(() => auth.user, () => { void refresh() })

defineExpose({ refresh })
</script>

<style scoped>
.comment-like {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
}

.divider {
  color: #cbd5e1;
}

:deep(.el-button) {
  padding: 0 6px;
  height: 24px;
  line-height: 24px;
  box-shadow: none;
}

:deep(.el-button:hover) {
  background-color: #f3f4f6;
}

.active {
  color: #2563eb !important;
  font-weight: 600;
}

.count {
  margin-left: 4px;
}
</style>
