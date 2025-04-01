# 异常处理机制

- 统一使用`@ControllerAdvice`和`@ExceptionHandler`处理全局异常。
- 定义业务异常基类`BusinessException`，业务相关异常继承该类。
- 为不同的HTTP错误（400、401、403、404、500）分别定义标准返回结构。
- 异常信息统一返回给客户端时应清晰明确，避免泄漏敏感信息。

## 异常体系设计
- 异常基类层次结构：
  ```
  BaseException (运行时异常)
    ├── BusinessException (业务逻辑异常)
    │     ├── ResourceNotFoundException
    │     ├── InvalidParameterException
    │     ├── DuplicateResourceException
    │     └── 其他业务异常...
    └── SystemException (系统级异常)
          ├── DatabaseException
          ├── RemoteServiceException
          ├── CacheException
          └── 其他系统异常...
  ```
- 每个异常类必须包含错误码、错误消息和HTTP状态码

## 全局异常处理
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(SystemException ex) {
        log.error("System exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getErrorCode(), "系统内部错误，请稍后重试"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "参数校验失败", errors));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系统内部错误，请稍后重试"));
    }
}
```

## 业务异常示例
```java
@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST);
    }
}

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            "RESOURCE_NOT_FOUND",
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.NOT_FOUND
        );
    }
}
```

## 常见错误
- 直接抛出原始异常（如SQLException）到Controller层
- 异常信息中包含敏感信息（如SQL语句、密码等）
- 未记录异常日志或日志级别不合理
- 异常处理不统一，导致客户端收到不一致的错误格式

## 最佳实践
- 在Service层捕获并转换底层异常为业务异常
- 异常信息国际化，支持多语言错误消息
- 关键操作异常通知（邮件、短信等）
- 使用MDC记录请求上下文，便于异常追踪
- 定期分析异常日志，优化系统稳定性 