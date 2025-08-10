export interface Problem {
  id: number
  title: string
  description: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  initialCode: { [language: string]: string }
  testCases: TestCase[]
}

export interface ProblemVO {
  id: number
  title: string
  description: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  initialCode: InitialCode[]
  testCases: TestCase[]
}

export interface InitialCode {
  language: string
  code: string
}

export interface TestCase {
  id: number
  inputs: InputDto[]
  output: string
  sample: boolean
  score: number
}

export interface InputDto {
  input: string
  inputName: string
}

export interface Submission {
  id: number
  problemId: number
  userId: number
  sourceCode: string
  language: string
  status: string
  score: number
  timeUsed: number
  memoryUsed: number
  compileInfo: string
  errorMessage: string
  createdAt: string
}

export interface SolutionCardVo {
  id: number
  title: string
  upvotes: number
  downvotes: number
  views: number
  createdAt: string
  authorUsername: string
  authorAvatar: string
  tags: string[]
  problem: string
}

export interface SolutionVo {
  id: number
  problemId: number
  userId: number
  title: string
  content: string
  language: string
  upvotes: number
  downvotes: number
  status: 'Pending' | 'Approved' | 'Rejected'
  createdAt: string
  updatedAt: string
  authorUsername: string
  authorAvatar: string
}

export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
