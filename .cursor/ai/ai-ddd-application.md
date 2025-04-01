# DDD应用层定义规范

## 应用服务(Application Service)
- 应用服务协调领域对象完成用例
- 不包含业务逻辑，业务逻辑应在领域层实现
- 负责事务管理、安全和日志记录
- 处理应用关注点，如参数验证、结果转换

```java
@Service
@Transactional
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderFactory orderFactory;
    private final DomainEventPublisher eventPublisher;
    private final OrderDTOMapper orderDTOMapper;
    
    public OrderApplicationService(OrderRepository orderRepository,
                                 CustomerRepository customerRepository,
                                 OrderFactory orderFactory,
                                 DomainEventPublisher eventPublisher,
                                 OrderDTOMapper orderDTOMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderFactory = orderFactory;
        this.eventPublisher = eventPublisher;
        this.orderDTOMapper = orderDTOMapper;
    }
    
    public OrderDTO createOrder(CreateOrderCommand command) {
        // 验证命令
        validateCustomerExists(command.getCustomerId());
        
        // 使用领域工厂创建领域对象
        CustomerId customerId = new CustomerId(command.getCustomerId());
        List<OrderLineItem> items = orderDTOMapper.toOrderLineItems(command.getItems());
        Order order = orderFactory.createOrder(customerId, items);
        
        // 持久化
        orderRepository.save(order);
        
        // 发布应用事件
        eventPublisher.publish(new OrderCreatedEvent(order.getId().getValue().toString()));
        
        // 返回DTO
        return orderDTOMapper.toDTO(order);
    }
    
    public void confirmOrder(String orderUuid) {
        OrderId orderId = new OrderId(UUID.fromString(orderUuid));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderUuid));
            
        // 使用领域层事件发布方式
        // 执行领域操作
        order.confirmOrder();
        
        // 持久化变更
        orderRepository.save(order);
    }
    
    @Transactional(readOnly = true)
    public OrderDTO getOrder(String orderUuid) {
        OrderId orderId = new OrderId(UUID.fromString(orderUuid));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderUuid));
        
        return orderDTOMapper.toDTO(order);
    }
    
    private void validateCustomerExists(Long customerId) {
        if (!customerRepository.existsById(new CustomerId(customerId))) {
            throw new CustomerNotFoundException(customerId);
        }
    }
}
```

## MapStruct DTO映射器
- 使用MapStruct实现领域对象与DTO之间的映射
- 确保视图层与领域模型的分离
- 减少手动编写繁琐的转换代码
- 处理复杂对象图的映射
- 统一命名约定与领域模型保持一致

```java
@Mapper(componentModel = "spring", uses = {OrderLineItemDTOMapper.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderDTOMapper {
    
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "customerId.value", target = "customerId")
    @Mapping(source = "status", target = "orderStatus")
    @Mapping(source = "totalAmount.amount", target = "totalAmount")
    OrderDTO toDTO(Order order);
    
    List<OrderDTO> toDTOList(List<Order> orders);
    
    @Mapping(target = "productId", expression = "java(new ProductId(item.getProductId()))")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", expression = "java(new Money(item.getUnitPrice()))")
    OrderLineItem toOrderLineItem(OrderLineItemDTO item);
    
    List<OrderLineItem> toOrderLineItems(List<OrderLineItemDTO> items);
}

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderLineItemDTOMapper {
    
    @Mapping(source = "productId.value", target = "productId")
    @Mapping(source = "unitPrice.amount", target = "unitPrice")
    @Mapping(source = "totalPrice.amount", target = "totalPrice")
    OrderLineItemDTO toDTO(OrderLineItem item);
    
    List<OrderLineItemDTO> toDTOList(List<OrderLineItem> items);
}
```

## 命令(Command)和查询(Query)对象
- 使用命令对象表示修改操作
- 使用查询对象表示查询操作
- 遵循命令查询责任分离(CQRS)原则
- 命令和查询对象应是不可变的

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {
    
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderLineItemDTO> items;
}

@Data
@Builder
public class OrderQuery {
    private Long customerId;
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
}
```

## 数据传输对象(DTO)
- 用于应用层与表示层之间的数据传输
- 隐藏领域模型细节
- 可根据视图需求定制
- 纯数据结构，不包含业务逻辑
- 命名与领域对象保持一致性

```java
@Data
@Builder
public class OrderDTO {
    private String id;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDateTime createdAt;
    private List<OrderLineItemDTO> items;
}

@Data
@Builder
public class OrderLineItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
```

## 应用事件(Application Event)
- 记录应用操作的事件
- 用于跨界限上下文的集成
- 区别于领域事件，应用事件关注应用操作

```java
public class OrderCreatedEvent {
    private final String orderId;
    private final LocalDateTime occurredOn;
    
    public OrderCreatedEvent(String orderId) {
        this.orderId = orderId;
        this.occurredOn = LocalDateTime.now();
    }
    
    // getter方法
    public String getOrderId() {
        return orderId;
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
```

## 集成服务(Integration Service)
- 协调多个有界上下文之间的交互
- 使用防腐层隔离外部系统
- 处理外部系统的数据转换

```java
@Service
public class ShippingIntegrationService {
    
    private final ShippingServiceClient shippingClient;
    private final ShippingDTOMapper shippingMapper;
    
    public ShippingIntegrationService(ShippingServiceClient shippingClient,
                                     ShippingDTOMapper shippingMapper) {
        this.shippingClient = shippingClient;
        this.shippingMapper = shippingMapper;
    }
    
    public void scheduleShipping(Order order) {
        ShippingRequest request = shippingMapper.toRequest(order);
        try {
            ShippingResponse response = shippingClient.createShippingOrder(request);
            // 处理响应
        } catch (ShippingServiceException e) {
            // 处理异常，可能通过重试策略或补偿事务
            throw new ShippingScheduleFailedException(order.getId().getValue().toString(), e);
        }
    }
}

@Mapper(componentModel = "spring")
public interface ShippingDTOMapper {
    @Mapping(source = "id.value", target = "orderUuid")
    @Mapping(source = "customerId.value", target = "customerId")
    ShippingRequest toRequest(Order order);
}
```

## 应用层与CQRS
- 应用服务处理命令/写操作，涉及业务规则验证
- 查询服务处理只读操作，可从基础设施层的查询服务中获取视图数据
- 清晰区分命令处理和查询处理的职责

```java
@Service
public class OrderQueryApplicationService {
    
    private final OrderQueryService orderQueryService;
    private final SecurityService securityService;
    
    // 处理权限检查等应用层关注点，然后委托给基础设施层的查询服务
    public OrderDetailDTO getOrderDetail(String orderUuid) {
        // 执行权限检查
        securityService.checkOrderAccess(orderUuid);
        
        // 委托给基础设施层的查询服务获取数据
        return orderQueryService.getOrderDetail(orderUuid);
    }
    
    // 用于获取订单列表，处理分页等应用层关注点
    public Page<OrderSummaryDTO> getOrderList(OrderQuery query) {
        // 处理分页、权限等
        return orderQueryService.getOrderSummaries(
            query.getCustomerId(),
            PageRequest.of(query.getPage(), query.getSize())
        );
    }
}
```

## 应用层异常处理
- 捕获领域异常并转换为应用异常
- 应用异常应提供清晰的错误信息
- 区分业务异常和技术异常
- 实现一致的异常处理策略

```java
@ControllerAdvice
public class ApplicationExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        ErrorResponse error = new ErrorResponse("BUSINESS_RULE_VIOLATION", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // 其他异常处理
}
```

## 最佳实践
- 保持应用服务轻量，不包含业务逻辑
- 使用事务边界包装领域操作
- 实现命令查询职责分离(CQRS)
- 使用DTO和MapStruct隔离领域模型和表示层
- 应用层异常应转换为对客户端友好的消息
- 使用MapStruct简化对象转换，避免手动映射带来的维护问题
- 明确区分应用层查询服务和基础设施层查询服务的职责 