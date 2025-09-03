package com.david.commons.redis.lock.aspect;

import com.david.commons.redis.exception.RedisLockException;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.annotation.DistributedLock;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 分布式锁 AOP 切面
 *
 * @author David
 */
@Slf4j
@Aspect
@Component
@Order(1) // 确保锁切面优先执行
public class DistributedLockAspect {

    private final DistributedLockManager lockManager;
    private final ExpressionParser parser = new SpelExpressionParser();

    public DistributedLockAspect(DistributedLockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
            throws Throwable {
        // 检查条件表达式
        if (!evaluateCondition(joinPoint, distributedLock)) {
            log.debug("锁条件不满足，不加锁直接执行方法");
            return joinPoint.proceed();
        }

        // 解析锁键
        String lockKey = parseLockKey(joinPoint, distributedLock);
        if (!StringUtils.hasText(lockKey)) {
            throw new RedisLockException("锁键不能为空");
        }

        log.debug(
                "尝试获取 {} 类型的锁，键为: {}", distributedLock.lockType(), lockKey);

        // 尝试获取锁并执行方法
        try {
            return lockManager.executeWithLock(
                    lockKey,
                    () -> {
                        try {
                            return joinPoint.proceed();
                        } catch (Throwable throwable) {
                            throw new RuntimeException("方法执行失败", throwable);
                        }
                    },
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit(),
                    distributedLock.lockType());
        } catch (RedisLockException e) {
            return handleLockFailure(joinPoint, distributedLock, e);
        } catch (RuntimeException e) {
            // 解包装方法执行异常
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
    }

    /** 评估条件表达式 */
    private boolean evaluateCondition(
            ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        String condition = distributedLock.condition();
        if (!StringUtils.hasText(condition)) {
            return true; // 没有条件表达式，默认为 true
        }

        try {
            EvaluationContext context = createEvaluationContext(joinPoint);
            Expression expression = parser.parseExpression(condition);
            Boolean result = expression.getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.warn("锁条件表达式评估失败: {}", condition, e);
            return true; // 条件评估失败时默认应用锁
        }
    }

    /** 解析锁键 */
    private String parseLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        String keyExpression = distributedLock.key();

        try {
            EvaluationContext context = createEvaluationContext(joinPoint);

            // 如果包含 #{} 表达式，使用模板解析
            if (keyExpression.contains("#{")) {
                Expression expression =
                        parser.parseExpression(
                                keyExpression,
                                new org.springframework.expression.common.TemplateParserContext());
                Object value = expression.getValue(context);
                return value != null ? value.toString() : "";
            } else {
                // 纯字符串或简单表达式
                Expression expression = parser.parseExpression(keyExpression);
                Object value = expression.getValue(context);
                return value != null ? value.toString() : keyExpression;
            }
        } catch (Exception e) {
            log.error("锁键表达式解析失败: {}", keyExpression, e);
            throw new RedisLockException("锁键解析失败: " + keyExpression, e);
        }
    }

    /** 创建 SpEL 评估上下文 */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 设置方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // 设置根对象信息
        LockExpressionRootObject rootObject =
                new LockExpressionRootObject(
                        method, args, joinPoint.getTarget(), joinPoint.getTarget().getClass());
        context.setRootObject(rootObject);

        return context;
    }

    /** 处理锁获取失败 */
    private Object handleLockFailure(
            ProceedingJoinPoint joinPoint,
            DistributedLock distributedLock,
            RedisLockException lockException)
            throws Throwable {

        DistributedLock.LockFailureStrategy strategy = distributedLock.failureStrategy();
        String lockKey = parseLockKey(joinPoint, distributedLock);

        log.warn("获取锁失败，键为: {}, 处理策略: {}", lockKey, strategy);

        return switch (strategy) {
            case RETURN_DEFAULT -> getDefaultReturnValue(joinPoint);
            case SKIP_LOCK -> {
                log.info("跳过加锁，直接执行方法，键为: {}", lockKey);
                yield joinPoint.proceed();
            }
            default -> throw lockException;
        };
    }

    /** 获取默认返回值 */
    private Object getDefaultReturnValue(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (returnType == void.class || returnType == Void.class) {
            return null;
        }

        // 基本类型的默认值
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0.0f;
        if (returnType == double.class) return 0.0d;
        if (returnType == char.class) return '\0';

        // 对象类型返回 null
        return null;
    }

    /** SpEL 表达式根对象 */
    public record LockExpressionRootObject(
            Method method, Object[] args, Object target, Class<?> targetClass) {

        public String getMethodName() {
            return method.getName();
        }
    }
}
