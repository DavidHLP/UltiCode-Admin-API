import type { TestCase } from './testCase'

export interface Problem {
  id: number
  title: string
  description: string
  timeLimit: number
  memoryLimit: number
  difficulty: 'Easy' | 'Medium' | 'Hard'
  category: string
  tags: string[]
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
