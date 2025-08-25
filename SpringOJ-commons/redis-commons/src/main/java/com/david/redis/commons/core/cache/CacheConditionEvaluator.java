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
 * 缓存条件评估器，用于评估SpEL条件表达式
 *
 * @author David
 */
@Component
public class CacheConditionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 评估缓存条件
     *
     * @param condition SpEL条件表达式
     * @param method    目标方法
     * @param args      方法参数
     * @param result    方法执行结果（可能为null）
     * @return 条件评估结果，如果条件为空则返回true
     */
    public boolean evaluateCondition(String condition, Method method, Object[] args, Object result) {
        if (!StringUtils.hasText(condition)) {
            return true;
        }

        try {
            Expression exp = parser.parseExpression(condition);
            EvaluationContext context = createEvaluationContext(method, args, result);

            Object value = exp.getValue(context);
            return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
        } catch (Exception e) {
            // 条件评估失败时，默认返回false，避免缓存不当的数据
            throw new IllegalArgumentException("缓存条件评估失败: " + condition, e);
        }
    }

    /**
     * 创建SpEL表达式求值上下文
     *
     * @param method 目标方法
     * @param args   方法参数
     * @param result 方法执行结果
     * @return 求值上下文
     */
    private EvaluationContext createEvaluationContext(Method method, Object[] args, Object result) {
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

        // 添加方法执行结果到上下文
        context.setVariable("result", result);

        // 添加方法信息到上下文
        context.setVariable("methodName", method.getName());
        context.setVariable("className", method.getDeclaringClass().getSimpleName());
        context.setVariable("fullClassName", method.getDeclaringClass().getName());

        // 添加常用的工具方法
        context.setVariable("T", new CacheKeyGenerator.SpELHelper());

        return context;
    }
}