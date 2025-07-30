export interface Response<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
