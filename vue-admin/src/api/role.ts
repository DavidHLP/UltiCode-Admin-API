import request from '@/utils/request'
import type { Role } from '@/types/role'

export function fetchRoles(): Promise<Role[]> {
  return request({
    url: '/role/api',
    method: 'get',
  })
}

export function createRole(data: Role): Promise<void> {
  return request({
    url: '/role/api',
    method: 'post',
    data,
  })
}

export function updateRole(id: number, data: Role): Promise<void> {
  return request({
    url: `/role/api/${id}`,
    method: 'put',
    data,
  })
}

export function deleteRole(id: number): Promise<void> {
  return request({
    url: `/role/api/${id}`,
    method: 'delete',
  })
}
