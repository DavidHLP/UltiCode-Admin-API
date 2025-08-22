export interface SolutionEditVo {
  problemId: number
  title: string
  content: string
  tags: string[]
  language: string
  userId?: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
}

// 评论 VO——后端 SolutionCommentVo
export interface SolutionCommentVo {
  id: number
  solutionId: number
  userId: number
  username: string
  avatar: string
  content: string
  parentId?: number
  rootId?: number
  replyToUserId?: number
  replyToUsername?: string
  upvotes?: number
  downvotes?: number
  children?: SolutionCommentVo[]
}

// 题解卡片 VO（列表项）——后端 SolutionCardVo
export interface SolutionCardVo {
  id: number
  problemId: number
  userId: number
  authorUsername: string
  authorAvatar?: string
  tags: string[]
  contentView: string
  title: string
  language: string
  upvotes: number
  downvotes: number
  comments: number
  views: number
}

// 题解详情 VO——后端 SolutionDetailVo
export interface SolutionVo {
  id: number
  problemId: number
  userId: number
  authorUsername: string
  authorAvatar?: string
  content: string
  title: string
  language: string
  upvotes?: number
  downvotes?: number
  comments?: number
  views?: number
  // 可选：用于编辑页预填
  tags?: string[]
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt?: string
  updatedAt?: string
  // 嵌套评论
  solutionComments?: SolutionCommentVo[]
}
