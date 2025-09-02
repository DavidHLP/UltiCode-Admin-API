package com.david.commons.redis.cache.aspect.operator;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 缓存键构建器
 *
 * <p>
 * 负责缓存键的构建、求值和规范化处理
 *
 * @author David
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheKeyBuilder {

    private final CacheExpressionEvaluator expressionEvaluator;
    private final RedisUtils redisUtils;

    /**
     * 构建完整的缓存键
     *
     * @param metadata 缓存元数据
     * @param context  缓存上下文
     * @param result   方法执行结果（可为null）
     * @return 规范化后的缓存键
     */
    public String buildCacheKey(CacheMetadata metadata, CacheContext context, Object result) {
        // 1) 求值键表达式
        String evaluatedKey = expressionEvaluator.evaluateKey(metadata.key(), context, result);

        // 2) 基础校验
        if (!StringUtils.hasText(evaluatedKey)) {
            throw new IllegalArgumentException(
                    "Evaluated cache key is empty for expression: " + metadata.key());
        }

        // 3) 规范化：去除由 SpEL 拼接导致的 null 片段与空片段，防止出现 :null 或尾随 null
        String sanitizedKey = sanitizeKey(evaluatedKey);

        if (!StringUtils.hasText(sanitizedKey)) {
            throw new IllegalArgumentException(
                    "Sanitized cache key is empty for expression: " + metadata.key());
        }

        // 4) 应用前缀：优先使用注解上的 keyPrefix，其次交给 redisUtils 全局前缀
        return applyPrefix(metadata, sanitizedKey);
    }

    /**
     * 规范化缓存键，去除null片段和空片段
     */
    private String sanitizeKey(String key) {
        // 保留结尾冒号语义：如果原始键以":"结尾，表示前缀模式意图（例如用于清除时追加通配符）
        boolean endsWithColon = key.endsWith(":");

        String joined = Arrays.stream(key.split(":"))
                .filter(StringUtils::hasText)
                .filter(seg -> !"null".equalsIgnoreCase(seg))
                .collect(Collectors.joining(":"));

        // 若原始键以冒号结尾且清洗后非空，则补回一个尾随冒号，方便后续 normalizePattern 追加"*"
        if (endsWithColon && StringUtils.hasText(joined) && !joined.endsWith(":")) {
            return joined + ":";
        }

        return joined;
    }

    /**
     * 应用缓存键前缀
     */
    private String applyPrefix(CacheMetadata metadata, String sanitizedKey) {
        String overridePrefix = metadata.keyPrefix();
        if (StringUtils.hasText(overridePrefix)) {
            return overridePrefix + sanitizedKey;
        }
        return redisUtils.buildKey(sanitizedKey);
    }
}
