# Next.js Admin Frontend

这是一个使用 Next.js、TailwindCSS 和 Shadcn UI 构建的管理系统前端。

## 技术栈

- **Next.js 14** - React 框架
- **TypeScript** - 类型安全的 JavaScript
- **TailwindCSS** - 实用优先的 CSS 框架
- **Shadcn UI** - 基于 Radix UI 的组件库
- **Zustand** - 状态管理
- **Axios** - HTTP 客户端
- **React Hook Form** - 表单处理
- **Zod** - 表单验证

## 功能特性

- 用户认证 (登录/登出)
- 权限控制
- 用户管理
- 角色管理
- 权限管理
- 个人中心

## 项目结构

```
src/
├── api/            # API 服务
├── app/            # 应用路由
│   ├── (auth)/     # 认证相关页面
│   └── (dashboard)/# 仪表盘相关页面
├── components/     # 组件
│   └── ui/         # UI 组件
├── hooks/          # 自定义 Hooks
├── lib/            # 工具函数
├── store/          # 状态管理
└── types/          # 类型定义
```

## 开始使用

### 安装依赖

```bash
npm install
# 或
yarn install
# 或
pnpm install
```

### 开发环境运行

```bash
npm run dev
# 或
yarn dev
# 或
pnpm dev
```

在浏览器中打开 [http://localhost:3000](http://localhost:3000) 查看结果。

### 构建生产版本

```bash
npm run build
# 或
yarn build
# 或
pnpm build
```

### 运行生产版本

```bash
npm run start
# 或
yarn start
# 或
pnpm start
```

## API 配置

默认情况下，API 请求会被代理到 `http://localhost:8080/api`。如需修改 API 地址，请编辑 `next.config.js` 文件中的 `rewrites` 配置。

```javascript
async rewrites() {
  return [
    {
      source: '/api/:path*',
      destination: 'http://your-api-server.com/api/:path*',
    },
  ];
}
```

## 认证与授权

系统使用基于 JWT 的认证机制。登录成功后，token 会被存储在 localStorage 中，并在后续请求中自动添加到请求头中。

权限控制基于用户角色和权限，可以在角色管理中配置不同角色的权限。
