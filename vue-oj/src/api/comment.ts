import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

// 创建评论（后端：/problems/api/view/solution/comment）
export function createComment(data: {
  solutionId: number
  content: string
  parentId?: number
  replyToUserId?: number
  userId?: number
}): Promise<void> {
  const authStore = useAuthStore()
  data.userId = authStore.user?.userId
  return request.post('/problems/api/view/solution/comment', data)
}
