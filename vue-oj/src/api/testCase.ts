import request from '@/utils/request'
import type { TestCaseVo } from '@/types/testCase'

// 获取某题目的测试用例（后端：/api/problems/view/testcase/?problemId=xxx）
export const fetchTestCasesByProblemId = (problemId: number): Promise<TestCaseVo[]> => {
  return request({
    url: '/api/problems/view/testcase/',
    method: 'get',
    params: { problemId },
  })
}
