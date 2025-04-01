# 从Java 11 (JEE) 到 Java 21 (Jakarta EE) 的迁移说明

## 概述

从Java 11使用的JavaEE (javax.*)迁移到Java 21使用的Jakarta EE (jakarta.*)，主要涉及以下变更：

1. 升级Spring Boot版本到3.x
2. 替换所有javax.*包引用为对应的jakarta.*包
3. 可能需要更新依赖库版本以兼容Jakarta EE

## 主要变更

### 1. 依赖变更

在`pom.xml`中：

```xml
<!-- 将Spring Boot版本升级到3.x -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
    <relativePath/>
</parent>

<!-- 将Java版本更新为21 -->
<properties>
    <java.version>21</java.version>
</properties>

<!-- 添加Jakarta Validation API -->
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
</dependency>

<!-- 添加Hibernate Validator (Jakarta EE实现) -->
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
</dependency>
```

### 2. 包导入变更

| JavaEE (旧) | Jakarta EE (新) |
|------------|----------------|
| javax.persistence.* | jakarta.persistence.* |
| javax.validation.* | jakarta.validation.* |
| javax.servlet.* | jakarta.servlet.* |
| javax.annotation.* | jakarta.annotation.* |
| javax.ws.rs.* | jakarta.ws.rs.* |

### 3. 代码修改示例

#### JPA实体类

```java
// 从
import javax.persistence.*;

// 变为
import jakarta.persistence.*;
```

#### 验证注解

```java
// 从
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

// 变为
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
```

#### Servlet相关

```java
// 从
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 变为
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
```

## 可能遇到的问题

1. IDE错误提示：这些错误通常在Maven重新导入或重启IDE后解决
2. 运行时异常：确保所有javax.*包都已替换为jakarta.*
3. 第三方库兼容性：确保第三方库与Jakarta EE兼容

## 结论

随着Java EE项目迁移到Eclipse基金会，包名从javax.*更改为jakarta.*是一项重要变更。在Java 21环境下，使用Jakarta EE是推荐的做法，而大多数框架（如Spring Boot 3.x）已经适配了这一变更。 