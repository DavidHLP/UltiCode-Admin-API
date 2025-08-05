export interface LoginRequest {
  username?: string
  password?: string
}

export interface RegisterRequest {
  username?: string
  password?: string
  email?: string
  code?: string
}

export interface Token {
  id: number
  userId: number
  token: string
  tokenType: string
  expired: boolean
  revoked: boolean
}

export interface AuthUser {
  userId: number
  username: string
  email: string
  role: {
    id: number
    roleName: string
  }
}
