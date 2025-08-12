// 与后端 CalculationController.getSubmissionCalendar 对齐
// GET /problems/api/calculate/submission/calendar?userId

// 后端 CalendarVo：
// - date: LocalDateTime（JSON 格式：'yyyy-MM-dd'）
// - count: Integer（当天提交次数）
export interface CalendarVo {
  date: string
  count: number
}

// 兼容旧命名（RightSidebar 原使用 SubmissionCalendar）
export type SubmissionCalendar = CalendarVo
