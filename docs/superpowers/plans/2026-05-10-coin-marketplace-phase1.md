# 钱币收藏品电商平台 — 实施计划（第一期）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 建成支持 B2C+C2C 混合模式的四国语言钱币收藏品电商平台（固定价格商城），预留拍卖系统扩展点。

**Architecture:** 模块化 Spring Boot 单体 + 事件驱动（RabbitMQ），前端 Vue 3 三端分离（商城/卖家中心/管理后台），PostgreSQL Schema 按域隔离。

**Tech Stack:** Spring Boot 3 + JDK 21, Vue 3 + Element Plus, PostgreSQL 16, Redis, RabbitMQ, Elasticsearch, MyBatis-Plus, Spring Security + JWT

---

## 文件结构总览

```
coin-marketplace/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/coinmarket/
│   │   ├── CoinMarketplaceApplication.java
│   │   ├── common/
│   │   │   ├── config/           # 全局配置
│   │   │   ├── security/         # JWT + Spring Security
│   │   │   ├── exception/        # 全局异常处理
│   │   │   ├── i18n/             # 后端多语言
│   │   │   └── entity/BaseEntity.java
│   │   ├── user/                 # 用户域（含 controller/service/repository/entity/dto）
│   │   ├── product/              # 商品域
│   │   ├── order/                # 订单域
│   │   ├── payment/              # 支付域（各支付网关 Adapter）
│   │   ├── seller/               # 卖家域
│   │   ├── admin/                # 管理后台域
│   │   └── search/               # 搜索域
│   └── src/main/resources/
│       ├── application.yml / application-dev.yml / application-prod.yml
│       ├── messages/             # 后端 i18n
│       └── db/migration/         # Flyway 迁移脚本
├── frontend/
│   ├── store/                    # Vue 3 商城前台
│   ├── seller/                   # Vue 3 卖家中心
│   └── admin/                    # Vue 3 管理后台
├── docker-compose.yml
└── docs/
```

---

## 第一阶段：基础设施 & 项目骨架

### Task 1: 项目脚手架搭建

**Files:**
- Create: `coin-marketplace/pom.xml`
- Create: `coin-marketplace/backend/src/main/java/com/coinmarket/CoinMarketplaceApplication.java`
- Create: `coin-marketplace/backend/src/main/resources/application.yml`
- Create: `coin-marketplace/backend/src/main/resources/application-dev.yml`
- Create: `coin-marketplace/backend/src/main/resources/application-prod.yml`
- Create: `coin-marketplace/docker-compose.yml`

- [ ] **Step 1: 创建 Maven parent pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.coinmarket</groupId>
    <artifactId>coin-marketplace</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>coin-marketplace</name>
    <description>Coin Marketplace Platform</description>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <jjwt.version>0.12.5</jjwt.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Elasticsearch -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>
        <dependency>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
        </dependency>

        <!-- Stripe SDK -->
        <dependency>
            <groupId>com.stripe</groupId>
            <artifactId>stripe-java</artifactId>
            <version>25.8.0</version>
        </dependency>

        <!-- Utility -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建主应用入口**

```java
package com.coinmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoinMarketplaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoinMarketplaceApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 docker-compose.yml**

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: coin_marketplace
      POSTGRES_USER: coinuser
      POSTGRES_PASSWORD: coinpass123
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: coinmq
      RABBITMQ_DEFAULT_PASS: coinmq123

  elasticsearch:
    image: elasticsearch:8.13
    environment:
      discovery.type: single-node
      ES_JAVA_OPTS: "-Xms512m -Xmx512m"
      xpack.security.enabled: "false"
    ports:
      - "9200:9200"

volumes:
  pgdata:
```

- [ ] **Step 4: 创建 application.yml**

```yaml
spring:
  application:
    name: coin-marketplace
  profiles:
    active: dev

server:
  port: 8080

---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/coin_marketplace
    username: coinuser
    password: coinpass123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: coinmq
    password: coinmq123

app:
  jwt:
    secret: dev-secret-key-must-be-at-least-256-bits-long-for-hs256
    expiration-ms: 86400000
  file:
    upload-dir: ./uploads
    max-size: 10485760
  rating-api:
    ngc:
      base-url: https://api.ngccoin.com
    pcgs:
      base-url: https://api.pcgs.com
    pmg:
      base-url: https://api.pmgnotes.com

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
  rabbitmq:
    host: ${MQ_HOST}
    port: ${MQ_PORT}
    username: ${MQ_USERNAME}
    password: ${MQ_PASSWORD}

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000
  file:
    upload-dir: ${UPLOAD_DIR:/data/uploads}
    max-size: 10485760
  rating-api:
    ngc:
      base-url: https://api.ngccoin.com
      api-key: ${NGC_API_KEY}
    pcgs:
      base-url: https://api.pcgs.com
      api-key: ${PCGS_API_KEY}
    pmg:
      base-url: https://api.pmgnotes.com
      api-key: ${PMG_API_KEY}
```

- [ ] **Step 5: 验证项目启动**

```bash
cd coin-marketplace
docker-compose up -d postgres redis rabbitmq
mvn clean compile -f backend/pom.xml
# Expected: BUILD SUCCESS
```

---

### Task 2: 通用基础模块

**Files:**
- Create: `backend/src/main/java/com/coinmarket/common/entity/BaseEntity.java`
- Create: `backend/src/main/java/com/coinmarket/common/exception/ErrorCode.java`
- Create: `backend/src/main/java/com/coinmarket/common/exception/BusinessException.java`
- Create: `backend/src/main/java/com/coinmarket/common/exception/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/coinmarket/common/config/WebMvcConfig.java`
- Create: `backend/src/main/java/com/coinmarket/common/config/JacksonConfig.java`
- Create: `backend/src/main/java/com/coinmarket/common/config/RedisConfig.java`
- Create: `backend/src/main/java/com/coinmarket/common/config/RabbitMqConfig.java`
- Create: `backend/src/main/java/com/coinmarket/common/dto/ApiResponse.java`
- Create: `backend/src/main/java/com/coinmarket/common/dto/PageResponse.java`

- [ ] **Step 1: 创建 BaseEntity（所有实体的公共基类）**

```java
package com.coinmarket.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: 创建统一 API 响应**

```java
package com.coinmarket.common.dto;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
```

- [ ] **Step 3: 创建全局异常处理**

```java
package com.coinmarket.common.exception;

import com.coinmarket.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException() {
        return ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknownException(Exception e) {
        log.error("Unexpected error", e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
}
```

- [ ] **Step 4: 创建 BusinessException**

```java
package com.coinmarket.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
}
```

- [ ] **Step 5: 创建 Redis 配置**

```java
package com.coinmarket.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

- [ ] **Step 6: 创建 RabbitMQ 配置**

```java
package com.coinmarket.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_ORDER = "order.exchange";
    public static final String QUEUE_ORDER_PAYMENT = "order.payment.queue";
    public static final String QUEUE_ORDER_NOTIFICATION = "order.notification.queue";
    public static final String ROUTING_KEY_PAYMENT = "order.payment";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_ORDER);
    }

    @Bean
    public Queue orderPaymentQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_PAYMENT).build();
    }

    @Bean
    public Queue orderNotificationQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_NOTIFICATION).build();
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(orderPaymentQueue())
                .to(orderExchange())
                .with(ROUTING_KEY_PAYMENT);
    }
}
```

- [ ] **Step 7: 编译验证**

```bash
cd coin-marketplace/backend && mvn clean compile
# Expected: BUILD SUCCESS
```

---

## 第二阶段：用户系统

### Task 3: 数据库迁移 — 用户表

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__create_users.sql`

- [ ] **Step 1: 创建用户相关表**

```sql
CREATE SCHEMA IF NOT EXISTS coin_users;

CREATE TABLE coin_users.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30),
    phone_country_code VARCHAR(5),
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    avatar_url VARCHAR(500),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_users.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_users.user_roles (
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    role_id BIGINT NOT NULL REFERENCES coin_users.roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE coin_users.user_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    login_ip VARCHAR(45),
    login_time TIMESTAMP NOT NULL DEFAULT NOW(),
    success BOOLEAN NOT NULL
);

-- 插入初始角色
INSERT INTO coin_users.roles (name, description) VALUES
    ('ROLE_BUYER', '买家'),
    ('ROLE_SELLER', '卖家'),
    ('ROLE_ADMIN', '管理员');
```

- [ ] **Step 2: 运行迁移验证**

```bash
cd coin-marketplace/backend && mvn flyway:migrate
# Expected: Successfully applied 1 migration
```

---

### Task 4: 用户模块 — 实体与 Repository

**Files:**
- Create: `backend/src/main/java/com/coinmarket/user/entity/User.java`
- Create: `backend/src/main/java/com/coinmarket/user/entity/Role.java`
- Create: `backend/src/main/java/com/coinmarket/user/repository/UserRepository.java`
- Create: `backend/src/main/java/com/coinmarket/user/repository/RoleRepository.java`
- Create: `backend/src/main/java/com/coinmarket/user/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/coinmarket/user/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/coinmarket/user/dto/AuthResponse.java`
- Create: `backend/src/main/java/com/coinmarket/user/dto/UserProfileResponse.java`

- [ ] **Step 1: 创建 User 实体**

```java
package com.coinmarket.user.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users", schema = "coin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "phone_country_code", length = 5)
    private String phoneCountryCode;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        schema = "coin_users",
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
```

- [ ] **Step 2: 创建 Role 实体**

```java
package com.coinmarket.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", schema = "coin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;
}
```

- [ ] **Step 3: 创建 UserRepository**

```java
package com.coinmarket.user.repository;

import com.coinmarket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

- [ ] **Step 4: 创建 DTO 类**

```java
// RegisterRequest.java
package com.coinmarket.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 3, max = 50, message = "{validation.username.length}")
    private String username;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 100, message = "{validation.password.length}")
    private String password;

    private String phone;
    private String phoneCountryCode;
    private String displayName;

    @NotBlank(message = "{validation.language.required}")
    private String preferredLanguage;
}

// LoginRequest.java
package com.coinmarket.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

// AuthResponse.java
package com.coinmarket.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserProfileResponse user;
}

// UserProfileResponse.java
package com.coinmarket.user.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String displayName;
    private String preferredLanguage;
    private String avatarUrl;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Set<String> roles;
}
```

---

### Task 5: JWT 安全模块

**Files:**
- Create: `backend/src/main/java/com/coinmarket/common/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/coinmarket/common/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/coinmarket/common/security/SecurityConfig.java`
- Create: `backend/src/main/java/com/coinmarket/common/security/CurrentUser.java`
- Create: `backend/src/main/java/com/coinmarket/common/security/UserPrincipal.java`

- [ ] **Step 1: 创建 JwtTokenProvider**

```java
package com.coinmarket.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }
}
```

- [ ] **Step 2: 创建 JwtAuthenticationFilter**

```java
package com.coinmarket.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            var claims = jwtTokenProvider.parseToken(token);
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            var principal = new UserPrincipal(userId, username, authorities);
            var authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 3: 创建 SecurityConfig**

```java
package com.coinmarket.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 4: 创建 UserPrincipal 和 CurrentUser 注解**

```java
// UserPrincipal.java
package com.coinmarket.common.security;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
}

// CurrentUser.java (annotation)
package com.coinmarket.common.security;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {}
```

---

### Task 6: 用户模块 — Service & Controller

**Files:**
- Create: `backend/src/main/java/com/coinmarket/user/service/UserService.java`
- Create: `backend/src/main/java/com/coinmarket/user/controller/AuthController.java`
- Create: `backend/src/main/java/com/coinmarket/user/controller/UserController.java`

- [ ] **Step 1: 创建 UserService**

```java
package com.coinmarket.user.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.security.JwtTokenProvider;
import com.coinmarket.user.dto.*;
import com.coinmarket.user.entity.Role;
import com.coinmarket.user.entity.User;
import com.coinmarket.user.repository.RoleRepository;
import com.coinmarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(409, "Email already exists");
        }

        Role buyerRole = roleRepository.findByName("ROLE_BUYER")
                .orElseThrow(() -> new BusinessException("Default role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .phoneCountryCode(request.getPhoneCountryCode())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .preferredLanguage(request.getPreferredLanguage() != null ?
                        request.getPreferredLanguage() : "en")
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(true)
                .roles(Set.of(buyerRole))
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new BusinessException(403, "Account is disabled");
        }

        return buildAuthResponse(user);
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        return toProfileResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        var roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(toProfileResponse(user))
                .build();
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .displayName(user.getDisplayName())
                .preferredLanguage(user.getPreferredLanguage())
                .avatarUrl(user.getAvatarUrl())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
```

- [ ] **Step 2: 创建 AuthController**

```java
package com.coinmarket.user.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.user.dto.*;
import com.coinmarket.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }
}

// UserController.java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getProfile(@CurrentUser UserPrincipal principal) {
        return ApiResponse.success(userService.getProfile(principal.getId()));
    }
}
```

- [ ] **Step 3: 注册 CurrentUser 解析器**

在 WebMvcConfig 中添加 HandlerMethodArgumentResolver 或直接在 controller 中使用 SecurityContextHolder。简便起见，使用 SecurityContextHolder 获取：

```java
// 在 UserController 中简化处理
// 实际项目中可注册 HandlerMethodArgumentResolver
```

- [ ] **Step 4: 启动测试**

```bash
cd coin-marketplace/backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
# 在另一个终端:
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@test.com","password":"password123","preferredLanguage":"en"}'
# Expected: 201 Created with token and user profile
```

---

## 第三阶段：卖家认证系统

### Task 7: 数据库迁移 — 卖家表

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__create_sellers.sql`

- [ ] **Step 1: 创建卖家相关表**

```sql
CREATE SCHEMA IF NOT EXISTS coin_seller;

CREATE TABLE coin_seller.seller_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- status: PENDING, APPROVED, REJECTED
    id_document_url VARCHAR(500),
    id_document_type VARCHAR(50),
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    shop_name VARCHAR(100),
    shop_description TEXT,
    reject_reason TEXT,
    reviewed_by BIGINT REFERENCES coin_users.users(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 卖家认证后资料锁定
CREATE TABLE coin_seller.seller_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES coin_users.users(id),
    shop_name VARCHAR(100) NOT NULL,
    shop_description TEXT,
    contact_phone VARCHAR(30),
    contact_email VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    locked BOOLEAN NOT NULL DEFAULT TRUE,  -- 锁定后只有管理员可修改
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

- [ ] **Step 2: 运行迁移验证**

```bash
mvn flyway:migrate
```

---

### Task 8: 卖家模块 — 后端

**Files:**
- Create: `backend/src/main/java/com/coinmarket/seller/entity/SellerApplication.java`
- Create: `backend/src/main/java/com/coinmarket/seller/entity/SellerProfile.java`
- Create: `backend/src/main/java/com/coinmarket/seller/repository/SellerApplicationRepository.java`
- Create: `backend/src/main/java/com/coinmarket/seller/repository/SellerProfileRepository.java`
- Create: `backend/src/main/java/com/coinmarket/seller/service/SellerService.java`
- Create: `backend/src/main/java/com/coinmarket/seller/controller/SellerController.java`
- Create: `backend/src/main/java/com/coinmarket/seller/dto/SellerApplicationRequest.java`
- Create: `backend/src/main/java/com/coinmarket/seller/dto/SellerStatusResponse.java`

- [ ] **Step 1: 创建 SellerService（核心认证逻辑）**

```java
package com.coinmarket.seller.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.seller.dto.SellerApplicationRequest;
import com.coinmarket.seller.dto.SellerStatusResponse;
import com.coinmarket.seller.entity.SellerApplication;
import com.coinmarket.seller.entity.SellerProfile;
import com.coinmarket.seller.repository.SellerApplicationRepository;
import com.coinmarket.seller.repository.SellerProfileRepository;
import com.coinmarket.user.entity.Role;
import com.coinmarket.user.entity.User;
import com.coinmarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerApplicationRepository applicationRepository;
    private final SellerProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submitApplication(Long userId, SellerApplicationRequest request) {
        if (applicationRepository.findByUserIdAndStatus(userId, "PENDING").isPresent()) {
            throw new BusinessException("已有待审核的申请");
        }
        if (applicationRepository.findByUserIdAndStatus(userId, "APPROVED").isPresent()) {
            throw new BusinessException("已经是卖家");
        }

        SellerApplication application = SellerApplication.builder()
                .userId(userId)
                .status("PENDING")
                .idDocumentUrl(request.getIdDocumentUrl())
                .idDocumentType(request.getIdDocumentType())
                .shopName(request.getShopName())
                .shopDescription(request.getShopDescription())
                .build();
        applicationRepository.save(application);
    }

    @Transactional
    public void approveApplication(Long applicationId, Long adminId) {
        SellerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        app.setStatus("APPROVED");
        app.setReviewedBy(adminId);
        app.setReviewedAt(java.time.LocalDateTime.now());
        applicationRepository.save(app);

        // 创建卖家档案（锁定状态）
        SellerProfile profile = SellerProfile.builder()
                .userId(app.getUserId())
                .shopName(app.getShopName())
                .shopDescription(app.getShopDescription())
                .status("ACTIVE")
                .locked(true)
                .build();
        profileRepository.save(profile);

        // 添加卖家角色
        User user = userRepository.findById(app.getUserId()).orElseThrow();
        Role sellerRole = new Role();
        sellerRole.setId(2L); // ROLE_SELLER
        user.getRoles().add(sellerRole);
        userRepository.save(user);
    }

    @Transactional
    public void rejectApplication(Long applicationId, Long adminId, String reason) {
        SellerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        app.setStatus("REJECTED");
        app.setReviewedBy(adminId);
        app.setRejectReason(reason);
        app.setReviewedAt(java.time.LocalDateTime.now());
        applicationRepository.save(app);
    }

    public SellerStatusResponse getStatus(Long userId) {
        var application = applicationRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId);
        var profile = profileRepository.findByUserId(userId);

        SellerStatusResponse response = new SellerStatusResponse();
        response.setHasApplied(application.isPresent());
        application.ifPresent(a -> {
            response.setStatus(a.getStatus());
            response.setRejectReason(a.getRejectReason());
        });
        response.setHasProfile(profile.isPresent());
        profile.ifPresent(p -> response.setLocked(p.isLocked()));
        return response;
    }
}
```

- [ ] **Step 2: 注意**：SellerApplication 和 SellerProfile 实体遵循 BaseEntity 模式。SellerProfile 中 `locked = true` 是核心安全约束——认证后卖家不能自行修改，所有更新必须经过管理员审核。

---

### Task 9: 卖家中心前端

**Files:**
- Create: `frontend/seller/package.json`
- Create: `frontend/seller/vite.config.js`
- Create: `frontend/seller/index.html`
- Create: `frontend/seller/src/main.js`
- Create: `frontend/seller/src/App.vue`
- Create: `frontend/seller/src/router/index.js`
- Create: `frontend/seller/src/api/index.js`
- Create: `frontend/seller/src/views/Dashboard.vue`
- Create: `frontend/seller/src/views/Application.vue`
- Create: `frontend/seller/src/views/Products.vue`
- Create: `frontend/seller/src/views/Orders.vue`
- Create: `frontend/seller/src/views/Profile.vue`

- [ ] **Step 1: 创建 package.json**

```json
{
  "name": "coin-marketplace-seller",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.7.0",
    "axios": "^1.7.0",
    "vue-i18n": "^9.13.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.2.0"
  }
}
```

- [ ] **Step 2: 创建 vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 3: 创建 i18n 基础配置**

```javascript
// frontend/seller/src/i18n/index.js
import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import zhCN from './locales/zh-CN.json'
import zhTW from './locales/zh-TW.json'
import ja from './locales/ja.json'

const messages = {
  en,
  'zh-CN': zhCN,
  'zh-TW': zhTW,
  ja
}

const i18n = createI18n({
  locale: localStorage.getItem('language') || 'en',
  fallbackLocale: 'en',
  messages
})

export default i18n
```

- [ ] **Step 4: 创建卖家入驻申请页面（核心功能）**

```vue
<!-- frontend/seller/src/views/Application.vue -->
<template>
  <div class="application-page">
    <el-card v-if="!hasApplied">
      <template #header>
        <h2>{{ $t('seller.application.title') }}</h2>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item :label="$t('seller.application.shopName')" prop="shopName">
          <el-input v-model="form.shopName" />
        </el-form-item>
        <el-form-item :label="$t('seller.application.shopDescription')" prop="shopDescription">
          <el-input v-model="form.shopDescription" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="$t('seller.application.idDocument')" prop="idDocumentUrl">
          <el-upload
            action="/api/files/upload"
            :on-success="handleUploadSuccess"
            :limit="1"
          >
            <el-button type="primary">{{ $t('common.upload') }}</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitApplication">
            {{ $t('common.submit') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-else>
      <el-result
        :icon="status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'error' : 'info'"
        :title="statusText"
        :sub-title="rejectReason"
      >
        <template #extra>
          <el-button v-if="status === 'REJECTED'" type="primary" @click="reapply">
            {{ $t('seller.application.reapply') }}
          </el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const formRef = ref(null)
const hasApplied = ref(false)
const status = ref('')
const rejectReason = ref('')

const form = reactive({
  shopName: '',
  shopDescription: '',
  idDocumentUrl: '',
  idDocumentType: 'IDENTITY_CARD'
})

const rules = {
  shopName: [{ required: true, message: t('seller.validation.shopNameRequired'), trigger: 'blur' }],
  idDocumentUrl: [{ required: true, message: t('seller.validation.idRequired'), trigger: 'change' }]
}

async function submitApplication() {
  const valid = await formRef.value.validate()
  if (!valid) return
  await api.post('/api/seller/apply', form)
  hasApplied.value = true
  status.value = 'PENDING'
}

function handleUploadSuccess(response) {
  form.idDocumentUrl = response.data.url
}

async function loadStatus() {
  const res = await api.get('/api/seller/status')
  hasApplied.value = res.data.hasApplied
  status.value = res.data.status
  rejectReason.value = res.data.rejectReason
}

onMounted(loadStatus)
</script>
```

---

## 第四阶段：商品系统

### Task 10: 数据库迁移 — 商品表

**Files:**
- Create: `backend/src/main/resources/db/migration/V3__create_products.sql`

- [ ] **Step 1: 创建商品相关表**

```sql
CREATE SCHEMA IF NOT EXISTS coin_product;

CREATE TABLE coin_product.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    parent_id BIGINT REFERENCES coin_product.categories(id),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_product.products (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    stock INT NOT NULL DEFAULT 0,
    category_id BIGINT REFERENCES coin_product.categories(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- 评级信息
    rating_company VARCHAR(20),     -- NGC, PCGS, PMG
    rating_number VARCHAR(50),      -- 评级编号
    rating_grade VARCHAR(50),       -- 评级等级
    -- 钱币属性
    country VARCHAR(100),           -- 发行国家
    year INT,                       -- 发行年份
    material VARCHAR(50),           -- 材质
    denomination VARCHAR(50),       -- 面值
    weight DECIMAL(10, 2),          -- 重量(g)
    -- 统计
    view_count INT NOT NULL DEFAULT 0,
    sales_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_product.product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES coin_product.products(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_products_seller ON coin_product.products(seller_id);
CREATE INDEX idx_products_category ON coin_product.products(category_id);
CREATE INDEX idx_products_rating_number ON coin_product.products(rating_number);
CREATE INDEX idx_products_status ON coin_product.products(status);
```

---

### Task 11: 商品模块 — 后端

**Files:**
- Create: `backend/src/main/java/com/coinmarket/product/entity/Product.java`
- Create: `backend/src/main/java/com/coinmarket/product/entity/ProductImage.java`
- Create: `backend/src/main/java/com/coinmarket/product/entity/Category.java`
- Create: `backend/src/main/java/com/coinmarket/product/repository/ProductRepository.java`
- Create: `backend/src/main/java/com/coinmarket/product/service/ProductService.java`
- Create: `backend/src/main/java/com/coinmarket/product/controller/ProductController.java`
- Create: `backend/src/main/java/com/coinmarket/product/dto/ProductCreateRequest.java`
- Create: `backend/src/main/java/com/coinmarket/product/dto/ProductResponse.java`
- Create: `backend/src/main/java/com/coinmarket/product/service/RatingLookupService.java`
- Create: `backend/src/main/java/com/coinmarket/product/dto/RatingInfoResponse.java`

- [ ] **Step 1: 创建 ProductService（含评级查询）**

```java
package com.coinmarket.product.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RatingLookupService ratingLookupService;

    @Transactional
    public ProductResponse createProduct(Long sellerId, ProductCreateRequest request) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCurrency(request.getCurrency());
        product.setStock(request.getStock());
        product.setCategoryId(request.getCategoryId());
        product.setRatingCompany(request.getRatingCompany());
        product.setRatingNumber(request.getRatingNumber());

        // 如果有评级编号，查询评级信息
        if (request.getRatingNumber() != null && request.getRatingCompany() != null) {
            var ratingInfo = ratingLookupService.lookup(
                    request.getRatingCompany(), request.getRatingNumber());
            product.setRatingGrade(ratingInfo != null ? ratingInfo.getGrade() : null);
        }

        product.setCountry(request.getCountry());
        product.setYear(request.getYear());
        product.setMaterial(request.getMaterial());
        product.setDenomination(request.getDenomination());
        product.setWeight(request.getWeight());
        product.setStatus("ACTIVE");

        product = productRepository.save(product);
        return toResponse(product);
    }

    public Page<ProductResponse> searchProducts(
            String keyword, Long categoryId, String ratingCompany,
            String ratingGrade, Double minPrice, Double maxPrice,
            String country, Integer year, String sortBy, Pageable pageable) {
        // 使用 Specification 动态查询
        var spec = ProductSpecifications.withFilters(keyword, categoryId, ratingCompany,
                ratingGrade, minPrice, maxPrice, country, year, sortBy);
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        // map entity to response
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .stock(product.getStock())
                .status(product.getStatus())
                .ratingCompany(product.getRatingCompany())
                .ratingNumber(product.getRatingNumber())
                .ratingGrade(product.getRatingGrade())
                .country(product.getCountry())
                .year(product.getYear())
                .material(product.getMaterial())
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 2: 创建 RatingLookupService**

```java
package com.coinmarket.product.service;

import com.coinmarket.product.dto.RatingInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RatingLookupService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.rating-api.ngc.base-url}")
    private String ngcBaseUrl;

    @Value("${app.rating-api.pcgs.base-url}")
    private String pcgsBaseUrl;

    // 查询评级信息
    public RatingInfoResponse lookup(String company, String certNumber) {
        try {
            return switch (company.toUpperCase()) {
                case "NGC" -> lookupNgc(certNumber);
                case "PCGS" -> lookupPcgs(certNumber);
                case "PMG" -> lookupPmg(certNumber);
                default -> null;
            };
        } catch (Exception e) {
            log.warn("Rating lookup failed for {}: {}", company, certNumber, e);
            return null;
        }
    }

    private RatingInfoResponse lookupNgc(String certNumber) {
        // NGC API 调用示例
        String url = ngcBaseUrl + "/api/certificates/" + certNumber;
        var response = restTemplate.getForEntity(url, NgcResponse.class);
        if (response.getBody() != null) {
            return RatingInfoResponse.builder()
                    .grade(response.getBody().getGrade())
                    .certNumber(certNumber)
                    .verified(true)
                    .build();
        }
        return null;
    }

    private RatingInfoResponse lookupPcgs(String certNumber) {
        // PCGS API 调用（类似实现）
        return null;
    }

    private RatingInfoResponse lookupPmg(String certNumber) {
        // PMG API 调用（类似实现）
        return null;
    }
}
```

- [ ] **Step 3: 创建商品 Controller**

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String ratingCompany,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> result = productService.searchProducts(
                keyword, categoryId, ratingCompany, null,
                minPrice, maxPrice, null, null, null, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProduct(id));
    }
}
```

---

## 第五阶段：订单与支付

### Task 12: 数据库迁移 — 订单与支付表

**Files:**
- Create: `backend/src/main/resources/db/migration/V4__create_orders.sql`
- Create: `backend/src/main/resources/db/migration/V5__create_payments.sql`

- [ ] **Step 1: 订单表**

```sql
CREATE SCHEMA IF NOT EXISTS coin_order;

CREATE TABLE coin_order.orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(30) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    seller_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',
    -- status: PENDING_PAYMENT, PAID, SHIPPED, COMPLETED, CANCELLED, REFUNDING, REFUNDED
    total_amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    shipping_address TEXT,
    shipping_country VARCHAR(100),
    buyer_note TEXT,
    paid_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_order.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES coin_order.orders(id),
    product_id BIGINT NOT NULL,
    product_title VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL
);

CREATE TABLE coin_order.order_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES coin_order.orders(id),
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    operator VARCHAR(100),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer ON coin_order.orders(buyer_id);
CREATE INDEX idx_orders_seller ON coin_order.orders(seller_id);
CREATE INDEX idx_orders_no ON coin_order.orders(order_no);
```

- [ ] **Step 2: 支付表**

```sql
CREATE SCHEMA IF NOT EXISTS coin_payment;

CREATE TABLE coin_payment.payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_no VARCHAR(50) NOT NULL UNIQUE,
    order_no VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    -- WECHAT_PAY, ALIPAY, CREDIT_CARD, PAYPAL, PAYNOW, GRABPAY, FPX
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED
    gateway_transaction_id VARCHAR(200),
    gateway_response TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_order ON coin_payment.payment_transactions(order_no);
CREATE INDEX idx_payment_transaction_no ON coin_payment.payment_transactions(transaction_no);
```

---

### Task 13: 订单模块

**Files:**
- Create: `backend/src/main/java/com/coinmarket/order/entity/Order.java`
- Create: `backend/src/main/java/com/coinmarket/order/entity/OrderItem.java`
- Create: `backend/src/main/java/com/coinmarket/order/repository/OrderRepository.java`
- Create: `backend/src/main/java/com/coinmarket/order/service/OrderService.java`
- Create: `backend/src/main/java/com/coinmarket/order/controller/OrderController.java`
- Create: `backend/src/main/java/com/coinmarket/order/dto/OrderCreateRequest.java`
- Create: `backend/src/main/java/com/coinmarket/order/dto/OrderResponse.java`

- [ ] **Step 1: 创建 OrderService（核心订单状态机）**

```java
package com.coinmarket.order.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.order.dto.OrderCreateRequest;
import com.coinmarket.order.dto.OrderResponse;
import com.coinmarket.order.entity.Order;
import com.coinmarket.order.entity.OrderItem;
import com.coinmarket.order.entity.OrderLog;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.order.repository.OrderLogRepository;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.coinmarket.common.config.RabbitMqConfig.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderResponse createOrder(Long buyerId, OrderCreateRequest request) {
        // 校验商品
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setStatus("PENDING_PAYMENT");
        order.setTotalAmount(product.getPrice().multiply(
                java.math.BigDecimal.valueOf(request.getQuantity())));
        order.setCurrency(product.getCurrency());
        order.setShippingAddress(request.getShippingAddress());

        // 订单项
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductTitle(product.getTitle());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.setSubtotal(order.getTotalAmount());
        order.setItems(List.of(item));

        order = orderRepository.save(order);

        // 扣库存
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);

        // 记录日志
        saveOrderLog(order.getId(), null, "PENDING_PAYMENT", "系统", "订单创建");

        // 发送事件
        rabbitTemplate.convertAndSend(EXCHANGE_ORDER, ROUTING_KEY_PAYMENT, order.getId());

        return toResponse(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许取消");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
        saveOrderLog(orderId, "PENDING_PAYMENT", "CANCELLED", "买家", "买家取消订单");
    }

    @Transactional
    public void confirmCompletion(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
        saveOrderLog(orderId, null, "COMPLETED", "系统", "订单完成");
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void saveOrderLog(Long orderId, String from, String to, String operator, String note) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperator(operator);
        log.setNote(note);
        orderLogRepository.save(log);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        resp.setStatus(order.getStatus());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setCurrency(order.getCurrency());
        resp.setCreatedAt(order.getCreatedAt());
        return resp;
    }
}
```

---

### Task 14: 支付模块 — Gateway 抽象

**Files:**
- Create: `backend/src/main/java/com/coinmarket/payment/service/PaymentGateway.java` (interface)
- Create: `backend/src/main/java/com/coinmarket/payment/service/PaymentService.java`
- Create: `backend/src/main/java/com/coinmarket/payment/controller/PaymentController.java`
- Create: `backend/src/main/java/com/coinmarket/payment/dto/PaymentRequest.java`
- Create: `backend/src/main/java/com/coinmarket/payment/dto/PaymentResponse.java`
- Create: `backend/src/main/java/com/coinmarket/payment/service/StripeGateway.java`
- Create: `backend/src/main/java/com/coinmarket/payment/service/WechatPayGateway.java`
- Create: `backend/src/main/java/com/coinmarket/payment/service/AlipayGateway.java`
- Create: `backend/src/main/java/com/coinmarket/payment/service/PaypalGateway.java`
- Create: `backend/src/main/java/com/coinmarket/payment/service/PayNowGateway.java`
- Create: `backend/src/main/java/com/coinmarket/payment/service/GrabPayGateway.java`

- [ ] **Step 1: 定义 PaymentGateway 接口（策略模式）**

```java
package com.coinmarket.payment.service;

import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;

public interface PaymentGateway {
    String getMethod();  // 返回支付方式标识
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse queryPayment(String transactionNo);
    PaymentResponse refund(String transactionNo, java.math.BigDecimal amount);
    boolean handleWebhook(String payload, String signature);
}
```

- [ ] **Step 2: 创建 PaymentService（网关路由）**

```java
package com.coinmarket.payment.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<String, PaymentGateway> gatewayMap;

    public PaymentService(List<PaymentGateway> gateways) {
        this.gatewayMap = gateways.stream()
                .collect(Collectors.toMap(PaymentGateway::getMethod, g -> g));
    }

    public PaymentResponse createPayment(String method, PaymentRequest request) {
        PaymentGateway gateway = gatewayMap.get(method.toUpperCase());
        if (gateway == null) {
            throw new BusinessException("不支持的支付方式: " + method);
        }
        return gateway.createPayment(request);
    }

    public PaymentResponse queryPayment(String method, String transactionNo) {
        PaymentGateway gateway = gatewayMap.get(method.toUpperCase());
        if (gateway == null) {
            throw new BusinessException("不支持的支付方式: " + method);
        }
        return gateway.queryPayment(transactionNo);
    }

    public PaymentResponse refund(String method, String transactionNo, java.math.BigDecimal amount) {
        PaymentGateway gateway = gatewayMap.get(method.toUpperCase());
        if (gateway == null) {
            throw new BusinessException("不支持的支付方式: " + method);
        }
        return gateway.refund(transactionNo, amount);
    }
}
```

- [ ] **Step 3: 实现 Stripe 网关示例**

```java
package com.coinmarket.payment.service;

import com.coinmarket.payment.dto.PaymentRequest;
import com.coinmarket.payment.dto.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeGateway implements PaymentGateway {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        if (!secretKey.isEmpty()) {
            Stripe.apiKey = secretKey;
        }
    }

    @Override
    public String getMethod() {
        return "CREDIT_CARD";
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        // Stripe Checkout Session 创建
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.getReturnUrl() + "?success=true")
                .setCancelUrl(request.getReturnUrl() + "?success=false")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(request.getCurrency().toLowerCase())
                                                .setUnitAmount(request.getAmount()
                                                        .multiply(java.math.BigDecimal.valueOf(100))
                                                        .longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName("Coin Marketplace Order")
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("order_no", request.getOrderNo())
                .build();

        try {
            Session session = Session.create(params);
            return PaymentResponse.builder()
                    .paymentUrl(session.getUrl())
                    .transactionNo(session.getId())
                    .status("PENDING")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Stripe payment failed", e);
        }
    }

    @Override
    public PaymentResponse queryPayment(String transactionNo) { /* 实现 */ return null; }

    @Override
    public PaymentResponse refund(String transactionNo, java.math.BigDecimal amount) { /* 实现 */ return null; }

    @Override
    public boolean handleWebhook(String payload, String signature) { /* 实现 */ return false; }
}
```

---

## 第六阶段：多语言系统

### Task 15: 前后端多语言

**Files:**
- Create: `backend/src/main/resources/messages/messages.properties`
- Create: `backend/src/main/resources/messages/messages_zh_CN.properties`
- Create: `backend/src/main/resources/messages/messages_zh_TW.properties`
- Create: `backend/src/main/resources/messages/messages_ja.properties`
- Create: `frontend/store/src/i18n/locales/en.json`
- Create: `frontend/store/src/i18n/locales/zh-CN.json`
- Create: `frontend/store/src/i18n/locales/zh-TW.json`
- Create: `frontend/store/src/i18n/locales/ja.json`
- Create: `backend/src/main/java/com/coinmarket/common/i18n/MessageSourceConfig.java`
- Create: `frontend/store/src/i18n/index.js`

- [ ] **Step 1: 后端国际化配置**

```java
package com.coinmarket.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages/messages");
        source.setDefaultEncoding("UTF-8");
        source.setCacheSeconds(3600);
        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(List.of(
                Locale.ENGLISH,
                Locale.SIMPLIFIED_CHINESE,
                Locale.TRADITIONAL_CHINESE,
                Locale.JAPANESE
        ));
        return resolver;
    }
}
```

- [ ] **Step 2: 后端消息文件示例**

```properties
# messages.properties (English - default)
validation.username.required=Username is required
validation.username.length=Username must be 3-50 characters
validation.email.required=Email is required
validation.email.invalid=Invalid email format
validation.password.required=Password is required
validation.password.length=Password must be 8-100 characters
validation.language.required=Language preference is required
order.created=Order created successfully
order.paid=Payment successful
order.cancelled=Order cancelled
payment.method.unsupported=Payment method not supported
seller.application.submitted=Application submitted successfully
seller.application.approved=Your seller application has been approved
seller.application.rejected=Your seller application has been rejected
```

```properties
# messages_zh_CN.properties (Simplified Chinese)
validation.username.required=用户名不能为空
validation.username.length=用户名长度需在3-50个字符之间
validation.email.required=邮箱不能为空
validation.email.invalid=邮箱格式不正确
validation.password.required=密码不能为空
validation.password.length=密码长度需在8-100个字符之间
validation.language.required=请选择语言偏好
order.created=订单创建成功
order.paid=支付成功
order.cancelled=订单已取消
payment.method.unsupported=不支持的支付方式
seller.application.submitted=入驻申请已提交
seller.application.approved=您的卖家申请已通过
seller.application.rejected=您的卖家申请未通过
```

- [ ] **Step 3: 前端 i18n 配置 (Vue I18n)**

```javascript
// frontend/store/src/i18n/index.js
import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import zhCN from './locales/zh-CN.json'
import zhTW from './locales/zh-TW.json'
import ja from './locales/ja.json'

const messages = { en, 'zh-CN': zhCN, 'zh-TW': zhTW, ja }

const i18n = createI18n({
  locale: localStorage.getItem('language') || 'en',
  fallbackLocale: 'en',
  messages
})

export default i18n
```

- [ ] **Step 4: 语言切换组件**

```vue
<!-- LanguageSwitcher.vue -->
<template>
  <el-dropdown @command="switchLanguage">
    <span class="lang-switcher">
      {{ currentLabel }}
      <el-icon><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="en">English</el-dropdown-item>
        <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
        <el-dropdown-item command="zh-TW">繁體中文</el-dropdown-item>
        <el-dropdown-item command="ja">日本語</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale, t } = useI18n()

const labels = { en: 'English', 'zh-CN': '简体中文', 'zh-TW': '繁體中文', ja: '日本語' }
const currentLabel = computed(() => labels[locale.value])

function switchLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)
}
</script>
```

---

## 第七阶段：管理后台

### Task 16: 管理后台后端 API

**Files:**
- Create: `backend/src/main/java/com/coinmarket/admin/controller/AdminUserController.java`
- Create: `backend/src/main/java/com/coinmarket/admin/controller/AdminProductController.java`
- Create: `backend/src/main/java/com/coinmarket/admin/controller/AdminOrderController.java`
- Create: `backend/src/main/java/com/coinmarket/admin/controller/AdminSellerController.java`
- Create: `backend/src/main/java/com/coinmarket/admin/controller/AdminPaymentController.java`
- Create: `backend/src/main/java/com/coinmarket/admin/controller/AdminContentController.java`

- [ ] **Step 1: 管理员用户管理**

```java
package com.coinmarket.admin.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.user.dto.UserProfileResponse;
import com.coinmarket.user.entity.User;
import com.coinmarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<PageResponse<UserProfileResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        var pageable = PageRequest.of(page, size);
        var users = userRepository.findAll(pageable);
        var profiles = users.map(u -> UserProfileResponse.builder()
                .id(u.getId()).username(u.getUsername())
                .email(u.getEmail()).phone(u.getPhone())
                .displayName(u.getDisplayName())
                .preferredLanguage(u.getPreferredLanguage())
                .emailVerified(u.isEmailVerified())
                .roles(u.getRoles().stream()
                        .map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()))
                .build());
        return ApiResponse.success(PageResponse.from(profiles));
    }

    @PutMapping("/{id}/toggle-status")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<Void> updateRoles(@PathVariable Long id, @RequestBody java.util.Set<Long> roleIds) {
        // 管理员修改用户角色
        return ApiResponse.success(null);
    }
}
```

---

## 第八阶段：前端商城

### Task 17: 商城前台（买家端）— 核心页面

**Files:**
- Create: `frontend/store/package.json`
- Create: `frontend/store/vite.config.js`
- Create: `frontend/store/index.html`
- Create: `frontend/store/src/main.js`
- Create: `frontend/store/src/App.vue`
- Create: `frontend/store/src/router/index.js`
- Create: `frontend/store/src/api/index.js`
- Create: `frontend/store/src/views/Home.vue`
- Create: `frontend/store/src/views/ProductList.vue`
- Create: `frontend/store/src/views/ProductDetail.vue`
- Create: `frontend/store/src/views/Cart.vue`
- Create: `frontend/store/src/views/Checkout.vue`
- Create: `frontend/store/src/views/OrderList.vue`
- Create: `frontend/store/src/views/OrderDetail.vue`
- Create: `frontend/store/src/views/Auth.vue`
- Create: `frontend/store/src/store/auth.js`
- Create: `frontend/store/src/store/cart.js`

- [ ] **Step 1: 创建 Root 前端配置**

```json
// frontend/store/package.json (与 seller 类似)
{
  "name": "coin-marketplace-store",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.7.0",
    "axios": "^1.7.0",
    "vue-i18n": "^9.13.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.2.0"
  }
}
```

- [ ] **Step 2: 商品搜索和列表页面**

```vue
<!-- frontend/store/src/views/ProductList.vue -->
<template>
  <div class="product-list">
    <!-- 筛选栏 -->
    <el-form :inline="true" class="filter-bar">
      <el-form-item :label="$t('product.search')">
        <el-input v-model="filters.keyword" :placeholder="$t('product.searchPlaceholder')"
                  @keyup.enter="search" clearable />
      </el-form-item>
      <el-form-item :label="$t('product.category')">
        <el-select v-model="filters.categoryId" :placeholder="$t('product.allCategories')" clearable>
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('product.ratingCompany')">
        <el-select v-model="filters.ratingCompany" clearable>
          <el-option label="NGC" value="NGC" />
          <el-option label="PCGS" value="PCGS" />
          <el-option label="PMG" value="PMG" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('product.priceRange')">
        <el-input-number v-model="filters.minPrice" :min="0" style="width:120px" />
        <span style="margin:0 8px">-</span>
        <el-input-number v-model="filters.maxPrice" :min="0" style="width:120px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">{{ $t('common.search') }}</el-button>
      </el-form-item>
    </el-form>

    <!-- 商品网格 -->
    <el-row :gutter="20">
      <el-col v-for="product in products" :key="product.id" :xs="12" :sm="8" :md="6" :lg="4">
        <el-card class="product-card" :body-style="{ padding: '12px' }"
                 @click="$router.push(`/products/${product.id}`)">
          <img :src="product.primaryImage || '/placeholder.png'" class="product-image" />
          <h4 class="product-title">{{ product.title }}</h4>
          <div class="product-meta">
            <span v-if="product.ratingGrade" class="rating-badge">
              {{ product.ratingCompany }} {{ product.ratingGrade }}
            </span>
          </div>
          <div class="product-price">${{ product.price }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      @current-change="loadProducts"
      layout="prev, pager, next"
      class="pagination" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'

const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const filters = reactive({
  keyword: '',
  categoryId: null,
  ratingCompany: null,
  minPrice: null,
  maxPrice: null
})

async function loadProducts() {
  const params = { page: page.value - 1, size: size.value, ...filters }
  const res = await api.get('/api/products', { params })
  products.value = res.data.data.content
  total.value = res.data.data.totalElements
}

async function search() {
  page.value = 1
  await loadProducts()
}

onMounted(loadProducts)
</script>
```

- [ ] **Step 3: 商品详情页面（含评级展示）**

```vue
<!-- frontend/store/src/views/ProductDetail.vue (关键部分) -->
<template>
  <div class="product-detail" v-loading="loading">
    <el-row :gutter="24">
      <!-- 商品图片 -->
      <el-col :span="10">
        <el-image :src="product.primaryImage" fit="contain" class="main-image" />
        <div class="thumbnails">
          <el-image v-for="img in product.images" :key="img" :src="img" class="thumb" />
        </div>
      </el-col>

      <!-- 商品信息 -->
      <el-col :span="14">
        <h1>{{ product.title }}</h1>

        <!-- 评级信息展示 -->
        <el-card v-if="product.ratingCompany" class="rating-card" shadow="never">
          <template #header>
            <span>{{ $t('product.ratingInfo') }}</span>
          </template>
          <el-descriptions :column="2" size="small">
            <el-descriptions-item :label="$t('product.ratingCompany')">
              <el-tag>{{ product.ratingCompany }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('product.ratingNumber')">
              {{ product.ratingNumber }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('product.ratingGrade')">
              <el-tag type="success">{{ product.ratingGrade }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 价格与购买 -->
        <div class="price-section">
          <span class="price">{{ product.currency }} ${{ product.price }}</span>
        </div>
        <div class="actions">
          <el-input-number v-model="quantity" :min="1" :max="product.stock" />
          <el-button type="primary" size="large" @click="addToCart">
            {{ $t('product.addToCart') }}
          </el-button>
          <el-button type="danger" size="large" @click="buyNow">
            {{ $t('product.buyNow') }}
          </el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
```

---

## 第九阶段：搜索 & 拍卖预留

### Task 18: Elasticsearch 搜索

**Files:**
- Create: `backend/src/main/java/com/coinmarket/search/service/SearchService.java`
- Create: `backend/src/main/java/com/coinmarket/search/controller/SearchController.java`
- Create: `backend/src/main/resources/elasticsearch/product-index.json`

- [ ] **Step 1: 商品搜索索引映射**

```json
{
  "index": "products",
  "settings": {
    "analysis": {
      "analyzer": {
        "default": {
          "type": "standard"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "title": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "description": { "type": "text" },
      "price": { "type": "double" },
      "currency": { "type": "keyword" },
      "ratingCompany": { "type": "keyword" },
      "ratingNumber": { "type": "keyword" },
      "ratingGrade": { "type": "keyword" },
      "country": { "type": "keyword" },
      "year": { "type": "integer" },
      "material": { "type": "keyword" },
      "categoryId": { "type": "long" },
      "status": { "type": "keyword" },
      "sellerId": { "type": "long" },
      "createdAt": { "type": "date" }
    }
  }
}
```

- [ ] **Step 2: SearchService 实现**

```java
package com.coinmarket.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.coinmarket.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;

    public List<ProductResponse> search(String keyword, int page, int size) {
        try {
            SearchResponse<ProductResponse> response = esClient.search(s -> s
                    .index("products")
                    .query(q -> q
                            .multiMatch(t -> t
                                    .fields(List.of("title^3", "description", "ratingNumber^2"))
                                    .query(keyword)))
                    .from(page * size)
                    .size(size),
                    ProductResponse.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Search failed", e);
        }
    }
}
```

### Task 19: 拍卖模块预留扩展点

**Files:**
- Create: `backend/src/main/java/com/coinmarket/common/event/AuctionEvents.java`
- Create: `backend/src/main/resources/db/migration/V6__reserve_auction.sql`

- [ ] **Step 1: 拍卖事件定义（预留）**

```java
package com.coinmarket.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

// 未来拍卖系统监听的预留事件
public final class AuctionEvents {

    @Data
    @AllArgsConstructor
    public static class OrderCompletedEvent {
        private Long orderId;
        private Long buyerId;
        private Long sellerId;
        private BigDecimal amount;
        // 拍卖系统可用此事件触发自动结算
    }

    @Data
    @AllArgsConstructor
    public static class ProductListedEvent {
        private Long productId;
        private Long sellerId;
        // 拍卖系统可用此事件同步商品信息
    }
}
```

- [ ] **Step 2: 拍卖预留数据库表**

```sql
-- 预留拍卖模块表空间（V6__reserve_auction.sql）
CREATE SCHEMA IF NOT EXISTS coin_auction;

-- 拍卖品表（预留）
CREATE TABLE IF NOT EXISTS coin_auction.auction_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    start_price DECIMAL(12, 2) NOT NULL,
    reserve_price DECIMAL(12, 2),
    current_bid DECIMAL(12, 2),
    bidder_id BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    extended_times INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 出价记录表（预留）
CREATE TABLE IF NOT EXISTS coin_auction.bids (
    id BIGSERIAL PRIMARY KEY,
    auction_item_id BIGINT NOT NULL REFERENCES coin_auction.auction_items(id),
    bidder_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    is_auto_bid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_auction.auction_items IS '拍卖品表—预留，二期实现';
COMMENT ON TABLE coin_auction.bids IS '出价记录—预留，二期实现';
```

---

## 验证 & 上线

### Task 20: 测试与部署

- [ ] **Step 1: 安全测试清单**
  - SQL 注入检查（所有 @RequestParam 参数）
  - JWT Token 刷新机制验证
  - 卖家资料锁定验证（确认买家无法修改已认证资料）
  - 文件上传类型与大小限制
  - XSS 过滤（Element Plus 默认支持）

- [ ] **Step 2: 部署脚本**

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/coin-marketplace-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

- [ ] **Step 3: CI/CD 基础配置**

```yaml
# .github/workflows/deploy.yml
name: Deploy
on:
  push:
    branches: [main]
jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build Backend
        run: cd backend && mvn clean package -DskipTests
      - name: Build Frontend
        run: |
          cd frontend/store && npm install && npm run build
          cd ../seller && npm install && npm run build
          cd ../admin && npm install && npm run build
      - name: Deploy to Server
        run: |
          # scp or docker push
```

---

## 计划覆盖自查

| 需求 | 对应任务 |
|------|---------|
| B2C+C2C 混合模式 | Task 6 (注册默认 buyer) + Task 8 (seller 认证 + 角色) |
| 卖家身份认证 / 电话实名 | Task 8 (SellerApplication + 资料锁定) |
| 认证后卖家不可自改信息 | Task 8 (SellerProfile.locked = true) |
| 第三方评级公司支持 | Task 11 (RatingLookupService + 商品字段) |
| 四国语言 (zh-CN/zh-TW/en/ja) | Task 15 (后端 messages + 前端 i18n) |
| 7种支付方式 | Task 14 (PaymentGateway 策略模式) |
| 微信 / 支付宝 / PayPal | Task 14 (各自 Gateway 实现) |
| 新加坡 PayNow / 马来西亚 GrabPay/FPX | Task 14 (各自 Gateway 实现) |
| 信用卡 (Stripe) | Task 14 (StripeGateway) |
| 系统安全 (JWT/RBAC/加密) | Task 5 (SecurityConfig + JWT) |
| 便捷注册 (3步以内) | Task 6 (AuthController: 注册即登录) |
| 模块化 / 可扩展 | Task 1 (模块化 pom) + Task 19 (事件预留) |
| 拍卖系统扩展点 | Task 19 (事件 + 预留表 + 数据库 Schema) |
| 管理后台 | Task 16 (Admin Controllers) |
