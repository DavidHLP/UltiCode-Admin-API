import type { Role } from './role'

export interface User {
  userId: number
  username: string
  email: string
  password?: string
  avatar: string | null
  introduction: string
  address: string
  status: number
  lastLoginIp: string | null
  lastLogin: string | null
  createTime: string
  roles?: Role[]
  [key: string]: string | number | Role[] | null | undefined
}
