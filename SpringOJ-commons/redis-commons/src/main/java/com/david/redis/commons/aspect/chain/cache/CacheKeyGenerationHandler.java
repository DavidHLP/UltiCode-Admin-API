package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.aspect.chain.utils.CacheKeyGenerator;
import com.david.redis.commons.properties.RedisCommonsProperties;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 缓存键生成处理器
 *
 * <p>
 * 负责生成缓存键，包括键表达式解析和前缀处理。
 * 
 * @author David
 */
@Component
public class CacheKeyGenerationHandler extends AbstractAspectHandler {

    public static final String CACHE_KEY_ATTR = "cache.key";
    public static final String CACHE_KEY_PREFIX_ATTR = "cache.key.prefix";
    private final CacheKeyGenerator keyGenerator;
    private final RedisCommonsProperties properties;

    public CacheKeyGenerationHandler(LogUtils logUtils, CacheKeyGenerator keyGenerator,
            RedisCommonsProperties properties) {
        super(logUtils);
        this.keyGenerator = keyGenerator;
        this.properties = properties;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE, AspectType.CACHE_EVICT);
    }

    @Override
    public int getOrder() {
        return 10; // 在条件判断之后，在缓存获取之前
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        try {
            String cacheKey = generateCacheKey(context);
            context.setAttribute(CACHE_KEY_ATTR, cacheKey);

            return chain.proceed(context);

        } catch (Exception e) {
            logException(context, "cache_key_generation", e, "缓存键生成失败: " + context.getMethod().getName());
            throw e;
        }
    }

    /**
     * 生成缓存键
     *
     * @param context 切面上下文
     * @return 缓存键
     */
    private String generateCacheKey(AspectContext context) {
        String keyExpression = getKeyExpression(context);
        String generatedKey = keyGenerator.generateKey(keyExpression, context.getMethod(), context.getArgs());

        // 添加键前缀
        String keyPrefix = getKeyPrefix(context);
        String fullKey = keyPrefix + generatedKey;

        context.setAttribute(CACHE_KEY_PREFIX_ATTR, keyPrefix);

        return fullKey;
    }

    /**
     * 获取键表达式
     *
     * @param context 切面上下文
     * @return 键表达式
     */
    private String getKeyExpression(AspectContext context) {
        if (context.getMethod() == null) {
            return ""; // 虚拟上下文返回空键表达式
        }

        if (context.getAspectType() == AspectType.CACHE) {
            RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
            return annotation != null ? annotation.key() : "";
        } else if (context.getAspectType() == AspectType.CACHE_EVICT) {
            RedisEvict annotation = context.getMethod().getAnnotation(RedisEvict.class);
            if (annotation != null && annotation.keys().length > 0) {
                return annotation.keys()[0]; // 处理第一个键表达式
            }
        }
        return "";
    }

    /**
     * 获取键前缀
     *
     * @param context 切面上下文
     * @return 键前缀
     */
    private String getKeyPrefix(AspectContext context) {
        String annotationPrefix = getAnnotationKeyPrefix(context);

        if (StringUtils.hasText(annotationPrefix)) {
            return annotationPrefix;
        }

        // 使用配置的默认前缀
        return properties.getCache().getKeyPrefix();
    }

    /**
     * 从注解获取键前缀
     *
     * @param context 切面上下文
     * @return 注解中的键前缀
     */
    private String getAnnotationKeyPrefix(AspectContext context) {
        if (context.getMethod() == null) {
            return ""; // 虚拟上下文返回空前缀
        }

        if (context.getAspectType() == AspectType.CACHE) {
            RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
            return annotation != null ? annotation.keyPrefix() : "";
        } else if (context.getAspectType() == AspectType.CACHE_EVICT) {
            RedisEvict annotation = context.getMethod().getAnnotation(RedisEvict.class);
            return annotation != null ? annotation.keyPrefix() : "";
        }
        return "";
    }
}
