package com.david.redis.commons.config;

import com.david.redis.commons.properties.RedisCommonsProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 基础配置类
 *
 * @author david
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {

    private final RedisCommonsProperties redisCommonsProperties;

    /** 配置 RedisTemplate */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("正在配置 RedisTemplate...");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // key 采用 String 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value 序列化方式采用 jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash 的 value 序列化方式采用 jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 开启事务支持
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();

        log.info("RedisTemplate 配置完成");
        return template;
    }

    /** 创建 Jackson 序列化器 */
    private Jackson2JsonRedisSerializer<Object> createJacksonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 创建自定义的 JavaTimeModule 来处理日期时间序列化问题
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置日期时间格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 自定义 LocalDateTime 反序列化器，支持多种格式
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter) {
            @Override
            public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser parser,
                    com.fasterxml.jackson.databind.DeserializationContext context) throws java.io.IOException {
                String dateString = parser.getValueAsString();
                if (dateString == null || dateString.trim().isEmpty()) {
                    return null;
                }

                try {
                    // 尝试完整的日期时间格式
                    if (dateString.contains(" ") || dateString.contains("T")) {
                        if (dateString.contains("T")) {
                            // ISO 格式
                            return LocalDateTime.parse(dateString);
                        } else {
                            // 自定义格式 yyyy-MM-dd HH:mm:ss
                            return LocalDateTime.parse(dateString, dateTimeFormatter);
                        }
                    } else {
                        // 只有日期的情况，添加默认时间 00:00:00
                        return java.time.LocalDate.parse(dateString, dateFormatter).atStartOfDay();
                    }
                } catch (Exception e) {
                    // 最后尝试 ISO 格式
                    try {
                        return LocalDateTime.parse(dateString);
                    } catch (Exception ex) {
                        log.warn("无法解析日期时间字符串: {}", dateString, ex);
                        return null;
                    }
                }
            }
        });

        // 配置序列化器
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

        // 注册自定义的 JavaTimeModule
        objectMapper.registerModule(javaTimeModule);

        // 禁用将日期写为时间戳的功能
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略未知属性，避免反序列化时出错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        log.info("已配置自定义 LocalDateTime 反序列化器，支持多种日期格式");
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }
}
