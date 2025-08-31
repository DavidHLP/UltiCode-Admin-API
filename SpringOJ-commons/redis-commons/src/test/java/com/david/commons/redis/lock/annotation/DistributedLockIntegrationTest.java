package com.david.commons.redis.lock.annotation;

import com.david.commons.redis.exception.RedisLockException;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.LockType;
import com.david.commons.redis.lock.aspect.DistributedLockAspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DistributedLock 注解集成测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class DistributedLockIntegrationTest {

    @Mock
    private DistributedLockManager lockManager;

    private TestService testService;
    private TestService proxiedTestService;

    @BeforeEach
    void setUp() {
        testService = new TestService();

        // 创建 AOP 代理
        AspectJProxyFactory factory = new AspectJProxyFactory(testService);
        factory.addAspect(new DistributedLockAspect(lockManager));
        proxiedTestService = factory.getProxy();
    }

    @Test
    void testBasicLockAnnotation() {
        // Given
        doAnswer(invocation -> {
            Supplier<String> supplier = invocation.getArgument(1);
            return supplier.get();
        }).when(lockManager).executeWithLock(
                eq("test:123"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));

        // When
        String result = proxiedTestService.basicLockMethod("123");

        // Then
        assertEquals("processed:123", result);
        verify(lockManager).executeWithLock(
                eq("test:123"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));
    }

    @Test
    void testSpELExpressionInKey() {
        // Given
        doAnswer(invocation -> {
            Supplier<String> supplier = invocation.getArgument(1);
            return supplier.get();
        }).when(lockManager).executeWithLock(
                eq("user:456"),
                any(Supplier.class),
                eq(5L),
                eq(60L),
                eq(TimeUnit.SECONDS),
                eq(LockType.FAIR));

        // When
        String result = proxiedTestService.spelKeyMethod("456");

        // Then
        assertEquals("user:456", result);
        verify(lockManager).executeWithLock(
                eq("user:456"),
                any(Supplier.class),
                eq(5L),
                eq(60L),
                eq(TimeUnit.SECONDS),
                eq(LockType.FAIR));
    }

    @Test
    void testObjectPropertyInKey() {
        // Given
        TestUser user = new TestUser("789", "testUser");
        doAnswer(invocation -> {
            Supplier<String> supplier = invocation.getArgument(1);
            return supplier.get();
        }).when(lockManager).executeWithLock(
                eq("user:789"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));

        // When
        String result = proxiedTestService.objectPropertyKeyMethod(user);

        // Then
        assertEquals("processed:789", result);
        verify(lockManager).executeWithLock(
                eq("user:789"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));
    }

    @Test
    void testConditionalLockTrue() {
        // Given - 条件为 true
        doAnswer(invocation -> {
            Supplier<String> supplier = invocation.getArgument(1);
            return supplier.get();
        }).when(lockManager).executeWithLock(
                eq("conditional:active"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));

        // When
        String result = proxiedTestService.conditionalLockMethod("active");

        // Then
        assertEquals("conditional:active", result);
        verify(lockManager).executeWithLock(
                eq("conditional:active"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));
    }

    @Test
    void testConditionalLockFalse() {
        // When - 条件为 false，应该跳过锁
        String result = proxiedTestService.conditionalLockMethod("inactive");

        // Then
        assertEquals("conditional:inactive", result);
        verifyNoInteractions(lockManager);
    }

    @Test
    void testLockFailureWithException() {
        // Given
        doThrow(new RedisLockException("Lock acquisition failed"))
                .when(lockManager).executeWithLock(
                        anyString(),
                        any(Supplier.class),
                        anyLong(),
                        anyLong(),
                        any(TimeUnit.class),
                        any(LockType.class));

        // When & Then
        assertThrows(RedisLockException.class, () -> proxiedTestService.basicLockMethod("123"));
    }

    @Test
    void testLockFailureWithReturnDefault() {
        // Given
        doThrow(new RedisLockException("Lock acquisition failed"))
                .when(lockManager).executeWithLock(
                        anyString(),
                        any(Supplier.class),
                        anyLong(),
                        anyLong(),
                        any(TimeUnit.class),
                        any(LockType.class));

        // When
        String result = proxiedTestService.returnDefaultOnFailureMethod("123");

        // Then
        assertNull(result); // 默认返回值为 null
    }

    @Test
    void testLockFailureWithSkipLock() {
        // Given
        doThrow(new RedisLockException("Lock acquisition failed"))
                .when(lockManager).executeWithLock(
                        anyString(),
                        any(Supplier.class),
                        anyLong(),
                        anyLong(),
                        any(TimeUnit.class),
                        any(LockType.class));

        // When
        String result = proxiedTestService.skipLockOnFailureMethod("123");

        // Then
        assertEquals("skipped:123", result); // 跳过锁直接执行
    }

    @Test
    void testVoidMethodWithLock() {
        // Given
        AtomicInteger counter = new AtomicInteger(0);
        doAnswer(invocation -> {
            Supplier<Object> supplier = invocation.getArgument(1);
            return supplier.get();
        }).when(lockManager).executeWithLock(
                eq("void:test"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));

        // When
        assertDoesNotThrow(() -> proxiedTestService.voidMethodWithLock("test", counter));

        // Then
        assertEquals(1, counter.get());
        verify(lockManager).executeWithLock(
                eq("void:test"),
                any(Supplier.class),
                eq(10L),
                eq(30L),
                eq(TimeUnit.SECONDS),
                eq(LockType.REENTRANT));
    }

    /**
     * 测试服务类
     */
    public static class TestService {

        @DistributedLock(key = "test:#{#id}")
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

        @DistributedLock(key = "test:#{#id}", failureStrategy = DistributedLock.LockFailureStrategy.RETURN_DEFAULT)
        public String returnDefaultOnFailureMethod(String id) {
            return "processed:" + id;
        }

        @DistributedLock(key = "test:#{#id}", failureStrategy = DistributedLock.LockFailureStrategy.SKIP_LOCK)
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