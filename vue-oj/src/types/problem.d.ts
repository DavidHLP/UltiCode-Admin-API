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

// ====== Extended types for Problem View, Submissions, and Solutions ======

export type SubmissionStatus =
  | 'PENDING'
  | 'JUDGING'
  | 'ACCEPTED'
  | 'WRONG_ANSWER'
  | 'TIME_LIMIT_EXCEEDED'
  | 'MEMORY_LIMIT_EXCEEDED'
  | 'RUNTIME_ERROR'
  | 'COMPILE_ERROR'
  | 'SYSTEM_ERROR'
  | string

export interface Submission {
  id: number
  userId?: number
  problemId: number
  language: string
  sourceCode?: string
  status: SubmissionStatus
  score?: number
  timeUsed?: number
  memoryUsed?: number
  compileInfo?: string
  judgeInfo?: string
  createdAt: string
}

export interface SolutionVo {
  id: number
  title: string
  content: string
  authorUsername?: string
  authorAvatar?: string
  upvotes?: number
  downvotes?: number
  createdAt: string
}

export interface SolutionCardVo {
  id: number
  title: string
  preview?: string
  authorUsername?: string
  upvotes?: number
  downvotes?: number
  createdAt: string
}
