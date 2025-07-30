import request from '@/utils/request'
import type { Page, Problem, ProblemBankQuery, SubmissionCalendar } from '@/types/problembank.d.ts'

export function getProblemBank(params: ProblemBankQuery): Promise<Page<Problem>> {
  return request({
    url: '/problem-bank/api',
    method: 'get',
    params,
  })
}

export function getSubmissionCalendar(): Promise<SubmissionCalendar[]> {
  return request({
    url: '/problem-bank/api/calendar',
    method: 'get',
  })
}
