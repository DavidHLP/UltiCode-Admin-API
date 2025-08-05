import request from '@/utils/request'
import type { AuthUser, LoginRequest, RegisterRequest, Token } from '@/types/auth'

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

export function register(data: RegisterRequest): Promise<void> {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data,
  })
}

export function sendVerificationCode(email: string): Promise<void> {
  return request({
    url: '/api/auth/send-code',
    method: 'post',
    params: { email },
  })
}

export function getUserInfo(): Promise<AuthUser> {
  return request({
    url: '/api/auth/me',
    method: 'get',
  })
}
