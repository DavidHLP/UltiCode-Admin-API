import request from '@/utils/request'
import type { LoginRequest, AuthResponse, AuthUser } from '@/types/auth'

export function login(data: LoginRequest): Promise<AuthResponse> {
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

export function getUserInfo(): Promise<AuthUser> {
  return request({
    url: '/api/auth/me',
    method: 'get',
  })
}
