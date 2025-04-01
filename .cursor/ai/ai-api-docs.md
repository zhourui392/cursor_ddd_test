# API文档规范

- 使用OpenAPI 3.0（Swagger）规范编写API文档
- 所有API接口必须有文档说明
- 文档应包含接口描述、请求参数、响应结构和错误码
- 文档应与代码同步更新，避免文档与实际接口不一致
- 提供在线文档访问入口

## OpenAPI注解使用规范
- 使用SpringDoc库实现OpenAPI 3.0规范
- 在Controller类上使用`@Tag`注解描述接口分组
- 在Controller方法上使用`@Operation`注解描述接口功能
- 使用`@Parameter`、`@RequestBody`描述请求参数
- 使用`@ApiResponse`描述响应结构和状态码

## 接口文档示例
```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户相关的API，包括注册、查询、修改等")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @Operation(
        summary = "获取用户列表",
        description = "分页获取用户列表，支持按用户名、邮箱等条件筛选",
        tags = {"用户管理"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "成功获取用户列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未授权",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "权限不足",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsers(
            @Parameter(description = "用户名（支持模糊查询）") 
            @RequestParam(required = false) String username,
            
            @Parameter(description = "邮箱（精确匹配）") 
            @RequestParam(required = false) String email,
            
            @Parameter(description = "创建日期起始（格式：yyyy-MM-dd）") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDateFrom,
            
            @Parameter(description = "创建日期截止（格式：yyyy-MM-dd）") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDateTo,
            
            @Parameter(description = "分页参数") 
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setUsername(username);
        criteria.setEmail(email);
        criteria.setCreatedDateFrom(createdDateFrom);
        criteria.setCreatedDateTo(createdDateTo);
        
        Page<UserDTO> users = userService.findUsers(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
    }
    
    @PostMapping
    @Operation(
        summary = "创建用户",
        description = "创建新用户，用户名和邮箱不能重复"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "用户创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "用户名或邮箱已存在",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "用户创建请求",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = UserCreateRequest.class)
                )
            )
            @Valid @RequestBody UserCreateRequest request) {
        
        UserDTO createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser));
    }
}
```

## 模型文档示例
```java
@Schema(description = "用户创建请求")
@Data
public class UserCreateRequest {
    
    @Schema(description = "用户名", example = "johndoe", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度必须在4-50个字符之间")
    private String username;
    
    @Schema(description = "密码", example = "P@ssw0rd", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
    
    @Schema(description = "邮箱", example = "john.doe@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    
    @Schema(description = "用户角色", example = "USER", defaultValue = "USER")
    private String role = "USER";
}

@Schema(description = "用户信息")
@Data
public class UserDTO {
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名", example = "johndoe")
    private String username;
    
    @Schema(description = "邮箱", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "手机号", example = "138****8000")
    private String phoneNumber;
    
    @Schema(description = "用户状态", example = "ACTIVE")
    private String status;
    
    @Schema(description = "创建时间", example = "2023-01-01T12:00:00")
    private LocalDateTime createdDate;
}
```

## OpenAPI配置
```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("用户服务API")
                .description("用户服务相关接口文档")
                .version("1.0.0")
                .contact(new Contact()
                    .name("开发团队")
                    .email("dev-team@example.com")
                    .url("https://example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .externalDocs(new ExternalDocumentation()
                .description("更多文档")
                .url("https://example.com/docs"))
            .servers(List.of(
                new Server().url("https://api.example.com").description("生产环境"),
                new Server().url("https://api-dev.example.com").description("开发环境")))
            .components(new Components()
                .securitySchemes(Map.of(
                    "bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT认证，在请求头中添加 Authorization: Bearer {token}")
                )))
            .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }
}
```

## 错误码文档
- 在API文档中明确定义错误码和错误信息
- 错误码应具有一致的格式和含义
- 提供错误码查询和说明页面

```java
@Schema(description = "API响应")
@Data
public class ApiResponse<T> {
    
    @Schema(description = "是否成功", example = "true")
    private boolean success;
    
    @Schema(description = "错误码", example = "USER_NOT_FOUND")
    private String errorCode;
    
    @Schema(description = "错误信息", example = "用户不存在")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    // 静态工厂方法
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }
    
    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
}
```

## 文档生成与访问
- 配置Swagger UI访问路径：`/swagger-ui.html`
- 配置OpenAPI文档JSON路径：`/v3/api-docs`
- 在开发和测试环境启用文档，生产环境可选择性禁用
- 提供文档导出功能（JSON、YAML、HTML等格式）

## 版本控制
- 在API路径中包含版本号：`/api/v1/users`
- 在文档中明确标注API版本
- 对于废弃的API，使用`@Deprecated`注解并在文档中说明替代方案
- 保留历史版本文档，便于客户端迁移

## 常见错误
- 文档与实际接口不一致
- 缺少必要的参数说明
- 错误码和错误信息不明确
- 文档更新不及时
- 敏感信息泄露在文档中

## 最佳实践
- 将API文档生成集成到CI/CD流程中
- 使用契约优先的API设计方法（先设计文档，再实现接口）
- 提供API变更日志，记录接口变更历史
- 实现API文档的版本控制
- 定期审查和更新API文档 