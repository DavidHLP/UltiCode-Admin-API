export interface Solution {
  id: number
  problemId: number
  userId: number
  title: string
  content: string
  language: string
  upvotes: number
  downvotes: number
  status: 'Pending' | 'Approved' | 'Rejected'
  createdAt: string
  updatedAt: string
}
