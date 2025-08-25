package com.david.redis.commons.core.cache;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 缓存键生成器，负责解析SpEL表达式生成缓存键
 *
 * @author David
 */
@Component
public class CacheKeyGenerator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 根据SpEL表达式生成缓存键
     *
     * @param keyExpression SpEL表达式
     * @param method        目标方法
     * @param args          方法参数
     * @return 生成的缓存键
     */
    public String generateKey(String keyExpression, Method method, Object[] args) {
        if (!StringUtils.hasText(keyExpression)) {
            return generateDefaultKey(method, args);
        }

        return resolveSpELExpression(keyExpression, method, args);
    }

    /**
     * 解析SpEL表达式
     *
     * @param expression SpEL表达式
     * @param method     目标方法
     * @param args       方法参数
     * @return 解析结果
     */
    public String resolveSpELExpression(String expression, Method method, Object[] args) {
        try {
            Expression exp = parser.parseExpression(expression);
            EvaluationContext context = createEvaluationContext(method, args);

            Object result = exp.getValue(context);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            throw new IllegalArgumentException("SpEL表达式解析失败: " + expression, e);
        }
    }

    /**
     * 创建SpEL表达式求值上下文
     *
     * @param method 目标方法
     * @param args   方法参数
     * @return 求值上下文
     */
    private EvaluationContext createEvaluationContext(Method method, Object[] args) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 添加方法参数到上下文
        Parameter[] parameters = method.getParameters();
        if (args != null && parameters.length > 0) {
            for (int i = 0; i < Math.min(parameters.length, args.length); i++) {
                context.setVariable(parameters[i].getName(), args[i]);
                // 同时支持p0, p1, p2...的参数引用方式
                context.setVariable("p" + i, args[i]);
            }
        }

        // 添加方法信息到上下文
        context.setVariable("methodName", method.getName());
        context.setVariable("className", method.getDeclaringClass().getSimpleName());
        context.setVariable("fullClassName", method.getDeclaringClass().getName());

        // 添加常用的工具方法
        context.setVariable("T", new SpELHelper());

        return context;
    }

    /**
     * 生成默认缓存键
     * 格式：className:methodName:参数哈希值
     *
     * @param method 目标方法
     * @param args   方法参数
     * @return 默认缓存键
     */
    private String generateDefaultKey(Method method, Object[] args) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(method.getDeclaringClass().getSimpleName())
                .append(":")
                .append(method.getName());

        if (args != null && args.length > 0) {
            keyBuilder.append(":");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    keyBuilder.append(",");
                }
                keyBuilder.append(args[i] != null ? args[i].toString() : "null");
            }
        }

        return keyBuilder.toString();
    }

    /**
     * SpEL表达式辅助类，提供常用的工具方法
     */
    public static class SpELHelper {

        /**
         * 获取对象的哈希值
         */
        public int hash(Object obj) {
            return obj != null ? obj.hashCode() : 0;
        }

        /**
         * 字符串连接
         */
        public String concat(Object... objects) {
            StringBuilder sb = new StringBuilder();
            for (Object obj : objects) {
                sb.append(obj != null ? obj.toString() : "");
            }
            return sb.toString();
        }

        /**
         * 获取当前时间戳
         */
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    }
}