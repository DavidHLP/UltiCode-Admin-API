package com.david.redis.commons.core.operations.support;

/**
 * Redis操作类型枚举
 *
 * <p>
 * 定义所有支持的Redis操作类型，用于统一操作标识和日志记录
 *
 * @author David
 */
public enum RedisOperationType {

    // ===== String Operations =====
    /** 设置键值 */
    SET("SET", "设置键值"),
    /** 设置键值并指定过期时间 */
    SETEX("SETEX", "设置键值并指定过期时间"),
    /** 获取键值 */
    GET("GET", "获取键值"),
    /** 删除键 */
    DEL("DEL", "删除键"),
    /** 设置键过期时间 */
    EXPIRE("EXPIRE", "设置键过期时间"),
    /** 检查键是否存在 */
    EXISTS("EXISTS", "检查键是否存在"),
    /** 获取键的剩余过期时间 */
    TTL("TTL", "获取键的剩余过期时间"),
    /** 获取匹配模式的键 */
    KEYS("KEYS", "获取匹配模式的键"),
    /** 扫描匹配模式的键 */
    SCAN("SCAN", "扫描匹配模式的键"),
    /** 批量获取键值 */
    MGET("MGET", "批量获取键值"),
    /** 批量设置键值 */
    MSET("MSET", "批量设置键值"),

    // ===== Hash Operations =====
    /** 设置哈希字段值 */
    HSET("HSET", "设置哈希字段值"),
    /** 获取哈希字段值 */
    HGET("HGET", "获取哈希字段值"),
    /** 删除哈希字段 */
    HDEL("HDEL", "删除哈希字段"),
    /** 检查哈希字段是否存在 */
    HEXISTS("HEXISTS", "检查哈希字段是否存在"),
    /** 获取哈希所有字段和值 */
    HGETALL("HGETALL", "获取哈希所有字段和值"),
    /** 获取哈希所有字段 */
    HKEYS("HKEYS", "获取哈希所有字段"),
    /** 获取哈希所有值 */
    HVALS("HVALS", "获取哈希所有值"),

    // ===== List Operations =====
    /** 从列表左侧推入元素 */
    LPUSH("LPUSH", "从列表左侧推入元素"),
    /** 从列表右侧推入元素 */
    RPUSH("RPUSH", "从列表右侧推入元素"),
    /** 从列表左侧弹出元素 */
    LPOP("LPOP", "从列表左侧弹出元素"),
    /** 从列表右侧弹出元素 */
    RPOP("RPOP", "从列表右侧弹出元素"),
    /** 获取列表长度 */
    LLEN("LLEN", "获取列表长度"),
    /** 获取列表范围内的元素 */
    LRANGE("LRANGE", "获取列表范围内的元素"),

    // ===== Set Operations =====
    /** 向集合添加成员 */
    SADD("SADD", "向集合添加成员"),
    /** 从集合移除成员 */
    SREM("SREM", "从集合移除成员"),
    /** 检查集合成员是否存在 */
    SISMEMBER("SISMEMBER", "检查集合成员是否存在"),
    /** 获取集合所有成员 */
    SMEMBERS("SMEMBERS", "获取集合所有成员"),
    /** 获取集合成员数量 */
    SCARD("SCARD", "获取集合成员数量"),

    // ===== Sorted Set Operations =====
    /** 向有序集合添加成员 */
    ZADD("ZADD", "向有序集合添加成员"),
    /** 从有序集合移除成员 */
    ZREM("ZREM", "从有序集合移除成员"),
    /** 获取有序集合成员数量 */
    ZCARD("ZCARD", "获取有序集合成员数量"),
    /** 获取有序集合范围内的成员 */
    ZRANGE("ZRANGE", "获取有序集合范围内的成员"),
    /** 获取有序集合成员分数 */
    ZSCORE("ZSCORE", "获取有序集合成员分数"),

    // ===== Transaction Operations =====
    /** 开始事务 */
    MULTI("MULTI", "开始事务"),
    /** 提交事务 */
    EXEC("EXEC", "提交事务"),
    /** 取消事务 */
    DISCARD("DISCARD", "取消事务"),
    /** 监视键 */
    WATCH("WATCH", "监视键"),
    /** 取消监视 */
    UNWATCH("UNWATCH", "取消监视"),

    // ===== Lock Operations =====
    /** 尝试获取锁 */
    TRY_LOCK("TRY_LOCK", "尝试获取锁"),
    /** 使用默认参数尝试获取锁 */
    TRY_LOCK_DEFAULT("TRY_LOCK_DEFAULT", "使用默认参数尝试获取锁"),
    /** 在锁保护下执行操作 */
    EXECUTE_WITH_LOCK("EXECUTE_WITH_LOCK", "在锁保护下执行操作"),
    /** 在锁保护下执行操作（无返回值） */
    EXECUTE_WITH_LOCK_VOID("EXECUTE_WITH_LOCK_VOID", "在锁保护下执行操作（无返回值）"),
    /** 使用默认参数在锁保护下执行操作 */
    EXECUTE_WITH_LOCK_DEFAULT("EXECUTE_WITH_LOCK_DEFAULT", "使用默认参数在锁保护下执行操作"),
    /** 使用默认参数在锁保护下执行操作（无返回值） */
    EXECUTE_WITH_LOCK_DEFAULT_VOID("EXECUTE_WITH_LOCK_DEFAULT_VOID", "使用默认参数在锁保护下执行操作（无返回值）"),
    /** 在锁保护下执行操作并重试 */
    EXECUTE_WITH_LOCK_RETRY("EXECUTE_WITH_LOCK_RETRY", "在锁保护下执行操作并重试"),
    /** 在锁保护下执行操作并重试（无返回值） */
    EXECUTE_WITH_LOCK_RETRY_VOID("EXECUTE_WITH_LOCK_RETRY_VOID", "在锁保护下执行操作并重试（无返回值）"),
    /** 使用默认参数在锁保护下执行操作并重试 */
    EXECUTE_WITH_LOCK_RETRY_DEFAULT("EXECUTE_WITH_LOCK_RETRY_DEFAULT", "使用默认参数在锁保护下执行操作并重试"),
    /** 使用默认参数在锁保护下执行操作并重试（无返回值） */
    EXECUTE_WITH_LOCK_RETRY_DEFAULT_VOID("EXECUTE_WITH_LOCK_RETRY_DEFAULT_VOID", "使用默认参数在锁保护下执行操作并重试（无返回值）"),
    /** 在锁保护下执行操作或执行降级操作 */
    EXECUTE_WITH_LOCK_OR_FALLBACK("EXECUTE_WITH_LOCK_OR_FALLBACK", "在锁保护下执行操作或执行降级操作"),
    /** 在锁保护下执行操作或执行降级操作（无返回值） */
    EXECUTE_WITH_LOCK_OR_FALLBACK_VOID("EXECUTE_WITH_LOCK_OR_FALLBACK_VOID", "在锁保护下执行操作或执行降级操作（无返回值）"),
    /** 使用默认参数在锁保护下执行操作或执行降级操作 */
    EXECUTE_WITH_LOCK_OR_FALLBACK_DEFAULT("EXECUTE_WITH_LOCK_OR_FALLBACK_DEFAULT", "使用默认参数在锁保护下执行操作或执行降级操作"),
    /** 使用默认参数在锁保护下执行操作或执行降级操作（无返回值） */
    EXECUTE_WITH_LOCK_OR_FALLBACK_DEFAULT_VOID("EXECUTE_WITH_LOCK_OR_FALLBACK_DEFAULT_VOID",
            "使用默认参数在锁保护下执行操作或执行降级操作（无返回值）"),
    /** 检查锁是否存在 */
    IS_LOCK_EXISTS("IS_LOCK_EXISTS", "检查锁是否存在"),
    /** 强制释放锁 */
    FORCE_UNLOCK("FORCE_UNLOCK", "强制释放锁"),
    /** 获取锁剩余时间 */
    GET_REMAINING_TTL("GET_REMAINING_TTL", "获取锁剩余时间"),
    /** 检查当前线程是否持有锁 */
    IS_HELD_BY_CURRENT_THREAD("IS_HELD_BY_CURRENT_THREAD", "检查当前线程是否持有锁"),
    /** 获取锁持有计数 */
    GET_HOLD_COUNT("GET_HOLD_COUNT", "获取锁持有计数"),

    // ===== Generic Operations =====
    /** 通用操作 */
    GENERIC("GENERIC", "通用操作"),
    /** 批量操作 */
    BATCH("BATCH", "批量操作"),
    /** 脚本执行 */
    SCRIPT("SCRIPT", "脚本执行"),
    /** 管道操作 */
    PIPELINE("PIPELINE", "管道操作");

    private final String command;
    private final String description;

    RedisOperationType(String command, String description) {
        this.command = command;
        this.description = description;
    }

    /**
     * 获取操作命令
     *
     * @return 操作命令
     */
    public String getCommand() {
        return command;
    }

    /**
     * 获取操作描述
     *
     * @return 操作描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据命令字符串获取操作类型
     *
     * @param command 命令字符串
     * @return 操作类型，如果找不到则返回 GENERIC
     */
    public static RedisOperationType fromCommand(String command) {
        if (command == null) {
            return GENERIC;
        }

        for (RedisOperationType type : values()) {
            if (type.command.equalsIgnoreCase(command)) {
                return type;
            }
        }
        return GENERIC;
    }

    @Override
    public String toString() {
        return command;
    }
}
