# MyBatis Plus实现DDD基础设施层

## 目录结构

```
infrastructure/
├── config/                # 配置类
│   ├── MybatisPlusConfig.java      # MyBatis Plus配置
│   └── MyMetaObjectHandler.java    # 自动填充处理器
├── convert/               # 对象转换器
│   └── PermissionConvert.java      # 权限对象转换器
├── mapper/                # MyBatis Mapper接口
│   └── PermissionMapper.java       # 权限Mapper接口
├── persistence/           # 数据持久化
│   └── entity/                     # 数据实体
│       └── PermissionDO.java       # 权限数据对象
├── repository/            # 仓储实现
│   └── impl/                       # 仓储接口实现
│       └── PermissionRepositoryImpl.java   # 权限仓储实现
├── service/               # 基础设施服务
│   └── PermissionQueryService.java # 权限查询服务
└── util/                  # 工具类
    └── SqlUtil.java                # SQL工具类
```

## 核心组件说明

### 1. 数据对象(DO)

数据对象使用MyBatis Plus的注解来映射数据库表：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("permission")
public class PermissionDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "name")
    private String name;
    // ...其他字段
}
```

### 2. Mapper接口

继承MyBatis Plus的BaseMapper接口，提供基础CRUD操作和自定义查询方法：

```java
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionDO> {
    
    @Select("SELECT * FROM permission WHERE code = #{code}")
    PermissionDO selectByCode(@Param("code") String code);
    
    // 更复杂的查询可以在XML中定义
    List<PermissionDO> findPermissionsByRoleId(@Param("roleId") Long roleId);
}
```

### 3. 仓储实现

领域仓储接口的实现类，使用Mapper接口进行数据操作，并通过转换器将DO和领域对象进行转换：

```java
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;
    private final PermissionConvert permissionConvert;

    @Override
    public Permission save(Permission permission) {
        PermissionDO permissionDO = permissionConvert.toData(permission);
        if (permissionDO.getId() == null) {
            permissionMapper.insert(permissionDO);
        } else {
            permissionMapper.updateById(permissionDO);
        }
        return permissionConvert.toDomain(permissionDO);
    }
    // ...其他方法
}
```

### 4. 查询服务

专用于复杂查询场景，直接返回DTO，优化读取性能：

```java
@Service
@RequiredArgsConstructor
public class PermissionQueryService {

    private final PermissionMapper permissionMapper;
    private final PermissionConvert permissionConvert;

    @Cacheable(value = "permission:page")
    public IPage<PermissionDTO> getPermissionsPage(int pageNum, int pageSize, String keyword) {
        // 使用MyBatis Plus的条件构造器和分页功能
        LambdaQueryWrapper<PermissionDO> queryWrapper = new LambdaQueryWrapper<>();
        // ...查询逻辑
        return permissionDTOPage;
    }
}
```

### 5. 配置和工具

- `MybatisPlusConfig`: 配置插件和扫描路径
- `MyMetaObjectHandler`: 实现自动填充功能
- `SqlUtil`: 提供SQL构建工具方法

## 与JPA的对比优势

1. 性能优势：MyBatis Plus可以精确控制SQL，查询性能更好
2. 灵活性：支持复杂SQL和存储过程，适合复杂查询场景
3. 代码生成：提供强大的代码生成工具，提高开发效率
4. 分页性能：内置分页插件，大数据量分页更高效
5. 条件构造器：提供强大的条件构造API，简化动态查询 