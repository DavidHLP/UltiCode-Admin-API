import request from '@/utils/request'
import type { Solution } from '@/types/solution'

export function fetchSolutions(): Promise<Solution[]> {
  return request({
    url: '/solutions/api/management',
    method: 'get',
  })
}

export function getSolution(id: number): Promise<Solution> {
  return request({
    url: `/solutions/api/management/${id}`,
    method: 'get',
  })
}

export function createSolution(data: Solution): Promise<void> {
  return request({
    url: '/solutions/api/management',
    method: 'post',
    data,
  })
}

export function updateSolution(data: Solution): Promise<void> {
  return request({
    url: `/solutions/api/management`,
    method: 'put',
    data,
  })
}

export function deleteSolution(id: number): Promise<void> {
  return request({
    url: `/solutions/api/management/${id}`,
    method: 'delete',
  })
}
