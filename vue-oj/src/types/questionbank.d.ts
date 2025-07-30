export interface Question {
  id: number
  title: string
  difficulty: 'Easy' | 'Medium' | 'Hard'
  passRate: number
  submissionCount: number
  status: 'completed' | 'attempted' | 'not-attempted'
  tags: string[]
}

export interface QuestionBankQuery {
  page: number
  size: number
  category?: '' | 'ALGORITHMS' | 'DATABASE' | 'SHELL' | '"MULTI_THREADING' | 'JAVASCRIPT' | 'PANDAS'
  difficulty?: 'Easy' | 'Medium' | 'Hard' | ''
  status?: 'completed' | 'attempted' | 'not-attempted' | ''
  tags?: string[]
  title?: string
  sortBy?: string
}

export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
