# LogCommons - 统一日志操作组件

## 简介

LogCommons 是一个基于 Spring Boot 的统一日志操作组件，提供了简洁易用的 API 来记录不同类型的日志。组件采用门面模式设计，支持业务、性能、安全、异常四类日志记录，内置敏感信息脱敏和异步处理能力。

## 特性

- 🚀 **门面模式设计** - 提供统一简洁的 API 接口
- 📊 **分类日志记录** - 支持业务、性能、安全、异常四类日志
- 🔒 **敏感信息脱敏** - 自动检测和脱敏敏感信息
- ⚡ **异步处理** - 支持异步日志处理，不影响业务性能
- 📈 **指标收集** - 集成 Micrometer，提供丰富的监控指标
- 🔧 **高度可配置** - 支持灵活的配置和自定义扩展
- 🔄 **降级处理** - 异常情况下自动降级保证日志正常输出

## 快速开始

### 1. 添加依赖

在你的`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>com.david</groupId>
    <artifactId>log-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在`application.yml`中添加配置（可选，组件提供合理默认值）：

```yaml
log-commons:
  enabled: true
  default-async: true
  modules:
    business:
      enabled: true
      level: INFO
    performance:
      enabled: true
      level: DEBUG
    security:
      enabled: true
      level: WARN
    exception:
      enabled: true
      level: ERROR
```

### 3. 使用示例

```java
import static com.david.log.commons.core.LogUtils.*;

@Service
public class UserService {

    public void createUser(User user) {
        try {
            // 记录业务操作审计
            business().audit(user.getId(), "createUser", "started", user.getUsername());

            // 性能监控
            long startTime = System.currentTimeMillis();

            // 业务逻辑...
            userRepository.save(user);

            long duration = System.currentTimeMillis() - startTime;
            performance().timing("createUser", duration, user.getId());

            // 记录成功事件
            business().event("USER_CREATED", user.getId(), user.getUsername());

        } catch (Exception e) {
            // 记录异常
            exception().business("createUser", e, user);
            throw e;
        }
    }

    public void loginUser(String username, String password, String ip) {
        boolean success = authenticate(username, password);

        // 记录安全日志
        security().login(username, "password", success, ip, getUserAgent());

        if (!success) {
            // 记录安全威胁
            security().threat("FAILED_LOGIN", "MEDIUM",
                "用户登录失败", ip);
        }
    }
}
```

## API 文档

### 业务日志 (Business)

用于记录用户操作审计、业务流程追踪等。

```java
// 用户操作审计
business().audit("user123", "login", "success", "额外信息");

// 业务流程追踪
business().trace("order_001", "payment", "completed", orderData);

// 业务事件记录
business().event("USER_REGISTERED", userId, email);

// 业务指标记录
business().metric("daily_orders", 1250, "region", "beijing");
```

### 性能日志 (Performance)

用于记录执行时间、资源使用等性能指标。

```java
// 方法执行时间
performance().timing("getUserInfo", 150, userId);

// SQL执行性能
performance().sql("SELECT * FROM users WHERE id = ?", 25, 1);

// HTTP请求性能
performance().http("POST", "/api/users", 200, 300);

// 内存使用情况
performance().memory("UserCache", 256, 1024);

// QPS统计
performance().qps("/api/users", 100.5, 50.2);
```

### 安全日志 (Security)

用于记录认证授权、安全事件等。

```java
// 用户登录
security().login("user123", "password", true, "192.168.1.1", userAgent);

// 用户登出
security().logout("user123", "session_001", "timeout");

// 权限检查
security().permission("user123", "user_data", "read", true);

// 安全威胁
security().threat("SQL_INJECTION", "HIGH", "检测到SQL注入攻击", "192.168.1.100");

// 数据访问
security().dataAccess("user123", "sensitive_data", "read", 10);
```

### 异常日志 (Exception)

用于记录各类异常信息。

```java
// 业务异常
exception().business("createUser", exception, userData);

// 系统异常
exception().system("UserService", exception, "CRITICAL");

// 网络异常
exception().network("http://api.example.com", exception, 3);

// 数据库异常
exception().database("INSERT INTO users...", exception, param1, param2);

// 验证异常
exception().validation("email", "invalid-email", "email_format", "邮箱格式错误");
```

## 配置说明

### 全局配置

| 配置项                   | 默认值 | 说明                 |
| ------------------------ | ------ | -------------------- |
| `enabled`                | `true` | 是否启用日志组件     |
| `default-async`          | `true` | 默认是否使用异步处理 |
| `buffer-size`            | `1000` | 异步缓冲区大小       |
| `flush-interval-seconds` | `5`    | 缓冲区刷新间隔       |
| `batch-size`             | `50`   | 批处理大小           |

### 模块配置

每个日志模块都支持以下配置：

| 配置项            | 默认值 | 说明             |
| ----------------- | ------ | ---------------- |
| `enabled`         | `true` | 是否启用该模块   |
| `level`           | `INFO` | 日志级别         |
| `async`           | `true` | 是否使用异步处理 |
| `metrics-enabled` | `true` | 是否启用指标收集 |

### 敏感信息脱敏

组件内置敏感信息脱敏功能，自动检测以下类型的敏感信息：

- 密码相关关键词
- 手机号码
- 邮箱地址
- 身份证号
- 银行卡号
- API 密钥

## 监控指标

组件集成 Micrometer 框架，提供以下监控指标：

- `log.operations.success` - 成功操作计数
- `log.operations.error` - 失败操作计数
- `log.operations.duration` - 操作执行时间

指标包含以下标签：

- `type` - 日志类型（BUSINESS/PERFORMANCE/SECURITY/EXCEPTION）
- `level` - 日志级别
- `module` - 模块名称
- `async` - 是否异步处理

## 最佳实践

### 1. 合理选择日志类型

- **业务日志**: 用于记录关键业务事件和用户操作
- **性能日志**: 用于监控性能瓶颈和资源使用
- **安全日志**: 用于安全审计和威胁检测
- **异常日志**: 用于错误诊断和问题排查

### 2. 异步 vs 同步

- **异步**: 适用于高频率的日志记录，不影响业务性能
- **同步**: 适用于关键错误和安全事件，确保及时输出

### 3. 敏感信息处理

组件会自动脱敏常见敏感信息，但建议：

- 避免在日志中记录完整的敏感数据
- 使用业务 ID 代替敏感信息
- 定期审查日志输出内容

### 4. 性能考虑

- 合理配置缓冲区大小
- 监控缓冲区使用率
- 根据业务量调整刷新间隔

## 故障排除

### 常见问题

1. **组件未正确初始化**

   - 确保在 Spring 容器中正确配置
   - 检查依赖注入是否正常

2. **日志未输出**

   - 检查模块是否启用
   - 确认日志级别配置
   - 查看缓冲区状态

3. **性能影响**
   - 启用异步处理
   - 调整缓冲区配置
   - 降低日志级别

### 调试配置

```yaml
logging:
  level:
    com.david.log.commons: DEBUG

log-commons:
  metrics:
    enabled: true
    detailed: true
```

## 扩展开发

### 自定义脱敏器

```java
@Component
public class CustomSensitiveDataMasker implements SensitiveDataMasker {
    // 实现自定义脱敏逻辑
}
```

### 自定义格式化器

```java
@Component
public class CustomLogFormatter implements LogFormatter {
    // 实现自定义格式化逻辑
}
```

## 版本信息

- **当前版本**: 0.0.1-SNAPSHOT
- **最低 Java 版本**: 17
- **Spring Boot 版本**: 3.x
- **依赖框架**: SLF4J, Micrometer

## 许可证

本项目遵循 MIT 许可证。
