export interface CalendarVo {
  date: string
  count: number
}

export interface UserOverviewVo {
  userId: number
  totalSubmissions: number
  acceptedSubmissions: number
  attemptedProblems: number
  solvedProblems: number
  passRate: number // 0-100
}
