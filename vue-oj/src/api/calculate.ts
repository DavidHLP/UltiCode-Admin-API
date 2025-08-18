import request from '@/utils/request'
import type { CalendarVo, UserOverviewVo } from '@/types/calculate.d.ts'
import type { Page } from '@/types/commons'
import type { SubmissionCardVo } from '@/types/submission'
import type { SolutionCardVo } from '@/types/solution'
export const getSubmissionCalendar = (userId?: number): Promise<CalendarVo[]> => {
  return request({
    url: '/problems/api/calculate/submission/calendar',
    method: 'get',
    params: { userId },
  })
}

export const getUserOverview = (): Promise<UserOverviewVo> => {
  return request.get('/problems/api/calculate/user/overview')
}

export const getUserSubmissionPage = (params: {
  page?: number
  size?: number
}): Promise<Page<SubmissionCardVo>> => {
  return request.get('/problems/api/calculate/submission/userInfo', {
    params: { page: params.page || 1, size: params.size || 10 },
  })
}

export const getUserSolutionPage = (params: {
  page?: number
  size?: number
}): Promise<Page<SolutionCardVo>> => {
  return request.get('/problems/api/calculate/solution/userInfo', {
    params: { page: params.page || 1, size: params.size || 10 },
  })
}
