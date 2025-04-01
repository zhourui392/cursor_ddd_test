# 日志规范

- 使用SLF4J作为日志门面，Logback作为实现
- 日志级别合理使用：ERROR、WARN、INFO、DEBUG、TRACE
- 敏感信息（密码、令牌等）不应记录在日志中
- 异常日志必须包含完整的堆栈信息

## 日志级别使用标准
- **ERROR**：影响系统正常运行的错误，需要立即关注
  - 系统崩溃或无法恢复的错误
  - 数据丢失或损坏
  - 外部系统连接失败且无法恢复
- **WARN**：潜在的问题，但不影响系统正常运行
  - 系统性能下降
  - 重试成功的操作
  - 即将耗尽的资源（连接池、磁盘空间等）
- **INFO**：系统正常运行的重要事件
  - 应用启动和关闭
  - 用户登录和注销
  - 关键业务操作（订单创建、支付等）
  - 定时任务执行
- **DEBUG**：用于问题诊断的详细信息
  - 方法调用参数和返回值
  - SQL语句和执行时间
  - 外部系统交互详情
- **TRACE**：最详细的调试信息，通常仅在开发环境使用


## 敏感信息脱敏
```java
public class SensitiveDataConverter {
    
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }
        int length = cardNumber.length();
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(length - 4, length);
    }
    
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        
        if (name.length() <= 2) {
            return name + "***@" + domain;
        }
        
        return name.substring(0, 2) + "***@" + domain;
    }
    
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return phoneNumber;
        }
        int length = phoneNumber.length();
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(length - 4, length);
    }
}

// 使用示例
log.info("Processing payment for card: {}", SensitiveDataConverter.maskCardNumber(cardNumber));
```

## 日志最佳实践
- 使用参数化日志，避免字符串拼接
  ```java
  // 正确
  log.debug("Processing order {} for user {}", orderId, userId);
  
  // 错误
  log.debug("Processing order " + orderId + " for user " + userId);
  ```
- 在记录异常时包含完整堆栈
  ```java
  try {
      // 业务逻辑
  } catch (Exception e) {
      log.error("Failed to process order: {}", orderId, e);
  }
  ```
- 避免在循环中频繁记录日志
- 使用日志分级，便于问题排查
- 定期归档和清理日志文件

## 常见错误
- 过度记录日志导致性能问题
- 未对敏感信息进行脱敏
- 日志级别使用不当
- 异步线程中MDC上下文丢失
- 日志中缺少关键上下文信息

## 最佳实践
- 在生产环境中默认使用INFO级别
- 实现动态调整日志级别的功能
- 定期审查日志内容，确保无敏感信息泄露
- 使用结构化日志（JSON格式）便于机器处理
- 为关键业务流程添加审计日志 