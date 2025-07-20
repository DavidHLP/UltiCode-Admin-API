import request from '@/utils/request'
import type { User } from '@/types/user'

export function fetchUsers(): Promise<User[]> {
  return request({
    url: '/user/api',
    method: 'get',
  })
}

export function getUser(id: number): Promise<User> {
  return request({
    url: `/user/api/${id}`,
    method: 'get',
  })
}

export function createUser(data: User): Promise<void> {
  return request({
    url: '/user/api',
    method: 'post',
    data,
  })
}

export function updateUser(id: number, data: User): Promise<void> {
  return request({
    url: `/user/api/${id}`,
    method: 'put',
    data,
  })
}

export function deleteUser(id: number): Promise<void> {
  return request({
    url: `/user/api/${id}`,
    method: 'delete',
  })
}
