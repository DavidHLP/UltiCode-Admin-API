package com.david.redis.commons.config;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.properties.RedisCommonsProperties;

import lombok.RequiredArgsConstructor;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 配置类
 *
 * @author david
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "spring.data.redis", name = "host")
public class RedissonConfig {

    private final RedisProperties redisProperties;
    private final RedisCommonsProperties redisCommonsProperties;
    private final LogUtils logUtils;

    /** 配置 RedissonClient */
    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        logUtils.business()
                .event("redisson_config", "configure_client", "start", "正在配置 RedissonClient");

        Config config = new Config();

        // 根据 Redis 配置类型进行配置
        if (redisProperties.getSentinel() != null) {
            // 哨兵模式
            configureSentinel(config);
        } else if (redisProperties.getCluster() != null) {
            // 集群模式
            configureCluster(config);
        } else {
            // 单机模式
            configureSingle(config);
        }

        RedissonClient redissonClient = Redisson.create(config);
        logUtils.business()
                .event("redisson_config", "configure_client", "success", "RedissonClient 配置完成");
        return redissonClient;
    }

    /** 配置单机模式 */
    private void configureSingle(Config config) {
        String address =
                String.format(
                        "redis://%s:%d", redisProperties.getHost(), redisProperties.getPort());

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisProperties.getDatabase())
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(5000)
                .setTimeout(3000)
                .setRetryAttempts(redisCommonsProperties.getLock().getRetryAttempts())
                .setRetryInterval(1500);

        // 设置密码
        if (StringUtils.hasText(redisProperties.getPassword())) {
            config.useSingleServer().setPassword(redisProperties.getPassword());
        }

        logUtils.business()
                .event("redisson_config", "configure_single", "success", "address: " + address);
    }

    /** 配置哨兵模式 */
    private void configureSentinel(Config config) {
        RedisProperties.Sentinel sentinel = redisProperties.getSentinel();

        config.useSentinelServers()
                .setMasterName(sentinel.getMaster())
                .addSentinelAddress(
                        sentinel.getNodes().stream()
                                .map(node -> "redis://" + node)
                                .toArray(String[]::new))
                .setDatabase(redisProperties.getDatabase())
                .setConnectTimeout(5000)
                .setTimeout(3000)
                .setRetryAttempts(redisCommonsProperties.getLock().getRetryAttempts())
                .setRetryInterval(1500);

        if (StringUtils.hasText(redisProperties.getPassword())) {
            config.useSentinelServers().setPassword(redisProperties.getPassword());
        }

        logUtils.business()
                .event(
                        "redisson_config",
                        "configure_sentinel",
                        "success",
                        "master: " + sentinel.getMaster(),
                        "nodes: " + sentinel.getNodes());
    }

    /** 配置集群模式 */
    private void configureCluster(Config config) {
        RedisProperties.Cluster cluster = redisProperties.getCluster();

        config.useClusterServers()
                .addNodeAddress(
                        cluster.getNodes().stream()
                                .map(node -> "redis://" + node)
                                .toArray(String[]::new))
                .setConnectTimeout(5000)
                .setTimeout(3000)
                .setRetryAttempts(redisCommonsProperties.getLock().getRetryAttempts())
                .setRetryInterval(1500);

        if (StringUtils.hasText(redisProperties.getPassword())) {
            config.useClusterServers().setPassword(redisProperties.getPassword());
        }

        logUtils.business()
                .event(
                        "redisson_config",
                        "configure_cluster",
                        "success",
                        "nodes: " + cluster.getNodes());
    }
}
