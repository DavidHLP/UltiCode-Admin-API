package com.david.commons.redis.cache;

import lombok.Builder;
import lombok.Data;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;

/**
 * 缓存操作上下文
 *
 * <p>封装缓存操作过程中需要的上下文信息，包括方法信息、参数、返回值等。 用于 SpEL 表达式求值和缓存操作处理。
 *
 * @author David
 */
@Data
@Builder
public class CacheContext {

    /** 目标方法 */
    private final Method method;

    /** 方法参数 */
    private final Object[] args;

    /** 目标对象 */
    private final Object target;

    /** 方法返回值（可能为 null） */
    private Object result;

    /**
     * 获取参数名称数组
     *
     * <p>用于 SpEL 表达式中通过参数名引用参数值
     */
    public String[] getParameterNames() {
        // 优先使用 Spring 的参数名发现器（支持 -parameters 或调试信息）
        ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        return discoverer.getParameterNames(method);
    }

    /** 获取方法名 */
    public String getMethodName() {
        return method.getName();
    }

    /** 获取目标类名 */
    public String getTargetClassName() {
        return target.getClass().getSimpleName();
    }
}
