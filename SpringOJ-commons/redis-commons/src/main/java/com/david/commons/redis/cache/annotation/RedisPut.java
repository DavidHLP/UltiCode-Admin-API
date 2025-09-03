package com.david.commons.redis.cache.annotation;

import com.david.commons.redis.serialization.enums.SerializationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis 缓存更新注解
 * <p>
 * 用于标记方法执行后强制更新缓存，无论缓存是否已存在。
 * 与 @RedisCacheable 不同，@RedisPut 总是执行方法并更新缓存。
 * </p>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * {@code
 * @RedisPut(key = "'user:' + #user.id", ttl = 3600)
 * public User updateUser(User user) {
 *     User updated = userService.update(user);
 *     return updated;
 * }
 * }
 * </pre>
 *
 * @author David
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisPut {

    /**
     * 缓存键表达式
     * <p>
     * 支持 SpEL 表达式，可以引用方法参数、返回值等。
     * 例如：'user:' + #userId, #user.id, #result.name
     * </p>
     *
     * @return 缓存键表达式
     */
    String key();

    /**
     * 缓存键前缀
     * <p>
     * 可选的键前缀，会自动添加到生成的缓存键前面。
     * 如果为空，则使用全局配置的前缀。
     * </p>
     *
     * @return 缓存键前缀
     */
    String keyPrefix() default "";

    /**
     * 缓存条件表达式
     * <p>
     * 支持 SpEL 表达式，只有当条件为 true 时才进行缓存更新。
     * 例如：#userId > 0, #user != null
     * </p>
     *
     * @return 缓存条件表达式，默认为空表示总是更新
     */
    String condition() default "";

    /**
     * 缓存排除条件表达式
     * <p>
     * 支持 SpEL 表达式，当条件为 true 时不进行缓存更新。
     * 可以引用方法返回值，例如：#result == null, #result.isEmpty()
     * </p>
     *
     * @return 缓存排除条件表达式，默认为空表示不排除
     */
    String unless() default "";

    /**
     * 缓存过期时间（秒）
     * <p>
     * 设置缓存的生存时间，单位为秒。
     * -1 表示使用全局默认 TTL，0 表示永不过期。
     * </p>
     *
     * @return 缓存过期时间，默认 -1 使用全局配置
     */
    long ttl() default -1L;

    /**
     * 序列化类型
     * <p>
     * 指定缓存值的序列化方式。
     * 不同的序列化方式在性能、大小、兼容性方面有不同特点。
     * </p>
     *
     * @return 序列化类型，默认使用 JSON
     */
    SerializationType serialization() default SerializationType.JSON;

    /**
     * 是否同步执行
     * <p>
     * true：同步执行缓存操作，确保缓存一致性
     * false：异步执行缓存操作，提高响应性能
     * </p>
     *
     * @return 是否同步执行，默认 true
     */
    boolean sync() default true;

    /**
     * 缓存空值
     * <p>
     * 是否缓存 null 值。
     * 当方法返回 null 时，是否将 null 值也缓存起来。
     * </p>
     *
     * @return 是否缓存空值，默认 true
     */
    boolean cacheNull() default true;

    /**
     * 空值缓存时间（秒）
     * <p>
     * 当缓存空值时，空值的过期时间。
     * 通常设置较短的时间，避免长时间缓存无效数据。
     * -1 表示使用全局配置。
     * </p>
     *
     * @return 空值缓存时间，默认 -1 使用全局配置
     */
    long nullTtl() default -1L;

    /**
     * 缓存值类型
     * <p>
     * 指定缓存值的预期类型，用于反序列化时确定目标类型。
     * 如果未指定（默认 {@code Void.class}），则在解析阶段默认取方法的返回类型。
     * </p>
     *
     * @return 缓存值类型，默认 {@code Void.class}
     */
    Class<?> type() default Void.class;

    /**
     * 缓存值表达式
     * <p>
     * 支持 SpEL 表达式，用于指定要缓存的值。
     * 默认缓存方法返回值，也可以缓存方法参数或其他表达式结果。
     * 例如：#result, #user, #result.data
     * </p>
     *
     * @return 缓存值表达式，默认为空表示缓存方法返回值
     */
    String value() default "";
}