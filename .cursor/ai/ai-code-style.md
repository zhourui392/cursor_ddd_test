# 代码风格规范

- 遵循阿里巴巴Java开发手册（P3C）规范
- 使用统一的代码格式化配置
- 代码提交前必须通过静态代码分析检查
- 命名规范清晰明确，避免使用缩写
- 适当添加注释，提高代码可读性

## 命名规范
- **包名**：全部小写，使用点分隔符，如`com.example.project.module`
- **类名**：大驼峰命名法（UpperCamelCase），如`UserService`
- **方法名**：小驼峰命名法（lowerCamelCase），如`getUserById`
- **变量名**：小驼峰命名法，如`userId`
- **常量名**：全大写，单词间用下划线分隔，如`MAX_RETRY_COUNT`
- **枚举类**：大驼峰命名法，枚举值全大写，如`UserStatus.ACTIVE`

## 代码格式
- 缩进使用4个空格，不使用Tab
- 行宽不超过120个字符
- 花括号使用K&R风格（左花括号不换行，右花括号独占一行）
- 类内部顺序：静态变量 > 实例变量 > 构造方法 > 静态方法 > 实例方法
- 方法内部顺序：参数校验 > 业务逻辑 > 结果返回
- 导入语句按照以下顺序分组：
  1. 静态导入
  2. java.*
  3. javax.*
  4. org.*
  5. com.*
  6. 其他导入

## 注释规范
- 类注释：描述类的功能、作者、创建日期等
- 方法注释：描述方法的功能、参数、返回值、异常等
- 变量注释：对于复杂变量或特殊含义的变量添加注释
- 代码注释：对于复杂逻辑或特殊处理添加注释
- 使用Javadoc规范编写注释

```java
/**
 * 用户服务实现类，提供用户相关的业务逻辑处理
 *
 * @author 开发者姓名
 * @since 1.0.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 构造方法，通过依赖注入初始化服务
     *
     * @param userRepository 用户数据访问对象
     * @param passwordEncoder 密码编码器
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户DTO对象
     * @throws ResourceNotFoundException 当用户不存在时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        // 参数校验
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        // 业务逻辑
        return userRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    /**
     * 将用户实体转换为DTO对象
     *
     * @param user 用户实体
     * @return 用户DTO对象
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        // 敏感信息脱敏
        dto.setPhoneNumber(SensitiveDataConverter.maskPhoneNumber(user.getPhoneNumber()));
        dto.setStatus(user.getStatus().name());
        dto.setCreatedDate(user.getCreatedDate());
        return dto;
    }
}
```

## 代码质量工具
- **Checkstyle**：检查代码风格和格式
- **PMD**：检查潜在的代码问题
- **SpotBugs**：检查潜在的bug
- **SonarQube**：综合代码质量分析

### Maven配置示例
```xml
<build>
    <plugins>
        <!-- Checkstyle -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.2.0</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <encoding>UTF-8</encoding>
                <consoleOutput>true</consoleOutput>
                <failsOnError>true</failsOnError>
                <linkXRef>false</linkXRef>
            </configuration>
            <executions>
                <execution>
                    <id>validate</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- PMD -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>3.19.0</version>
            <configuration>
                <sourceEncoding>UTF-8</sourceEncoding>
                <targetJdk>17</targetJdk>
                <printFailingErrors>true</printFailingErrors>
                <rulesets>
                    <ruleset>pmd-ruleset.xml</ruleset>
                </rulesets>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- SpotBugs -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.7.3.0</version>
            <configuration>
                <effort>Max</effort>
                <threshold>Medium</threshold>
                <xmlOutput>true</xmlOutput>
                <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 阿里巴巴P3C规范要点
- **命名规约**
  - 避免使用拼音或中文拼音首字母
  - 避免使用无意义的前缀或后缀
  - 接口和实现类命名规则：接口名 + Impl
- **常量定义**
  - 不允许任何魔法值（即未经预先定义的常量）直接出现在代码中
  - 浮点数比较必须使用精确的方式，不能使用`==`
- **代码格式**
  - 单行字符数限制不超过120个
  - 方法体内的执行语句组、变量的定义语句组、不同的业务逻辑之间或者不同的语义之间插入一个空行
- **OOP规约**
  - 避免通过一个类的对象引用访问此类的静态变量或静态方法，应该直接使用类名
  - 所有的覆写方法必须加`@Override`注解
  - 相同参数类型，相同业务含义，才可以使用Java的可变参数
- **集合处理**
  - 判断所有集合内部的元素是否为空，使用`isEmpty()`方法
  - 使用entrySet遍历Map类集合
  - 不要在foreach循环里进行元素的remove/add操作
- **并发处理**
  - 创建线程或线程池时请指定有意义的线程名称
  - 线程资源必须通过线程池提供，不允许在应用中自行显式创建线程
  - 使用`ThreadLocal`需要注意内存泄漏问题
- **控制语句**
  - 在if/else/for/while/do语句中必须使用大括号
  - 表达异常的分支时，少用if-else方式，多用卫语句
- **注释规约**
  - 类、类属性、类方法的注释必须使用Javadoc规范
  - 所有的抽象方法（包括接口中的方法）必须要用Javadoc注释
  - 方法内部单行注释，在被注释语句上方另起一行，使用`//`注释

## IDE配置
- 提供统一的IDE配置文件（.editorconfig）
- 提供统一的代码风格配置文件（IntelliJ IDEA、Eclipse等）
- 配置自动格式化和导入优化

### .editorconfig示例
```
root = true

[*]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
max_line_length = 120
tab_width = 4
trim_trailing_whitespace = true

[*.{yml,yaml,json,xml}]
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

## Git提交规范
- 提交信息格式：`<type>: <subject>`
- 类型（type）：
  - feat：新功能
  - fix：修复bug
  - docs：文档变更
  - style：代码格式（不影响代码运行的变动）
  - refactor：重构（既不是新增功能，也不是修改bug的代码变动）
  - perf：性能优化
  - test：增加测试
  - chore：构建过程或辅助工具的变动
- 主题（subject）：简短描述，不超过50个字符

## 常见错误
- 命名不规范，使用拼音或无意义的缩写
- 代码格式不一致，缩进混乱
- 注释缺失或过时
- 魔法值直接硬编码在代码中
- 代码重复，未提取公共方法

## 最佳实践
- 使用IDE插件自动检查代码风格
- 在CI/CD流程中集成代码质量检查
- 定期进行代码审查（Code Review）
- 使用SonarQube等工具持续监控代码质量
- 遵循SOLID原则和设计模式 