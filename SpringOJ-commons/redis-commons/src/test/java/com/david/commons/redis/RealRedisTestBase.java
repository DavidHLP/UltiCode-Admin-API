package com.david.commons.redis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

/**
 * Redis 真实连接测试基类
 * <p>
 * 替代 TestcontainersRedisTestBase，使用真实的本地Redis实例进行测试
 * 
 * @author David
 */
@TestPropertySource(properties = {
    "spring.redis.commons.enabled=true",
    "spring.redis.commons.key-prefix=test:",
    "spring.redis.commons.serialization.default-type=JSON",
    "spring.redis.commons.serialization.enable-performance-monitoring=true",
    "spring.redis.commons.cache.default-ttl=3600",
    "spring.redis.commons.lock.default-wait-time=10",
    "spring.redis.commons.lock.default-lease-time=30",
    "spring.redis.commons.monitoring.enabled=true",
    // 关闭系统/处理器指标绑定，避免在某些环境下 JDK cgroup 探测导致的 NPE
    "management.metrics.binders.processor.enabled=false",
    "management.metrics.binders.uptime.enabled=false"
})
public abstract class RealRedisTestBase {

    // Redis连接配置常量
    protected static final String REDIS_HOST = "192.168.252.1";
    protected static final int REDIS_PORT = 6379;
    protected static final int REDIS_DATABASE = 0;
    protected static final long REDIS_TIMEOUT = 1800000; // 30分钟，毫秒格式
    protected static final String REDIS_PASSWORD = "Alone117";

    // 测试用键前缀
    protected static final String TEST_KEY_PREFIX = "test:";

    /**
     * Redis连接检查
     */
    @BeforeAll
    static void checkRedisConnection() {
        System.out.println("=== Redis 连接配置 ===");
        System.out.println("Host: " + REDIS_HOST);
        System.out.println("Port: " + REDIS_PORT);
        System.out.println("Database: " + REDIS_DATABASE);
        System.out.println("Timeout: " + REDIS_TIMEOUT + "ms");
        System.out.println("Password: [已配置]");
        System.out.println("====================");
    }

    /**
     * 每个测试前清理测试键
     */
    @BeforeEach
    void cleanupTestKeys() {
        // 可在子类中重写以添加特定的清理逻辑
    }

    /**
     * 测试配置类
     */
    @TestConfiguration
    @EnableAutoConfiguration(exclude = { SystemMetricsAutoConfiguration.class })
    public static class RealRedisTestConfiguration {

        /**
         * 配置Redis连接工厂
         */
        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            // Redis独立配置
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
            redisConfig.setHostName(REDIS_HOST);
            redisConfig.setPort(REDIS_PORT);
            redisConfig.setDatabase(REDIS_DATABASE);
            redisConfig.setPassword(REDIS_PASSWORD);

            // Lettuce客户端配置
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .commandTimeout(Duration.ofMillis(REDIS_TIMEOUT))
                    .shutdownTimeout(Duration.ofMillis(200))
                    .build();

            return new LettuceConnectionFactory(redisConfig, clientConfig);
        }

        /**
         * 配置RedisTemplate
         */
        @Bean
        @Primary
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);

            // 使用String序列化器作为键序列化器
            StringRedisSerializer stringSerializer = new StringRedisSerializer();
            template.setKeySerializer(stringSerializer);
            template.setHashKeySerializer(stringSerializer);

            // 使用JSON序列化器作为值序列化器
            GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
            template.setValueSerializer(jsonSerializer);
            template.setHashValueSerializer(jsonSerializer);

            template.afterPropertiesSet();
            return template;
        }

        /**
         * 配置Redisson客户端
         */
        @Bean
        @Primary
        public RedissonClient redissonClient() {
            Config config = new Config();
            
            // 使用单节点模式
            config.useSingleServer()
                    .setAddress("redis://" + REDIS_HOST + ":" + REDIS_PORT)
                    .setDatabase(REDIS_DATABASE)
                    .setPassword(REDIS_PASSWORD)
                    .setTimeout((int) REDIS_TIMEOUT)
                    .setConnectTimeout((int) REDIS_TIMEOUT)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500)
                    .setConnectionMinimumIdleSize(8)
                    .setConnectionPoolSize(32);

            return Redisson.create(config);
        }
    }

    /**
     * 生成测试键名
     */
    protected String testKey(String key) {
        return TEST_KEY_PREFIX + key;
    }

    /**
     * 生成随机测试键名
     */
    protected String randomTestKey() {
        return testKey("key-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
    }

    /**
     * 验证Redis连接是否可用
     */
    protected boolean isRedisAvailable(RedisTemplate<String, Object> redisTemplate) {
        try {
            redisTemplate.opsForValue().set(testKey("connection-test"), "test", Duration.ofSeconds(5));
            String result = (String) redisTemplate.opsForValue().get(testKey("connection-test"));
            redisTemplate.delete(testKey("connection-test"));
            return "test".equals(result);
        } catch (Exception e) {
            System.err.println("Redis连接测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 验证Redisson连接是否可用
     */
    protected boolean isRedissonAvailable(RedissonClient redissonClient) {
        try {
            var bucket = redissonClient.getBucket(testKey("redisson-connection-test"));
            bucket.set("test", 5, java.util.concurrent.TimeUnit.SECONDS);
            String result = (String) bucket.get();
            bucket.delete();
            return "test".equals(result);
        } catch (Exception e) {
            System.err.println("Redisson连接测试失败: " + e.getMessage());
            return false;
        }
    }
}
