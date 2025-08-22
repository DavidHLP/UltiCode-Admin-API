import request from '@/utils/request'
import type { SolutionCardVo, SolutionVo } from '@/types/solution'
import type { SolutionEditVo } from '@/types/solution'
import type { Page } from '@/types/commons'
import { useAuthStore } from '@/stores/auth'

export interface SolutionQueryParams {
  problemId: number
  page?: number
  size?: number
  keyword?: string
}

/**
 * 获取某个题目下的所有题解（分页）
 * @param params 查询参数
 * @returns 分页的题解列表
 */
export const getSolutionsByProblemId = (
  params: SolutionQueryParams,
): Promise<Page<SolutionCardVo>> => {
  return request.get('/problems/api/view/solution/page', {
    params: {
      page: params.page || 1,
      size: params.size || 10,
      problemId: params.problemId,
      keyword: params.keyword || '',
    },
  })
}

/**
 * 获取单个题解详情
 * @param solutionId 题解ID
 * @returns 题解详情
 */
export const getSolutionById = (solutionId: number): Promise<SolutionVo> => {
  return request.get('/problems/api/view/solution', {
    params: { solutionId },
  })
}

/**
 * 新增题解
 * @param solution 题解数据
 * @returns 新增的题解ID
 */
export const addSolution = (solution: SolutionEditVo): Promise<void> => {
  solution.userId = useAuthStore().user?.userId
  return request.post('/problems/api/view/solution', solution)
}

/**
 * 更新题解
 * @param solution 题解数据
 * @returns 更新是否成功
 */
export const updateSolution = (
  solution: Partial<SolutionEditVo> & { id: number },
): Promise<void> => {
  return request.put('/problems/api/view/solution', solution)
}
