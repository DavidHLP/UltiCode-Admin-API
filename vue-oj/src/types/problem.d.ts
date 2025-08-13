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

// --------------------
// Client-side models
// --------------------

// 前端调试卡片使用的输入参数类型
export interface InputDto {
  inputName: string
  input: string
}

// 前端调试卡片使用的测试用例类型（由服务端 TestCaseVo 映射而来）
export interface TestCase {
  id: number
  inputs: InputDto[]
  output: string
  sample: boolean
  score: number
}

// 判题提交结果（最小集合，按前端实际使用字段定义）
export interface Submission {
  id: number
  problemId: number
  userId?: number
  status: string // e.g. PENDING, JUDGING, Accepted, Wrong Answer, etc.
  language: string
  sourceCode?: string
  timeUsed?: number
  memoryUsed?: number
  score?: number
  compileInfo?: string
  errorMessage?: string
  createdAt?: string
}

// 前端题目详情页使用的 Problem 模型
export interface Problem {
  id: number
  title: string
  description: string
  difficulty: ProblemDifficulty
  // 语言 -> 默认代码
  initialCode: Record<string, string>
  // 用于 DebugCard 的测试用例
  testCases: TestCase[]
}
