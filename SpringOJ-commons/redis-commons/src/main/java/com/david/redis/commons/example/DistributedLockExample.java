package com.david.redis.commons.example;

import com.david.redis.commons.core.DistributedLockManager;
import com.david.redis.commons.core.interfaces.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 分布式锁使用示例
 *
 * 演示如何使用DistributedLockManager进行分布式锁操作
 *
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExample {

    private final DistributedLockManager lockManager;

    /**
     * 示例1：基本的锁使用（try-with-resources）
     */
    public void basicLockExample() {
        String lockKey = "example:basic-lock";

        try (RedisLock lock = lockManager.tryLock(lockKey, Duration.ofSeconds(5), Duration.ofSeconds(30))) {
            log.info("获取到锁，开始执行业务逻辑...");

            // 模拟业务操作
            Thread.sleep(1000);

            log.info("业务逻辑执行完成");
        } catch (Exception e) {
            log.error("执行业务逻辑失败", e);
        }
        // 锁会自动释放
    }

    /**
     * 示例2：使用executeWithLock方法（有返回值）
     */
    public String executeWithLockExample() {
        String lockKey = "example:execute-lock";

        return lockManager.executeWithLock(
                lockKey,
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                () -> {
                    log.info("在锁保护下执行操作...");

                    // 模拟业务操作
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("操作被中断", e);
                    }

                    return "操作结果";
                });
    }

    /**
     * 示例3：使用executeWithLock方法（无返回值）
     */
    public void executeWithLockVoidExample() {
        String lockKey = "example:void-lock";

        lockManager.executeWithLock(
                lockKey,
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                () -> {
                    log.info("在锁保护下执行无返回值操作...");

                    // 模拟业务操作
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("操作被中断", e);
                    }

                    log.info("无返回值操作完成");
                });
    }

    /**
     * 示例4：使用默认配置的锁
     */
    public void defaultConfigLockExample() {
        String lockKey = "example:default-lock";

        // 使用配置文件中的默认等待时间和租约时间
        lockManager.executeWithLock(lockKey, () -> {
            log.info("使用默认配置执行操作...");
            return "默认配置操作完成";
        });
    }

    /**
     * 示例5：锁续期示例
     */
    public void lockExtensionExample() {
        String lockKey = "example:extension-lock";

        try (RedisLock lock = lockManager.tryLock(lockKey, Duration.ofSeconds(5), Duration.ofSeconds(10))) {
            log.info("获取到锁，剩余时间: {}", lock.getRemainingLeaseTime());

            // 模拟长时间操作
            Thread.sleep(5000);

            // 续期锁
            boolean extended = lock.tryExtendLease(Duration.ofSeconds(20));
            if (extended) {
                log.info("锁续期成功，新的剩余时间: {}", lock.getRemainingLeaseTime());
            } else {
                log.warn("锁续期失败");
            }

            // 继续执行业务逻辑
            Thread.sleep(3000);

        } catch (Exception e) {
            log.error("锁续期示例执行失败", e);
        }
    }

    /**
     * 示例6：检查锁状态
     */
    public void lockStatusExample() {
        String lockKey = "example:status-lock";

        // 检查锁是否存在
        boolean exists = lockManager.isLockExists(lockKey);
        log.info("锁 {} 是否存在: {}", lockKey, exists);

        try (RedisLock lock = lockManager.tryLock(lockKey)) {
            log.info("锁是否被当前线程持有: {}", lock.isLocked());
            log.info("锁是否被任何线程持有: {}", lock.isHeldByAnyThread());
            log.info("锁的剩余时间: {}", lock.getRemainingLeaseTime());

            // 模拟业务操作
            Thread.sleep(1000);

        } catch (Exception e) {
            log.error("锁状态示例执行失败", e);
        }
    }
}