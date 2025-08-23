package com.david.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 企业级 Redis Hash 缓存服务（支持 Java 常见对象）
 *
 * 特点：
 * - 以 Hash 结构存储，field 为字段名，value 为对象（JSON 序列化）
 * - 支持分布式锁，按 key+field 维度限流，防止并发击穿
 * - 写入时为整个 Hash key 设置过期时间（TTL 仅作用于 key）
 * - 过期时间加入 0~10% 抖动，缓解雪崩
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheHashService {

    private static final String LOCK_PREFIX = "lock:cache:hash:"; // 最终 lock key = LOCK_PREFIX + key + ":" + field

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    /** 默认 Hash 缓存 TTL（秒），用于回填时未显式指定 TTL 的情况 */
    @Value("${redis.cache.hash.ttl-seconds:300}")
    private long defaultTtlSeconds;

    /** 默认锁租约时间（秒），防止持锁节点宕机导致死锁 */
    @Value("${redis.cache.lock.lease-seconds:30}")
    private long defaultLockLeaseSeconds;

    /**
     * HSET 一个字段，并可选为整个 key 设置 TTL（带抖动）
     */
    public <T> boolean hSet(String key, String field, T value, Long timeout, TimeUnit unit) {
        if (key == null || field == null) {
            log.warn("hSet(): key or field is null");
            return false;
        }
        try {
            redisTemplate.opsForHash().put(key, field, value);
            if (timeout != null && timeout > 0 && unit != null) {
                long real = addJitter(timeout);
                redisTemplate.expire(key, Duration.ofMillis(unit.toMillis(real)));
            }
            return true;
        } catch (DataAccessException e) {
            log.error("hSet() redis error, key={}, field={}", key, field, e);
            return false;
        } catch (Exception e) {
            log.error("hSet() unexpected error, key={}, field={}", key, field, e);
            return false;
        }
    }

    /**
     * HSET 一个字段，使用默认 TTL（秒）
     */
    public <T> boolean hSet(String key, String field, T value) {
        long ttl = Math.max(1L, defaultTtlSeconds);
        return hSet(key, field, value, ttl, TimeUnit.SECONDS);
    }

    /**
     * HGET 一个字段（泛型）
     */
    public <T> T hGet(String key, String field, Class<T> type) {
        if (key == null || field == null) {
            log.warn("hGet(): key or field is null");
            return null;
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        try {
            Object v = redisTemplate.opsForHash().get(key, field);
            if (v == null) return null;
            return type.cast(v);
        } catch (DataAccessException e) {
            log.error("hGet() redis error, key={}, field={}", key, field, e);
            return null;
        } catch (Exception e) {
            log.error("hGet() unexpected error, key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * HGET 字符串便捷方法
     */
    public String hGet(String key, String field) {
        return hGet(key, field, String.class);
    }

    /**
     * HGETALL（泛型）
     */
    public <T> Map<String, T> hGetAll(String key, Class<T> type) {
        if (key == null) {
            log.warn("hGetAll(): key is null");
            return Collections.emptyMap();
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null || entries.isEmpty()) return Collections.emptyMap();
            Map<String, T> result = new HashMap<>(entries.size());
            for (Map.Entry<Object, Object> e : entries.entrySet()) {
                Object val = e.getValue();
                T casted = (val == null) ? null : type.cast(val);
                result.put(String.valueOf(e.getKey()), casted);
            }
            return result;
        } catch (DataAccessException e) {
            log.error("hGetAll() redis error, key={}", key, e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("hGetAll() unexpected error, key={}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * HGETALL 字符串便捷方法
     */
    public Map<String, String> hGetAll(String key) {
        return hGetAll(key, String.class);
    }

    /**
     * HDEL 若干字段
     */
    public boolean hDel(String key, String... fields) {
        if (key == null || fields == null || fields.length == 0) {
            log.warn("hDel(): key or fields is null/empty");
            return false;
        }
        try {
            Long removed = redisTemplate.opsForHash().delete(key, (Object[]) fields);
            return removed != null && removed > 0;
        } catch (DataAccessException e) {
            log.error("hDel() redis error, key={}", key, e);
            return false;
        } catch (Exception e) {
            log.error("hDel() unexpected error, key={}", key, e);
            return false;
        }
    }

    /**
     * 删除整个 Hash key
     */
    public boolean delete(String key) {
        if (key == null) {
            log.warn("delete(): key is null");
            return false;
        }
        try {
            Boolean rs = redisTemplate.delete(key);
            return Boolean.TRUE.equals(rs);
        } catch (DataAccessException e) {
            log.error("delete() redis error, key={}", key, e);
            return false;
        } catch (Exception e) {
            log.error("delete() unexpected error, key={}", key, e);
            return false;
        }
    }

    /**
     * 带分布式锁的 HGET：Cache-Aside + Double-Check
     * 未命中时加锁（key+field），获取后 Double-Check，再通过 loader 加载并回填 Hash 字段与 TTL
     */
    public <T> T hGetWithLock(String key, String field, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> loader, Class<T> type) {
        if (key == null || field == null) {
            log.warn("hGetWithLock(): key or field is null");
            return null;
        }
        try {
            // 1) 先查缓存
            T cached = hGet(key, field, type);
            if (cached != null) return cached;

            String lockKey = buildLockKey(key, field);
            RLock lock = redissonClient.getLock(lockKey);
            boolean locked = false;
            try {
                locked = lock.tryLock(waitTime, leaseTime, unit);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("hGetWithLock(): interrupted acquiring lock, key={}, field={}", key, field, ie);
                return null;
            } catch (Exception e) {
                log.error("hGetWithLock(): error acquiring lock, key={}, field={}", key, field, e);
                return null;
            }
            if (!locked) {
                log.warn("hGetWithLock(): failed to acquire lock, key={}, field={}", key, field);
                return hGet(key, field, type);
            }

            try {
                // 2) Double-Check
                cached = hGet(key, field, type);
                if (cached != null) return cached;

                // 3) 回源
                T loaded = null;
                try {
                    loaded = loader == null ? null : loader.get();
                } catch (Exception loadEx) {
                    log.error("hGetWithLock(): loader error, key={}, field={}", key, field, loadEx);
                }

                // 4) 回填
                if (loaded != null) {
                    long ttl = Math.max(1L, defaultTtlSeconds);
                    hSet(key, field, loaded, ttl, TimeUnit.SECONDS);
                }
                return loaded;
            } finally {
                try {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                } catch (Exception unlockEx) {
                    log.error("hGetWithLock(): unlock failed, key={}, field={}", key, field, unlockEx);
                }
            }
        } catch (Exception e) {
            log.error("hGetWithLock(): unexpected error, key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * 带分布式锁的 HGET（字符串便捷方法）
     */
    public String hGetWithLock(String key, String field, long waitTime, long leaseTime, TimeUnit unit, Supplier<String> loader) {
        return hGetWithLock(key, field, waitTime, leaseTime, unit, loader, String.class);
    }

    /**
     * 带锁 HSET（热点字段重建）
     */
    public <T> boolean tryHSetWithLock(String key, String field, T value, long timeout, TimeUnit unit, long waitTime, TimeUnit lockUnit) {
        if (key == null || field == null) {
            log.warn("tryHSetWithLock(): key or field is null");
            return false;
        }
        String lockKey = buildLockKey(key, field);
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            long leaseTime = lockUnit.convert(Math.max(1L, defaultLockLeaseSeconds), TimeUnit.SECONDS);
            try {
                locked = lock.tryLock(waitTime, leaseTime, lockUnit);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("tryHSetWithLock(): interrupted acquiring lock, key={}, field={}", key, field, ie);
                return false;
            }
            if (!locked) {
                log.warn("tryHSetWithLock(): failed to acquire lock, key={}, field={}", key, field);
                return false;
            }
            return hSet(key, field, value, timeout, unit);
        } catch (DataAccessException e) {
            log.error("tryHSetWithLock(): redis error, key={}, field={}", key, field, e);
            return false;
        } catch (Exception e) {
            log.error("tryHSetWithLock(): unexpected error, key={}, field={}", key, field, e);
            return false;
        } finally {
            try {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception unlockEx) {
                log.error("tryHSetWithLock(): unlock failed, key={}, field={}", key, field, unlockEx);
            }
        }
    }

    private String buildLockKey(String key, String field) {
        return LOCK_PREFIX + key + ":" + field;
    }

    /**
     * 为给定超时时间添加 0~10% 的随机抖动
     */
    private long addJitter(long timeout) {
        if (timeout <= 0) return timeout;
        double ratio = ThreadLocalRandom.current().nextDouble(0.0, 0.1);
        long delta = (long) Math.floor(timeout * ratio);
        return timeout + delta;
    }
}
