import request from './request';

/**
 * Get permission list
 * @param params Query parameters
 */
export function getPermissionList(params?: any) {
  return request({
    url: '/permissions',
    method: 'get',
    params
  });
}

/**
 * Get permission by ID
 * @param id Permission ID
 */
export function getPermissionById(id: string) {
  return request({
    url: `/permissions/${id}`,
    method: 'get'
  });
}

/**
 * Create permission
 * @param data Permission data
 */
export function createPermission(data: any) {
  return request({
    url: '/permissions',
    method: 'post',
    data
  });
}

/**
 * Update permission
 * @param id Permission ID
 * @param data Permission data
 */
export function updatePermission(id: string, data: any) {
  return request({
    url: `/permissions/${id}`,
    method: 'put',
    data
  });
}

/**
 * Delete permission
 * @param id Permission ID
 */
export function deletePermission(id: string) {
  return request({
    url: `/permissions/${id}`,
    method: 'delete'
  });
}
