# DDD基础设施层定义规范

## 数据对象(Data Object)
- 数据对象(DO)负责数据库映射
- 包含所有MyBatis Plus相关注解
- 纯粹的数据容器，不包含业务逻辑
- 与领域模型完全分离

```java
@Data
@TableName("orders")
@KeySequence("seq_order")
public class OrderDO {
    
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    
    @TableField(value = "uuid", insertStrategy = FieldStrategy.NOT_NULL)
    private String uuid;
    
    @TableField("customer_id")
    private Long customerId;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("status")
    private String status;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // MyBatis Plus不直接支持一对多关系
    // 需要在Service层手动处理关联数据
    @TableField(exist = false)
    private List<OrderLineItemDO> items = new ArrayList<>();
}

@Data
@TableName("order_items")
@KeySequence("seq_order_item")
public class OrderLineItemDO {
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    
    @TableField("order_id")
    private Long orderId;
    
    @TableField("product_id")
    private Long productId;
    
    @TableField("quantity")
    private Integer quantity;
    
    @TableField("unit_price")
    private BigDecimal unitPrice;
    
    @TableField("total_price")
    private BigDecimal totalPrice;
}
```

## MapStruct对象映射器
- 使用MapStruct自动生成DO和领域对象之间的映射
- 在基础设施层中实现领域模型与DO的转换
- 保持映射代码简洁、声明式
- 处理复杂对象和集合映射
- 处理可能的null值情况

```java
@Mapper(componentModel = "spring", uses = {OrderLineItemConvert.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderConvert {
    
    @Mapping(target = "id", expression = "java(source.getUuid() != null ? new OrderId(UUID.fromString(source.getUuid())) : null)")
    @Mapping(target = "customerId", expression = "java(new CustomerId(source.getCustomerId()))")
    @Mapping(target = "status", expression = "java(OrderStatus.valueOf(source.getStatus()))")
    @Mapping(target = "totalAmount", expression = "java(new Money(source.getTotalAmount()))")
    Order toDomain(OrderDO source);
    
    @InheritInverseConfiguration
    @Mapping(target = "uuid", expression = "java(source.getId().getValue().toString())")
    OrderDO toData(Order source);
    
    List<Order> toDomainList(List<OrderDO> source);
    List<OrderDO> toDataList(List<Order> source);
}

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderLineItemConvert {
    
    @Mapping(target = "productId", expression = "java(new ProductId(source.getProductId()))")
    @Mapping(target = "unitPrice", expression = "java(new Money(source.getUnitPrice()))")
    @Mapping(target = "totalPrice", expression = "java(new Money(source.getTotalPrice()))")
    OrderLineItem toDomain(OrderLineItemDO source);
    
    @InheritInverseConfiguration
    OrderLineItemDO toData(OrderLineItem source);
    
    List<OrderLineItem> toDomainList(List<OrderLineItemDO> source);
    List<OrderLineItemDO> toDataList(List<OrderLineItem> source);
}
```

## 仓储实现(Repository Implementation)
- 实现领域层定义的仓储接口
- 处理数据持久化和查询的细节
- 使用MapStruct进行DO和领域模型的相互转换
- 隐藏数据访问技术细节

```java
@Repository
public class MybatisOrderRepository implements OrderRepository {
    
    private final OrderMapper orderMapper;
    private final OrderLineItemMapper orderLineItemMapper;
    private final OrderConvert orderConvert;
    
    public MybatisOrderRepository(OrderMapper orderMapper, 
                           OrderLineItemMapper orderLineItemMapper,
                           OrderConvert orderConvert) {
        this.orderMapper = orderMapper;
        this.orderLineItemMapper = orderLineItemMapper;
        this.orderConvert = orderConvert;
    }
    
    @Override
    @Transactional
    public void save(Order order) {
        OrderDO orderDO = orderConvert.toData(order);
        
        if (orderDO.getId() == null) {
            orderMapper.insert(orderDO);
        } else {
            orderMapper.updateById(orderDO);
            // 处理子项，先删除再重新添加
            LambdaQueryWrapper<OrderLineItemDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderLineItemDO::getOrderId, orderDO.getId());
            orderLineItemMapper.delete(queryWrapper);
        }
        
        // 保存订单项
        if (order.getLineItems() != null && !order.getLineItems().isEmpty()) {
            List<OrderLineItemDO> items = orderConvert.toData(order).getItems();
            for (OrderLineItemDO item : items) {
                item.setOrderId(orderDO.getId());
                orderLineItemMapper.insert(item);
            }
        }
    }
    
    @Override
    public Optional<Order> findById(OrderId id) {
        // 查询订单基本信息
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getUuid, id.getValue().toString());
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        
        if (orderDO == null) {
            return Optional.empty();
        }
        
        // 查询订单项
        LambdaQueryWrapper<OrderLineItemDO> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OrderLineItemDO::getOrderId, orderDO.getId());
        List<OrderLineItemDO> items = orderLineItemMapper.selectList(itemQueryWrapper);
        orderDO.setItems(items);
        
        return Optional.of(orderConvert.toDomain(orderDO));
    }
    
    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getCustomerId, customerId.getValue());
        List<OrderDO> orderDOs = orderMapper.selectList(queryWrapper);
        
        // 填充订单项
        orderDOs.forEach(orderDO -> {
            LambdaQueryWrapper<OrderLineItemDO> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OrderLineItemDO::getOrderId, orderDO.getId());
            List<OrderLineItemDO> items = orderLineItemMapper.selectList(itemQueryWrapper);
            orderDO.setItems(items);
        });
        
        return orderConvert.toDomainList(orderDOs);
    }
    
    @Override
    @Transactional
    public void remove(Order order) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getUuid, order.getId().getValue().toString());
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        
        if (orderDO != null) {
            // 先删除订单项
            LambdaQueryWrapper<OrderLineItemDO> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OrderLineItemDO::getOrderId, orderDO.getId());
            orderLineItemMapper.delete(itemQueryWrapper);
            
            // 再删除订单
            orderMapper.deleteById(orderDO.getId());
        }
    }
}
```

## MyBatis Plus Mapper
- 创建MyBatis Plus Mapper接口操作DO
- 提供基础CRUD和自定义查询方法
- 仅在基础设施层内部使用，不对外暴露

```java
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {
    // 可以添加自定义查询方法
    @Select("SELECT * FROM orders WHERE customer_id = #{customerId} AND status = #{status}")
    List<OrderDO> findByCustomerIdAndStatus(Long customerId, String status);
}

@Mapper
public interface OrderLineItemMapper extends BaseMapper<OrderLineItemDO> {
    // 基础CRUD方法由BaseMapper提供
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderLineItemDO> findByOrderId(@Param("orderId") Long orderId);
}
```

## 领域事件发布者(Domain Event Publisher)
- 实现领域事件的发布机制
- 支持事件订阅和处理
- 提供事务边界内的事件发布
- 提供静态访问方式兼容领域层调用

```java
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    // 提供静态访问实例，供领域层使用
    private static SpringDomainEventPublisher instance;
    
    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        // 设置静态实例
        SpringDomainEventPublisher.instance = this;
    }
    
    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
    
    // 静态方法，供领域层调用
    public static void publishEvent(DomainEvent event) {
        if (instance == null) {
            throw new IllegalStateException("DomainEventPublisher has not been initialized");
        }
        instance.publish(event);
    }
}
```

## 查询实现(Query Implementation)
- 实现CQRS模式中的查询端
- 直接从数据库查询视图数据
- 返回专用于视图的数据结构
- 可使用缓存提高性能
- 明确与应用层查询服务的职责区分

```java
@Service
public class OrderQueryService {
    
    private final OrderMapper orderMapper;
    private final OrderLineItemMapper orderLineItemMapper;
    private final CacheManager cacheManager;
    
    // 用于复杂查询和报表等读取优化场景
    @Cacheable(value = "orderSummaries", key = "#customerId")
    public List<OrderSummaryDTO> getOrderSummaries(Long customerId) {
        // 使用自定义查询
        List<OrderDO> orders = orderMapper.findByCustomerIdAndStatus(customerId, "CONFIRMED");
        
        // 转换为DTO
        return orders.stream()
            .map(orderDO -> {
                OrderSummaryDTO dto = new OrderSummaryDTO();
                dto.setOrderId(orderDO.getId());
                dto.setOrderDate(orderDO.getCreatedAt());
                dto.setStatus(orderDO.getStatus());
                dto.setTotalAmount(orderDO.getTotalAmount());
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    // 用于需要优化性能的复杂视图查询
    public OrderDetailDTO getOrderDetail(String orderUuid) {
        // 查询订单基本信息
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getUuid, orderUuid);
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        
        if (orderDO == null) {
            return null;
        }
        
        // 查询订单项
        LambdaQueryWrapper<OrderLineItemDO> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OrderLineItemDO::getOrderId, orderDO.getId());
        List<OrderLineItemDO> items = orderLineItemMapper.selectList(itemQueryWrapper);
        orderDO.setItems(items);
        
        // 转换为DTO
        return OrderDetailConvert.INSTANCE.toDetailDTO(orderDO);
    }
    
    // 用于报表和统计查询
    public OrderStatisticsDTO getOrderStatistics(LocalDate fromDate, LocalDate toDate) {
        // 使用MyBatis Plus提供的条件构造器进行复杂查询
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(OrderDO::getCreatedAt, 
                           fromDate.atStartOfDay(), 
                           toDate.plusDays(1).atStartOfDay());
        
        // 统计查询实现
        List<OrderDO> orders = orderMapper.selectList(queryWrapper);
        
        // 计算统计指标
        OrderStatisticsDTO statistics = new OrderStatisticsDTO();
        // ... 计算统计数据
        return statistics;
    }
}
```

## 最佳实践
- 保持基础设施与领域模型的隔离
- 使用依赖注入和抽象接口解耦
- 为所有外部系统集成提供测试替身
- 实现详细的监控和日志记录
- 使用对象映射框架(MapStruct)保持领域模型与DO的转换简洁高效
- 确保DO只存在于基础设施层，不泄露到其他层
- 明确查询服务与应用服务的职责划分：查询服务处理复杂报表和视图优化 