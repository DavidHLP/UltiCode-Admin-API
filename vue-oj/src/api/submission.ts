import type { Submission } from '@/types/problem'
import request from '@/utils/request.ts'

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
