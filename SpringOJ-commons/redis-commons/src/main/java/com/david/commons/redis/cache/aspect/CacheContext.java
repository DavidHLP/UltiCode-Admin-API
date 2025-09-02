package com.david.commons.redis.cache.aspect;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

/**
 * 缓存操作上下文
 * <p>
 * 封装缓存操作过程中需要的上下文信息，包括方法信息、参数、返回值等。
 * 用于 SpEL 表达式求值和缓存操作处理。
 * </p>
 *
 * @author David
 */
@Data
@Builder
public class CacheContext {

    /**
     * 目标方法
     */
    private final Method method;

    /**
     * 方法参数
     */
    private final Object[] args;

    /**
     * 目标对象
     */
    private final Object target;

    /**
     * 方法返回值（可能为 null）
     */
    private Object result;

    /**
     * 获取参数名称数组
     * <p>
     * 用于 SpEL 表达式中通过参数名引用参数值
     * </p>
     */
    public String[] getParameterNames() {
        // 优先使用 Spring 的参数名发现器（支持 -parameters 或调试信息）
        ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] discovered = discoverer.getParameterNames(method);
        if (discovered != null && discovered.length == args.length) {
            return discovered;
        }

        // 回退：使用 arg0, arg1, ... 的形式
        String[] names = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            names[i] = "arg" + i;
        }
        return names;
    }

    /**
     * 根据参数名获取参数值
     */
    public Object getArgumentValue(String paramName) {
        if (paramName.startsWith("arg")) {
            try {
                int index = Integer.parseInt(paramName.substring(3));
                if (index >= 0 && index < args.length) {
                    return args[index];
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        return null;
    }

    /**
     * 获取方法名
     */
    public String getMethodName() {
        return method.getName();
    }

    /**
     * 获取目标类名
     */
    public String getTargetClassName() {
        return target.getClass().getSimpleName();
    }
}