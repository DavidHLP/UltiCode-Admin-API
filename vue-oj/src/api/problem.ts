import request from '@/utils/request'
import type { ProblemVO, ProblemQueryParams } from '@/types/problem'

export const getProblemById = (id: number): Promise<ProblemVO> => {
  return request({
    url: `/problems/api/view/${id}`,
    method: 'get',
  })
}

export const submitCode = (data: {
  problemId: number
  sourceCode: string
  language: string
}): Promise<number> => {
  return request({
    url: '/judge/api/submit',
    method: 'post',
    data,
  })
}

export const getProblemPage = (params: ProblemQueryParams): Promise<ProblemVO[]> => {
  return request({
    url: '/problems/api/list',
    method: 'get',
    params,
  })
}
