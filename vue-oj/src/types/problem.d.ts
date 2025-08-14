export type ProblemDifficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type CategoryType =
  | 'ALGORITHMS'
  | 'DATABASE'
  | 'SHELL'
  | 'MULTI_THREADING'
  | 'JAVASCRIPT'
  | 'PANDAS'

export interface ProblemVo {
  id: number
  title: string
  difficulty: ProblemDifficulty
  category: CategoryType
  tags: string[]
  passRate: number
}

export interface ProblemPageQuery {
  page: number
  size: number
  keyword?: string
  difficulty?: ProblemDifficulty | ''
  category?: CategoryType | ''
}

export interface ProblemDetailVo {
  id: number
  title: string
  description: string
  difficulty: ProblemDifficulty
}
