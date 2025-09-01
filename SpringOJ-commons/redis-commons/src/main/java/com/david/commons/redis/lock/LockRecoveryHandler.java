package com.david.commons.redis.lock;

import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 锁恢复处理器 处理锁释放异常和死锁恢复
 *
 * @author David
 */
@Slf4j
public class LockRecoveryHandler {

    private final ScheduledExecutorService recoveryExecutor;
    private final ConcurrentHashMap<String, LockInfo> activeLocks;
    private final long recoveryCheckInterval;
    private volatile boolean started = false;

    public LockRecoveryHandler() {
        this(30, TimeUnit.SECONDS);
    }

    public LockRecoveryHandler(long recoveryCheckInterval, TimeUnit unit) {
        this.recoveryCheckInterval = unit.toMillis(recoveryCheckInterval);
        this.recoveryExecutor =
                Executors.newSingleThreadScheduledExecutor(
                        r -> {
                            Thread t = new Thread(r, "lock-recovery-thread");
                            t.setDaemon(true);
                            return t;
                        });
        this.activeLocks = new ConcurrentHashMap<>();
    }

    /** 注册活跃锁 */
    public void registerLock(String key, RLock lock, long leaseTime, TimeUnit unit) {
        LockInfo lockInfo =
                new LockInfo(lock, System.currentTimeMillis(), unit.toMillis(leaseTime));
        activeLocks.put(key, lockInfo);
        log.debug("Registered active lock: {}", key);
    }

    /** 注销锁 */
    public void unregisterLock(String key) {
        LockInfo removed = activeLocks.remove(key);
        if (removed != null) {
            log.debug("Unregistered lock: {}", key);
        }
    }

    /** 尝试恢复锁释放异常 */
    public boolean tryRecoverLock(String key, RLock lock, Exception originalException) {
        log.warn(
                "Attempting to recover lock: {} due to exception: {}",
                key,
                originalException.getMessage());

        try {
            // 检查锁是否为null
            if (lock == null) {
                log.warn("Cannot recover null lock for key: {}", key);
                return false;
            }

            // 检查锁是否仍然被当前线程持有
            if (lock.isHeldByCurrentThread()) {
                // 尝试强制释放
                boolean forceUnlocked = lock.forceUnlock();
                if (forceUnlocked) {
                    log.info("Successfully force unlocked: {}", key);
                    unregisterLock(key);
                    return true;
                } else {
                    log.warn("Failed to force unlock: {}", key);
                }
            } else {
                log.info(
                        "Lock {} is no longer held by current thread, considering it recovered",
                        key);
                unregisterLock(key);
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to recover lock: {}", key, e);
        }

        return false;
    }

    /** 启动恢复任务 */
    public synchronized void startRecoveryTask() {
        if (!started) {
            recoveryExecutor.scheduleWithFixedDelay(
                    this::performRecoveryCheck,
                    recoveryCheckInterval,
                    recoveryCheckInterval,
                    TimeUnit.MILLISECONDS);
            started = true;
            log.info("Lock recovery task started with interval: {}ms", recoveryCheckInterval);
        }
    }

    /** 执行恢复检查 */
    private void performRecoveryCheck() {
        if (activeLocks.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        activeLocks
                .entrySet()
                .removeIf(
                        entry -> {
                            String key = entry.getKey();
                            LockInfo lockInfo = entry.getValue();

                            try {
                                // 检查锁是否已过期
                                if (currentTime - lockInfo.getAcquiredTime()
                                        > lockInfo.getLeaseTime() + 5000) { // 5秒容错
                                    log.warn(
                                            "Detected potentially expired lock: {}, attempting cleanup",
                                            key);

                                    RLock lock = lockInfo.getLock();
                                    if (!lock.isLocked()) {
                                        log.info(
                                                "Lock {} is no longer active, removing from tracking",
                                                key);
                                        return true; // 从map中移除
                                    }

                                    // 如果锁仍然活跃但可能有问题，记录警告
                                    if (!lock.isHeldByCurrentThread()) {
                                        log.warn(
                                                "Lock {} is active but not held by any tracked thread",
                                                key);
                                    }
                                }

                                return false; // 保留在map中

                            } catch (Exception e) {
                                log.error("Error during recovery check for lock: {}", key, e);
                                return false; // 保留在map中，下次再检查
                            }
                        });
    }

    /** 关闭恢复处理器 */
    public synchronized void shutdown() {
        if (started) {
            log.info("Shutting down lock recovery handler");
            recoveryExecutor.shutdown();
            try {
                if (!recoveryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    recoveryExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                recoveryExecutor.shutdownNow();
            }
            activeLocks.clear();
            started = false;
        }
    }

    /** 锁信息 */
    private static class LockInfo {
        private final RLock lock;
        private final long acquiredTime;
        private final long leaseTime;

        public LockInfo(RLock lock, long acquiredTime, long leaseTime) {
            this.lock = lock;
            this.acquiredTime = acquiredTime;
            this.leaseTime = leaseTime;
        }

        public RLock getLock() {
            return lock;
        }

        public long getAcquiredTime() {
            return acquiredTime;
        }

        public long getLeaseTime() {
            return leaseTime;
        }
    }
}
