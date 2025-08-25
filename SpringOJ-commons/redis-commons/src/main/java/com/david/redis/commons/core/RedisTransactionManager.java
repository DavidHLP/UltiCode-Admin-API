package com.david.redis.commons.core;

import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.exception.RedisTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Redis事务管理器
 *
 * <p>
 * 负责管理Redis事务的生命周期，包括事务的创建、提交、回滚和嵌套事务处理。
 * 支持事务传播行为和超时控制。
 * </p>
 *
 * @author David
 */
@Slf4j
@Component
public class RedisTransactionManager {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 线程本地事务上下文栈，支持嵌套事务
     */
    private final ThreadLocal<List<TransactionContext>> transactionStack = new ThreadLocal<>();

    /**
     * 活跃事务映射，用于监控和管理
     */
    private final ConcurrentMap<String, TransactionContext> activeTransactions = new ConcurrentHashMap<>();

    public RedisTransactionManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 开始事务
     *
     * @param annotation 事务注解配置
     * @return 事务上下文
     */
    public TransactionContext beginTransaction(RedisTransactional annotation) {
        String transactionId = generateTransactionId();

        log.debug("开始Redis事务: {}, 传播行为: {}, 只读: {}",
                transactionId, annotation.propagation(), annotation.readOnly());

        List<TransactionContext> stack = getOrCreateTransactionStack();
        TransactionContext parentContext = stack.isEmpty() ? null : stack.get(stack.size() - 1);

        // 根据传播行为处理事务
        TransactionContext context = handlePropagation(annotation, transactionId, parentContext);

        // 添加到事务栈
        stack.add(context);
        activeTransactions.put(transactionId, context);

        // 如果不是只读事务且需要创建新事务，则开始Redis事务
        if (!context.isReadOnly() && context.isNewTransaction()) {
            try {
                redisTemplate.multi();
                context.setTransactionStarted(true);
                log.debug("Redis MULTI命令已执行，事务ID: {}", transactionId);
            } catch (Exception e) {
                // 清理上下文
                stack.remove(context);
                activeTransactions.remove(transactionId);
                throw new RedisTransactionException("开始Redis事务失败", e, new ArrayList<>(), transactionId, false);
            }
        }

        return context;
    }

    /**
     * 提交事务
     *
     * @param context 事务上下文
     */
    public void commitTransaction(TransactionContext context) {
        String transactionId = context.getTransactionId();

        try {
            log.debug("提交Redis事务: {}", transactionId);

            // 只有新事务且已开始的事务才需要执行EXEC
            if (context.isNewTransaction() && context.isTransactionStarted() && !context.isReadOnly()) {
                List<Object> results = redisTemplate.exec();

                if (results == null) {
                    // EXEC返回null表示事务被DISCARD或WATCH的键被修改
                    throw new RedisTransactionException("Redis事务执行失败，可能由于WATCH的键被修改",
                            new ArrayList<>(), transactionId, false);
                }

                log.debug("Redis事务提交成功: {}, 执行了{}个操作", transactionId, results.size());
            }

            context.setCommitted(true);

        } catch (Exception e) {
            log.error("Redis事务提交失败: {}", transactionId, e);

            // 尝试回滚
            try {
                rollbackTransaction(context);
            } catch (Exception rollbackException) {
                log.error("事务回滚也失败了: {}", transactionId, rollbackException);
                throw RedisTransactionException.rollbackFailed(transactionId, rollbackException);
            }

            if (e instanceof RedisTransactionException) {
                throw e;
            } else {
                throw RedisTransactionException.commitFailed(transactionId, context.getOperations());
            }
        } finally {
            cleanupTransaction(context);
        }
    }

    /**
     * 回滚事务
     *
     * @param context 事务上下文
     */
    public void rollbackTransaction(TransactionContext context) {
        String transactionId = context.getTransactionId();

        try {
            log.debug("回滚Redis事务: {}", transactionId);

            // 只有新事务且已开始的事务才需要执行DISCARD
            if (context.isNewTransaction() && context.isTransactionStarted() && !context.isReadOnly()) {
                redisTemplate.discard();
                log.debug("Redis DISCARD命令已执行，事务ID: {}", transactionId);
            }

            context.setRolledBack(true);

        } catch (Exception e) {
            log.error("Redis事务回滚失败: {}", transactionId, e);
            throw RedisTransactionException.rollbackFailed(transactionId, e);
        } finally {
            cleanupTransaction(context);
        }
    }

    /**
     * 获取当前事务上下文
     *
     * @return 当前事务上下文，如果没有事务则返回null
     */
    public TransactionContext getCurrentTransaction() {
        List<TransactionContext> stack = transactionStack.get();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.get(stack.size() - 1);
    }

    /**
     * 检查是否在事务中
     *
     * @return 如果当前线程在事务中则返回true
     */
    public boolean isInTransaction() {
        return getCurrentTransaction() != null;
    }

    /**
     * 为当前事务添加操作记录
     *
     * @param operation 操作描述
     */
    public void addOperation(String operation) {
        TransactionContext context = getCurrentTransaction();
        if (context != null) {
            context.addOperation(operation);
        }
    }

    /**
     * 处理事务传播行为
     */
    private TransactionContext handlePropagation(RedisTransactional annotation, String transactionId,
            TransactionContext parentContext) {
        RedisTransactional.Propagation propagation = annotation.propagation();

        switch (propagation) {
            case REQUIRED:
                if (parentContext != null) {
                    // 参与现有事务
                    return createParticipatingContext(transactionId, parentContext, annotation);
                } else {
                    // 创建新事务
                    return createNewTransactionContext(transactionId, annotation);
                }

            case SUPPORTS:
                if (parentContext != null) {
                    return createParticipatingContext(transactionId, parentContext, annotation);
                } else {
                    return createNonTransactionalContext(transactionId, annotation);
                }

            case MANDATORY:
                if (parentContext == null) {
                    throw new RedisTransactionException("MANDATORY传播行为要求存在活跃事务，但当前没有事务");
                }
                return createParticipatingContext(transactionId, parentContext, annotation);

            case REQUIRES_NEW:
                // 总是创建新事务
                return createNewTransactionContext(transactionId, annotation);

            case NOT_SUPPORTED:
                // 不支持事务，挂起当前事务
                return createNonTransactionalContext(transactionId, annotation);

            case NEVER:
                if (parentContext != null) {
                    throw new RedisTransactionException("NEVER传播行为不允许在事务中执行");
                }
                return createNonTransactionalContext(transactionId, annotation);

            case NESTED:
                if (parentContext != null) {
                    return createNestedTransactionContext(transactionId, parentContext, annotation);
                } else {
                    return createNewTransactionContext(transactionId, annotation);
                }

            default:
                throw new RedisTransactionException("不支持的事务传播行为: " + propagation);
        }
    }

    /**
     * 创建新事务上下文
     */
    private TransactionContext createNewTransactionContext(String transactionId, RedisTransactional annotation) {
        return new TransactionContext(transactionId, true, annotation.readOnly(),
                annotation.timeout(), annotation.label());
    }

    /**
     * 创建参与现有事务的上下文
     */
    private TransactionContext createParticipatingContext(String transactionId, TransactionContext parentContext,
            RedisTransactional annotation) {
        return new TransactionContext(transactionId, false, annotation.readOnly(),
                annotation.timeout(), annotation.label(), parentContext.getTransactionId());
    }

    /**
     * 创建非事务上下文
     */
    private TransactionContext createNonTransactionalContext(String transactionId, RedisTransactional annotation) {
        return new TransactionContext(transactionId, false, true, 0, annotation.label());
    }

    /**
     * 创建嵌套事务上下文
     */
    private TransactionContext createNestedTransactionContext(String transactionId, TransactionContext parentContext,
            RedisTransactional annotation) {
        // Redis不直接支持嵌套事务，这里创建一个新的事务上下文
        // 但标记为嵌套，以便在异常处理时区别对待
        TransactionContext context = new TransactionContext(transactionId, true, annotation.readOnly(),
                annotation.timeout(), annotation.label(), parentContext.getTransactionId());
        context.setNested(true);
        return context;
    }

    /**
     * 获取或创建事务栈
     */
    private List<TransactionContext> getOrCreateTransactionStack() {
        List<TransactionContext> stack = transactionStack.get();
        if (stack == null) {
            stack = new ArrayList<>();
            transactionStack.set(stack);
        }
        return stack;
    }

    /**
     * 清理事务上下文
     */
    private void cleanupTransaction(TransactionContext context) {
        String transactionId = context.getTransactionId();

        // 从活跃事务中移除
        activeTransactions.remove(transactionId);

        // 从事务栈中移除
        List<TransactionContext> stack = transactionStack.get();
        if (stack != null) {
            stack.remove(context);
            if (stack.isEmpty()) {
                transactionStack.remove();
            }
        }

        log.debug("事务上下文已清理: {}", transactionId);
    }

    /**
     * 生成事务ID
     */
    private String generateTransactionId() {
        return "redis-tx-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 获取活跃事务数量（用于监控）
     */
    public int getActiveTransactionCount() {
        return activeTransactions.size();
    }

    /**
     * 获取所有活跃事务ID（用于监控）
     */
    public List<String> getActiveTransactionIds() {
        return new ArrayList<>(activeTransactions.keySet());
    }
}