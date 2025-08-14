import request from '@/utils/request.ts'
import type { Page } from '@/types/commons'
import type { SubmissionCardVo, SubmissionDetailVo } from '@/types/submission'

// 分页查询题目下的提交卡片数据
export const fetchSubmissionPage = (
  params: { problemId: number; page: number; size: number },
): Promise<Page<SubmissionCardVo>> => {
  return request({
    url: '/problems/api/view/submission/page',
    method: 'get',
    params,
  })
}

// 提交详情
export const fetchSubmissionDetail = (submissionId: number): Promise<SubmissionDetailVo> => {
  return request({
    url: '/problems/api/view/submission/detail',
    method: 'get',
    params: { submissionId },
  })
}
