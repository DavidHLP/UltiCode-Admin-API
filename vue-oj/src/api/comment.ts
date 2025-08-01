import request from '@/utils/request'
import type { SolutionCommentDto, SolutionCommentVo } from '@/types/comment'

// 创建评论
export function createComment(data: SolutionCommentDto): Promise<SolutionCommentVo> {
  return request({
    url: '/problems/api/solution-comments',
    method: 'post',
    data,
  })
}

// 获取指定题解的评论列表
export function getCommentsBySolutionId(solutionId: number): Promise<SolutionCommentVo[]> {
  return request({
    url: `/problems/api/solution-comments/solution/${solutionId}`,
    method: 'get',
  })
}
