package com.david.commons.redis.exception;

/**
 * Redis Commons 错误码定义
 *
 * @author David
 */
public final class RedisErrorCodes {

    private RedisErrorCodes() {
        // 工具类，禁止实例化
    }

    // ========== 连接相关错误码 ==========

    /**
     * Redis 连接失败
     */
    public static final String CONNECTION_FAILED = "REDIS_CONNECTION_FAILED";

    /**
     * Redis 连接超时
     */
    public static final String CONNECTION_TIMEOUT = "REDIS_CONNECTION_TIMEOUT";

    /**
     * Redis 连接池耗尽
     */
    public static final String CONNECTION_POOL_EXHAUSTED = "REDIS_CONNECTION_POOL_EXHAUSTED";

    /**
     * Redis 连接已关闭
     */
    public static final String CONNECTION_CLOSED = "REDIS_CONNECTION_CLOSED";

    // ========== 序列化相关错误码 ==========

    /**
     * 序列化失败
     */
    public static final String SERIALIZATION_FAILED = "REDIS_SERIALIZATION_FAILED";

    /**
     * 反序列化失败
     */
    public static final String DESERIALIZATION_FAILED = "REDIS_DESERIALIZATION_FAILED";

    /**
     * 不支持的序列化类型
     */
    public static final String UNSUPPORTED_SERIALIZATION_TYPE = "REDIS_UNSUPPORTED_SERIALIZATION_TYPE";

    /**
     * 序列化器未找到
     */
    public static final String SERIALIZER_NOT_FOUND = "REDIS_SERIALIZER_NOT_FOUND";

    // ========== 锁相关错误码 ==========

    /**
     * 获取锁失败
     */
    public static final String LOCK_ACQUISITION_FAILED = "REDIS_LOCK_ACQUISITION_FAILED";

    /**
     * 锁获取超时
     */
    public static final String LOCK_ACQUISITION_TIMEOUT = "REDIS_LOCK_ACQUISITION_TIMEOUT";

    /**
     * 锁释放失败
     */
    public static final String LOCK_RELEASE_FAILED = "REDIS_LOCK_RELEASE_FAILED";

    /**
     * 锁已过期
     */
    public static final String LOCK_EXPIRED = "REDIS_LOCK_EXPIRED";

    /**
     * 锁不存在
     */
    public static final String LOCK_NOT_EXISTS = "REDIS_LOCK_NOT_EXISTS";

    /**
     * 锁被其他线程持有
     */
    public static final String LOCK_HELD_BY_OTHER_THREAD = "REDIS_LOCK_HELD_BY_OTHER_THREAD";

    /**
     * 锁获取被中断
     */
    public static final String LOCK_INTERRUPTED = "REDIS_LOCK_INTERRUPTED";

    /**
     * 锁超时
     */
    public static final String LOCK_TIMEOUT = "REDIS_LOCK_TIMEOUT";

    /**
     * 锁操作失败
     */
    public static final String LOCK_OPERATION_FAILED = "REDIS_LOCK_OPERATION_FAILED";

    /**
     * 强制解锁失败
     */
    public static final String LOCK_FORCE_UNLOCK_FAILED = "REDIS_LOCK_FORCE_UNLOCK_FAILED";

    /**
     * 不支持的锁类型
     */
    public static final String UNSUPPORTED_LOCK_TYPE = "REDIS_UNSUPPORTED_LOCK_TYPE";

    // ========== 缓存相关错误码 ==========

    /**
     * 缓存操作失败
     */
    public static final String CACHE_OPERATION_FAILED = "REDIS_CACHE_OPERATION_FAILED";

    /**
     * 缓存键为空
     */
    public static final String CACHE_KEY_EMPTY = "REDIS_CACHE_KEY_EMPTY";

    /**
     * 缓存值为空
     */
    public static final String CACHE_VALUE_EMPTY = "REDIS_CACHE_VALUE_EMPTY";

    /**
     * 缓存过期时间无效
     */
    public static final String CACHE_TTL_INVALID = "REDIS_CACHE_TTL_INVALID";

    /**
     * 缓存注解配置错误
     */
    public static final String CACHE_ANNOTATION_CONFIG_ERROR = "REDIS_CACHE_ANNOTATION_CONFIG_ERROR";

    /**
     * SpEL 表达式解析失败
     */
    public static final String SPEL_EXPRESSION_PARSE_FAILED = "REDIS_SPEL_EXPRESSION_PARSE_FAILED";

    // ========== 配置相关错误码 ==========

    /**
     * 配置参数无效
     */
    public static final String CONFIG_PARAMETER_INVALID = "REDIS_CONFIG_PARAMETER_INVALID";

    /**
     * 配置文件加载失败
     */
    public static final String CONFIG_FILE_LOAD_FAILED = "REDIS_CONFIG_FILE_LOAD_FAILED";

    /**
     * 自动配置失败
     */
    public static final String AUTO_CONFIGURATION_FAILED = "REDIS_AUTO_CONFIGURATION_FAILED";

    // ========== 操作相关错误码 ==========

    /**
     * 操作超时
     */
    public static final String OPERATION_TIMEOUT = "REDIS_OPERATION_TIMEOUT";

    /**
     * 操作被中断
     */
    public static final String OPERATION_INTERRUPTED = "REDIS_OPERATION_INTERRUPTED";

    /**
     * 批量操作部分失败
     */
    public static final String BATCH_OPERATION_PARTIAL_FAILED = "REDIS_BATCH_OPERATION_PARTIAL_FAILED";

    /**
     * 键不存在
     */
    public static final String KEY_NOT_EXISTS = "REDIS_KEY_NOT_EXISTS";

    /**
     * 数据类型不匹配
     */
    public static final String DATA_TYPE_MISMATCH = "REDIS_DATA_TYPE_MISMATCH";

    // ========== 监控相关错误码 ==========

    /**
     * 健康检查失败
     */
    public static final String HEALTH_CHECK_FAILED = "REDIS_HEALTH_CHECK_FAILED";

    /**
     * 指标收集失败
     */
    public static final String METRICS_COLLECTION_FAILED = "REDIS_METRICS_COLLECTION_FAILED";

    // ========== 防护相关错误码 ==========

    /**
     * 布隆过滤器初始化失败
     */
    public static final String BLOOM_FILTER_INIT_FAILED = "REDIS_BLOOM_FILTER_INIT_FAILED";

    /**
     * 熔断器开启
     */
    public static final String CIRCUIT_BREAKER_OPEN = "REDIS_CIRCUIT_BREAKER_OPEN";

    /**
     * 限流触发
     */
    public static final String RATE_LIMIT_EXCEEDED = "REDIS_RATE_LIMIT_EXCEEDED";
}