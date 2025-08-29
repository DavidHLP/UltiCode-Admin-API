package com.david.redis.commons.core.cache;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * 缓存操作上下文，用于在AOP处理过程中传递缓存相关信息
 *
 * @author David
 */
@Getter
public class CacheOperationContext {

    /** -- GETTER -- 获取目标方法 */
    private final Method method;

    /** -- GETTER -- 获取方法参数 */
    private final Object[] args;

    /** -- GETTER -- 获取目标对象 */
    private final Object target;

    /** -- GETTER -- 获取生成的缓存键 */
    private final String[] generatedKeys;

    /** -- GETTER -- 获取方法执行结果 -- SETTER -- 设置方法执行结果 */
    @Setter private Object result;

    public CacheOperationContext(Method method, Object[] args, Object target) {
        this.method = method;
        this.args = args;
        this.target = target;
        this.generatedKeys = new String[0];
    }

    public CacheOperationContext(
            Method method, Object[] args, Object target, String[] generatedKeys) {
        this.method = method;
        this.args = args;
        this.target = target;
        this.generatedKeys = generatedKeys != null ? generatedKeys : new String[0];
    }

    /** 获取方法签名字符串 */
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

    /** 获取参数值的字符串表示 */
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
