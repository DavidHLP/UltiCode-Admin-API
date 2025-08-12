import request from '@/utils/request'
import type { ProblemPageQuery, ProblemVo, ProblemDetailVo } from '@/types/problem.d.ts'
import type { Page } from '@/types/commons.d.ts'

export const getProblemPage = (params: ProblemPageQuery): Promise<Page<ProblemVo>> => {
  return request({
    url: '/problems/api/view/problem/page',
    method: 'get',
    params,
  })
}

export const getProblemDetailVoById = (id: number): Promise<ProblemDetailVo> => {
  return request({
    url: `/problems/api/view/problem/detail`,
    method: 'get',
    params: { id },
  })
}

// 提交代码（与后端 SubmissionController.createSubmission 对齐）
// 返回提交ID，便于前端轮询判题结果
export const submitCode = (payload: { problemId: number; sourceCode: string; language: string }): Promise<number> => {
  return request({
    url: '/submissions/api',
    method: 'post',
    data: payload,
  }).then((res) => (res as unknown as { id: number }).id)
}

// 获取代码模板（与后端 ProblemViewController.getCodeTemplate 对齐）
// 返回纯字符串模板，避免前端重复定义类型
export const getCodeTemplate = (params: { problemId: number; language: string }): Promise<string> => {
  return request({
    url: '/problems/api/view/problem/codetemplate',
    method: 'get',
    params,
  }).then((res) => res as unknown as string)
}
