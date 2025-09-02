package com.david.commons.redis.serialization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 序列化策略选择器 - 简化为固定使用 JSON 序列化 通过 Jackson 和 Class 信息进行反序列化
 *
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SerializationStrategySelector {

    private final RedisSerializerFactory serializerFactory;

    /**
     * 为指定对象选择序列化策略 - 统一返回 JSON
     *
     * @param object 待序列化的对象
     * @return JSON 序列化策略
     */
    public SerializationType selectStrategy(Object object) {
        return SerializationType.JSON;
    }

    /**
     * 为指定类型选择序列化策略 - 统一返回 JSON
     *
     * @param type 对象类型
     * @return JSON 序列化策略
     */
    public SerializationType selectStrategy(Class<?> type) {
        return SerializationType.JSON;
    }

    /**
     * 获取指定策略的序列化器 - 统一返回 JSON 序列化器
     *
     * @param strategy 序列化策略（忽略，统一使用 JSON）
     * @param type 对象类型
     * @return JSON 序列化器（Object 泛型，适配方法级反序列化）
     */
    public RedisSerializer<Object> getSerializerWithFallback(
            SerializationType strategy, Class<?> type) {
        return serializerFactory.getDefaultSerializer();
    }

    /** 获取默认序列化策略 - 固定返回 JSON */
    public SerializationType getDefaultStrategy() {
        return SerializationType.JSON;
    }
}
