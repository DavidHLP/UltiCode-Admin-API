import type { TestCase } from './testCase'

export interface Problem {
  id: number
  title: string
  description: string
  solutionFunctionName: string
  timeLimit: number
  memoryLimit: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  category: string
  tags: string[]
  problemType: 'ACM' | 'OI'
  solvedCount: number
  submissionCount: number
  createdBy: number
  isVisible: boolean
  createdAt: string
  updatedAt: string
  testCases?: TestCase[]
}

export interface Category {
  category: string
  description: string
}

// 固定的类别常量（前端写死，不再从后端获取）
export const CATEGORIES: Category[] = [
  { category: 'ALGORITHMS', description: '算法' },
  { category: 'DATABASE', description: '数据库' },
  { category: 'SHELL', description: 'Shell' },
  { category: 'MULTI_THREADING', description: '多线程' },
  { category: 'JAVASCRIPT', description: 'JavaScript' },
  { category: 'PANDAS', description: 'Pandas' },
]
