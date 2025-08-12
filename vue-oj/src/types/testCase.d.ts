export interface TestCaseInput {
  id: number
  testCaseOutputId: number
  testCaseName: string
  inputType: string
  inputContent: string
  orderIndex: number
}

export interface TestCaseOutput {
  id: number
  problemId: number
  output: string
  outputType: string
  score: number
  isSample: boolean
}

export interface TestCaseVo {
  id: number
  problemId: number
  testCaseInputs: TestCaseInput[]
  testCaseOutput: TestCaseOutput
}
