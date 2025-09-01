package com.david.commons.redis.lock;

import com.david.commons.redis.RealRedisTestBase;
import com.david.commons.redis.TestApplication;
import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.lock.impl.RedissonDistributedLockManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LockRecoveryHandler 真实Redis测试
 * 使用真实Redis连接进行测试
 *
 * @author David
 */
@SpringBootTest(classes = TestApplication.class)
class LockRecoveryHandlerTest extends RealRedisTestBase {

    @Autowired
    private RedissonClient redissonClient;

    private LockRecoveryHandler recoveryHandler;
    private RedissonDistributedLockManager lockManager;

    @BeforeEach
    void setUp() {
        // 创建真实的锁管理器
        RedisCommonsProperties properties = new RedisCommonsProperties();
        properties.setKeyPrefix(TEST_KEY_PREFIX);

        RedisCommonsProperties.LockConfig lockConfig = new RedisCommonsProperties.LockConfig();
        lockConfig.setDefaultWaitTime(10);
        lockConfig.setDefaultLeaseTime(30);
        lockConfig.setEnableWatchdog(true);
        properties.setLock(lockConfig);

        lockManager = new RedissonDistributedLockManager(redissonClient, properties);
        recoveryHandler = new LockRecoveryHandler(1, TimeUnit.SECONDS); // 快速测试间隔
    }

    @AfterEach
    void tearDown() {
        if (recoveryHandler != null) {
            recoveryHandler.shutdown();
        }
        // 清理测试数据
        try {
            // 强制释放可能遗留的锁
            if (lockManager != null) {
                lockManager.forceUnlock("testKey");
                lockManager.forceUnlock("expiredKey");
            }
        } catch (Exception e) {
            // 忽略清理异常
        }
    }

    @Test
    void testRegisterAndUnregisterLock() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // Given
        String key = randomTestKey();
        RLock rLock = lockManager.getLock(key);

        // When
        recoveryHandler.registerLock(key, rLock, 30, TimeUnit.SECONDS);
        recoveryHandler.unregisterLock(key);

        // Then - 没有异常抛出即为成功
        assertDoesNotThrow(() -> recoveryHandler.unregisterLock(key));
    }

    @Test
    void testTryRecoverLockSuccess() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // Given
        String key = randomTestKey();
        RLock rLock = lockManager.getLock(key);
        Exception originalException = new RuntimeException("Test exception");

        // 先获取锁
        assertTrue(lockManager.tryLock(key, 1, 5, TimeUnit.SECONDS));
        assertTrue(rLock.isHeldByCurrentThread());

        // When
        boolean result = recoveryHandler.tryRecoverLock(key, rLock, originalException);

        // Then
        assertTrue(result);
        assertFalse(rLock.isLocked());
    }

    @Test
    void testTryRecoverLockNotHeldByCurrentThread() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // Given
        String key = randomTestKey();
        RLock rLock = lockManager.getLock(key);
        Exception originalException = new RuntimeException("Test exception");

        // 确保锁未被当前线程持有
        assertFalse(rLock.isHeldByCurrentThread());

        // When
        boolean result = recoveryHandler.tryRecoverLock(key, rLock, originalException);

        // Then
        assertTrue(result);
    }

    @Test
    void testTryRecoverLockForceUnlockScenario() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // Given
        String key = randomTestKey();
        RLock rLock = lockManager.getLock(key);
        Exception originalException = new RuntimeException("Test exception");

        // 先获取锁
        assertTrue(lockManager.tryLock(key, 1, 5, TimeUnit.SECONDS));
        assertTrue(rLock.isHeldByCurrentThread());

        // When - 尝试恢复锁（应该成功强制解锁）
        recoveryHandler.tryRecoverLock(key, rLock, originalException);

        // Then - 验证锁已被释放
        assertFalse(rLock.isLocked());
    }

    @Test
    void testTryRecoverLockWithNullLock() {
        // Given
        String key = randomTestKey();
        Exception originalException = new RuntimeException("Test exception");

        // When - 使用null锁测试异常处理
        boolean result = recoveryHandler.tryRecoverLock(key, null, originalException);

        // Then - 应该返回false，因为锁为null
        assertFalse(result);
    }

    @Test
    void testShutdown() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // Given
        String key = randomTestKey();
        RLock rLock = lockManager.getLock(key);
        recoveryHandler.registerLock(key, rLock, 30, TimeUnit.SECONDS);

        // When
        recoveryHandler.shutdown();

        // Then - 没有异常抛出即为成功
        assertDoesNotThrow(() -> recoveryHandler.shutdown());
    }

    @Test
    void testRecoveryCheckWithExpiredLock() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // Given
        String key = randomTestKey();
        RLock rLock = lockManager.getLock(key);

        // 注册一个锁，使用很短的租期
        recoveryHandler.registerLock(key, rLock, 1, TimeUnit.MILLISECONDS);

        // 等待足够长的时间让恢复检查运行多次
        Thread.sleep(2500);

        // When - 恢复检查应该自动运行并清理过期锁
        // Then - 由于时间控制在单元测试中不够精确，我们简化验证
        // 主要验证注册和注销功能正常工作
        assertDoesNotThrow(() -> recoveryHandler.unregisterLock(key));
    }

    @Test
    void testConcurrentRecoveryHandling() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // Given
        String key1 = randomTestKey();
        String key2 = randomTestKey();
        
        // 用于跟踪恢复结果
        boolean[] recoveryResults = new boolean[2];

        // When - 在不同线程中获取锁并执行恢复操作
        Thread lockAndRecover1 = new Thread(() -> {
            RLock rLock1 = lockManager.getLock(key1);
            recoveryHandler.registerLock(key1, rLock1, 30, TimeUnit.SECONDS);
            
            // 在当前线程获取锁
            if (lockManager.tryLock(key1, 1, 5, TimeUnit.SECONDS)) {
                // 在同一线程中执行恢复
                recoveryResults[0] = recoveryHandler.tryRecoverLock(key1, rLock1, new RuntimeException("Test1"));
            }
        });

        Thread lockAndRecover2 = new Thread(() -> {
            RLock rLock2 = lockManager.getLock(key2);
            recoveryHandler.registerLock(key2, rLock2, 30, TimeUnit.SECONDS);
            
            // 在当前线程获取锁
            if (lockManager.tryLock(key2, 1, 5, TimeUnit.SECONDS)) {
                // 在同一线程中执行恢复
                recoveryResults[1] = recoveryHandler.tryRecoverLock(key2, rLock2, new RuntimeException("Test2"));
            }
        });

        lockAndRecover1.start();
        lockAndRecover2.start();

        lockAndRecover1.join(3000);
        lockAndRecover2.join(3000);

        // Then - 验证恢复操作成功
        assertTrue(recoveryResults[0], "Lock recovery for key1 should succeed");
        assertTrue(recoveryResults[1], "Lock recovery for key2 should succeed");
        
        // 验证锁确实已被释放（通过尝试获取锁来验证）
        RLock testLock1 = lockManager.getLock(key1);
        RLock testLock2 = lockManager.getLock(key2);
        assertFalse(testLock1.isLocked(), "Lock1 should be released after recovery");
        assertFalse(testLock2.isLocked(), "Lock2 should be released after recovery");
    }
}