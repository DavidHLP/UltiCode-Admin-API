import type { TestCase } from './testCase'

export interface CodeTemplate {
  id?: number
  problemId: number
  language: string
  mainWrapperTemplate?: string
  solutionTemplate: string
}

export interface Problem {
  id: number
  title: string
  description: string
  timeLimit: number
  memoryLimit: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  category: string
  tags: string[]
  solvedCount: number
  submissionCount: number
  createdBy: number
  isVisible: boolean
  createdAt: string
  updatedAt: string
  testCases?: TestCase[]
  codeTemplates?: CodeTemplate[]
}

export interface Category {
  category: string
  description: string
}
