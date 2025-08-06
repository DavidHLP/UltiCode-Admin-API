export interface Solution {
  id?: number
  problemId?: number
  userId?: number
  title: string
  content: string
  language: string
  upvotes?: number
  downvotes?: number
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt?: string
  updatedAt?: string
}
