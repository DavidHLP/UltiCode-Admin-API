import request from '@/utils/request'
import { requestData } from '@/utils/request'
import type { Problem } from '@/types/problem.d'
import type { PageResult } from '@/types/commons'

export function fetchProblems(): Promise<Problem[]> {
  return request({
    url: '/problems/api/management/problem',
    method: 'get',
  })
}

export function fetchProblemPage(params: {
  page: number
  size: number
  keyword?: string
  difficulty?: string
  category?: string
  isVisible?: boolean
}): Promise<PageResult<Problem>> {
  return requestData<PageResult<Problem>>({
    url: '/problems/api/management/problem/page',
    method: 'get',
    params,
  })
}

export function getProblem(id: number): Promise<Problem> {
  return request({
    url: `/problems/api/management/problem/${id}`,
    method: 'get',
  })
}

export function createProblem(data: Problem): Promise<void> {
  return request({
    url: '/problems/api/management/problem',
    method: 'post',
    data,
  })
}

export function updateProblem(data: Problem): Promise<void> {
  return request({
    url: `/problems/api/management/problem`,
    method: 'put',
    data,
  })
}

export function deleteProblem(id: number): Promise<void> {
  return request({
    url: `/problems/api/management/problem/${id}`,
    method: 'delete',
  })
}
