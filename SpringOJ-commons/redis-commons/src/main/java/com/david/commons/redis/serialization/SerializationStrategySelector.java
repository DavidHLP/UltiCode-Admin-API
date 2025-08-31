package com.david.commons.redis.serialization;

import com.david.commons.redis.config.RedisCommonsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化策略选择器
 * 负责根据对象类型和配置自动选择最优的序列化策略
 *
 * @author David
 */
@Slf4j
@Component
public class SerializationStrategySelector {

    private final RedisSerializerFactory serializerFactory;
    private final RedisCommonsProperties properties;

    /**
     * 类型到序列化策略的缓存映射
     */
    private final Map<Class<?>, SerializationType> typeStrategyCache = new ConcurrentHashMap<>();

    /**
     * 降级策略链：当首选策略失败时的备选方案
     */
    private final SerializationType[] fallbackChain = {
            SerializationType.JSON, // 最通用的选择
            SerializationType.JDK, // Java 原生支持
            SerializationType.KRYO // 高性能选择
    };

    @Autowired
    public SerializationStrategySelector(RedisSerializerFactory serializerFactory,
            RedisCommonsProperties properties) {
        this.serializerFactory = serializerFactory;
        this.properties = properties;
    }

    /**
     * 为指定对象选择最优的序列化策略
     *
     * @param object 待序列化的对象
     * @return 最优的序列化策略
     */
    public SerializationType selectStrategy(Object object) {
        if (object == null) {
            return getDefaultStrategy();
        }

        return selectStrategy(object.getClass());
    }

    /**
     * 为指定类型选择最优的序列化策略
     *
     * @param type 对象类型
     * @return 最优的序列化策略
     */
    public SerializationType selectStrategy(Class<?> type) {
        if (type == null) {
            return getDefaultStrategy();
        }

        // 先从缓存中查找
        SerializationType cachedStrategy = typeStrategyCache.get(type);
        if (cachedStrategy != null) {
            return cachedStrategy;
        }

        // 计算最优策略
        SerializationType optimalStrategy = computeOptimalStrategy(type);

        // 验证策略是否可用
        SerializationType finalStrategy = validateAndFallback(optimalStrategy, type);

        // 缓存结果
        typeStrategyCache.put(type, finalStrategy);

        log.debug("Selected serialization strategy {} for type {}", finalStrategy, type.getName());
        return finalStrategy;
    }

    /**
     * 获取指定策略的序列化器，如果失败则使用降级策略
     *
     * @param strategy 首选策略
     * @param type     对象类型
     * @param <T>      对象类型泛型
     * @return 可用的序列化器
     */
    public <T> RedisSerializer<T> getSerializerWithFallback(SerializationType strategy, Class<T> type) {
        try {
            RedisSerializer<T> serializer = serializerFactory.getSerializer(strategy);
            if (serializer.supports(type)) {
                return serializer;
            }
        } catch (Exception e) {
            log.warn("Failed to get serializer for strategy {}, falling back", strategy, e);
        }

        // 使用降级策略
        for (SerializationType fallback : fallbackChain) {
            if (fallback == strategy) {
                continue; // 跳过已经尝试过的策略
            }

            try {
                RedisSerializer<T> serializer = serializerFactory.getSerializer(fallback);
                if (serializer.supports(type)) {
                    log.info("Using fallback serialization strategy {} for type {}", fallback, type.getName());
                    return serializer;
                }
            } catch (Exception e) {
                log.debug("Fallback strategy {} also failed for type {}", fallback, type.getName(), e);
            }
        }

        // 如果所有策略都失败，返回默认的 JSON 序列化器
        log.error("All serialization strategies failed for type {}, using default JSON serializer", type.getName());
        return serializerFactory.getSerializer(SerializationType.JSON);
    }

    /**
     * 计算指定类型的最优序列化策略
     */
    private SerializationType computeOptimalStrategy(Class<?> type) {
        // 1. 检查是否有明确的配置指定
        SerializationType configuredDefault = properties.getSerialization().getDefaultType();

        // 2. 基于类型特征进行智能选择

        // 基本类型和字符串：使用 JSON（可读性好，调试友好）
        if (isPrimitiveOrWrapper(type) || String.class.equals(type)) {
            return SerializationType.JSON;
        }

        // 集合类型：根据大小和性能需求选择
        if (java.util.Collection.class.isAssignableFrom(type) ||
                java.util.Map.class.isAssignableFrom(type)) {
            // 对于集合类型，如果配置了高性能模式，使用 Kryo
            return configuredDefault == SerializationType.KRYO ? SerializationType.KRYO : SerializationType.JSON;
        }

        // 实现了 Serializable 的复杂对象：优先使用 Kryo（高性能）
        if (Serializable.class.isAssignableFrom(type)) {
            return SerializationType.KRYO;
        }

        // 其他情况：使用配置的默认策略或 JSON
        return configuredDefault != null ? configuredDefault : SerializationType.JSON;
    }

    /**
     * 验证策略是否可用，如果不可用则选择降级策略
     */
    private SerializationType validateAndFallback(SerializationType strategy, Class<?> type) {
        try {
            if (serializerFactory.supports(strategy, type)) {
                return strategy;
            }
        } catch (Exception e) {
            log.debug("Strategy {} validation failed for type {}", strategy, type.getName(), e);
        }

        // 寻找可用的降级策略
        for (SerializationType fallback : fallbackChain) {
            if (fallback == strategy) {
                continue;
            }

            try {
                if (serializerFactory.supports(fallback, type)) {
                    log.info("Using fallback strategy {} instead of {} for type {}",
                            fallback, strategy, type.getName());
                    return fallback;
                }
            } catch (Exception e) {
                log.debug("Fallback strategy {} also not supported for type {}", fallback, type.getName());
            }
        }

        // 如果所有策略都不支持，返回 JSON 作为最后的选择
        log.warn("No suitable serialization strategy found for type {}, using JSON as last resort", type.getName());
        return SerializationType.JSON;
    }

    /**
     * 获取默认序列化策略
     */
    private SerializationType getDefaultStrategy() {
        SerializationType defaultType = properties.getSerialization().getDefaultType();
        return defaultType != null ? defaultType : SerializationType.JSON;
    }

    /**
     * 检查是否为基本类型或其包装类
     */
    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(Boolean.class) ||
                type.equals(Byte.class) ||
                type.equals(Character.class) ||
                type.equals(Short.class) ||
                type.equals(Integer.class) ||
                type.equals(Long.class) ||
                type.equals(Float.class) ||
                type.equals(Double.class);
    }

    /**
     * 清除策略缓存
     */
    public void clearCache() {
        typeStrategyCache.clear();
        log.info("Serialization strategy cache cleared");
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format("Strategy cache size: %d entries", typeStrategyCache.size());
    }

    /**
     * 手动设置类型的序列化策略
     *
     * @param type     类型
     * @param strategy 策略
     */
    public void setStrategyForType(Class<?> type, SerializationType strategy) {
        if (serializerFactory.supports(strategy, type)) {
            typeStrategyCache.put(type, strategy);
            log.info("Manually set serialization strategy {} for type {}", strategy, type.getName());
        } else {
            throw new IllegalArgumentException(
                    String.format("Strategy %s does not support type %s", strategy, type.getName()));
        }
    }

    /**
     * 移除类型的缓存策略
     *
     * @param type 类型
     */
    public void removeStrategyForType(Class<?> type) {
        SerializationType removed = typeStrategyCache.remove(type);
        if (removed != null) {
            log.info("Removed cached strategy {} for type {}", removed, type.getName());
        }
    }
}