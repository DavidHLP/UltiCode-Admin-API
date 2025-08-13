import request from '@/utils/request'

// 创建评论（后端：/problems/api/view/solution/comment）
export function createComment(data: {
  solutionId: number
  content: string
  parentId?: number
  replyToUserId?: number
}): Promise<void> {
  return request.post('/problems/api/view/solution/comment', data)
}
