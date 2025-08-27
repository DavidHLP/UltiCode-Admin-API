package com.david.redis.commons.core.operations.interfaces;

import com.david.redis.commons.core.lock.RedisCallback;
import com.david.redis.commons.core.transaction.TransactionContext;

import java.util.function.Consumer;

/**
 * Redis事务操作接口
 * 
 * <p>定义所有事务相关的Redis操作方法
 * 
 * @author David
 */
public interface RedisTransactionOperations {

    /**
     * 在Redis事务中执行操作
     *
     * <p>使用Redis的MULTI/EXEC命令来确保操作的原子性。 如果回调中的任何操作失败，整个事务将被回滚。
     *
     * @param callback 事务回调
     * @param <T> 返回值类型
     * @return 事务执行结果
     */
    <T> T executeInTransaction(RedisCallback<T> callback);

    /**
     * 在Redis事务中执行操作（无返回值版本）
     *
     * <p>便捷方法：接受一个消费型回调，无需返回值。
     *
     * @param action 事务中要执行的操作
     */
    void executeInTransaction(Consumer<Object> action);

    /**
     * 监视一个或多个键，用于乐观锁
     *
     * <p>在事务开始前调用此方法来监视键的变化。 如果在事务执行期间被监视的键发生了变化，事务将被丢弃。
     *
     * @param keys 要监视的键
     */
    void watch(String... keys);

    /**
     * 取消对所有键的监视
     */
    void unwatch();

    /**
     * 检查是否在事务中
     *
     * @return 如果当前在事务中返回true
     */
    boolean isInTransaction();

    /**
     * 获取当前事务上下文
     *
     * @return 当前事务上下文，如果不在事务中则返回null
     */
    TransactionContext getCurrentTransactionContext();
}
