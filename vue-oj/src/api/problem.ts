import request from '@/utils/request'
import type { Problem, Submission, Solution } from '@/types/problem'

export const getProblemById = (id: number): Promise<Problem> => {
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

export const getSubmissionById = (id: number): Promise<Submission> => {
  return request({
    url: `/judge/api/submission/${id}`,
    method: 'get',
  })
}

export const getSubmissionsByProblemId = (problemId: number): Promise<Submission[]> => {
  return request({
    url: `/submissions/api/view/problem/${problemId}`,
    method: 'get',
  })
}

export const getSolutionsByProblemId = (problemId: number): Promise<Solution[]> => {
  return request({
    url: `/solutions/api/view/problem/${problemId}`,
    method: 'get',
  })
}
