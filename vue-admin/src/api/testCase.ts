import request from '@/utils/request'
import type { TestCase } from '@/types/testCase'

export function fetchTestCases(problemId: number): Promise<TestCase[]> {
  return request({
    url: `/testcases/api/problem/${problemId}`,
    method: 'get',
  })
}

export function createTestCase(data: TestCase): Promise<void> {
  return request({
    url: '/testcases/api',
    method: 'post',
    data,
  })
}

export function updateTestCase(id: number, data: TestCase): Promise<void> {
  return request({
    url: `/testcases/api/${id}`,
    method: 'put',
    data,
  })
}

export function deleteTestCase(id: number): Promise<void> {
  return request({
    url: `/testcases/api/${id}`,
    method: 'delete',
  })
}
