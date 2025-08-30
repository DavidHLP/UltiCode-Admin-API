package com.david.redis.commons.aspect.chain.transaction;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.aspect.chain.utils.CacheKeyGenerator;
import com.david.redis.commons.core.lock.interfaces.RedisLock;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Set;

/**
 * 锁获取处理器
 *
 * <p>
 * 负责获取分布式锁，支持事务级别的锁管理。
 * 
 * @author David
 */
@Component
public class LockAcquisitionHandler extends AbstractAspectHandler {

    public static final String REDIS_LOCK_ATTR = "transaction.redis.lock";
    public static final String RESOLVED_LOCK_KEY_ATTR = "transaction.resolved.lock.key";
    private final RedisUtils redisUtils;
    private final CacheKeyGenerator cacheKeyGenerator;

    public LockAcquisitionHandler(LogUtils logUtils, RedisUtils redisUtils, CacheKeyGenerator cacheKeyGenerator) {
        super(logUtils);
        this.redisUtils = redisUtils;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 10; // 在验证之后，事务开始之前
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        RedisTransactional annotation = context.getAttribute(TransactionValidationHandler.TRANSACTION_CONFIG_ATTR);
        return annotation != null && StringUtils.hasText(annotation.lockKey());
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        RedisTransactional annotation = context.getAttribute(TransactionValidationHandler.TRANSACTION_CONFIG_ATTR);

        try {
            String resolvedLockKey = resolveLockKey(context, annotation);
            context.setAttribute(RESOLVED_LOCK_KEY_ATTR, resolvedLockKey);

            // 获取锁
            RedisLock lock = acquireLock(context, annotation, resolvedLockKey);
            context.setAttribute(REDIS_LOCK_ATTR, lock);

            logExecution(context, "lock_acquisition", "分布式锁获取成功: " + resolvedLockKey);

            try {
                // 继续执行链
                return chain.proceed(context);
            } finally {
                // 确保锁被释放
                releaseLock(context, lock, resolvedLockKey);
            }

        } catch (Exception e) {
            logException(context, "lock_acquisition", e, "分布式锁获取失败");
            throw e;
        }
    }

    /**
     * 解析锁键表达式
     *
     * @param context    切面上下文
     * @param annotation 事务注解
     * @return 解析后的锁键
     */
    private String resolveLockKey(AspectContext context, RedisTransactional annotation) {
        String lockKeyExp = annotation.lockKey();
        return cacheKeyGenerator.resolveSpELExpression(
                lockKeyExp,
                context.getMethod(),
                context.getArgs());
    }

    /**
     * 获取分布式锁
     *
     * @param context    切面上下文
     * @param annotation 事务注解
     * @param lockKey    锁键
     * @return Redis锁实例
     * @throws Exception 获取锁失败
     */
    private RedisLock acquireLock(AspectContext context, RedisTransactional annotation, String lockKey)
            throws Exception {
        Duration waitTime = Duration.ofMillis(Math.max(0, annotation.lockWaitTimeMs()));
        Duration leaseTime = Duration.ofMillis(Math.max(0, annotation.lockLeaseTimeMs()));

        logExecution(context, "lock_acquisition", "尝试获取锁: " + lockKey);

        RedisLock lock = redisUtils.locks().tryLock(lockKey, waitTime, leaseTime);

        if (lock == null) {
            throw new IllegalStateException("Failed to acquire lock: " + lockKey);
        }

        return lock;
    }

    /**
     * 释放分布式锁
     *
     * @param context 切面上下文
     * @param lock    Redis锁实例
     * @param lockKey 锁键
     */
    private void releaseLock(AspectContext context, RedisLock lock, String lockKey) {
        if (lock != null) {
            try {
                lock.close(); // 使用 try-with-resources 自动释放
                logExecution(context, "lock_release", "锁释放成功: " + lockKey);
            } catch (Exception e) {
                logException(context, "lock_release", e, "锁释放失败: " + lockKey);
            }
        }
    }
}
