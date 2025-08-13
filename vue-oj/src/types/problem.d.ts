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

// 题解卡片 VO（列表项）
export interface SolutionCardVo {
  id: number
  title: string
  problem: string
  tags: string[]
  authorUsername: string
  authorAvatar?: string
  createdAt: string
  upvotes: number
  views: number
}

// 题解详情 VO
export interface SolutionVo {
  id: number
  title: string
  content: string
  authorUsername: string
  authorAvatar?: string
  createdAt: string
  upvotes?: number
  downvotes?: number
}
