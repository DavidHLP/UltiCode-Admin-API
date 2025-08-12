import request from '@/utils/request'
import type { TestCase } from '@/types/testCase'

export function createTestCase(data: Partial<TestCase>): Promise<TestCase> {
  return request({
    url: '/problems/api/management/testcase',
    method: 'post',
    data,
  })
}

export function updateTestCase(data: Partial<TestCase>): Promise<TestCase> {
  return request({
    url: `/problems/api/management/testcase`,
    method: 'put',
    data,
  })
}

export function deleteTestCase(id: number): Promise<void> {
  return request({
    url: `/problems/api/management/testcase/${id}`,
    method: 'delete',
  })
}

// 按题目ID获取测试用例列表
export function fetchTestCasesByProblemId(problemId: number): Promise<TestCase[]> {
  return request({
    url: `/problems/api/management/testcase/${problemId}`,
    method: 'get',
  })
}

