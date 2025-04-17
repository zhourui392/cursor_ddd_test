import request from './request';

/**
 * Get user list
 * @param params Query parameters
 */
export function getUserList(params?: any) {
  return request({
    url: '/users',
    method: 'get',
    params
  });
}

/**
 * Get user by ID
 * @param id User ID
 */
export function getUserById(id: string) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  });
}

/**
 * Create user
 * @param data User data
 */
export function createUser(data: any) {
  return request({
    url: '/users',
    method: 'post',
    data
  });
}

/**
 * Update user
 * @param id User ID
 * @param data User data
 */
export function updateUser(id: string, data: any) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  });
}

/**
 * Delete user
 * @param id User ID
 */
export function deleteUser(id: string) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  });
}

/**
 * Update user password
 * @param id User ID
 * @param data Password data
 */
export function updateUserPassword(id: string, data: any) {
  return request({
    url: `/users/${id}/password`,
    method: 'put',
    data
  });
}
