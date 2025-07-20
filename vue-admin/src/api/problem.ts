import request from '@/utils/request'
import type { Problem } from '@/types/problem'

export function fetchProblems(): Promise<Problem[]> {
  return request({
    url: '/problems/api',
    method: 'get',
  })
}

export function getProblem(id: number): Promise<Problem> {
  return request({
    url: `/problems/api/${id}`,
    method: 'get',
  })
}

export function createProblem(data: Problem): Promise<void> {
  return request({
    url: '/problems/api',
    method: 'post',
    data,
  })
}

export function updateProblem(id: number, data: Problem): Promise<void> {
  return request({
    url: `/problems/api/${id}`,
    method: 'put',
    data,
  })
}

export function deleteProblem(id: number): Promise<void> {
  return request({
    url: `/problems/api/${id}`,
    method: 'delete',
  })
}
