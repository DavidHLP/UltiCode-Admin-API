import request from '@/utils/request'
import type { LoginRequest, Token } from '@/types/auth'

export function login(data: LoginRequest): Promise<Token> {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data,
  })
}

export function logout(data: { token: string }): Promise<void> {
  return request({
    url: '/api/auth/logout',
    method: 'post',
    data,
  })
}
