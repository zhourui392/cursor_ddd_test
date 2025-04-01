# 测试规范

- 测试使用JUnit5和Mockito框架。
- 单元测试覆盖核心业务逻辑，确保每个方法至少一个测试用例。
- 集成测试使用Testcontainers确保数据库真实环境。
- 测试类命名格式为`XxxServiceTest`或`XxxControllerTest`。
- 每个测试方法命名清晰体现测试场景，如`testCreateOrderWithInvalidData`。
- 保证测试执行的幂等性，避免相互依赖。

## 单元测试规范
- 使用`@Mock`和`@InjectMocks`进行依赖注入
- 测试方法遵循AAA模式：Arrange（准备）、Act（执行）、Assert（断言）
- 使用`@DisplayName`注解提供可读性更好的测试描述
- 使用`@Nested`注解组织相关测试场景
- 使用参数化测试`@ParameterizedTest`处理多种输入场景

## 集成测试规范
- 使用`@SpringBootTest`进行Spring上下文集成测试
- 数据库测试使用`@DataJpaTest`或Testcontainers
- API测试使用`@WebMvcTest`或`RestAssured`
- 每个测试方法后清理测试数据，保证测试环境干净
- 使用`@TestPropertySource`指定测试专用配置

## 测试覆盖率要求
- 单元测试覆盖率：业务逻辑代码 > 80%
- 集成测试覆盖率：API接口 > 90%
- 使用Jacoco生成测试覆盖率报告
- 关键业务流程必须有端到端测试覆盖

## 示例代码
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Nested
    @DisplayName("用户创建测试")
    class CreateUserTests {
        
        @Test
        @DisplayName("正常创建用户")
        void testCreateUserSuccess() {
            // Arrange
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("testuser");
            request.setPassword("password");
            request.setEmail("test@example.com");
            
            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername(request.getUsername());
            savedUser.setEmail(request.getEmail());
            
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            
            // Act
            UserDTO result = userService.createUser(request);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
            assertEquals("test@example.com", result.getEmail());
            
            verify(passwordEncoder).encode("password");
            verify(userRepository).save(any(User.class));
        }
        
        @Test
        @DisplayName("用户名已存在")
        void testCreateUserWithDuplicateUsername() {
            // Arrange
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("existinguser");
            request.setPassword("password");
            request.setEmail("test@example.com");
            
            when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.of(new User()));
            
            // Act & Assert
            assertThrows(DuplicateResourceException.class, () -> {
                userService.createUser(request);
            });
            
            verify(userRepository).findByUsername("existinguser");
            verify(userRepository, never()).save(any(User.class));
        }
    }
}

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("用户API集成测试")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("创建用户API测试")
    void testCreateUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("integrationuser");
        request.setPassword("password123");
        request.setEmail("integration@example.com");
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("integrationuser"))
                .andExpect(jsonPath("$.data.email").value("integration@example.com"));
                
        assertTrue(userRepository.findByUsername("integrationuser").isPresent());
    }
}
```

## 常见错误
- 测试代码中包含硬编码的测试数据
- 测试之间存在隐式依赖
- 未模拟外部依赖导致测试不稳定
- 测试覆盖率低或只测试了正常路径

## 最佳实践
- 使用测试工厂或构建器模式创建测试数据
- 使用BDDMockito提高测试可读性（given/when/then）
- 编写契约测试确保微服务间接口兼容
- 使用测试夹具（Test Fixtures）管理复杂测试数据
- 定期运行性能测试和安全测试 