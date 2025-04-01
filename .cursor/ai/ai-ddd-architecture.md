# DDD架构整体规范

## 项目结构
```
com.example.project
├── domain                # 领域层
│   ├── model             # 领域模型
│   │   ├── entity        # 实体
│   │   ├── valueobject   # 值对象
│   │   ├── aggregate     # 聚合根
│   │   └── event         # 领域事件
│   ├── service           # 领域服务
│   └── repository        # 仓储接口
├── application           # 应用层
│   ├── service           # 应用服务
│   ├── dto               # 数据传输对象
│   ├── command           # 命令对象
│   ├── query             # 查询对象
│   └── event             # 应用事件
├── facade                # 接口层
│   ├── rest              # REST接口
│   ├── graphql           # GraphQL接口(可选)
│   ├── grpc              # gRPC接口(可选)
│   └── dto               # 接口层DTO
└── infrastructure        # 基础设施层
    ├── repository        # 仓储实现
    ├── persistence       # 持久化相关
    │   └── entity        # 持久化实体
    ├── client            # 外部系统客户端
    ├── message           # 消息队列
    ├── cache             # 缓存
    ├── config            # 配置
    └── security          # 安全相关
```

## 边界上下文(Bounded Context)
- 根据业务领域划分不同的边界上下文
- 每个边界上下文有自己的领域模型和语言
- 上下文间通过上下文映射进行集成
- 明确定义上下文边界和职责

```
// 订单上下文
com.example.order
├── domain
│   ├── model
│   │   ├── Order
│   │   ├── OrderItem
│   │   └── ...
// 支付上下文
com.example.payment
├── domain
│   ├── model
│   │   ├── Payment
│   │   ├── PaymentMethod
│   │   └── ...
// 用户上下文
com.example.user
├── domain
│   ├── model
│   │   ├── User
│   │   ├── Role
│   │   └── ...
```

## 包命名规范
- 领域对象所在包：`com.example.{context}.domain.model`
- 领域服务所在包：`com.example.{context}.domain.service`
- 仓储接口所在包：`com.example.{context}.domain.repository`
- 应用服务所在包：`com.example.{context}.application.service`
- 接口控制器所在包：`com.example.{context}.facade.rest`
- 仓储实现所在包：`com.example.{context}.infrastructure.repository`

## 分层依赖规则
- 领域层：不依赖其他层
- 应用层：依赖领域层
- 接口层：依赖应用层
- 基础设施层：依赖领域层(实现接口)，可能依赖应用层

```java
// 依赖规则示例
@Component
public class OrderApplicationService {
    // 只依赖领域层的接口
    private final OrderRepository orderRepository;  // 领域仓储接口
    private final OrderFactory orderFactory;        // 领域工厂
    
    // 不直接依赖基础设施层的实现
}

@Repository
public class JpaOrderRepository implements OrderRepository {
    // 实现领域层接口，只在基础设施层中
    private final OrderJpaRepository orderJpaRepository;  // Spring Data JPA接口
}
```

## 模块化策略
- 按照边界上下文划分模块
- 模块内部遵循分层架构
- 模块间通过接口或事件通信
- 模块应具有高内聚低耦合特性

```
// 模块结构示例
order-service/
├── order-domain/        # 订单领域模块
├── order-application/   # 订单应用模块
├── order-facade/        # 订单接口模块
├── order-infrastructure/ # 订单基础设施模块
└── pom.xml

payment-service/
├── payment-domain/      # 支付领域模块
├── payment-application/ # 支付应用模块
...
```

## 事件驱动架构
- 使用领域事件实现业务流程协调
- 基础设施层实现事件发布-订阅机制
- 跨边界上下文通信优先使用事件
- 实现最终一致性的数据同步

```java
// 领域事件发布
@Component
public class DomainEventPublisherImpl implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

// 事件监听器
@Component
@Slf4j
public class OrderCreatedEventListener {
    
    private final InventoryService inventoryService;
    
    @EventListener
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("订单创建事件接收: {}", event.getOrderId());
        // 处理库存锁定
        inventoryService.lockInventory(event.getOrderId());
    }
}
```

## 领域驱动设计战术模式
- **实体**: 具有唯一标识和生命周期的对象
- **值对象**: 不可变对象，没有唯一标识
- **聚合根**: 控制对聚合内部实体的访问
- **领域服务**: 无状态的领域逻辑
- **工厂**: 创建复杂领域对象
- **仓储**: 持久化和检索聚合
- **领域事件**: 领域中发生的重要事情

## 代码风格与惯例
- 领域类名与现实业务概念一致
- 贫血模型 vs 充血模型：优先使用充血模型
- 方法命名应体现业务意图，避免技术语言
- 边界上下文内使用一致的术语
- 遵循单一职责原则和开闭原则

```java
// 好的示例 - 充血模型
public class Order {
    private OrderStatus status;
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new OrderCannotBeConfirmedException();
        }
        this.status = OrderStatus.CONFIRMED;
        // 发布领域事件
    }
}

// 避免 - 贫血模型
public class Order {
    private OrderStatus status;
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

// 在服务中处理业务逻辑
public class OrderService {
    public void confirmOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderCannotBeConfirmedException();
        }
        order.setStatus(OrderStatus.CONFIRMED);
    }
}
```

## 测试策略
- 领域层：单元测试领域逻辑
- 应用层：单元测试应用服务，模拟领域服务
- 接口层：集成测试REST API
- 端到端测试：验证业务流程

```java
// 领域层测试
@Test
void shouldConfirmPendingOrder() {
    // Given
    Order order = Order.create(CUSTOMER_ID, ITEMS);
    
    // When
    order.confirm();
    
    // Then
    assertEquals(OrderStatus.CONFIRMED, order.getStatus());
}

// 应用层测试
@Test
void shouldCreateOrderAndReturnDTO() {
    // Given
    CreateOrderCommand command = new CreateOrderCommand(CUSTOMER_ID, ITEMS);
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    
    // When
    OrderDTO result = orderApplicationService.createOrder(command);
    
    // Then
    assertNotNull(result);
    assertEquals(ORDER_ID, result.getId());
    verify(orderRepository).save(any(Order.class));
    verify(eventPublisher).publish(any(OrderCreatedEvent.class));
}
```

## 领域特定语言(DSL)
- 使用Builder模式创建流畅的领域API
- 领域语言应接近业务语言
- 隐藏技术细节，突出业务概念
- 在测试中使用可读性高的DSL

```java
// 领域DSL示例
Order order = OrderBuilder.create()
    .forCustomer(customerId)
    .withItem(productId, 2)
    .withItem(anotherProductId, 1)
    .withShippingAddress(address)
    .withPaymentMethod(paymentMethod)
    .build();
    
// 测试DSL示例
@Test
void shouldCalculateCorrectTotal() {
    // Given
    Order order = OrderFixture.pendingOrder()
        .withItem().product("iPhone").quantity(1).price(Money.of(999)).add()
        .withItem().product("Case").quantity(2).price(Money.of(29.99)).add()
        .build();
    
    // When/Then
    assertEquals(Money.of(1058.98), order.getTotalAmount());
}
```

## 反模式与常见错误
- 贫血领域模型：实体仅包含状态，行为在服务中
- 模糊的领域概念：命名不清或与业务不符
- 持久化泄漏：领域模型受ORM框架影响过大
- 过大的聚合：聚合包含过多实体导致性能和一致性问题
- 缺乏边界上下文：所有领域概念混杂在一起

## 最佳实践
- 领域模型与业务语言一致，建立通用语言
- 保持聚合的小规模和明确边界
- 使用工厂方法创建复杂的聚合
- 实体实现业务规则和验证逻辑
- 使用值对象表示不需要唯一标识的概念
- 边界上下文之间通过上下文映射明确关系 