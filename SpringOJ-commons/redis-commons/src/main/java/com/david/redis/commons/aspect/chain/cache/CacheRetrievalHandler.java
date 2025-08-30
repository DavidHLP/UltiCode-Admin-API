package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.RedisUtils;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存获取处理器
 *
 * <p>
 * 负责从 Redis 中获取缓存数据，处理反序列化异常。
 * 
 * @author David
 */
@Component
public class CacheRetrievalHandler extends AbstractAspectHandler {

    public static final String CACHED_VALUE_ATTR = "cache.value";
    public static final String CACHE_HIT_ATTR = "cache.hit";
    private final RedisUtils redisUtils;

    public CacheRetrievalHandler(LogUtils logUtils, RedisUtils redisUtils) {
        super(logUtils);
        this.redisUtils = redisUtils;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE);
    }

    @Override
    public int getOrder() {
        return 20; // 在键生成之后，方法执行之前
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        // 只有条件满足且有缓存键时才处理
        Boolean conditionMet = context.getAttribute(CacheConditionHandler.CACHE_CONDITION_MET_ATTR, false);
        String cacheKey = context.getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR);

        return conditionMet && cacheKey != null;
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        String cacheKey = context.getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR);

        try {
            Object cachedValue = getCachedValue(context, cacheKey);
            boolean cacheHit = cachedValue != null;

            context.setAttribute(CACHED_VALUE_ATTR, cachedValue);
            context.setAttribute(CACHE_HIT_ATTR, cacheHit);

            if (cacheHit) {
                context.setResult(cachedValue);
                logExecution(context, "cache_hit", "缓存命中: " + cacheKey);

                // 缓存命中，继续执行链（可能有其他处理器需要处理，如指标收集）
                return chain.proceed(context);
            } else {
                return chain.proceed(context);
            }

        } catch (Exception e) {
            logException(context, "cache_retrieval", e, "缓存获取失败: " + cacheKey);
            context.setAttribute(CACHE_HIT_ATTR, false);
            // 缓存获取失败，继续执行原方法
            return chain.proceed(context);
        }
    }

    /**
     * 从缓存获取数据
     *
     * @param context  切面上下文
     * @param cacheKey 缓存键
     * @return 缓存值，如果不存在或获取失败则返回 null
     */
    private Object getCachedValue(AspectContext context, String cacheKey) {
        if (context.getMethod() == null) {
            return null; // 虚拟上下文不获取缓存
        }

        try {
            RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
            if (annotation == null) {
                return null;
            }

            Class<?> type = annotation.type();
            if (type != Object.class) {
                // 如果指定了具体类型，直接使用类型化的get方法
                return redisUtils.strings().get(cacheKey, type);
            } else {
                // 使用字符串方法获取原始值
                return redisUtils.strings().getString(cacheKey);
            }
        } catch (org.springframework.data.redis.serializer.SerializationException e) {
            // 反序列化失败，可能是数据格式不兼容，删除损坏的缓存
            logExecution(context, "cache_deserialize_fail", "反序列化失败: " + cacheKey);
            deleteCorruptedCache(cacheKey);
            return null;
        } catch (com.david.redis.commons.exception.RedisOperationException e) {
            // Redis操作异常，可能包含反序列化错误
            if (e.getCause() instanceof org.springframework.data.redis.serializer.SerializationException) {
                logExecution(context, "cache_deserialize_fail", "反序列化失败: " + cacheKey);
                deleteCorruptedCache(cacheKey);
                return null;
            }
            logExecution(context, "cache_op_fail", "缓存操作失败: " + cacheKey);
            return null;
        } catch (Exception e) {
            logExecution(context, "cache_get_fail", "缓存获取异常: " + cacheKey);
            return null;
        }
    }

    /**
     * 删除损坏的缓存
     *
     * @param cacheKey 缓存键
     */
    private void deleteCorruptedCache(String cacheKey) {
        try {
            redisUtils.strings().delete(cacheKey);
        } catch (Exception deleteEx) {
            logUtils.exception()
                    .business(
                            "cache_delete_corrupted_failed",
                            deleteEx,
                            "删除损坏缓存失败",
                            "cacheKey: " + cacheKey);
        }
    }
}
