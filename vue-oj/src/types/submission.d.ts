export interface SubmissionCardVo {
  id: number
  userId: number
  problemId: number
  language: string
  status: string
  timeUsed?: number
  memoryUsed?: number
}

export interface SubmissionDetailVo extends SubmissionCardVo {
  sourceCode?: string
  compileInfo?: string
  judgeInfo?: string
}
