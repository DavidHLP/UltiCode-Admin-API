package com.david.commons.redis.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis 缓存清除注解
 * <p>
 * 用于标记方法执行后需要清除指定的缓存。
 * 支持 SpEL 表达式动态生成缓存键和条件判断。
 * </p>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * {@code
 * &#64;RedisEvict(key = "'user:' + #userId")
 * public void deleteUser(Long userId) {
 *     userService.delete(userId);
 * }
 *
 * @RedisEvict(key = "'user:*'", allEntries = true)
 * public void clearAllUserCache() {
 *     // 清除所有用户缓存
 * }
 * }
 * </pre>
 *
 * @author David
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisEvict {

    /**
     * 缓存键表达式
     * <p>
     * 支持 SpEL 表达式，可以引用方法参数。
     * 当 allEntries = true 时，支持通配符模式。
     * 例如：'user:' + #userId, 'user:*'
     * </p>
     *
     * @return 缓存键表达式
     */
    String key() default "";

    /**
     * 多个缓存键表达式
     * <p>
     * 支持 SpEL 表达式，遍历求值后逐一清除。
     * 当同时配置 key 与 keys 时，优先使用 keys。
     * 当 allEntries = true 时，单个表达式可为模式（如：'user:*'）。
     * </p>
     *
     * @return 缓存键表达式数组，默认空
     */
    String[] keys() default {};

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
     * 清除条件表达式
     * <p>
     * 支持 SpEL 表达式，只有当条件为 true 时才进行缓存清除。
     * 例如：#userId > 0, #result.success
     * </p>
     *
     * @return 清除条件表达式，默认为空表示总是清除
     */
    String condition() default "";

    /**
     * 是否清除所有匹配的缓存项
     * <p>
     * true：清除所有匹配 key 模式的缓存项（支持通配符）
     * false：只清除精确匹配的单个缓存项
     * </p>
     *
     * @return 是否清除所有匹配项，默认 false
     */
    boolean allEntries() default false;

    /**
     * 执行时机
     * <p>
     * true：方法执行前清除缓存
     * false：方法执行后清除缓存
     * </p>
     *
     * @return 是否在方法执行前清除，默认 false（执行后清除）
     */
    boolean beforeInvocation() default false;

    /**
     * 是否同步执行
     * <p>
     * true：同步执行缓存清除操作
     * false：异步执行缓存清除操作
     * </p>
     *
     * @return 是否同步执行，默认 true
     */
    boolean sync() default true;

    /**
     * 批量清除大小
     * <p>
     * 当 allEntries = true 时，每批清除的缓存项数量。
     * 避免一次性清除过多缓存项导致性能问题。
     * 0 表示不限制批量大小。
     * </p>
     *
     * @return 批量清除大小，默认 1000
     */
    int batchSize() default 1000;
}