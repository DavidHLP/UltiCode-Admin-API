export interface Question {
  id: number
  title: string
  tags: string[]
  difficulty: string
  passRate: number
  submissionCount: number
  status: string
}

export interface QuestionBankQuery {
  page: number
  size: number
  category: string
  difficulty: string
  status: string
  tags: string[]
  title: string
  sortBy: string
}

export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface SubmissionCalendar {
  date: string
  count: number
}
