import request from './request'

/**
 * 获取权限列表
 * @param {Object} params - 查询参数
 */
export function getPermissionList(params) {
  return request({
    url: '/permissions',
    method: 'get',
    params
  })
}

/**
 * 根据 ID 获取权限
 * @param {number} id - 权限 ID
 */
export function getPermissionById(id) {
  return request({
    url: `/permissions/${id}`,
    method: 'get'
  })
}

/**
 * 创建权限
 * @param {Object} data - 权限信息
 */
export function createPermission(data) {
  return request({
    url: '/permissions',
    method: 'post',
    data
  })
}

/**
 * 更新权限
 * @param {number} id - 权限 ID
 * @param {Object} data - 权限信息
 */
export function updatePermission(id, data) {
  return request({
    url: `/permissions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除权限
 * @param {number} id - 权限 ID
 */
export function deletePermission(id) {
  return request({
    url: `/permissions/${id}`,
    method: 'delete'
  })
} 