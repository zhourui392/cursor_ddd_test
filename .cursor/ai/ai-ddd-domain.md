# DDD领域层定义规范

## 领域实体(Entity)
- 领域实体必须包含唯一标识符(ID)
- 实体类应实现业务行为和验证规则
- 使用值对象表示无需唯一标识的概念
- 保持实体的不变性规则和业务约束
- 领域实体应纯粹关注业务逻辑，不包含持久化注解
- 使用Lombok的@Getter/@Setter注解自动生成访问器方法
- 推荐使用@Getter和必要的特定@Setter，避免过度暴露可变性

```java
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

@Getter
@AllArgsConstructor
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private Money totalAmount;
    private OrderStatus status;
    private List<OrderLineItem> lineItems = new ArrayList<>();
    
    // 通过工厂方法创建
    public static Order create(CustomerId customerId, List<OrderLineItem> items) {
        Order order = new Order();
        order.id = new OrderId(UUID.randomUUID());
        order.customerId = customerId;
        order.status = OrderStatus.PENDING;
        order.lineItems.addAll(items);
        order.recalculateTotal();
        return order;
    }
    
    // 业务行为
    public void addItem(OrderLineItem item) {
        validateOrderIsEditable();
        lineItems.add(item);
        recalculateTotal();
    }
    
    public void confirmOrder() {
        validateOrderIsConfirmable();
        status = OrderStatus.CONFIRMED;
        // 生成订单确认事件
        DomainEventPublisher.publishEvent(new OrderConfirmedEvent(this.id));
    }
    
    // 业务规则验证
    private void validateOrderIsEditable() {
        if (status != OrderStatus.PENDING) {
            throw new OrderNotEditableException(id);
        }
    }
    
    private void validateOrderIsConfirmable() {
        if (status != OrderStatus.PENDING) {
            throw new OrderNotConfirmableException(id);
        }
        if (lineItems.isEmpty()) {
            throw new EmptyOrderException(id);
        }
    }
    
    private void recalculateTotal() {
        this.totalAmount = lineItems.stream()
            .map(OrderLineItem::getTotal)
            .reduce(Money.ZERO, Money::add);
    }
    
    // 对于不应暴露的可变集合，提供不可变视图
    public List<OrderLineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }
}
```

## 值对象(Value Object)
- 值对象是无需唯一标识的不可变对象
- 相同属性的值对象被视为相等
- 实现`equals()`, `hashCode()`方法确保值相等性比较
- 不包含持久化相关注解
- 使用Lombok的@Value注解创建不可变值对象

```java
import lombok.Value;

@Value
public class Money {
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    
    BigDecimal amount;
    
    public Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(new BigDecimal(quantity)));
    }
    
    // 其他货币运算方法
}

// ID值对象示例
@Value
public class OrderId {
    UUID id;
    
    public OrderId(UUID id) {
        this.id = Objects.requireNonNull(id, "ID不能为空");
    }
    
    public UUID getValue() {
        return id;
    }
    
    @Override
    public String toString() {
        return id.toString();
    }
}
```

## 聚合根(Aggregate Root)
- 定义聚合边界和一致性规则
- 只能通过聚合根访问内部实体
- 聚合应尽量保持小规模
- 聚合之间使用ID引用

```java
// Order类既是实体也是聚合根
```

## 领域服务(Domain Service)
- 不属于任何实体的领域操作
- 处理多个聚合之间的操作
- 无状态，不持有数据
- 命名应反映业务概念和行为

```java
public class OrderDiscountService {
    
    public Money calculateDiscount(Order order, DiscountPolicy policy) {
        // 根据订单信息和折扣策略计算折扣
        if (policy == DiscountPolicy.VOLUME_BASED) {
            return calculateVolumeBasedDiscount(order);
        } else if (policy == DiscountPolicy.LOYALTY_BASED) {
            return calculateLoyaltyBasedDiscount(order);
        }
        return Money.ZERO;
    }
    
    private Money calculateVolumeBasedDiscount(Order order) {
        // 基于订单量的折扣逻辑
        if (order.getLineItems().size() > 10) {
            return order.getTotalAmount().multiply(0.05);
        }
        return Money.ZERO;
    }
    
    private Money calculateLoyaltyBasedDiscount(Order order) {
        // 基于客户忠诚度的折扣逻辑
        return Money.ZERO;
    }
}
```

## 领域事件(Domain Event)
- 表示领域中发生的重要事件
- 命名使用过去时态(OrderConfirmed)
- 包含事件发生时的相关数据
- 使用发布-订阅模式传播
- 使用Lombok的@Getter注解简化访问器方法定义

```java
import lombok.Getter;

public interface DomainEvent {
    LocalDateTime getOccurredOn();
}

@Getter
public class OrderConfirmedEvent implements DomainEvent {
    private final OrderId orderId;
    private final LocalDateTime occurredOn;
    
    public OrderConfirmedEvent(OrderId orderId) {
        this.orderId = orderId;
        this.occurredOn = LocalDateTime.now();
    }
}
```

## 领域事件发布器(Domain Event Publisher)
- 提供发布领域事件的接口
- 使领域对象能够产生事件而不依赖具体实现
- 基础设施层提供实现

```java
public interface DomainEventPublisher {
    void publish(DomainEvent event);
    
    // 静态访问方式，由基础设施层提供实现
    static void publishEvent(DomainEvent event) {
        // 此静态方法将由基础设施层的实现类覆盖
        // 适配领域模型调用方式
        throw new UnsupportedOperationException("领域事件发布器未初始化");
    }
}
```

## 仓储接口(Repository Interface)
- 在领域层定义仓储接口
- 方法名反映领域概念
- 只操作聚合根
- 保持简单，隐藏持久化细节

```java
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
    void remove(Order order);
}
```

## 领域异常
- 针对特定领域规则违反创建异常类
- 异常应有明确的业务含义
- 命名应体现业务概念(OrderNotEditableException)

```java
public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

public class OrderNotEditableException extends DomainException {
    public OrderNotEditableException(OrderId orderId) {
        super("Order " + orderId.getValue() + " is not in editable state");
    }
}
```

## 最佳实践
- 使用丰富的领域模型，避免贫血模型
- 业务逻辑应位于实体和值对象中
- 使用工厂方法创建复杂对象
- 使用领域事件实现模块间通信
- 保持领域模型与持久化模型的完全分离
- 领域对象不应依赖任何基础设施组件
- 统一的命名约定确保概念的一致性 