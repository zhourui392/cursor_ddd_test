import request from './request'

/**
 * 获取角色列表
 * @param {Object} params - 查询参数
 */
export function getRoleList(params) {
  return request({
    url: '/roles',
    method: 'get',
    params
  })
}

/**
 * 根据 ID 获取角色
 * @param {number} id - 角色 ID
 */
export function getRoleById(id) {
  return request({
    url: `/roles/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 * @param {Object} data - 角色信息
 */
export function createRole(data) {
  return request({
    url: '/roles',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 * @param {number} id - 角色 ID
 * @param {Object} data - 角色信息
 */
export function updateRole(id, data) {
  return request({
    url: `/roles/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 * @param {number} id - 角色 ID
 */
export function deleteRole(id) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  })
}

/**
 * 为角色添加权限
 * @param {string} roleCode - 角色编码
 * @param {string} permissionCode - 权限编码
 */
export function addPermissionToRole(roleCode, permissionCode) {
  return request({
    url: `/roles/${roleCode}/permissions/${permissionCode}`,
    method: 'post'
  })
}

/**
 * 从角色中移除权限
 * @param {string} roleCode - 角色编码
 * @param {string} permissionCode - 权限编码
 */
export function removePermissionFromRole(roleCode, permissionCode) {
  return request({
    url: `/roles/${roleCode}/permissions/${permissionCode}`,
    method: 'delete'
  })
} 