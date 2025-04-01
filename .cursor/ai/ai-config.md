# 配置管理规范

- 使用Spring Boot的配置机制管理应用配置
- 配置文件按环境分离：application.yml, application-dev.yml, application-prod.yml
- 敏感配置（密码、密钥等）不应硬编码在配置文件中
- 使用环境变量或外部配置服务存储敏感信息
- 配置变更应有审计记录

## 配置文件结构
- 配置文件应按功能模块组织，保持结构清晰
- 标准配置文件结构示例：
  ```yaml
  spring:
    application:
      name: service-name
    
    # 数据源配置
    datasource:
      url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:mydb}
      username: ${DB_USER:root}
      password: ${DB_PASSWORD:password}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
    
    # JPA配置
    jpa:
      hibernate:
        ddl-auto: none
      properties:
        hibernate:
          dialect: org.hibernate.dialect.MySQL8Dialect
    
    # 缓存配置
    cache:
      type: redis
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
    
    # 安全配置
    security:
      jwt:
        secret-key: ${JWT_SECRET:default-dev-only-secret}
        expiration: 86400000  # 24小时
    
  # 应用自定义配置
  app:
    feature-flags:
      new-ui-enabled: true
    cors:
      allowed-origins: http://localhost:3000,https://example.com
    upload:
      max-file-size: 10MB
      allowed-types: jpg,png,pdf
  
  # 服务器配置
  server:
    port: ${SERVER_PORT:8080}
    servlet:
      context-path: /api
  
  # 日志配置
  logging:
    level:
      root: INFO
      com.example: DEBUG
      org.hibernate.SQL: DEBUG
  ```

## 环境变量使用规范
- 环境变量命名规范：大写字母，下划线分隔，如`DB_PASSWORD`
- 在配置文件中使用`${ENV_VAR:default-value}`格式引用环境变量
- 本地开发使用`.env`文件管理环境变量，但不提交到代码仓库
- 生产环境通过容器编排工具（Kubernetes）管理环境变量

## 配置加密
- 使用Jasypt等工具加密敏感配置
- 加密配置示例：
  ```yaml
  spring:
    datasource:
      password: ENC(encrypted-password-string)
  
  jasypt:
    encryptor:
      password: ${JASYPT_PASSWORD}
      algorithm: PBEWithMD5AndDES
  ```
- 加密密钥通过环境变量传入，不存储在代码或配置文件中

## 配置类设计
```java
@Configuration
@ConfigurationProperties(prefix = "app.feature-flags")
@Validated
@Data
public class FeatureFlagsConfig {
    
    private boolean newUiEnabled;
    private boolean betaFeaturesEnabled;
    
    // 可以添加自定义验证逻辑
    @PostConstruct
    public void validate() {
        // 验证配置有效性
    }
}

@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Data
public class CorsConfig {
    
    @NotEmpty(message = "至少需要配置一个允许的源")
    private List<String> allowedOrigins;
    
    private List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE");
    
    private boolean allowCredentials = true;
    
    private long maxAge = 3600;
}
```

## 配置刷新机制
- 使用Spring Cloud Config实现配置中心化管理
- 通过`@RefreshScope`注解支持配置热更新
- 实现配置变更通知机制，如Webhook或消息队列
- 配置变更应记录审计日志

## 常见错误
- 敏感信息硬编码在配置文件中
- 不同环境使用相同的配置值
- 配置过于分散，难以管理
- 缺少配置文档说明

## 最佳实践
- 使用配置服务器集中管理配置（Spring Cloud Config）
- 实现配置健康检查，验证配置完整性
- 为所有配置提供默认值，增强应用健壮性
- 使用配置元数据提供IDE自动完成支持
- 定期审查配置，移除未使用的配置项 