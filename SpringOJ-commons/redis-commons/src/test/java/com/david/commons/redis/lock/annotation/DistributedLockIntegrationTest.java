package com.david.commons.redis.lock.annotation;

import com.david.commons.redis.exception.RedisLockException;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.LockType;
import com.david.commons.redis.RealRedisTestBase;
import com.david.commons.redis.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DistributedLock 注解集成测试
 *
 * @author David
 */
@SpringBootTest(classes = TestApplication.class)
@Import(DistributedLockIntegrationTest.TestConfig.class)
class DistributedLockIntegrationTest extends RealRedisTestBase {

    @Autowired
    private DistributedLockManager lockManager;

    @Autowired
    private TestService testService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Test
    void testBasicLockAnnotation() {
        // When
        String result = testService.basicLockMethod("123");

        // Then
        assertEquals("processed:123", result);
    }

    @Test
    void testSpELExpressionInKey() {
        // When
        String result = testService.spelKeyMethod("456");

        // Then
        assertEquals("user:456", result);
    }

    @Test
    void testObjectPropertyInKey() {
        TestUser user = new TestUser("789", "testUser");
        // When
        String result = testService.objectPropertyKeyMethod(user);

        // Then
        assertEquals("processed:789", result);
    }

    @Test
    void testConditionalLockTrue() {
        // When
        String result = testService.conditionalLockMethod("active");

        // Then
        assertEquals("conditional:active", result);
    }

    @Test
    void testConditionalLockFalse() {
        // When - 条件为 false，应该跳过锁
        String result = testService.conditionalLockMethod("inactive");

        // Then
        assertEquals("conditional:inactive", result);
    }

    @Test
    void testLockFailureWithException() {
        // 在其他线程持有同一把锁，确保本线程获取失败并抛出异常
        String key = "test:123";
        CountDownLatch ready = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                boolean ok = lockManager.tryLock(key, 1, 3, TimeUnit.SECONDS);
                if (ok) {
                    ready.countDown();
                    Thread.sleep(1500);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                try { lockManager.unlock(key); } catch (Exception ignored) {}
            }
        });
        t.start();
        try {
            ready.await(2, TimeUnit.SECONDS);
            assertThrows(RedisLockException.class, () -> testService.basicLockMethod("123"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            try { t.join(5000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            try { lockManager.forceUnlock(key); } catch (Exception ignored) {}
        }
    }

    @Test
    void testLockFailureWithReturnDefault() {
        String key = "test:123";
        CountDownLatch ready = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                boolean ok = lockManager.tryLock(key, 1, 3, TimeUnit.SECONDS);
                if (ok) {
                    ready.countDown();
                    Thread.sleep(1500);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                try { lockManager.unlock(key); } catch (Exception ignored) {}
            }
        });
        t.start();
        try {
            ready.await(2, TimeUnit.SECONDS);
            String result = testService.returnDefaultOnFailureMethod("123");
            assertNull(result); // 默认返回值为 null
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            try { t.join(5000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            try { lockManager.forceUnlock(key); } catch (Exception ignored) {}
        }
    }

    @Test
    void testLockFailureWithSkipLock() {
        String key = "test:123";
        CountDownLatch ready = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                boolean ok = lockManager.tryLock(key, 1, 3, TimeUnit.SECONDS);
                if (ok) {
                    ready.countDown();
                    Thread.sleep(1500);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                try { lockManager.unlock(key); } catch (Exception ignored) {}
            }
        });
        t.start();
        try {
            ready.await(2, TimeUnit.SECONDS);
            String result = testService.skipLockOnFailureMethod("123");
            assertEquals("skipped:123", result); // 跳过锁直接执行
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            try { t.join(5000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            try { lockManager.forceUnlock(key); } catch (Exception ignored) {}
        }
    }

    @Test
    void testVoidMethodWithLock() {
        AtomicInteger counter = new AtomicInteger(0);
        // When
        assertDoesNotThrow(() -> testService.voidMethodWithLock("test", counter));

        // Then
        assertEquals(1, counter.get());
    }

    /**
     * 测试服务类
     */
    public static class TestService {

        @DistributedLock(key = "test:#{#id}", waitTime = 1, leaseTime = 3, timeUnit = TimeUnit.SECONDS)
        public String basicLockMethod(String id) {
            return "processed:" + id;
        }

        @DistributedLock(key = "user:#{#userId}", lockType = LockType.FAIR, waitTime = 5, leaseTime = 60)
        public String spelKeyMethod(String userId) {
            return "user:" + userId;
        }

        @DistributedLock(key = "user:#{#user.id}")
        public String objectPropertyKeyMethod(TestUser user) {
            return "processed:" + user.getId();
        }

        @DistributedLock(key = "conditional:#{#status}", condition = "#status == 'active'")
        public String conditionalLockMethod(String status) {
            return "conditional:" + status;
        }

        @DistributedLock(key = "test:#{#id}", waitTime = 1, leaseTime = 3, timeUnit = TimeUnit.SECONDS, failureStrategy = DistributedLock.LockFailureStrategy.RETURN_DEFAULT)
        public String returnDefaultOnFailureMethod(String id) {
            return "processed:" + id;
        }

        @DistributedLock(key = "test:#{#id}", waitTime = 1, leaseTime = 3, timeUnit = TimeUnit.SECONDS, failureStrategy = DistributedLock.LockFailureStrategy.SKIP_LOCK)
        public String skipLockOnFailureMethod(String id) {
            return "skipped:" + id;
        }

        @DistributedLock(key = "void:#{#param}")
        public void voidMethodWithLock(String param, AtomicInteger counter) {
            counter.incrementAndGet();
        }
    }

    /**
     * 测试用户类
     */
    public static class TestUser {
        private String id;
        private String name;

        public TestUser(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}