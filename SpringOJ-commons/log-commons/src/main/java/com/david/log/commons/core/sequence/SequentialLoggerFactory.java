package com.david.log.commons.core.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 序列化日志工厂
 * 
 * <p>
 * 创建和管理序列化日志包装器，确保所有日志都具备顺序性保证
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class SequentialLoggerFactory {

    /**
     * 缓存已创建的SequentialLogger实例
     */
    private static final ConcurrentMap<String, SequentialLogger> LOGGER_CACHE = new ConcurrentHashMap<>();

    /**
     * 私有构造函数，防止实例化
     */
    private SequentialLoggerFactory() {
        throw new UnsupportedOperationException(
                "SequentialLoggerFactory is a utility class and cannot be instantiated");
    }

    /**
     * 获取序列化日志器
     * 
     * @param name 日志器名称
     * @return SequentialLogger实例
     */
    public static SequentialLogger getLogger(String name) {
        return LOGGER_CACHE.computeIfAbsent(name, n -> {
            Logger slf4jLogger = LoggerFactory.getLogger(n);
            return new SequentialLogger(slf4jLogger);
        });
    }

	/**
     * 清理缓存（主要用于测试和内存管理）
     */
    public static void clearCache() {
        LOGGER_CACHE.clear();
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存大小
     */
    public static int getCacheSize() {
        return LOGGER_CACHE.size();
    }
}
