import request from '@/utils/request'
import type {
  LikeDislikeRecordVo,
  LikeStatusParams,
  TakeActionParams,
  StatsParams,
} from '@/types/like'

const BASE = '/api/problems/likeDislike'

/** 获取用户对目标的操作状态（含统计） */
export const getStatus = (params: LikeStatusParams): Promise<LikeDislikeRecordVo> => {
  return request.get(`${BASE}/status`, { params })
}

/** 执行动作：LIKE 或 DISLIKE（服务端会处理取消/切换） */
export const takeAction = (params: TakeActionParams): Promise<LikeDislikeRecordVo> => {
  // 后端使用 @RequestParam，参数通过 query 传递
  return request.post(`${BASE}/action`, undefined, { params })
}

/** 取消当前动作 */
export const cancelAction = (params: LikeStatusParams): Promise<LikeDislikeRecordVo> => {
  return request.delete(`${BASE}/cancel`, { params })
}

/** 获取统计数据 */
export const getStats = (params: StatsParams): Promise<LikeDislikeRecordVo> => {
  return request.get(`${BASE}/stats`, { params })
}
