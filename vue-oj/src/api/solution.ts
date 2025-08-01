import request from '@/utils/request'
import type { SolutionCardVo, SolutionVo } from '@/types/problem'
import type { Solution } from '@/types/solution'
import type { Page } from '@/types/commons'

export interface SolutionQueryParams {
  problemId: number
  page?: number
  size?: number
  title?: string
  sort?: 'hot' | 'new'
}

/**
 * 获取某个题目下的所有题解（分页）
 * @param params 查询参数
 * @returns 分页的题解列表
 */
export const getSolutionsByProblemId = (
  params: SolutionQueryParams,
): Promise<Page<SolutionCardVo>> => {
  return request.get('/solutions/api/view/problem', {
    params: {
      problemId: params.problemId,
      page: params.page || 1,
      size: params.size || 10,
      title: params.title || '',
      sort: params.sort || 'hot',
    },
  })
}

/**
 * 获取单个题解详情
 * @param solutionId 题解ID
 * @returns 题解详情
 */
export const getSolutionById = (solutionId: number): Promise<SolutionVo> => {
  return request.get('/solutions/api/view/', {
    params: { id: solutionId },
  })
}

/**
 * 点赞/点踩题解
 * @param solutionId 题解ID
 * @param type 投票类型：'up' 或 'down'
 */
export const voteSolution = (solutionId: number, type: 'up' | 'down'): Promise<void> => {
  return request.post(`/solutions/api/vote/${solutionId}`, { type })
}

/**
 * 新增题解
 * @param solution 题解数据
 * @returns 新增的题解ID
 */
export const addSolution = (solution: Solution): Promise<number> => {
  return request.post('/solutions/api/view/', solution)
}

/**
 * 更新题解
 * @param id 题解ID
 * @param solution 题解数据
 * @returns 更新是否成功
 */
export const updateSolution = (id: number, solution: Partial<Solution>): Promise<boolean> => {
  return request.put(`/solutions/api/view/${id}`, solution)
}
