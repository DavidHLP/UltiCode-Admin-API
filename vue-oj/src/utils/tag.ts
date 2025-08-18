import type { ProblemDifficulty } from '@/types/problem'
import type { JudgeStatus, JudgeStatusMeta } from '@/types/submission.d'
import { JUDGE_STATUS_MAP } from '@/types/submission.d'

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'
export type TagEffect = 'light' | 'dark' | 'plain'

// 仅 Element Plus <el-tag> 支持的 type 集合（不含 primary）
type ElTagType = Exclude<TagType, 'primary'>

type DifficultyMeta = { label: string; type: TagType }

const DIFFICULTY_MAP: Record<ProblemDifficulty, DifficultyMeta> = {
  EASY: { label: '简单', type: 'success' },
  MEDIUM: { label: '中等', type: 'warning' },
  HARD: { label: '困难', type: 'danger' },
}

const normalizeDifficulty = (
  difficulty: string | ProblemDifficulty | null | undefined,
): ProblemDifficulty | undefined => {
  if (!difficulty) return undefined
  const key = String(difficulty).toUpperCase() as ProblemDifficulty
  return key === 'EASY' || key === 'MEDIUM' || key === 'HARD' ? key : undefined
}

export const isProblemDifficulty = (val: unknown): val is ProblemDifficulty =>
  typeof val === 'string' && ['EASY', 'MEDIUM', 'HARD'].includes(val.toUpperCase())

export const getDifficultyTagType = (
  difficulty: string | ProblemDifficulty | null | undefined,
): TagType => {
  const key = normalizeDifficulty(difficulty)
  return key ? DIFFICULTY_MAP[key].type : 'info'
}

export const getDifficultyChinese = (
  difficulty: string | ProblemDifficulty | null | undefined,
): string => {
  const key = normalizeDifficulty(difficulty)
  return key ? DIFFICULTY_MAP[key].label : '未知'
}

export const getDifficultyInfo = (
  difficulty: string | ProblemDifficulty | null | undefined,
): DifficultyMeta => {
  const key = normalizeDifficulty(difficulty)
  return key ? DIFFICULTY_MAP[key] : { label: '未知', type: 'info' }
}

const normalizeJudgeStatus = (
  status: string | JudgeStatus | null | undefined,
): JudgeStatus | undefined => {
  if (!status) return undefined
  const key = String(status).toUpperCase() as JudgeStatus
  return key in JUDGE_STATUS_MAP ? key : undefined
}

export const isJudgeStatus = (val: unknown): val is JudgeStatus =>
  typeof val === 'string' && (val.toUpperCase() as JudgeStatus) in JUDGE_STATUS_MAP

export const getJudgeStatusTagType = (status: string | JudgeStatus | null | undefined): TagType => {
  const key = normalizeJudgeStatus(status)
  // 规范化：Element Plus Tag 不支持 'primary'，统一降级为 'info'
  const toElTagType = (t: TagType): ElTagType => (t === 'primary' ? 'info' : t)
  return key ? toElTagType(JUDGE_STATUS_MAP[key].type) : 'info'
}

export const getJudgeStatusChinese = (status: string | JudgeStatus | null | undefined): string => {
  const key = normalizeJudgeStatus(status)
  return key ? JUDGE_STATUS_MAP[key].label : '未知状态'
}

export const getJudgeStatusInfo = (
  status: string | JudgeStatus | null | undefined,
): JudgeStatusMeta => {
  const key = normalizeJudgeStatus(status)
  return key ? JUDGE_STATUS_MAP[key] : { label: '未知状态', type: 'info' }
}

// 为不同 JudgeStatus 提供更丰富的 Tag 展示属性
export interface JudgeStatusTagProps {
  type: ElTagType
  effect?: TagEffect
  hit?: boolean
}

const JUDGE_STATUS_TAG_PROPS: Record<JudgeStatus, JudgeStatusTagProps> = {
  PENDING: { type: 'info', effect: 'plain', hit: false },
  JUDGING: { type: 'warning', effect: 'dark', hit: true },
  ACCEPTED: { type: 'success', effect: 'dark', hit: true },
  CONTINUE: { type: 'warning', effect: 'light', hit: true },
  WRONG_ANSWER: { type: 'danger', effect: 'dark', hit: true },
  TIME_LIMIT_EXCEEDED: { type: 'warning', effect: 'plain', hit: true },
  MEMORY_LIMIT_EXCEEDED: { type: 'warning', effect: 'plain', hit: true },
  OUTPUT_LIMIT_EXCEEDED: { type: 'warning', effect: 'plain', hit: true },
  RUNTIME_ERROR: { type: 'danger', effect: 'dark', hit: true },
  COMPILE_ERROR: { type: 'danger', effect: 'plain', hit: false },
  SYSTEM_ERROR: { type: 'danger', effect: 'dark', hit: true },
  PRESENTATION_ERROR: { type: 'warning', effect: 'light', hit: false },
  SECURITY_ERROR: { type: 'danger', effect: 'dark', hit: true },
}

export const getJudgeStatusTagProps = (
  status: string | JudgeStatus | null | undefined,
): JudgeStatusTagProps => {
  const key = normalizeJudgeStatus(status)
  return key ? JUDGE_STATUS_TAG_PROPS[key] : { type: 'info', effect: 'plain', hit: false }
}
