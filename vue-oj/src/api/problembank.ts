import request from '@/utils/request'
import type { Page, Question, QuestionBankQuery, SubmissionCalendar } from '@/types/questionbank'

export function getProblemBank(params: QuestionBankQuery): Promise<Page<Question>> {
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
