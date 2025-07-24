import request from '@/utils/request'
import type { Page, Question, QuestionBankQuery } from '@/types/questionbank'

export function getQuestionBank(params: QuestionBankQuery): Promise<Page<Question>> {
  return request({
    url: '/question-bank/api',
    method: 'get',
    params,
  })
}
