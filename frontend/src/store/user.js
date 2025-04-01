import { defineStore } from 'pinia'
import { login, getUserInfo } from '../api/auth'
import request from '../api/request'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    permissions: []
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token && !!state.userInfo,
    username: (state) => state.userInfo?.username || '',
    nickname: (state) => state.userInfo?.nickname || '',
    roles: (state) => state.userInfo?.roles || []
  },
  
  actions: {
    /**
     * 用户登录
     */
    async loginAction(loginData) {
      try {
        console.log('调用登录API', loginData.username)
        const res = await login(loginData)
        console.log('登录API响应:', res)
        
        // 检查响应中是否包含token
        if (!res.data) {
          console.error('登录响应没有data字段', res)
          return Promise.reject(new Error('登录响应格式错误，缺少data字段'))
        }
        
        // 检查是否有token字段，兼容不同的返回格式
        let token = null;
        if (res.data.token) {
          // 直接有token字段
          token = res.data.token;
        } else if (res.data.tokenType && res.data.accessToken) {
          // OAuth2格式
          token = `${res.data.tokenType} ${res.data.accessToken}`;
        } else if (res.data.tokenType && res.data.token) {
          // 当前接口返回的格式
          token = res.data.token;
        }
        
        if (!token) {
          console.error('无法从响应中提取token', res.data)
          return Promise.reject(new Error('登录响应格式错误，无法提取token'))
        }
        
        console.log('获取到token:', token)
        
        this.token = token
        localStorage.setItem('token', token)
        console.log('token已保存到localStorage')
        
        return Promise.resolve(res)
      } catch (error) {
        console.error('登录操作失败:', error)
        return Promise.reject(error)
      }
    },
    
    /**
     * 获取用户信息 - 尝试多种可能的API路径
     */
    async getUserInfo() {
      try {
        console.log('-------- 开始获取用户信息 --------')
        
        // 尝试多种可能的API路径
        const apiPaths = [
          '/users/current'
        ]
        
        let lastError = null
        let userData = null
        
        // 依次尝试不同的路径
        for (const path of apiPaths) {
          console.log('尝试API路径:', path)
          try {
            const res = await request({
              url: path,
              method: 'get'
            })
            
            if (res.data) {
              console.log('用户信息获取成功，使用路径:', path)
              userData = res
              break
            }
          } catch (error) {
            console.log(`路径 ${path} 失败:`, error.message)
            lastError = error
          }
        }
        
        if (!userData) {
          console.error('所有API路径均失败', lastError)
          return Promise.reject(lastError || new Error('获取用户信息失败'))
        }
        
        console.log('用户信息API响应:', userData)
        
        if (!userData.data) {
          console.error('用户信息响应格式错误', userData)
          return Promise.reject(new Error('获取用户信息失败，响应格式错误'))
        }
        
        this.userInfo = userData.data
        console.log('用户信息已保存到store:', JSON.stringify(this.userInfo, null, 2))
        
        // 提取所有权限码
        let permissions = []
        
        if (userData.data.roles && userData.data.roles.length > 0) {
          console.log('处理用户角色权限...')
          console.log('用户角色:', JSON.stringify(userData.data.roles, null, 2))
          
          // 判断是否有管理员角色
          const hasAdminRole = userData.data.roles.some(role => role.code === 'ROLE_ADMIN')
          
          if (hasAdminRole) {
            // 管理员角色拥有所有权限
            console.log('检测到管理员角色，授予所有权限')
            permissions = [
              'USER_VIEW', 'USER_ADD', 'USER_EDIT', 'USER_DELETE',
              'ROLE_VIEW', 'ROLE_ADD', 'ROLE_EDIT', 'ROLE_DELETE',
              'PERMISSION_VIEW', 'PERMISSION_ADD', 'PERMISSION_EDIT', 'PERMISSION_DELETE',
              'MENU_VIEW', 'MENU_ADD', 'MENU_EDIT', 'MENU_DELETE'
            ]
          } else {
            // 普通用户，通过角色的权限列表获取权限
            userData.data.roles.forEach(role => {
              if (role.permissions && role.permissions.length > 0) {
                role.permissions.forEach(permission => {
                  if (permission.code && !permissions.includes(permission.code)) {
                    permissions.push(permission.code)
                  }
                })
              }
            })
          }
        }
        
        console.log('角色权限处理完成，当前权限列表:', permissions)
        
        // 尝试获取额外的权限信息 - 始终调用
        console.log('开始获取额外权限信息...')
        try {
          const permRes = await request({
            url: '/users/current/permissions',
            method: 'get'
          })
          
          console.log('权限接口返回:', JSON.stringify(permRes, null, 2))
          
          if (permRes.data && permRes.data.permissions) {
            console.log('获取到额外权限列表:', permRes.data.permissions)
            permRes.data.permissions.forEach(perm => {
              if (!permissions.includes(perm)) {
                permissions.push(perm)
              }
            })
          }
        } catch (err) {
          console.warn('获取额外权限信息失败:', err)
          // 失败不影响主流程
        }
        
        this.permissions = permissions
        console.log('最终权限列表已更新:', permissions)
        
        // 将权限列表也存储到localStorage，以便在页面刷新后仍能使用
        localStorage.setItem('permissions', JSON.stringify(permissions))
        
        console.log('-------- 用户信息获取完成 --------')
        return Promise.resolve(userData)
      } catch (error) {
        console.error('获取用户信息最终失败:', error)
        return Promise.reject(error)
      }
    },
    
    /**
     * 检查是否有指定权限
     */
    hasPermission(permission) {
      if (!permission) return true
      
      // 从localStorage加载权限（应对页面刷新情况）
      if (this.permissions.length === 0) {
        const storedPermissions = localStorage.getItem('permissions')
        if (storedPermissions) {
          this.permissions = JSON.parse(storedPermissions)
          console.log('从localStorage恢复权限列表:', this.permissions)
        }
      }
      
      // 检查是否有管理员角色
      const hasAdminRole = this.userInfo?.roles?.some(role => role.code === 'ROLE_ADMIN') || false
      if (hasAdminRole) {
        console.log(`管理员角色检查权限: ${permission} - 通过`)
        return true
      }
      
      const result = this.permissions.includes(permission)
      console.log(`权限检查: ${permission} - ${result ? '通过' : '拒绝'}`)
      return result
    },
    
    /**
     * 退出登录
     */
    logout() {
      this.token = ''
      this.userInfo = null
      this.permissions = []
      localStorage.removeItem('token')
      localStorage.removeItem('permissions')
      localStorage.removeItem('loadingUserInfo')
      
      // 可以在这里调用退出登录接口
      request({
        url: '/auth/logout',
        method: 'post'
      }).catch(err => {
        console.log('退出登录API调用失败，但不影响本地登出:', err)
      })
    }
  }
}) 