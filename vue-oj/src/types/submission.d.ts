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
  errorTestCaseId?: number
  errorTestCaseOutput?: string
  errorTestCaseExpectOutput?: string
}

export type JudgeStatus =
  | 'PENDING'
  | 'JUDGING'
  | 'ACCEPTED'
  | 'CONTINUE'
  | 'WRONG_ANSWER'
  | 'TIME_LIMIT_EXCEEDED'
  | 'MEMORY_LIMIT_EXCEEDED'
  | 'OUTPUT_LIMIT_EXCEEDED'
  | 'RUNTIME_ERROR'
  | 'COMPILE_ERROR'
  | 'SYSTEM_ERROR'
  | 'PRESENTATION_ERROR'
  | 'SECURITY_ERROR'

export type JudgeStatusMeta = { label: string; type: TagType }

export const JUDGE_STATUS_MAP: Record<JudgeStatus, JudgeStatusMeta> = {
  PENDING: { label: '等待判题', type: 'info' },
  JUDGING: { label: '判题中', type: 'primary' },
  ACCEPTED: { label: '答案正确', type: 'success' },
  CONTINUE: { label: '继续判题', type: 'primary' },
  WRONG_ANSWER: { label: '答案错误', type: 'danger' },
  TIME_LIMIT_EXCEEDED: { label: '时间超限', type: 'warning' },
  MEMORY_LIMIT_EXCEEDED: { label: '内存超限', type: 'warning' },
  OUTPUT_LIMIT_EXCEEDED: { label: '输出超限', type: 'warning' },
  RUNTIME_ERROR: { label: '运行时错误', type: 'danger' },
  COMPILE_ERROR: { label: '编译错误', type: 'danger' },
  SYSTEM_ERROR: { label: '系统错误', type: 'danger' },
  PRESENTATION_ERROR: { label: '格式错误', type: 'warning' },
  SECURITY_ERROR: { label: '安全错误', type: 'danger' },
}
