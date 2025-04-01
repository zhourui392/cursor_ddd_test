import request from './request'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 */
export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

/**
 * 根据 ID 获取用户
 * @param {number} id - 用户 ID
 */
export function getUserById(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 * @param {Object} data - 用户信息
 */
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 * @param {number} id - 用户 ID
 * @param {Object} data - 用户信息
 */
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id - 用户 ID
 */
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

/**
 * 为用户添加角色
 * @param {string} username - 用户名
 * @param {string} roleCode - 角色编码
 */
export function addRoleToUser(username, roleCode) {
  return request({
    url: `/users/${username}/roles/${roleCode}`,
    method: 'post'
  })
}

/**
 * 从用户中移除角色
 * @param {string} username - 用户名
 * @param {string} roleCode - 角色编码
 */
export function removeRoleFromUser(username, roleCode) {
  return request({
    url: `/users/${username}/roles/${roleCode}`,
    method: 'delete'
  })
} 