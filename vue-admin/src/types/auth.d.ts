export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface LoginResponse {
  success: boolean
  message: string
}

export interface User {
  id: number
  username: string
  email?: string
  roles?: string[]
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
