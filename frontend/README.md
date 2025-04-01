# 权限管理系统前端

基于 Vue 3 + Element Plus 的权限管理系统前端，配合 Spring Boot 后端使用。

## 功能特性

- 用户登录/注销
- 基于角色的权限控制（RBAC）
- 用户管理（查询、新增、编辑、删除）
- 角色管理（查询、新增、编辑、删除）
- 权限管理（查询、新增、编辑、删除）
- 个人信息管理

## 技术栈

- Vue 3
- Vue Router
- Pinia
- Element Plus
- Axios
- Vite

## 开发环境

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 生产环境

```bash
# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/             # API 接口
│   ├── assets/          # 资源文件（样式、图片等）
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── store/           # 状态管理（Pinia）
│   ├── utils/           # 工具函数
│   ├── views/           # 页面组件
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── index.html           # HTML 模板
├── vite.config.js       # Vite 配置
└── package.json         # 项目依赖和脚本
```

## 接口对接

前端默认将 API 请求代理到 `http://localhost:8080`，可以在 `vite.config.js` 中修改配置。

API 基础路径为 `/api`，可根据后端实际情况进行调整。 