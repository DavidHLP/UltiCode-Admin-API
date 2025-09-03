package com.david.commons.redis.cache.expression;

import com.david.commons.redis.cache.CacheContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存 SpEL 表达式求值器
 *
 * <p>负责解析和求值缓存注解中的 SpEL 表达式，包括缓存键、条件表达式等。 支持引用方法参数、返回值、目标对象等上下文信息。
 *
 * @author David
 */
@Component
@Slf4j
public class CacheExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /** 表达式缓存，避免重复解析相同的表达式 */
    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /**
     * 求值缓存键表达式
     *
     * @param keyExpression 键表达式
     * @param context 缓存上下文
     * @param result 方法返回值（可能为 null）
     * @return 求值结果
     */
    public String evaluateKey(String keyExpression, CacheContext context, Object result) {
        if (keyExpression == null || keyExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("缓存键表达式不能为空");
        }

        // 检查是否为SpEL表达式，如果不是则直接返回字面量字符串
        if (!isSpelExpression(keyExpression)) {
            log.debug("处理字面量缓存键: {}", keyExpression);
            return keyExpression;
        }

        try {
            log.debug("处理SpEL缓存键表达式: {}", keyExpression);
            Expression expression = getExpression(keyExpression);
            EvaluationContext evalContext = createEvaluationContext(context, result);

            Object value = expression.getValue(evalContext);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            log.error("SpEL表达式解析失败: {}", keyExpression, e);
            throw new CacheExpressionException("缓存键表达式解析失败: " + keyExpression, e);
        }
    }

    /**
     * 求值条件表达式
     *
     * @param conditionExpression 条件表达式
     * @param context 缓存上下文
     * @param result 方法返回值（可能为 null）
     * @return 条件求值结果
     */
    public boolean evaluateCondition(
            String conditionExpression, CacheContext context, Object result) {
        if (conditionExpression == null || conditionExpression.trim().isEmpty()) {
            return true; // 空条件表达式默认为 true
        }

        try {
            Expression expression = getExpression(conditionExpression);
            EvaluationContext evalContext = createEvaluationContext(context, result);

            Boolean value = expression.getValue(evalContext, Boolean.class);
            return value != null ? value : false;
        } catch (Exception e) {
            log.error("条件表达式求值失败: {}", conditionExpression, e);
            // 条件表达式求值失败时，为了安全起见返回 false
            return false;
        }
    }

    /**
     * 求值缓存值表达式
     *
     * @param valueExpression 值表达式
     * @param context 缓存上下文
     * @param result 方法返回值
     * @return 求值结果
     */
    public Object evaluateValue(String valueExpression, CacheContext context, Object result) {
        if (valueExpression == null || valueExpression.trim().isEmpty()) {
            return result; // 空值表达式默认返回方法返回值
        }

        try {
            Expression expression = getExpression(valueExpression);
            EvaluationContext evalContext = createEvaluationContext(context, result);

            return expression.getValue(evalContext);
        } catch (Exception e) {
            log.error("缓存值表达式求值失败: {}", valueExpression, e);
            throw new CacheExpressionException("缓存值表达式求值失败: " + valueExpression, e);
        }
    }

    /**
     * 判断是否为SpEL表达式 SpEL表达式的特征： 1. 包含#开头的变量引用 (#p0, #a0, #result, #methodName等) 2. 包含{}模板表达式 3.
     * 包含方法调用() 4. 包含属性访问(.) 5. 包含数组/集合访问[] 6. 包含运算符(+, -, *, /, %, &&, ||, !, ==, !=, >, <, >=,
     * <=等)
     */
    private boolean isSpelExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        // 检查SpEL特征标记
        return expression.contains("#")
                || // 变量引用
                expression.contains("{")
                || // 模板表达式
                expression.contains("}")
                || expression.contains("(")
                || // 方法调用
                expression.contains(")")
                || expression.contains("[")
                || // 数组/集合访问
                expression.contains("]")
                || expression.matches(".*[.][a-zA-Z_$][a-zA-Z0-9_$]*.*")
                || // 属性访问
                expression.contains(" + ")
                || // 运算符（用空格分隔避免误判URL等）
                expression.contains(" - ")
                || expression.contains(" * ")
                || expression.contains(" / ")
                || expression.contains(" % ")
                || expression.contains(" && ")
                || expression.contains(" || ")
                || expression.contains(" == ")
                || expression.contains(" != ")
                || expression.contains(" >= ")
                || expression.contains(" <= ")
                || expression.contains(" > ")
                || expression.contains(" < ")
                || expression.contains("!");
    }

    /** 获取表达式对象（带缓存） */
    private Expression getExpression(String expressionString) {
        return expressionCache.computeIfAbsent(expressionString, parser::parseExpression);
    }

    /** 创建 SpEL 求值上下文 */
    private EvaluationContext createEvaluationContext(CacheContext context, Object result) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();

        // 设置根对象为目标对象
        evalContext.setRootObject(context.getTarget());

        // 注册方法参数 (#p0/#a0/真实参数名)
        Object[] args = context.getArgs();
        String[] paramNames = context.getParameterNames();

        for (int i = 0; i < args.length; i++) {
            evalContext.setVariable("a" + i, args[i]);
            evalContext.setVariable("p" + i, args[i]);
        }
        if (paramNames != null && paramNames.length == args.length) {
            for (int i = 0; i < args.length; i++) {
                evalContext.setVariable(paramNames[i], args[i]);
            }
        }

        // 注册方法返回值
        if (result != null) {
            evalContext.setVariable("result", result);
        }

        // 注册方法信息
        evalContext.setVariable("method", context.getMethod());
        evalContext.setVariable("methodName", context.getMethodName());
        evalContext.setVariable("targetClass", context.getTarget().getClass());
        evalContext.setVariable("targetClassName", context.getTargetClassName());

        // 注册常用的工具类和函数
        registerUtilityFunctions(evalContext);

        return evalContext;
    }

    /** 注册工具函数到 SpEL 上下文 */
    private void registerUtilityFunctions(StandardEvaluationContext evalContext) {
        // 注册字符串工具函数
        java.lang.reflect.Method isEmptyMethod = getMethodByName(String.class, "isEmpty");
        if (isEmptyMethod != null) {
            evalContext.registerFunction("isEmpty", isEmptyMethod);
        }

        java.lang.reflect.Method hasTextMethod =
                getMethodByName(
                        org.springframework.util.StringUtils.class, "hasText", String.class);
        if (hasTextMethod != null) {
            evalContext.registerFunction("hasText", hasTextMethod);
        }

        // 注册集合工具函数
        java.lang.reflect.Method isCollectionEmptyMethod =
                getMethodByName(
                        org.springframework.util.CollectionUtils.class,
                        "isEmpty",
                        java.util.Collection.class);
        if (isCollectionEmptyMethod != null) {
            evalContext.registerFunction("isCollectionEmpty", isCollectionEmptyMethod);
        }

        // 注册对象工具函数
        java.lang.reflect.Method isNullMethod =
                getMethodByName(java.util.Objects.class, "isNull", Object.class);
        if (isNullMethod != null) {
            evalContext.registerFunction("isNull", isNullMethod);
        }

        java.lang.reflect.Method nonNullMethod =
                getMethodByName(java.util.Objects.class, "nonNull", Object.class);
        if (nonNullMethod != null) {
            evalContext.registerFunction("nonNull", nonNullMethod);
        }
    }

    /** 通过反射获取方法 */
    private java.lang.reflect.Method getMethodByName(
            Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            log.warn("未找到方法: {}.{}", clazz.getSimpleName(), methodName);
            return null;
        }
    }

    /** 清空表达式缓存 */
    public void clearExpressionCache() {
        expressionCache.clear();
        log.debug("缓存表达式缓存已清空");
    }

    /** 获取表达式缓存大小 */
    public int getExpressionCacheSize() {
        return expressionCache.size();
    }
}
