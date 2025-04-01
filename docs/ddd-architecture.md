# DDD架构详解

本文档详细解释了本项目中领域驱动设计(Domain-Driven Design, DDD)的应用和实现方式。

## DDD概述

领域驱动设计是一种软件开发方法，它强调：

1. **以领域为中心**：将业务领域置于设计的核心位置
2. **统一语言**：业务专家和开发团队使用共同的语言进行交流
3. **模型驱动设计**：通过领域模型驱动软件设计和开发
4. **界限上下文**：明确定义模型的适用范围和边界
5. **分层架构**：将软件分为不同的层次，各层之间有明确的职责

## 项目分层架构

本项目遵循经典的DDD分层架构：

### 领域层 (Domain Layer)

领域层是业务核心，包含所有业务规则和领域知识，不依赖于其他层。

```java
// 领域实体示例 - User.java
@Getter
@AllArgsConstructor
public class User {
    private UserId id;
    private String username;
    // ...
    
    // 领域行为
    public void addRole(Role role) {
        this.roles.add(Objects.requireNonNull(role, "角色不能为空"));
    }
    
    // 工厂方法创建聚合根
    public static User create(String username, String password, String nickname, String email, String phone) {
        User user = new User(username, password);
        user.nickname = nickname;
        user.email = new Email(email);
        user.phone = new Phone(phone);
        return user;
    }
}
```

领域层的核心组件：

1. **实体(Entity)**：具有唯一标识和生命周期的对象
2. **值对象(Value Object)**：没有唯一标识，通过其属性值定义的对象
3. **聚合(Aggregate)**：相关实体和值对象的集合，以聚合根为入口
4. **领域服务(Domain Service)**：处理跨实体的业务规则
5. **仓储接口(Repository Interface)**：定义聚合的持久化规范

### 应用层 (Application Layer)

应用层协调领域对象来执行用例，封装和编排业务流程。

```java
// 应用服务示例 - UserApplicationServiceImpl.java
@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationServiceImpl implements UserApplicationService {
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    
    @Override
    public void addRoleToUser(String username, String roleCode) {
        try {
            // 委托给领域服务处理
            userDomainService.assignRoleToUser(username, roleCode);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
```

应用层的核心组件：

1. **应用服务(Application Service)**：用例的编排器
2. **命令(Command)**：表示用户意图的不可变对象
3. **DTO(Data Transfer Object)**：跨层数据传输对象
4. **装配器(Assembler)**：在不同层之间转换对象

### 基础设施层 (Infrastructure Layer)

基础设施层提供技术实现，包括持久化、消息、安全等。

```java
// 仓储实现示例 - UserRepositoryImpl.java
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;
    private final UserConvert userConvert;
    
    @Override
    @Transactional
    public User save(User user) {
        // 入参校验
        Objects.requireNonNull(user, "待保存的用户不能为空");
        
        // 数据库操作实现
        UserDO userDO = userConvert.toData(user);
        userMapper.insert(userDO);
        
        return userConvert.toDomain(userDO);
    }
}
```

基础设施层的核心组件：

1. **仓储实现(Repository Implementation)**：领域仓储接口的具体实现
2. **ORM映射(Mapper)**：对象关系映射
3. **第三方服务整合**：如消息队列、缓存等
4. **安全机制**：认证、授权、加密等

### 接口层/用户界面层 (Interface/UI Layer)

接口层处理用户交互，将用户请求转换为命令或查询。

```java
// REST控制器示例 - UserController.java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userApplicationService;
    
    @PostMapping("/{username}/roles/{roleCode}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<ApiResponse<Void>> addRoleToUser(
            @PathVariable String username, 
            @PathVariable String roleCode) {
        userApplicationService.addRoleToUser(username, roleCode);
        return ResponseEntity.ok(ApiResponse.success(null, "角色已添加到用户"));
    }
}
```

## DDD战术设计模式

### 实体与值对象

```java
// 实体 - 具有唯一身份
public class User {
    private UserId id;  // 身份标识
    // ...
}

// 值对象 - 通过属性定义
public class Email {
    private final String value;
    
    public Email(String value) {
        if (value == null || !isValid(value)) {
            throw new IllegalArgumentException("无效的邮箱地址");
        }
        this.value = value;
    }
    
    // 值对象相等性比较基于属性值
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
}
```

### 聚合与聚合根

```java
// User作为聚合根
public class User {
    private UserId id;
    private Set<Role> roles;  // 聚合内的实体
    
    // 聚合根控制内部实体的访问
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
}
```

### 仓储模式

```java
// 仓储接口 - 领域层
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByUsername(String username);
    void delete(User user);
}

// 仓储实现 - 基础设施层
@Repository
public class UserRepositoryImpl implements UserRepository {
    // 实现细节...
}
```

### 领域服务

```java
// 领域服务 - 处理跨聚合的业务逻辑
@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    // 涉及两个聚合的操作放在领域服务中
    public User assignRoleToUser(String username, String roleCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
                
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        
        user.addRole(role);
        return userRepository.save(user);
    }
}
```

## DDD战略设计

### 限界上下文 (Bounded Context)

本项目主要包含以下限界上下文：

1. **用户与权限上下文**：处理用户、角色、权限的管理
2. **菜单上下文**：处理系统导航结构
3. **安全上下文**：处理认证与授权

### 上下文映射 (Context Mapping)

上下文之间的关系：

1. **用户与权限**与**菜单**上下文之间：通过共享内核 (Shared Kernel) 方式集成
2. **用户与权限**与**安全**上下文之间：通过防腐层 (ACL) 隔离变化

## 项目中的DDD最佳实践

1. **富领域模型**：业务逻辑封装在领域对象中
2. **贫血DTO**：在层间数据传输使用无行为的DTO
3. **命令模式**：使用命令对象表达用户意图
4. **工厂方法**：使用静态工厂方法创建聚合
5. **值对象不可变**：确保值对象创建后不可修改

## 反模式与避免方法

1. **贫血领域模型**：避免将所有业务逻辑放在服务中
2. **事务脚本**：避免在应用服务中编写过程式代码
3. **记录集**：避免使用数据库表映射对象作为领域模型
4. **智能UI**：避免在表示层实现业务逻辑

## 实现挑战与解决方案

1. **挑战**：聚合间的一致性
   **解决方案**：使用领域事件和最终一致性

2. **挑战**：性能与领域模型的平衡
   **解决方案**：针对查询的性能优化，CQRS模式

3. **挑战**：复杂对象图的持久化
   **解决方案**：使用自定义转换器和ORM映射 