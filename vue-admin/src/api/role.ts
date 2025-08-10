import request from '@/utils/request'
import type { Role } from '@/types/role'
import { requestData } from '@/utils/request'
import type { PageResult } from '@/types/commons'

export function fetchRoles(): Promise<Role[]> {
  return request({
    url: '/user/api/role',
    method: 'get',
  })
}

export function fetchRolePage(params: {
  page: number
  size: number
  keyword?: string
  status?: number
}): Promise<PageResult<Role>> {
  return requestData<PageResult<Role>>({
    url: '/user/api/role/page',
    method: 'get',
    params,
  })
}

export function createRole(data: Role): Promise<void> {
  return request({
    url: '/user/api/role',
    method: 'post',
    data,
  })
}

export function updateRole(id: number, data: Role): Promise<void> {
  return request({
    url: `/user/api/role/${id}`,
    method: 'put',
    data,
  })
}

export function deleteRole(id: number): Promise<void> {
  return request({
    url: `/user/api/role/${id}`,
    method: 'delete',
  })
}
