import request from '@/utils/request'
import type { ProblemPageQuery, ProblemVo, ProblemDetailVo } from '@/types/problem.d.ts'
import type { Page } from '@/types/commons.d.ts'

export const getProblemPage = (params: ProblemPageQuery): Promise<Page<ProblemVo>> => {
  return request({
    url: '/api/problems/view/problem/page',
    method: 'get',
    params,
  })
}

export const getProblemDetailVoById = (id: number): Promise<ProblemDetailVo> => {
  return request({
    url: `/api/problems/view/problem/detail`,
    method: 'get',
    params: { id },
  })
}

// 获取代码模板（与后端 ProblemViewController.getCodeTemplate 对齐）
// 返回纯字符串模板，避免前端重复定义类型
export const getCodeTemplate = (params: {
  problemId: number
  language: string
}): Promise<string> => {
  return request({
    url: '/api/problems/view/problem/codetemplate',
    method: 'get',
    params,
  }).then((res) => res as unknown as string)
}
