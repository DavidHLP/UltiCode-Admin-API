# 日志顺序性优化设计文档

## 1. 设计目标

确保SpringOJ日志模块的顺序性，基于@Slf4j为基础，最小化重写。

## 2. 核心解决方案

### 2.1 全局序列号机制
- 使用`AtomicLong`生成全局递增序列号
- 每条日志添加序列号前缀：`[SEQ:00001]`
- 保证严格的时序关系，即使在同一毫秒内

### 2.2 线程安全的时间戳
- 使用`ThreadLocal<DateTimeFormatter>`避免格式化竞争
- 纳秒级时间戳辅助排序：`HH:mm:ss.SSS.nnnnnnn`

### 2.3 简化架构设计
- **保留现有API兼容性**
- **简化BaseLogger**：去除复杂的自定义格式化
- **直接使用@Slf4j**：减少中间层处理
- **最小化重写**：只在必要时增强顺序性

## 3. 架构设计

```mermaid
graph TD
    A[LogUtils API] --> B[SequentialLoggerFactory]
    B --> C[SequentialLogger包装器]
    C --> D[@Slf4j Logger]
    C --> E[序列号生成器]
    E --> F[AtomicLong Counter]
```

## 4. 实现策略

### 4.1 核心组件
1. **SequentialLoggerFactory** - 序列化日志工厂
2. **SequentialLogger** - 序列化日志包装器
3. **LogSequenceGenerator** - 序列号生成器
4. **ThreadSafeFormatter** - 线程安全格式化器

### 4.2 最小化改动原则
- 保持现有LogUtils API不变
- BaseLogger简化为序列化包装
- 其他Logger类继承简化后的BaseLogger
- 业务日志等特殊需求通过MDC实现

## 5. 顺序性保证机制

### 5.1 多线程顺序保证
```java
// 序列号 + 时间戳 + 线程信息
[SEQ:00001][2024-08-31 16:30:10.123.456789][Thread-1] 日志消息
[SEQ:00002][2024-08-31 16:30:10.123.456790][Thread-2] 日志消息
```

### 5.2 异步处理支持
- 预留AsyncLogProcessor接口
- 支持有序异步日志队列
- 批量写入优化

## 6. 性能优化

### 6.1 高性能设计
- AtomicLong序列号生成：纳秒级性能
- ThreadLocal时间格式化：避免锁竞争
- 最小化字符串拼接：使用StringBuilder
- 延迟初始化：按需创建组件

### 6.2 内存管理
- 无状态设计：避免内存泄漏
- 对象池复用：减少GC压力

## 7. 兼容性保证

- 现有API保持100%兼容
- 配置无缝迁移
- 日志格式向后兼容（可配置）
