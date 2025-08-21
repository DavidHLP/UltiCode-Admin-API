export type SolutionStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface SolutionManagementCard {
  id: number
  problemId: number
  userId: number
  authorUsername: string
  authorAvatar: string
  title: string
  language: string
  status: SolutionStatus
}
