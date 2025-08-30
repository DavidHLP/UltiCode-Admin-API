package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.aspect.chain.utils.CacheKeyGenerator;
import com.david.redis.commons.properties.RedisCommonsProperties;
import com.david.redis.commons.enums.EvictTiming;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 缓存驱逐处理器
 *
 * <p>
 * 负责执行缓存驱逐操作，支持精确键和模式键驱逐。
 *
 * @author David
 */
@Component
public class CacheEvictionHandler extends AbstractAspectHandler {

    private static final String EVICT_PROCESSED_PREFIX = "evict_processed_";
    private static final String DELETED_SUFFIX_PREFIX = ":__DELETED__:";
    private static final int MAX_DISPLAY_KEYS = 10;
    private static final int PREVIEW_KEYS_COUNT = 5;

    private final RedisUtils redisUtils;
    private final CacheKeyGenerator keyGenerator;
    private final RedisCommonsProperties properties;

    public CacheEvictionHandler(
            LogUtils logUtils,
            RedisUtils redisUtils,
            CacheKeyGenerator keyGenerator,
            RedisCommonsProperties properties) {
        super(logUtils);
        this.redisUtils = redisUtils;
        this.keyGenerator = keyGenerator;
        this.properties = properties;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE_EVICT);
    }

    @Override
    public int getOrder() {
        return 30; // 基础顺序，会根据beforeInvocation动态调整
    }

    @Override
    public boolean canHandle(AspectContext context) {
        return super.canHandle(context)
                && getRedisEvictAnnotation(context).isPresent()
                && isConditionMet(context)
                && !isEvictProcessed(context);
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        var annotation = getRedisEvictAnnotation(context)
                .orElseThrow(() -> new IllegalStateException("RedisEvict annotation not found"));

        boolean beforeInvocation = annotation.beforeInvocation();
        String evictProcessedKey = EVICT_PROCESSED_PREFIX + beforeInvocation;

        try {
            if (beforeInvocation) {
                return handleBeforeInvocation(context, chain, evictProcessedKey);
            } else {
                return handleAfterInvocation(context, chain, evictProcessedKey);
            }
        } catch (Exception e) {
            return handleEvictionException(context, chain, e, beforeInvocation, evictProcessedKey);
        }
    }

    private Object handleBeforeInvocation(AspectContext context, AspectChain chain, String evictProcessedKey) throws Throwable {
        evictCache(context);
        context.setAttribute(evictProcessedKey, true);
        return chain.proceed(context);
    }

    private Object handleAfterInvocation(AspectContext context, AspectChain chain, String evictProcessedKey) throws Throwable {
        Object result = chain.proceed(context);

        if (context.isMethodExecuted()) {
            evictCache(context);
            context.setAttribute(evictProcessedKey, true);
        }

        return result;
    }

    private Object handleEvictionException(AspectContext context, AspectChain chain, Exception e,
                                           boolean beforeInvocation, String evictProcessedKey) throws Throwable {
        logException(context, "cache_evict", e, "缓存驱逐失败");

        if (beforeInvocation) {
            context.setAttribute(evictProcessedKey, true);
        }

        return chain.proceed(context);
    }

    /**
     * 执行缓存驱逐
     */
    private void evictCache(AspectContext context) {
        if (context.getMethod() == null) {
            return; // 虚拟上下文不驱逐缓存
        }

        getRedisEvictAnnotation(context)
                .ifPresent(annotation -> processEvictionByTiming(context, annotation));
    }

    private void processEvictionByTiming(AspectContext context, RedisEvict annotation) {
        EvictTiming timing = annotation.timing();
        long delayMs = annotation.delayMs();

        if (timing == EvictTiming.DELAYED || delayMs > 0) {
            scheduleDelayedEviction(context, annotation, Math.max(delayMs, 0));
        } else if (timing == EvictTiming.CASCADE || annotation.cascade()) {
            performCascadeEviction(context, annotation);
        } else {
            performImmediateEviction(context, annotation);
        }
    }

    /**
     * 执行立即驱逐
     */
    private void performImmediateEviction(AspectContext context, RedisEvict annotation) {
        if (annotation.softDelete()) {
            performSoftDelete(context, annotation);
        } else {
            performHardDelete(context, annotation);
        }
    }

    private void performHardDelete(AspectContext context, RedisEvict annotation) {
        if (annotation.allEntries()) {
            evictAllEntries(context, annotation);
        } else {
            evictSpecificKeys(context, annotation);
        }
    }

    /**
     * 执行软删除
     */
    private void performSoftDelete(AspectContext context, RedisEvict annotation) {
        String softDeleteSuffix = DELETED_SUFFIX_PREFIX + System.currentTimeMillis();

        if (annotation.allEntries()) {
            performSoftDeleteAll(context, annotation, softDeleteSuffix);
        } else {
            performSoftDeleteSpecific(context, annotation, softDeleteSuffix);
        }
    }

    private void performSoftDeleteAll(AspectContext context, RedisEvict annotation, String suffix) {
        String keyPrefix = getKeyPrefix(annotation);
        String pattern = keyPrefix + "*";

        try {
            Set<String> keys = getKeysFromPattern(pattern);
            int softDeleteCount = keys.stream()
                    .filter(Predicate.not(this::isDeletedKey))
                    .mapToInt(key -> processSoftDeleteKey(key, suffix) ? 1 : 0)
                    .sum();

            logExecution(context, "cache_soft_delete_all",
                    "软删除模式: %s, 删除数量: %d".formatted(pattern, softDeleteCount));
        } catch (Exception e) {
            logException(context, "soft_delete_all", e, "软删除模式失败: " + pattern);
            throw e;
        }
    }

    private void performSoftDeleteSpecific(AspectContext context, RedisEvict annotation, String suffix) {
        List<String> keysToSoftDelete = generateKeysToEvict(context, annotation, annotation.keys());

        int softDeleteCount = keysToSoftDelete.stream()
                .filter(Predicate.not(this::isDeletedKey))
                .mapToInt(key -> processSoftDeleteKey(key, suffix) ? 1 : 0)
                .sum();

        logExecution(context, "cache_soft_delete_keys",
                "软删除键数: %d, 删除数量: %d".formatted(keysToSoftDelete.size(), softDeleteCount));
    }

    private boolean processSoftDeleteKey(String key, String suffix) {
        try {
            return Optional.ofNullable(redisUtils.strings().getString(key))
                    .map(value -> {
                        String newKey = key + suffix;
                        redisUtils.strings().set(newKey, value);
                        redisUtils.strings().delete(key);
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            // 单个键的软删除失败不抛出异常，只返回false
            return false;
        }
    }

    private boolean isDeletedKey(String key) {
        return key.contains(DELETED_SUFFIX_PREFIX);
    }

    /**
     * 执行延迟驱逐
     */
    private void scheduleDelayedEviction(AspectContext context, RedisEvict annotation, long delayMs) {
        if (delayMs <= 0) {
            performImmediateEviction(context, annotation);
            return;
        }

        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    try {
                        performImmediateEviction(context, annotation);
                    } catch (Exception e) {
                        logException(context, "delayed_evict", e,
                                "延迟驱逐失败: %dms".formatted(delayMs));
                    }
                });
    }

    /**
     * 执行级联驱逐
     */
    private void performCascadeEviction(AspectContext context, RedisEvict annotation) {
        // 首先执行基本驱逐
        performImmediateEviction(context, annotation);

        // 然后执行级联删除
        String[] cascadePatterns = annotation.cascadePatterns();
        if (cascadePatterns.length > 0) {
            String keyPrefix = getKeyPrefix(annotation);

            for (String pattern : cascadePatterns) {
                processCascadePattern(context, pattern, keyPrefix);
            }
        }
    }

    private void processCascadePattern(AspectContext context, String pattern, String keyPrefix) {
        try {
            String resolvedPattern = keyGenerator.resolveSpELExpression(
                    pattern, context.getMethod(), context.getArgs());
            String fullPattern = keyPrefix + resolvedPattern;

            deleteByPattern(context, fullPattern);
        } catch (Exception e) {
            logException(context, "cascade_evict", e, "级联驱逐失败: " + pattern);
        }
    }

    /**
     * 驱逐所有缓存条目
     */
    private void evictAllEntries(AspectContext context, RedisEvict annotation) {
        String keyPrefix = getKeyPrefix(annotation);
        String pattern = keyPrefix + "*";

        try {
            Set<String> keys = getKeysFromPattern(pattern);

            if (keys.isEmpty()) {
                logExecution(context, "cache_evict_all", "模式驱逐: %s, 无匹配的key".formatted(pattern));
                return;
            }

            Long deletedCount = redisUtils.strings().delete(keys.toArray(String[]::new));
            String keysList = formatKeysList(keys);

            logExecution(context, "cache_evict_all",
                    "模式驱逐: %s, 删除数量: %d, 被删除的key: %s".formatted(pattern, deletedCount, keysList));
        } catch (Exception e) {
            logException(context, "cache_evict_all", e, "模式驱逐失败: " + pattern);
            throw e;
        }
    }

    /**
     * 驱逐指定键的缓存
     */
    private void evictSpecificKeys(AspectContext context, RedisEvict annotation) {
        String[] keyExpressions = annotation.keys();
        if (keyExpressions.length == 0) {
            return;
        }

        List<String> keysToEvict = generateKeysToEvict(context, annotation, keyExpressions);
        if (keysToEvict.isEmpty()) {
            return;
        }

        int totalDeleted = executeEviction(context, keysToEvict);

        logExecution(context, "cache_evict_keys",
                "键驱逐: [%s], 删除数量: %d".formatted(String.join(", ", keysToEvict), totalDeleted));
    }

    /**
     * 生成要驱逐的键列表
     */
    private List<String> generateKeysToEvict(
            AspectContext context, RedisEvict annotation, String[] keyExpressions) {
        String keyPrefix = getKeyPrefix(annotation);

        return Stream.of(keyExpressions)
                .map(keyExpression -> generateSingleKey(context, keyExpression, keyPrefix))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<String> generateSingleKey(AspectContext context, String keyExpression, String keyPrefix) {
        try {
            String generatedKey = keyGenerator.generateKey(
                    keyExpression, context.getMethod(), context.getArgs());
            return Optional.of(keyPrefix + generatedKey);
        } catch (Exception e) {
            logException(context, "evict_key_gen", e, "驱逐键生成失败: " + keyExpression);
            return Optional.empty();
        }
    }

    /**
     * 执行驱逐操作
     */
    private int executeEviction(AspectContext context, List<String> keysToEvict) {
        var keyGroups = keysToEvict.stream()
                .collect(Collectors.groupingBy(this::isPatternKey));

        List<String> exactKeys = keyGroups.getOrDefault(false, List.of());
        List<String> patternKeys = keyGroups.getOrDefault(true, List.of());

        int totalDeleted = 0;

        // 批量删除精确键
        if (!exactKeys.isEmpty()) {
            Long exactDeleted = redisUtils.strings().delete(exactKeys.toArray(String[]::new));
            totalDeleted += Optional.ofNullable(exactDeleted).orElse(0L).intValue();
        }

        // 处理模式键
        totalDeleted += patternKeys.stream()
                .mapToInt(pattern -> Optional.ofNullable(deleteByPattern(context, pattern))
                        .orElse(0L).intValue())
                .sum();

        return totalDeleted;
    }

    private boolean isPatternKey(String key) {
        return key.contains("*") || key.contains("?");
    }

    /**
     * 根据模式删除缓存键
     */
    private Long deleteByPattern(AspectContext context, String pattern) {
        try {
            Set<String> matchedKeys = getKeysFromPattern(pattern);

            if (matchedKeys.isEmpty()) {
                return 0L;
            }

            return redisUtils.strings().delete(matchedKeys.toArray(String[]::new));
        } catch (Exception e) {
            logException(context, "pattern_evict", e, "模式驱逐失败: " + pattern);
            return 0L;
        }
    }

    // 辅助方法

    private Optional<RedisEvict> getRedisEvictAnnotation(AspectContext context) {
        return Optional.ofNullable(context.getMethod())
                .map(method -> method.getAnnotation(RedisEvict.class));
    }

    private boolean isConditionMet(AspectContext context) {
        return context.getAttribute(CacheConditionHandler.EVICT_CONDITION_MET_ATTR, true);
    }

    private boolean isEvictProcessed(AspectContext context) {
        return getRedisEvictAnnotation(context)
                .map(annotation -> {
                    String evictProcessedKey = EVICT_PROCESSED_PREFIX + annotation.beforeInvocation();
                    return context.getAttribute(evictProcessedKey, false);
                })
                .orElse(false);
    }

    private Set<String> getKeysFromPattern(String pattern) {
        Set<String> keys = redisUtils.strings().scanKeys(pattern);
        if (keys.isEmpty()) {
            keys = redisUtils.strings().keys(pattern);
        }
        return keys;
    }

    private String formatKeysList(Set<String> keys) {
        if (keys.size() <= MAX_DISPLAY_KEYS) {
            return "[%s]".formatted(String.join(", ", keys));
        } else {
            String preview = keys.stream()
                    .limit(PREVIEW_KEYS_COUNT)
                    .collect(Collectors.joining(", "));
            return "[%d个key: %s...]".formatted(keys.size(), preview);
        }
    }

    private String getKeyPrefix(RedisEvict annotation) {
        return StringUtils.hasText(annotation.keyPrefix())
                ? annotation.keyPrefix()
                : properties.getCache().getKeyPrefix();
    }
}