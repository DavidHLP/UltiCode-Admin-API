package com.david.cache.aspect;

import com.david.cache.annotation.RedisCache;
import com.david.cache.enums.CacheType;
import com.david.cache.support.NullValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用缓存切面，实现 @RedisCache 的声明式缓存能力。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedisCacheAspect {

    private static final String LOCK_PREFIX = "lock:cache:anno:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    @Value("${redis.cache.lock.wait-seconds:10}")
    private long defaultLockWaitSeconds;
    @Value("${redis.cache.lock.lease-seconds:30}")
    private long defaultLockLeaseSeconds;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(redisCache)")
    public Object around(ProceedingJoinPoint pjp, RedisCache redisCache) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class<?> returnType = signature.getReturnType();

        String cacheKey = null;
        try {
            cacheKey = buildCacheKey(redisCache, pjp, method);
        } catch (Exception e) {
            log.error("[AOP] Build cache key failed, method={}.{}, err= {}", method.getDeclaringClass().getSimpleName(), method.getName(), e.getMessage(), e);
            // key 构建失败则降级为直接执行业务方法
            return pjp.proceed();
        }

        if (!StringUtils.hasText(cacheKey)) {
            log.warn("[AOP] Empty cache key, degrade to method execution. method={}.{}", method.getDeclaringClass().getSimpleName(), method.getName());
            return pjp.proceed();
        }

        CacheType type = redisCache.type();
        try {
            return switch (type) {
                case READ -> handleRead(pjp, method, cacheKey, redisCache, returnType);
                case WRITE -> handleWrite(pjp, cacheKey, redisCache);
                case DELETE -> handleDelete(pjp, cacheKey);
            };
        } catch (Throwable ex) {
            // 保持业务方法语义：仅在缓存/锁相关出错时记录日志并降级到执行业务
            log.error("[AOP] Cache handling error, degrade to method. method={}.{}", method.getDeclaringClass().getSimpleName(), method.getName(), ex);
            return pjp.proceed();
        }
    }

    private Object handleRead(ProceedingJoinPoint pjp, Method method, String cacheKey, RedisCache rc, Class<?> returnType) throws Throwable {
        boolean useLock = rc.useLock();
        boolean cacheNull = rc.cacheNull() && !returnType.isPrimitive();
        long timeout = rc.timeout();
        TimeUnit unit = rc.unit();

        // 1) 先尝试从缓存读取
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                if (cached instanceof NullValue) {
                    return null;
                }
                return cached;
            }
        } catch (Exception e) {
            log.error("[AOP][READ] Redis get error, key={}", cacheKey, e);
        }

        if (!useLock) {
            // 无锁：直接回源
            Object result = pjp.proceed();
            tryWrite(cacheKey, result, timeout, unit, cacheNull);
            return result;
        }

        // 使用分布式锁
        String lockKey = LOCK_PREFIX + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            try {
                locked = lock.tryLock(defaultLockWaitSeconds, defaultLockLeaseSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("[AOP][READ] Interrupted while acquiring lock, key={}", cacheKey, ie);
            } catch (Exception e) {
                log.error("[AOP][READ] Error while acquiring lock, key={}", cacheKey, e);
            }

            if (!locked) {
                // 未获取到锁：退避 -> 再查缓存 -> 直接返回（避免并发回源 DB）
                try {
                    Object cachedAgain = redisTemplate.opsForValue().get(cacheKey);
                    if (cachedAgain != null) {
                        if (cachedAgain instanceof NullValue) {
                            return null;
                        }
                        return cachedAgain;
                    }
                } catch (Exception e) {
                    log.error("[AOP][READ] Re-check cache failed, key={}", cacheKey, e);
                }
                log.warn("[AOP][READ] Lock not acquired and cache still empty, return null to avoid stampede. key={}", cacheKey);
                return null;
            }

            // Double-Check
            try {
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    if (cached instanceof NullValue) {
                        return null;
                    }
                    return cached;
                }
            } catch (Exception e) {
                log.error("[AOP][READ] Double-check get failed, key={}", cacheKey, e);
            }

            Object result = pjp.proceed();
            tryWrite(cacheKey, result, timeout, unit, cacheNull);
            return result;
        } finally {
            try {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("[AOP][READ] Unlock failed, key={}", cacheKey, e);
            }
        }
    }

    private Object handleWrite(ProceedingJoinPoint pjp, String cacheKey, RedisCache rc) throws Throwable {
        boolean useLock = rc.useLock();
        boolean cacheNull = rc.cacheNull();
        long timeout = rc.timeout();
        TimeUnit unit = rc.unit();

        if (!useLock) {
            Object result = pjp.proceed();
            tryWrite(cacheKey, result, timeout, unit, cacheNull);
            return result;
        }

        String lockKey = LOCK_PREFIX + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            try {
                locked = lock.tryLock(defaultLockWaitSeconds, defaultLockLeaseSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("[AOP][WRITE] Interrupted while acquiring lock, key={}", cacheKey, ie);
            } catch (Exception e) {
                log.error("[AOP][WRITE] Error while acquiring lock, key={}", cacheKey, e);
            }

            Object result = pjp.proceed();
            tryWrite(cacheKey, result, timeout, unit, cacheNull);
            return result;
        } finally {
            try {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("[AOP][WRITE] Unlock failed, key={}", cacheKey, e);
            }
        }
    }

    private Object handleDelete(ProceedingJoinPoint pjp, String cacheKey) throws Throwable {
        Object result = pjp.proceed();
        try {
            Boolean ok = redisTemplate.delete(cacheKey);
            log.debug("[AOP][DELETE] delete key={}, result={}", cacheKey, ok);
        } catch (Exception e) {
            log.error("[AOP][DELETE] delete failed, key={}", cacheKey, e);
        }
        return result;
    }

    private void tryWrite(String cacheKey, Object value, long timeout, TimeUnit unit, boolean cacheNull) {
        try {
            Object toCache = value;
            if (toCache == null) {
                if (!cacheNull) {
                    return;
                }
                toCache = NullValue.INSTANCE;
            }
            long realTimeout = addJitter(timeout);
            if (realTimeout > 0) {
                redisTemplate.opsForValue().set(cacheKey, toCache, realTimeout, unit);
            } else {
                redisTemplate.opsForValue().set(cacheKey, toCache);
            }
        } catch (Exception e) {
            log.error("[AOP] write cache failed, key={}", cacheKey, e);
        }
    }

    private String buildCacheKey(RedisCache rc, ProceedingJoinPoint pjp, Method method) {
        String keyPrefix = rc.keyPrefix();
        String spel = rc.key();
        EvaluationContext context = new StandardEvaluationContext(pjp.getTarget());
        Object[] args = pjp.getArgs();
        String[] paramNames = ((MethodSignature) pjp.getSignature()).getParameterNames();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        // 兼容 #p0/#a0 以及 #args 访问
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                context.setVariable("p" + i, args[i]);
                context.setVariable("a" + i, args[i]);
            }
            context.setVariable("args", args);
        }
        Expression exp = parser.parseExpression(spel);
        Object keyObj = exp.getValue(context);
        String keyStr = Objects.toString(keyObj, "");
        if (StringUtils.hasText(keyPrefix)) {
            return keyPrefix + ":" + keyStr;
        }
        return keyStr;
    }

    private long addJitter(long timeout) {
        if (timeout <= 0) {
            return timeout;
        }
        double ratio = ThreadLocalRandom.current().nextDouble(0.0, 0.1);
        long delta = (long) Math.floor(timeout * ratio);
        return timeout + delta;
    }
}
