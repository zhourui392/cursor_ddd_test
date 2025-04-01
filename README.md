# DDD权限管理系统

基于领域驱动设计(DDD)的RBAC权限管理系统，采用前后端分离架构，后端使用Spring Boot + MyBatis Plus实现，前端使用Vue3 + Element Plus实现。

## 项目架构

本项目采用经典的DDD分层架构：

```
应用层(Application) ←→ 领域层(Domain) ←→ 基础设施层(Infrastructure)
        ↑                                      ↑
        ↓                                      ↓
      接口层(Interface/API)               数据持久层(Persistence)
```

### 后端架构

```
com.example.demo
├── application         # 应用层 - 用例编排
│   ├── command         # 命令对象 - 表示用户意图
│   ├── dto             # 数据传输对象 - 应用服务的输入/输出
│   └── service         # 应用服务 - 编排领域对象
├── domain              # 领域层 - 业务核心
│   ├── model           # 领域模型
│   │   ├── entity      # 实体
│   │   └── valueobject # 值对象
│   ├── repository      # 仓储接口
│   └── service         # 领域服务
├── infrastructure      # 基础设施层 - 技术细节
│   ├── config          # 配置
│   ├── convert         # 对象转换器
│   ├── mapper          # MyBatis映射器
│   ├── persistence     # 持久化相关
│   │   └── entity      # 数据库实体
│   ├── repository      # 仓储实现
│   ├── security        # 安全相关
│   └── util            # 工具类
└── facade              # 外观层/接口层
    ├── dto             # 接口数据传输对象
    └── rest            # REST控制器
```

### 前端架构

```
frontend
├── public             # 静态资源
├── src                # 源代码
│   ├── api            # API请求
│   ├── assets         # 资源文件
│   ├── components     # 公共组件
│   ├── router         # 路由配置
│   ├── store          # 状态管理
│   ├── styles         # 样式文件
│   ├── utils          # 工具函数
│   └── views          # 页面组件
│       ├── layout     # 布局组件
│       ├── user       # 用户管理
│       ├── role       # 角色管理
│       ├── permission # 权限管理
│       └── menu       # 菜单管理
├── .env.*             # 环境变量配置
└── package.json       # 依赖配置
```

## 领域模型

系统核心领域模型采用RBAC（基于角色的访问控制）模型：

- **用户(User)**: 系统用户，可以被赋予一个或多个角色
- **角色(Role)**: 权限的集合，定义特定类型用户的权限边界
- **权限(Permission)**: 系统中的原子操作权限
- **菜单(Menu)**: 系统导航结构，与权限关联

### 模型关系

- 用户 1--* 角色: 一个用户可以拥有多个角色
- 角色 1--* 权限: 一个角色可以包含多个权限
- 菜单 1--* 权限: 一个菜单可以关联多个权限

## 后端技术栈

- **Spring Boot**: 应用框架
- **Spring Security**: 认证与授权
- **MyBatis Plus**: ORM框架
- **JWT**: 无状态认证
- **MapStruct**: 对象映射
- **Lombok**: 减少模板代码
- **H2/MySQL**: 数据库

### 特色实现

1. **领域驱动设计(DDD)**:
   - 聚合根: User、Role由各自的Repository管理
   - 值对象: UserId、Email等不可变对象
   - 领域服务: 处理跨聚合的操作

2. **仓储模式(Repository Pattern)**:
   - 为每个聚合根定义统一仓储接口
   - 基础设施层实现数据持久化细节

3. **CQRS模式**:
   - 使用Command对象表示修改意图
   - 使用DTO对象返回查询结果  

## 前端技术栈

- **Vue 3**: 核心前端框架
- **Vite**: 构建工具
- **Pinia**: 状态管理
- **Vue Router**: 路由管理
- **Element Plus**: UI组件库
- **Axios**: HTTP客户端

### 特色实现

1. **基于Token的认证**:
   - JWT令牌存储在localStorage
   - 全局请求拦截器添加授权头
   - 路由守卫控制页面访问权限

2. **动态路由**:
   - 根据用户权限动态加载路由
   - 权限指令控制UI元素显示

3. **响应式设计**:
   - 桌面和移动设备自适应布局

## 权限控制

系统权限控制分为两个层面：

1. **后端权限控制**:
   - API级别权限: 基于Spring Security的@PreAuthorize注解
   - 方法级别权限: 自定义权限评估器

2. **前端权限控制**:
   - 路由级别: 路由守卫检查访问权限
   - 组件级别: v-permission指令控制元素显示

## 快速开始

### 后端启动

```bash
# 编译
./mvnw clean package

# 运行
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

默认后端服务启动在 http://localhost:8080

### 前端启动

```bash
# 安装依赖
cd frontend
npm install

# 开发环境启动
npm run dev

# 构建生产环境
npm run build
```

默认前端开发服务启动在 http://localhost:3000

## 系统账号

系统初始化时会创建以下账号：

- 管理员账号: admin / 123456

## 项目规范

- 代码符合Alibaba P3C编码规约
- 使用统一的异常处理机制
- 所有API统一返回格式
- Git提交信息遵循Angular规范

## 参考资料

- [领域驱动设计参考](https://www.domainlanguage.com/ddd/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Vue 3 文档](https://v3.vuejs.org/)
- [Spring Boot文档](https://docs.spring.io/spring-boot/docs/current/reference/html/) 