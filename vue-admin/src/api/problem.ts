import request from '@/utils/request'
import type { Problem, Category, CodeTemplate } from '@/types/problem'
import type { TestCase } from '@/types/testCase'

export function fetchProblems(): Promise<Problem[]> {
  return request({
    url: '/problems/api/management',
    method: 'get',
  })
}

export function getProblem(id: number): Promise<Problem> {
  return request({
    url: `/problems/api/management/${id}`,
    method: 'get',
  })
}

export function createProblem(data: Problem): Promise<void> {
  return request({
    url: '/problems/api/management',
    method: 'post',
    data,
  })
}

export function updateProblem(data: Problem): Promise<void> {
  return request({
    url: `/problems/api/management`,
    method: 'put',
    data,
  })
}

export function deleteProblem(id: number): Promise<void> {
  return request({
    url: `/problems/api/management/${id}`,
    method: 'delete',
  })
}

export function fetchCategories(): Promise<Category[]> {
  return request({
    url: '/problems/api/management/categories',
    method: 'get',
  })
}

export function getTestCasesByProblemId(problemId: number): Promise<TestCase[]> {
  return request({
    url: `/problems/api/management/testcases/problem/${problemId}`,
    method: 'get',
  })
}

export function getCodeTemplatesByProblemId(problemId: number): Promise<CodeTemplate[]> {
  return request({
    url: `/problems/api/management/codetemplates/problem/${problemId}`,
    method: 'get',
  })
}

export function createCodeTemplate(data: CodeTemplate): Promise<CodeTemplate> {
  return request({
    url: '/problems/api/management/codetemplates',
    method: 'post',
    data,
  })
}

export function updateCodeTemplate(data: CodeTemplate): Promise<CodeTemplate> {
  return request({
    url: '/problems/api/management/codetemplates',
    method: 'put',
    data,
  })
}

export function deleteCodeTemplate(id: number): Promise<void> {
  return request({
    url: `/problems/api/management/codetemplates/${id}`,
    method: 'delete',
  })
}
