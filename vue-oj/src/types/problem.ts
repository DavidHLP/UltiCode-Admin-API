export interface Problem {
  id: number
  title: string
  description: string
  difficulty: 'Easy' | 'Medium' | 'Hard'
  initialCode: { [language: string]: string }
  testCases: TestCase[]
}

export interface TestCase {
  id: number
  input: string
  output: string
  sample: boolean
  score: number
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

export interface Solution {
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
