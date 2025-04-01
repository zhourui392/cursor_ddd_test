# DDD权限管理系统前端

本项目是基于Vue3和Element Plus开发的DDD权限管理系统前端部分，采用现代前端技术栈和最佳实践。

## 技术栈

- **核心框架**：Vue 3 + Composition API
- **构建工具**：Vite
- **状态管理**：Pinia
- **路由管理**：Vue Router 4
- **UI组件库**：Element Plus
- **HTTP客户端**：Axios
- **样式处理**：SCSS + CSS变量
- **代码规范**：ESLint + Prettier
- **包管理器**：npm/yarn/pnpm

## 项目结构

```
frontend/
├── public/                 # 静态资源
│   └── favicon.ico         # 网站图标
├── src/                    # 源代码
│   ├── api/                # API接口封装
│   │   ├── auth.js         # 认证相关接口
│   │   ├── request.js      # Axios封装和拦截器
│   │   ├── user.js         # 用户相关接口
│   │   ├── role.js         # 角色相关接口
│   │   ├── permission.js   # 权限相关接口
│   │   └── menu.js         # 菜单相关接口
│   ├── assets/             # 资源文件
│   │   ├── images/         # 图片资源
│   │   └── styles/         # 全局样式
│   ├── components/         # 全局公共组件
│   │   ├── common/         # 通用组件
│   │   ├── layout/         # 布局组件
│   │   └── business/       # 业务组件
│   ├── router/             # 路由配置
│   │   └── index.js        # 路由主文件
│   ├── store/              # Pinia状态管理
│   │   ├── modules/        # 状态模块
│   │   └── index.js        # 状态入口
│   ├── utils/              # 工具函数
│   │   ├── auth.js         # 认证工具
│   │   ├── validate.js     # 验证工具
│   │   └── formatter.js    # 格式化工具
│   ├── views/              # 页面组件
│   │   ├── login/          # 登录页
│   │   ├── dashboard/      # 仪表盘
│   │   ├── user/           # 用户管理
│   │   ├── role/           # 角色管理
│   │   ├── permission/     # 权限管理
│   │   └── menu/           # 菜单管理
│   ├── App.vue             # 根组件
│   ├── main.js             # 入口文件
│   └── permission.js       # 权限控制
├── .env                    # 环境变量
├── .env.development        # 开发环境变量
├── .env.production         # 生产环境变量
├── index.html              # HTML模板
├── vite.config.js          # Vite配置
├── package.json            # 项目依赖
└── README.md               # 项目说明
```

## 功能特性

1. **基于Token的身份验证**
   - JWT令牌认证
   - 自动刷新令牌
   - 安全登出机制

2. **动态权限控制**
   - 基于角色的菜单权限
   - 按钮级别权限控制
   - 权限指令 `v-permission`

3. **响应式设计**
   - 桌面和移动设备适配
   - 暗黑模式支持

4. **主要业务页面**
   - 用户管理
   - 角色管理
   - 权限管理
   - 菜单管理

## 快速开始

### 环境要求

- Node.js 16+
- npm 7+ / yarn / pnpm

### 开发环境启动

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

开发服务器将在 http://localhost:5173 启动

### 构建生产版本

```bash
# 构建生产环境
npm run build

# 预览生产构建
npm run preview
```

## 主要技术实现

### 1. 网络请求

使用封装的Axios实例处理所有API请求：

```javascript
// src/api/request.js
import axios from 'axios'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== '200') {
      // 处理业务错误
      return Promise.reject(new Error(res.message || '错误'))
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 处理401认证错误
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default service
```

### 2. 权限控制

路由权限与动态菜单：

```javascript
// src/permission.js
import router from './router'
import { useUserStore } from './store/user'
import NProgress from 'nprogress'

const whiteList = ['/login', '/403', '/404']

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const hasToken = localStorage.getItem('token')
  
  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (userStore.userInfo) {
        next()
      } else {
        try {
          await userStore.getUserInfo()
          next({ ...to, replace: true })
        } catch (error) {
          userStore.logout()
          next('/login')
          NProgress.done()
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`)
      NProgress.done()
    }
  }
})
```

### 3. 状态管理

使用Pinia进行状态管理：

```javascript
// src/store/user.js
import { defineStore } from 'pinia'
import { login, getUserInfo } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    permissions: []
  }),
  
  getters: {
    // 计算属性
    hasPermission: (state) => (permissionCode) => {
      return state.permissions.includes(permissionCode)
    }
  },
  
  actions: {
    // 异步操作
    async loginAction(loginData) {
      const res = await login(loginData)
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
      return res
    },
    
    async getUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data
      // 提取权限代码
      this.permissions = extractPermissions(res.data)
      return res
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
      this.permissions = []
      localStorage.removeItem('token')
    }
  }
})
```

## 代码规范

项目遵循以下代码规范：

1. **组件命名**：使用PascalCase命名
2. **文件命名**：使用kebab-case命名
3. **CSS类命名**：使用BEM命名法
4. **JS变量命名**：使用camelCase命名
5. **常量命名**：使用大写下划线连接

## 最佳实践

1. **组件设计**
   - 单一职责原则
   - Composition API与逻辑复用
   - 按需导入

2. **性能优化**
   - 路由懒加载
   - 组件异步加载
   - 虚拟滚动大列表

3. **安全最佳实践**
   - XSS防御
   - CSRF防御
   - 敏感数据处理

## 常见问题

**Q: 如何添加新页面？**
A: 1. 在views目录创建页面组件 2. 在router中添加路由配置 3. 在菜单管理添加新菜单项

**Q: 如何添加新权限？**
A: 1. 在后端添加权限编码 2. 在前端使用v-permission指令或hasPermission方法检查权限 