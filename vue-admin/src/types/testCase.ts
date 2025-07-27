export interface TestCase {
  id: number
  problemId: number
  input: string
  output: string
  score: number
  isSample: boolean
  createdAt: string
}