export interface LoginRequest {
  username?: string
  password?: string
}

export interface Token {
  id: number
  userId: number
  token: string
  tokenType: string
  expired: boolean
  revoked: boolean
}
