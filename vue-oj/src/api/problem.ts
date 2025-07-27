import request from '@/utils/request'
import type { Problem, Submission } from '@/types/problem'

export const getProblemById = (id: number): Promise<Problem> => {
  return request({
    url: `/problems/api/${id}`,
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

export const getSubmissionById = (id: number): Promise<Submission> => {
  return request({
    url: `/judge/api/submission/${id}`,
    method: 'get',
  })
}
