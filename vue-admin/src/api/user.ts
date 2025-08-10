import request from '@/utils/request'
import type { User } from '@/types/user'
import type { PageResult } from '@/types/commons'
export function createUser(data: User): Promise<void> {
  return request({
    url: '/user/api/user',
    method: 'post',
    data,
  })
}

export function updateUser(id: number, data: User): Promise<void> {
  return request({
    url: `/user/api/user/${id}`,
    method: 'put',
    data,
  })
}

export function deleteUser(id: number): Promise<void> {
  return request({
    url: `/user/api/user/${id}`,
    method: 'delete',
  })
}

export function fetchUsersPage(params: { page: number; size: number; keyword?: string; roleId?: number }): Promise<PageResult<User>> {
  return request({
    url: '/user/api/user/page',
    method: 'get',
    params,
  })
}
