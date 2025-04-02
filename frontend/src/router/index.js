import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'USER_VIEW' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('../views/role/RoleList.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'ROLE_VIEW' }
      },
      {
        path: 'permission',
        name: 'Permission',
        component: () => import('../views/permission/PermissionList.vue'),
        meta: { title: '权限管理', icon: 'Key', permission: 'PERMISSION_VIEW' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/NotFound.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫触发，目标路径:', to.path)
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  console.log('当前token:', token ? '已存在' : '不存在')
  
  // 处理不需要验证的页面
  if (to.meta.requiresAuth === false) {
    console.log('访问不需要验证的页面')
    if (token && to.path === '/login') {
      // 已登录则跳转到首页
      console.log('已登录状态访问登录页，重定向到首页')
      next({ path: '/' })
    } else {
      next()
    }
    return
  }
  
  // 处理需要验证的页面
  if (!token) {
    // 未登录跳转到登录页
    console.log('未登录访问需要验证的页面，重定向到登录页')
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  
  // 已有token的情况
  if (!userStore.userInfo) {
    console.log('已有token但没有用户信息，获取用户信息')
    // 标记正在加载用户信息，防止重复请求
    const loadingUserInfo = localStorage.getItem('loadingUserInfo')
    if (loadingUserInfo === 'true') {
      console.log('正在获取用户信息中，避免重复请求')
      next()
      return
    }
    
    localStorage.setItem('loadingUserInfo', 'true')
    
    // 获取用户信息
    userStore.getUserInfo().then(() => {
      localStorage.removeItem('loadingUserInfo')
      console.log('用户信息获取成功')
      
      // 检查权限
      if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
        console.log('无权限访问，重定向到403页面')
        next({ path: '/403' })
      } else {
        console.log('有权限访问，允许导航')
        // 确保导航完成
        next({ ...to, replace: true })
      }
    }).catch((error) => {
      // 获取用户信息失败，清除token并跳转到登录页
      localStorage.removeItem('loadingUserInfo')
      console.error('获取用户信息失败:', error)
      localStorage.removeItem('token')
      next({ path: '/login', query: { redirect: to.fullPath } })
    })
  } else {
    // 检查权限
    if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
      console.log('无权限访问，重定向到403页面')
      next({ path: '/403' })
    } else {
      console.log('已有用户信息且有权限访问，允许导航')
      next()
    }
  }
})

export default router 