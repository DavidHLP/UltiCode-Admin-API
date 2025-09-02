package com.david.commons.redis.cache.aspect.operator;

import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.config.RedisCommonsProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 缓存TTL管理器
 *
 * <p>负责TTL的计算与策略管理，支持空值TTL和默认TTL
 *
 * @author David
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheTtlManager {

    private final RedisCommonsProperties properties;

    /**
     * 确定缓存值的TTL
     *
     * @param metadata 缓存元数据
     * @param value 要缓存的值
     * @return TTL值（秒）
     *         <ul>
     *         <li>大于0：指定TTL</li>
     *         <li>等于0：永不过期</li>
     *         <li>小于0：使用默认TTL</li>
     *         </ul>
     */
    public long determineTtl(CacheMetadata metadata, Object value) {
        // 如果值为 null，使用空值 TTL
        if (value == null && metadata.nullTtl() >= 0) {
            long nullTtl = metadata.nullTtl() > 0
                    ? metadata.nullTtl()
                    : properties.getCache().getNullCacheTtl();
            log.debug("Using null TTL: {} for null value", nullTtl);
            return nullTtl;
        }

        // 使用配置的 TTL
        if (metadata.ttl() >= 0) {
            log.debug("Using configured TTL: {} from metadata", metadata.ttl());
            return metadata.ttl();
        }

        // 使用默认 TTL
        long defaultTtl = properties.getCache().getDefaultTtl();
        log.debug("Using default TTL: {}", defaultTtl);
        return defaultTtl;
    }

    /**
     * 判断是否为永不过期
     */
    public boolean isNeverExpire(long ttl) {
        return ttl == 0;
    }

    /**
     * 判断是否使用默认TTL
     */
    public boolean shouldUseDefault(long ttl) {
        return ttl < 0;
    }

    /**
     * 获取默认TTL
     */
    public long getDefaultTtl() {
        return properties.getCache().getDefaultTtl();
    }

    /**
     * 获取空值缓存TTL
     */
    public long getNullCacheTtl() {
        return properties.getCache().getNullCacheTtl();
    }
}
