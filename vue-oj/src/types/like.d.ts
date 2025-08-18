export type TargetType = 'ARTICLE' | 'COMMENT' | 'REPLY' | 'SOLUTION'
export type ActionType = 'LIKE' | 'DISLIKE'
export type UserAction = 'NONE' | 'LIKE' | 'DISLIKE'

export interface LikeDislikeRecordVo {
  userAction?: UserAction | null
  likeCount: number
  dislikeCount: number
  totalCount: number
}

export interface LikeStatusParams {
  userId: number
  targetType: TargetType
  targetId: number
}

export interface TakeActionParams extends LikeStatusParams {
  actionType: ActionType
}

export interface StatsParams {
  targetType: TargetType
  targetId: number
}
