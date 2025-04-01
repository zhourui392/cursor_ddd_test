# 分页和查询响应定义

- 默认分页接口使用`Pageable`对象实现分页查询。
- 返回数据时使用统一分页结构：
  ```json
  {
    "total": 100,
    "page": 1,
    "size": 10,
    "data": []
  }
  ```
- 提供默认排序规则，允许客户端通过参数自定义排序。
- 查询接口避免直接返回List，应始终使用分页接口（防止性能问题）。

## 分页参数规范
- 页码参数：`page`（从0开始）
- 每页大小参数：`size`（默认值20，最大值100）
- 排序参数：`sort`（格式：`field,direction`，如`createTime,desc`）
- 默认排序：创建时间倒序（`createdDate,desc`）
- 支持多字段排序：`sort=name,asc&sort=createdDate,desc`

## 高级查询支持
- 使用规范化的查询参数格式：
  - 精确匹配：`field=value`
  - 模糊匹配：`field.contains=value`
  - 范围查询：`field.gt=value`、`field.lt=value`、`field.between=min,max`
  - 多值查询：`field.in=value1,value2,value3`
- 复杂条件查询使用Specification接口实现动态查询

## 分页响应封装
```java
@Data
public class PageResponse<T> {
    private List<T> data;
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private boolean first;
    private boolean last;
    
    public static <T> PageResponse<T> from(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setData(page.getContent());
        response.setTotal(page.getTotalElements());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}
```

## 示例代码
### Controller层
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDateTo,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setName(name);
        criteria.setEmail(email);
        criteria.setCreatedDateFrom(createdDateFrom);
        criteria.setCreatedDateTo(createdDateTo);
        
        Page<UserDTO> users = userService.findUsers(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
    }
}
```

### Service层
```java
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserSpecification userSpecification;
    
    public UserServiceImpl(UserRepository userRepository, UserSpecification userSpecification) {
        this.userRepository = userRepository;
        this.userSpecification = userSpecification;
    }
    
    @Override
    public Page<UserDTO> findUsers(UserSearchCriteria criteria, Pageable pageable) {
        Specification<User> spec = userSpecification.buildSpecification(criteria);
        return userRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }
    
    private UserDTO convertToDTO(User user) {
        // 实体转DTO逻辑
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }
}
```

### Specification实现
```java
@Component
public class UserSpecification {
    
    public Specification<User> buildSpecification(UserSearchCriteria criteria) {
        return Specification
            .where(hasName(criteria.getName()))
            .and(hasEmail(criteria.getEmail()))
            .and(createdDateBetween(criteria.getCreatedDateFrom(), criteria.getCreatedDateTo()));
    }
    
    private Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(name)) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }
    
    private Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(email)) {
                return null;
            }
            return cb.equal(root.get("email"), email);
        };
    }
    
    private Specification<User> createdDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return null;
            }
            
            if (from != null && to != null) {
                return cb.between(
                    root.get("createdDate"),
                    from.atStartOfDay(),
                    to.plusDays(1).atStartOfDay()
                );
            }
            
            if (from != null) {
                return cb.greaterThanOrEqualTo(
                    root.get("createdDate"),
                    from.atStartOfDay()
                );
            }
            
            return cb.lessThan(
                root.get("createdDate"),
                to.plusDays(1).atStartOfDay()
            );
        };
    }
}
```

## 常见错误
- 未限制分页大小导致大量数据查询
- 复杂排序未考虑索引导致性能问题
- 返回不一致的分页结构
- 未处理空结果集情况

## 最佳实践
- 为分页查询添加适当的缓存
- 使用投影查询（Projection）减少不必要的字段查询
- 大数据量场景考虑游标分页或键集分页
- 前端实现"无限滚动"加载优化用户体验
- 分页查询添加超时控制，避免长时间阻塞 