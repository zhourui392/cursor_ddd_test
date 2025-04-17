import request from './request';

/**
 * Get role list
 * @param params Query parameters
 */
export function getRoleList(params?: any) {
  return request({
    url: '/roles',
    method: 'get',
    params
  });
}

/**
 * Get role by ID
 * @param id Role ID
 */
export function getRoleById(id: string) {
  return request({
    url: `/roles/${id}`,
    method: 'get'
  });
}

/**
 * Create role
 * @param data Role data
 */
export function createRole(data: any) {
  return request({
    url: '/roles',
    method: 'post',
    data
  });
}

/**
 * Update role
 * @param id Role ID
 * @param data Role data
 */
export function updateRole(id: string, data: any) {
  return request({
    url: `/roles/${id}`,
    method: 'put',
    data
  });
}

/**
 * Delete role
 * @param id Role ID
 */
export function deleteRole(id: string) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  });
}

/**
 * Get all permissions for role assignment
 */
export function getAllPermissions() {
  return request({
    url: '/permissions',
    method: 'get'
  });
}
