package com.david.redis.commons.core.operations.support;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;

/**
 * Redis结果处理器
 *
 * <p>统一的Redis操作结果处理和类型转换组件
 *
 * @author David
 */
@Component
public class RedisResultProcessor {

    /**
     * 转换单个值
     *
     * @param value 原始值
     * @param clazz 目标类型
     * @param <T> 目标类型
     * @return 转换后的值
     */
    public <T> T convertSingle(Object value, Class<T> clazz) {
        if (value == null) {
            return null;
        }
        return RedisTypeConverter.convertValue(value, clazz);
    }

    /**
     * 转换单个值，支持空值默认处理
     *
     * @param value 原始值
     * @param clazz 目标类型
     * @param defaultSupplier 默认值提供器
     * @param <T> 目标类型
     * @return 转换后的值或默认值
     */
    public <T> T convertSingleWithDefault(
            Object value, Class<T> clazz, Supplier<T> defaultSupplier) {
        if (value == null) {
            return defaultSupplier != null ? defaultSupplier.get() : null;
        }
        return RedisTypeConverter.convertValue(value, clazz);
    }

    /**
     * 转换Set集合
     *
     * @param rawSet 原始Set
     * @param clazz 元素目标类型
     * @param <T> 元素类型
     * @return 转换后的Set
     */
    public <T> Set<T> convertSet(Set<Object> rawSet, Class<T> clazz) {
        if (rawSet == null || rawSet.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<T> result = new LinkedHashSet<>();
        for (Object item : rawSet) {
            if (item != null) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            }
        }
        return result;
    }

    /**
     * 转换List集合
     *
     * @param rawList 原始List
     * @param clazz 元素目标类型
     * @param <T> 元素类型
     * @return 转换后的List
     */
    public <T> List<T> convertList(List<Object> rawList, Class<T> clazz) {
        if (rawList == null || rawList.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();
        for (Object item : rawList) {
            if (item != null) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            } else {
                result.add(null); // 保持List中的null元素
            }
        }
        return result;
    }

    /**
     * 转换Map，键转换为String
     *
     * @param rawMap 原始Map
     * @return 转换后的Map
     */
    public Map<String, Object> convertMapKeysToString(Map<Object, Object> rawMap) {
        if (rawMap == null || rawMap.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey().toString() : null;
            if (key != null) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    /**
     * 转换Object Set为String Set
     *
     * @param rawSet 原始Set
     * @return String Set
     */
    public Set<String> convertToStringSet(Set<Object> rawSet) {
        if (rawSet == null || rawSet.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<String> result = new LinkedHashSet<>();
        for (Object item : rawSet) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }

    /**
     * 处理可能为null的数值结果
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的值
     */
    public Long handleNullLong(Long value, long defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * 处理可能为null的Boolean结果
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的值
     */
    public Boolean handleNullBoolean(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * 处理可能为null的Double结果
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的值
     */
    public Double handleNullDouble(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * 安全地获取集合大小
     *
     * @param collection 集合
     * @return 集合大小，null时返回0
     */
    public int safeSize(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }

    /**
     * 安全地获取Map大小
     *
     * @param map Map对象
     * @return Map大小，null时返回0
     */
    public int safeSize(Map<?, ?> map) {
        return map != null ? map.size() : 0;
    }

    /**
     * 检查集合是否为空或null
     *
     * @param collection 集合
     * @return 是否为空
     */
    public boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 检查Map是否为空或null
     *
     * @param map Map对象
     * @return 是否为空
     */
    public boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
