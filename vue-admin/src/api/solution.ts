import { requestData } from '@/utils/request'
import type { PageResult } from '@/types/commons'
import type { SolutionManagementCard, SolutionStatus } from '@/types/solution.d'

export interface SolutionManagementQuery {
  page: number
  size: number
  keyword?: string
  problemId?: number
  userId?: number
  status?: SolutionStatus
}

export function fetchSolutionManagementPage(
  params: SolutionManagementQuery
): Promise<PageResult<SolutionManagementCard>> {
  return requestData<PageResult<SolutionManagementCard>>({
    url: '/problems/api/management/solution/pages',
    method: 'get',
    params
  })
}

// 题解详情（管理端使用当前的 /view/solution/detail，返回实体信息）
export interface SolutionDetail {
  id: number
  problemId: number
  userId: number
  title: string
  content: string
  language: string
  status: SolutionStatus
  views?: number
  comments?: number
  upvotes?: number
  downvotes?: number
}

export function fetchSolutionDetail(solutionId: number): Promise<SolutionDetail> {
  return requestData<SolutionDetail>({
    url: '/problems/api/management/solution/detail',
    method: 'get',
    params: { solutionId }
  })
}

export function acceptSolution(id: number): Promise<boolean> {
  return requestData<boolean>({
    url: '/problems/api/management/solution/accept',
    method: 'post',
    data: { id }
  })
}

export function rejectSolution(id: number): Promise<boolean> {
  return requestData<boolean>({
    url: '/problems/api/management/solution/reject',
    method: 'post',
    data: { id }
  })
}
