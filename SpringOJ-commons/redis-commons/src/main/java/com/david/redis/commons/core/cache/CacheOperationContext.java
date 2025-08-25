package com.david.redis.commons.core.cache;

import java.lang.reflect.Method;

/**
 * 缓存操作上下文，用于在AOP处理过程中传递缓存相关信息
 *
 * @author David
 */
public class CacheOperationContext {

    private final Method method;
    private final Object[] args;
    private final Object target;
    private final String[] generatedKeys;
    private Object result;

    public CacheOperationContext(Method method, Object[] args, Object target) {
        this.method = method;
        this.args = args;
        this.target = target;
        this.generatedKeys = new String[0];
    }

    public CacheOperationContext(Method method, Object[] args, Object target, String[] generatedKeys) {
        this.method = method;
        this.args = args;
        this.target = target;
        this.generatedKeys = generatedKeys != null ? generatedKeys : new String[0];
    }

    /**
     * 获取目标方法
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 获取方法参数
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * 获取目标对象
     */
    public Object getTarget() {
        return target;
    }

    /**
     * 获取生成的缓存键
     */
    public String[] getGeneratedKeys() {
        return generatedKeys;
    }

    /**
     * 获取方法执行结果
     */
    public Object getResult() {
        return result;
    }

    /**
     * 设置方法执行结果
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取方法签名字符串
     */
    public String getMethodSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getSimpleName())
                .append(".")
                .append(method.getName())
                .append("(");

        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(paramTypes[i].getSimpleName());
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * 获取参数值的字符串表示
     */
    public String getArgsString() {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(args[i] != null ? args[i].toString() : "null");
        }
        sb.append("]");
        return sb.toString();
    }
}