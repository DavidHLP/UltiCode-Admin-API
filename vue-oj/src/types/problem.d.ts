export type ProblemDifficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type CategoryType =
  | 'ALGORITHMS'
  | 'DATABASE'
  | 'SHELL'
  | 'MULTI_THREADING'
  | 'JAVASCRIPT'
  | 'PANDAS'

// 排序方式需要与后端 ProblemViewController/Page 接口的 sort 参数保持一致
export type ProblemSort =
  | 'default'
  | 'id'
  | 'difficulty'
  | 'acceptance'
  | 'number'
  | 'recent_submit'

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
  sort?: ProblemSort | ''
}

export interface ProblemDetailVo {
  id: number
  title: string
  description: string
  difficulty: ProblemDifficulty
}
