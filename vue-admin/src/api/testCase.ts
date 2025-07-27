import request from '@/utils/request'
import type { TestCase } from '@/types/testCase'

export function createTestCase(data: Partial<TestCase>): Promise<TestCase> {
  return request({
    url: '/problems/api/testcases',
    method: 'post',
    data,
  })
}

export function updateTestCase(data: Partial<TestCase>): Promise<TestCase> {
  return request({
    url: `/problems/api/testcases`,
    method: 'put',
    data,
  })
}

export function deleteTestCase(id: number): Promise<void> {
  return request({
    url: `/problems/api/testcases/${id}`,
    method: 'delete',
  })
}
