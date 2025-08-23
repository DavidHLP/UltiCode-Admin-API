package com.david.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 企业级 Redis String 缓存服务
 *
 * <p>特点：
 * - 专注 String 类型高效操作；
 * - 通过 Redisson 分布式锁保证高并发场景下的数据一致性；
 * - 采用 Cache-Aside 模式 + Double-Check 缓存回填；
 * - 在设置过期时间时自动加入 0~10% 的抖动（Jitter），缓解缓存雪崩；
 * - 健壮的异常处理与详尽日志，避免将未检查异常向上抛出。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheStringService {

    private static final String LOCK_PREFIX = "lock:cache:string:";

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    /** 默认缓存 TTL（秒），用于 getWithLock 回填时未显式指定 TTL 的情况 */
    @Value("${redis.cache.string.ttl-seconds:300}")
    private long defaultTtlSeconds;

    /** 默认锁租约时间（秒），防止持锁节点宕机导致死锁 */
    @Value("${redis.cache.lock.lease-seconds:30}")
    private long defaultLockLeaseSeconds;

    /**
     * 设置 String 值并带过期时间，过期时间会自动添加 0~10% 的随机抖动
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时长
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public boolean set(String key, String value, long timeout, TimeUnit unit) {
        if (key == null) {
            log.warn("set(): key is null");
            return false;
        }
        try {
            long realTimeout = addJitter(timeout);
            if (realTimeout > 0) {
                stringRedisTemplate.opsForValue().set(key, value, Duration.ofMillis(unit.toMillis(realTimeout)));
            } else {
                stringRedisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (DataAccessException e) {
            log.error("Redis set() DataAccessException, key={}", key, e);
            return false;
        } catch (Exception e) {
            log.error("Redis set() unexpected error, key={}", key, e);
            return false;
        }
    }

    /**
     * 获取 String 值
     *
     * @param key 缓存键
     * @return 值或 null
     */
    public String get(String key) {
        if (key == null) {
            log.warn("get(): key is null");
            return null;
        }
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            log.error("Redis get() DataAccessException, key={}", key, e);
            return null;
        } catch (Exception e) {
            log.error("Redis get() unexpected error, key={}", key, e);
            return null;
        }
    }

    /**
     * Cache-Aside + 分布式锁的读取：
     * 1) 先读缓存，命中直接返回；
     * 2) 未命中，尝试获取分布式锁；
     * 3) 获取锁后 Double-Check 缓存；
     * 4) 仍未命中则执行 dbLoader 加载，并回填缓存（使用默认 TTL，并添加抖动）；
     * 5) finally 中可靠释放锁。
     *
     * @param key       缓存键
     * @param waitTime  获取锁最大等待时长
     * @param leaseTime 锁租约时长（避免死锁）
     * @param unit      时间单位
     * @param dbLoader  数据加载器，如 () -> dao.selectById(id).getName()
     * @return 值或 null（当获取锁失败时不会触发 dbLoader，以避免击穿）
     */
    public String getWithLock(String key, long waitTime, long leaseTime, TimeUnit unit, Supplier<String> dbLoader) {
        if (key == null) {
            log.warn("getWithLock(): key is null");
            return null;
        }
        try {
            // 1) 先查缓存
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }

            String lockKey = LOCK_PREFIX + key;
            RLock lock = redissonClient.getLock(lockKey);
            boolean locked = false;
            try {
                locked = lock.tryLock(waitTime, leaseTime, unit);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("getWithLock(): interrupted while acquiring lock, key={}", key, ie);
                return null;
            } catch (Exception e) {
                log.error("getWithLock(): error while acquiring lock, key={}", key, e);
                return null;
            }

            if (!locked) {
                // 未获取到锁，避免同时回源，直接再读一次缓存
                log.warn("getWithLock(): failed to acquire lock, key={}", key);
                try {
                    return stringRedisTemplate.opsForValue().get(key);
                } catch (Exception re) {
                    log.error("getWithLock(): re-check cache failed, key={}", key, re);
                    return null;
                }
            }

            try {
                // 2) Double-Check
                cached = stringRedisTemplate.opsForValue().get(key);
                if (cached != null) {
                    return cached;
                }
                // 3) 回源加载
                String loaded = null;
                try {
                    loaded = dbLoader == null ? null : dbLoader.get();
                } catch (Exception loadEx) {
                    log.error("getWithLock(): dbLoader threw exception, key={}", key, loadEx);
                }
                // 4) 回填（仅在非 null 时回填，String 服务不缓存 null）
                if (loaded != null) {
                    long ttl = Math.max(1L, defaultTtlSeconds);
                    set(key, loaded, ttl, TimeUnit.SECONDS);
                }
                return loaded;
            } finally {
                try {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                } catch (Exception unlockEx) {
                    log.error("getWithLock(): unlock failed, key={}", key, unlockEx);
                }
            }
        } catch (Exception e) {
            log.error("getWithLock(): unexpected error, key={}", key, e);
            return null;
        }
    }

    /**
     * 删除键
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        if (key == null) {
            log.warn("delete(): key is null");
            return false;
        }
        try {
            Boolean rs = stringRedisTemplate.delete(key);
            return Boolean.TRUE.equals(rs);
        } catch (DataAccessException e) {
            log.error("Redis delete() DataAccessException, key={}", key, e);
            return false;
        } catch (Exception e) {
            log.error("Redis delete() unexpected error, key={}", key, e);
            return false;
        }
    }

    /**
     * 尝试获取锁后设置值（用于热点 Key 重建，防止击穿）。
     *
     * @param key       缓存键
     * @param value     缓存值
     * @param timeout   值过期时长
     * @param unit      值过期时间单位
     * @param waitTime  锁等待时长
     * @param lockUnit  锁时间单位
     * @return 是否成功设置（若未获得锁则返回 false，不做任何操作）
     */
    public boolean trySetWithLock(String key, String value, long timeout, TimeUnit unit, long waitTime, TimeUnit lockUnit) {
        if (key == null) {
            log.warn("trySetWithLock(): key is null");
            return false;
        }
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            long leaseTime = lockUnit.convert(Math.max(1L, defaultLockLeaseSeconds), TimeUnit.SECONDS);
            try {
                locked = lock.tryLock(waitTime, leaseTime, lockUnit);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("trySetWithLock(): interrupted acquiring lock, key={}", key, ie);
                return false;
            }
            if (!locked) {
                log.warn("trySetWithLock(): failed to acquire lock, key={}", key);
                return false;
            }
            long realTimeout = addJitter(timeout);
            if (realTimeout > 0) {
                stringRedisTemplate.opsForValue().set(key, value, Duration.ofMillis(unit.toMillis(realTimeout)));
            } else {
                stringRedisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (DataAccessException e) {
            log.error("trySetWithLock(): redis error, key={}", key, e);
            return false;
        } catch (Exception e) {
            log.error("trySetWithLock(): unexpected error, key={}", key, e);
            return false;
        } finally {
            try {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception unlockEx) {
                log.error("trySetWithLock(): unlock failed, key={}", key, unlockEx);
            }
        }
    }

    /**
     * 为给定超时时间添加 0~10% 的随机抖动
     */
    private long addJitter(long timeout) {
        if (timeout <= 0) {
            return timeout;
        }
        double ratio = ThreadLocalRandom.current().nextDouble(0.0, 0.1);
        long delta = (long) Math.floor(timeout * ratio);
        return timeout + delta;
    }
}
