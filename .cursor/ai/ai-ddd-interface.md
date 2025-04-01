# DDD接口层定义规范

## REST控制器(REST Controller)
- 接口层负责处理HTTP请求和返回响应
- 将请求参数转换为命令或查询对象
- 调用应用服务处理业务逻辑
- 处理认证、授权、请求验证

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {
    
    private final OrderApplicationService orderApplicationService;
    private final OrderQueryApplicationService orderQueryApplicationService;
    
    public OrderController(OrderApplicationService orderApplicationService,
                        OrderQueryApplicationService orderQueryApplicationService) {
        this.orderApplicationService = orderApplicationService;
        this.orderQueryApplicationService = orderQueryApplicationService;
    }
    
    @PostMapping
    @Operation(summary = "创建订单", description = "创建新订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "订单创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        // 请求转换为命令对象
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(userId)
                .items(mapToOrderLineItemDTOs(request.getItems()))
                .build();
        
        // 调用应用服务
        OrderDTO orderDTO = orderApplicationService.createOrder(command);
        
        // 构造响应
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderDTO));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详情")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable String orderId) {
        OrderDTO orderDTO = orderApplicationService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(orderDTO));
    }
    
    @PutMapping("/{orderId}/confirm")
    @Operation(summary = "确认订单", description = "将订单状态更改为确认")
    public ResponseEntity<ApiResponse<Void>> confirmOrder(@PathVariable String orderId) {
        orderApplicationService.confirmOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping
    @Operation(summary = "获取订单列表", description = "根据条件查询订单列表")
    public ResponseEntity<ApiResponse<Page<OrderSummaryDTO>>> getOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        OrderQuery query = OrderQuery.builder()
                .customerId(customerId)
                .status(status)
                .page(page)
                .size(size)
                .build();
                
        Page<OrderSummaryDTO> orders = orderQueryApplicationService.getOrderList(query);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    private List<OrderLineItemDTO> mapToOrderLineItemDTOs(List<OrderLineItemRequest> items) {
        return items.stream()
                .map(item -> OrderLineItemDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
    }
}
```

## 请求/响应模型(Request/Response Model)
- 定义接口层的请求和响应数据模型
- 包含输入验证注解
- 与领域模型和应用层DTO解耦
- 适配前端所需的数据结构

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderLineItemRequest> items;
    
    private String couponCode;
    
    private AddressRequest shippingAddress;
}

@Data
@Builder
public class OrderLineItemRequest {
    
    @NotNull(message = "产品ID不能为空")
    private Long productId;
    
    @Min(value = 1, message = "数量最小为1")
    private int quantity;
    
    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;
}

@Data
@Builder
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    
    private static final String SUCCESS_CODE = "200";
    private static final String SUCCESS_MESSAGE = "操作成功";
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
```

## WebFlux响应式接口(对于响应式应用)
- 使用Spring WebFlux实现响应式接口
- 返回`Mono`或`Flux`类型
- 使用响应式编程处理请求
- 适用于高并发、低延迟场景

```java
@RestController
@RequestMapping("/api/v1/reactive/orders")
public class ReactiveOrderController {
    
    private final ReactiveOrderApplicationService orderService;
    
    @GetMapping
    public Flux<OrderDTO> getOrders(@RequestParam(required = false) Long customerId) {
        if (customerId != null) {
            return orderService.findByCustomerId(customerId);
        }
        return orderService.findAll();
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrderDTO>> getOrder(@PathVariable String id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDTO> createOrder(@Valid @RequestBody Mono<CreateOrderRequest> request) {
        return request
                .map(this::mapToCommand)
                .flatMap(orderService::createOrder);
    }
}
```

## 认证和授权(Authentication & Authorization)
- 实现JWT或OAuth2认证
- 使用Spring Security进行授权控制
- 定义细粒度的权限控制
- 处理认证与业务逻辑分离

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers("/api/v1/orders/**").hasRole("USER")
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .apply(new JwtConfigurer(jwtTokenProvider));
        
        return http.build();
    }
}
```

## 异常处理与接口层规范
- 集中处理异常转换为HTTP响应
- 统一的错误响应格式
- 不同类型异常映射到对应HTTP状态码
- 详细的错误信息和错误代码

```java
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(OrderNotFoundException ex) {
        ApiResponse<Void> response = ApiResponse.error("ORDER_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(OrderNotEditableException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotEditable(OrderNotEditableException ex) {
        ApiResponse<Void> response = ApiResponse.error("ORDER_NOT_EDITABLE", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        ApiResponse<Map<String, String>> response = ApiResponse.error("VALIDATION_FAILED", "请求参数验证失败");
        response.setData(errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```

## 接口文档(API Documentation)
- 使用SpringDoc OpenAPI 3生成接口文档
- 详细描述每个接口的用途、参数和响应
- 提供示例请求和响应
- 文档版本与API版本保持一致

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("订单管理API")
                        .version("1.0")
                        .description("订单管理系统API文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("team@example.com")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
```

## 接口层与CQRS模式
- 分离命令控制器和查询控制器
- 命令接口通过POST、PUT、DELETE等修改数据
- 查询接口通过GET方法获取数据
- 命令接口和查询接口可使用不同的模型

```java
@RestController
@RequestMapping("/api/v1/orders/commands")
public class OrderCommandController {
    
    private final OrderApplicationService orderApplicationService;
    
    // 处理创建、更新、删除等命令
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@RequestBody CreateOrderRequest request) {
        // 实现创建订单命令
    }
    
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmOrder(@PathVariable String orderId) {
        // 实现确认订单命令
    }
}

@RestController
@RequestMapping("/api/v1/orders/queries")
public class OrderQueryController {
    
    private final OrderQueryApplicationService orderQueryService;
    
    // 处理各种查询操作
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable String orderId) {
        // 实现查询订单
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryDTO>>> searchOrders(OrderSearchRequest request) {
        // 实现搜索订单
    }
}
```

## 最佳实践
- 保持接口简洁明了，遵循REST原则
- 实现一致的错误处理和响应格式
- 使用统一的请求参数验证机制
- 接口层只负责数据转换和请求处理，不包含业务逻辑
- 提供详细的API文档和使用示例
- 使用命名约定统一表达概念，与领域层和应用层保持一致性 