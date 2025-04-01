# 监控和性能优化

- 服务默认暴露Prometheus格式的监控指标。
- 使用Spring Boot Actuator暴露健康检查接口（`/actuator/health`）。
- 在Controller层使用AOP或Interceptor记录请求处理耗时和请求频率。
- 服务日志输出规范统一，清晰标明异常、耗时与请求路径。
- 使用Grafana进行指标展示，提供标准Dashboard模板。

## 监控指标体系
- 系统级指标：CPU、内存、磁盘、网络
- JVM指标：堆内存、GC、线程数、类加载
- 应用指标：
  - 请求量：QPS、TPS
  - 响应时间：平均、P95、P99
  - 错误率：4xx、5xx比例
  - 业务指标：订单量、用户注册数等
- 依赖服务指标：数据库、缓存、消息队列、第三方API

## 性能优化指南
### 数据库优化
- 使用索引优化查询性能
- 避免N+1查询问题
- 使用分页查询处理大结果集
- 优化SQL语句，避免全表扫描
- 合理设置连接池参数

### JVM调优
- 根据实际需求设置堆内存大小
- 选择合适的GC算法
- 调整GC参数减少停顿时间
- 使用JMX监控JVM运行状态
- 定期分析GC日志

### 缓存策略
- 多级缓存：本地缓存 + 分布式缓存
- 热点数据预加载
- 合理设置缓存过期策略
- 缓存穿透、缓存击穿、缓存雪崩防护

## 监控告警配置
- 设置关键指标阈值告警
- 告警级别：Info、Warning、Critical
- 告警渠道：邮件、短信、企业微信/钉钉
- 告警抑制策略，避免告警风暴
- 设置告警升级机制

## 示例代码
### 自定义监控指标
```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "my-service");
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

@Service
public class OrderService {
    
    private final Counter orderCounter;
    private final Timer orderProcessTimer;
    
    public OrderService(MeterRegistry registry) {
        this.orderCounter = registry.counter("orders.created.count", "type", "new");
        this.orderProcessTimer = registry.timer("orders.process.time");
    }
    
    @Timed(value = "orders.create.time", percentiles = {0.5, 0.95, 0.99})
    public Order createOrder(OrderRequest request) {
        // 业务逻辑
        orderCounter.increment();
        return orderProcessTimer.record(() -> {
            // 处理订单逻辑
            return new Order();
        });
    }
}
```

### 性能分析AOP
```java
@Aspect
@Component
@Slf4j
public class PerformanceMonitorAspect {
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("{}#{} executed in {} ms", className, methodName, executionTime);
            return result;
        } catch (Throwable e) {
            log.error("Exception in {}#{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
```

## 常见性能问题排查
- 内存泄漏：使用JProfiler或MAT分析堆转储
- CPU高负载：使用top、jstack分析热点线程
- 慢SQL：使用慢查询日志、执行计划分析
- 接口超时：使用分布式追踪定位瓶颈
- 连接泄漏：监控连接池使用情况

## 最佳实践
- 实现优雅关闭，确保服务下线不影响用户
- 使用分布式追踪（如Skywalking、Zipkin）跟踪请求链路
- 实现熔断和限流保护系统
- 定期进行压力测试评估系统容量
- 建立性能基线，持续监控性能变化 