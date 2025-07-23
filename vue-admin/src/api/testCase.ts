import request from '@/utils/request'
import type { TestCase, TestCaseContent } from '@/types/testCase'

export function fetchTestCases(problemId: number): Promise<TestCase[]> {
  return request({
    url: `/problems/api/testcases/problem/${problemId}`,
    method: 'get',
  })
}

export function createTestCase(data: Partial<TestCase> & { inputContent: string; outputContent: string }): Promise<TestCase> {
  return request({
    url: '/problems/api/testcases',
    method: 'post',
    data,
  })
}

export function updateTestCase(id: number, data: Partial<TestCase> & { inputContent: string; outputContent: string }): Promise<TestCase> {
  return request({
    url: `/problems/api/testcases/${id}`,
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

export function getTestCaseContent(id: number): Promise<TestCaseContent> {
  return request({
    url: `/problems/api/testcases/${id}/content`,
    method: 'get',
  })
}
