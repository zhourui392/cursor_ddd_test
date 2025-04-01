# 权限与认证规范

- 使用Spring Security框架统一管理认证和权限。
- 所有接口默认启用RBAC（角色权限）控制。
- 在Controller方法上使用`@PreAuthorize`或`@Secured`注解明确权限要求。
- 权限命名规范统一，如`ROLE_ADMIN`, `ROLE_USER`, `PERMISSION_ORDER_CREATE`等。
- 提供JWT或OAuth2认证机制，避免使用Session。

## 认证机制
- 使用JWT实现无状态认证
- JWT结构规范：
  ```
  Header: {
    "alg": "HS256",
    "typ": "JWT"
  }
  Payload: {
    "sub": "用户ID",
    "name": "用户名",
    "roles": ["ROLE_USER"],
    "iat": 发布时间,
    "exp": 过期时间
  }
  ```
- Token有效期：访问令牌2小时，刷新令牌7天
- 实现令牌刷新机制，避免用户频繁登录
- 敏感操作需要二次认证

## 权限模型设计
- 基于RBAC（角色权限控制）模型
- 权限层次：
  - 角色（Role）：如ADMIN、USER、MANAGER
  - 权限（Permission）：如CREATE_USER、VIEW_REPORT
  - 资源（Resource）：如USER、ORDER、PRODUCT
  - 操作（Operation）：如CREATE、READ、UPDATE、DELETE
- 权限命名规范：`资源_操作`，如`USER_CREATE`、`ORDER_VIEW`
- 角色命名规范：`ROLE_角色名`，如`ROLE_ADMIN`、`ROLE_USER`

## 安全配置
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 方法级权限控制
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAllUsers()));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userService.createUser(request)));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 自定义安全表达式
```java
@Component("userSecurity")
public class UserSecurityEvaluator {
    
    private final AuthenticationFacade authenticationFacade;
    
    public UserSecurityEvaluator(AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }
    
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername().equals(userId.toString());
    }
}
```

## 常见安全问题
- CSRF攻击：对于前后端分离应用，使用CSRF Token或SameSite Cookie
- XSS攻击：输入验证、输出编码、使用Content-Security-Policy
- 敏感数据泄露：加密存储敏感信息，传输使用HTTPS
- 权限提升：严格校验权限，避免水平/垂直越权
- 暴力破解：实现登录失败次数限制和账户锁定

## 最佳实践
- 实现API访问频率限制（Rate Limiting）
- 敏感操作添加日志审计
- 定期进行安全漏洞扫描
- 实现OAuth2.0授权码模式用于第三方应用授权
- 使用HTTPS并配置适当的安全头部 