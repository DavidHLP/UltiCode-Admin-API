import request from '@/utils/request'
import type { CalendarVo } from '@/types/calculate.d.ts'
export const getSubmissionCalendar = (userId?: number): Promise<CalendarVo[]> => {
  return request({
    url: '/problems/api/calculate/submission/calendar',
    method: 'get',
    params: { userId },
  })
}
