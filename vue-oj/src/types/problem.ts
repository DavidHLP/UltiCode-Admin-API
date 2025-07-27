export interface Problem {
  id: number
  title: string
  difficulty: 'Easy' | 'Medium' | 'Hard'
  description: string
  tags: string[]
  initialCode: {
    [language: string]: string
  }
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
}
