import request from './request'

/**
 * 获取菜单列表
 */
export function getMenuList() {
  return request({
    url: '/menus',
    method: 'get'
  })
}

/**
 * 获取菜单树
 */
export function getMenuTree() {
  return request({
    url: '/menus/tree',
    method: 'get'
  })
}

/**
 * 根据ID获取菜单
 * @param {number} id - 菜单ID
 */
export function getMenuById(id) {
  return request({
    url: `/menus/${id}`,
    method: 'get'
  })
}

/**
 * 创建菜单
 * @param {Object} data - 菜单信息
 */
export function createMenu(data) {
  return request({
    url: '/menus',
    method: 'post',
    data
  })
}

/**
 * 更新菜单
 * @param {number} id - 菜单ID
 * @param {Object} data - 菜单信息
 */
export function updateMenu(id, data) {
  return request({
    url: `/menus/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 * @param {number} id - 菜单ID
 */
export function deleteMenu(id) {
  return request({
    url: `/menus/${id}`,
    method: 'delete'
  })
} 