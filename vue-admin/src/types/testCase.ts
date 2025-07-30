export interface TestCase {
  id: number
  problemId: number
  inputs: InputDto[]
  output: string
  score: number
  isSample: boolean
  createdAt: string
}

export interface InputDto {
  input: string
  inputName: string
}
