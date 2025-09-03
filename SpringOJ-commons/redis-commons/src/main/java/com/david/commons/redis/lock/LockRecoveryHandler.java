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
        log.debug("注册活跃锁: {}", key);
    }

    /** 注销锁 */
    public void unregisterLock(String key) {
        LockInfo removed = activeLocks.remove(key);
        if (removed != null) {
            log.debug("注销锁: {}", key);
        }
    }

    /** 尝试恢复锁释放异常 */
    public boolean tryRecoverLock(String key, RLock lock, Exception originalException) {
        log.warn(
                "尝试恢复锁: {}，由于异常: {}",
                key,
                originalException.getMessage());

        try {
            // 检查锁是否为null
            if (lock == null) {
                log.warn("无法恢复空锁，键: {}", key);
                return false;
            }

            // 检查锁是否仍然被当前线程持有
            if (lock.isHeldByCurrentThread()) {
                // 尝试强制释放
                boolean forceUnlocked = lock.forceUnlock();
                if (forceUnlocked) {
                    log.info("成功强制解锁: {}", key);
                    unregisterLock(key);
                    return true;
                } else {
                    log.warn("强制解锁失败: {}", key);
                }
            } else {
                log.info(
                        "锁 {} 不再由当前线程持有，认为已恢复",
                        key);
                unregisterLock(key);
                return true;
            }
        } catch (Exception e) {
            log.error("恢复锁失败: {}", key, e);
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
            log.info("锁恢复任务已启动，间隔: {}ms", recoveryCheckInterval);
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
                                            "检测到可能过期的锁: {}，尝试清理",
                                            key);

                                    RLock lock = lockInfo.getLock();
                                    if (!lock.isLocked()) {
                                        log.info(
                                                "锁 {} 不再活跃，从跟踪中移除",
                                                key);
                                        return true; // 从map中移除
                                    }

                                    // 如果锁仍然活跃但可能有问题，记录警告
                                    if (!lock.isHeldByCurrentThread()) {
                                        log.warn(
                                                "锁 {} 活跃但未被任何跟踪的线程持有",
                                                key);
                                    }
                                }

                                return false; // 保留在map中

                            } catch (Exception e) {
                                log.error("锁恢复检查期间出错: {}", key, e);
                                return false; // 保留在map中，下次再检查
                            }
                        });
    }

    /** 关闭恢复处理器 */
    public synchronized void shutdown() {
        if (started) {
            log.info("关闭锁恢复处理器");
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
