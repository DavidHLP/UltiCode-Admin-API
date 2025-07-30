export interface Problem {
  id: number
  title: string
  tags: string[]
  difficulty: string
  passRate: number
  submissionCount: number
  status: string
}

export interface ProblemBankQuery {
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
}

export interface SubmissionCalendar {
  date: string
  count: number
}

export interface Problem {
  id: number
  title: string
  difficulty: 'Easy' | 'Medium' | 'Hard'
  passRate: number
  submissionCount: number
  status: 'completed' | 'attempted' | 'not-attempted'
  tags: string[]
}
