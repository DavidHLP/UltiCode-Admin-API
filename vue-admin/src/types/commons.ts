export interface Response<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: string
}
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
