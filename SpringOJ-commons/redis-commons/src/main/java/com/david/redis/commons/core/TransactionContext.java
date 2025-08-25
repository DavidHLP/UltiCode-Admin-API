package com.david.redis.commons.core;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redis事务上下文
 *
 * <p>
 * 保存事务执行过程中的状态信息，包括事务ID、操作记录、
 * 时间戳等，用于事务管理和监控。
 * </p>
 *
 * @author David
 */
@Getter
@Setter
public class TransactionContext {

    /**
     * 事务唯一标识
     */
    private final String transactionId;

    /**
     * 是否为新事务（相对于嵌套或参与现有事务）
     */
    private final boolean newTransaction;

    /**
     * 是否为只读事务
     */
    private final boolean readOnly;

    /**
     * 事务超时时间（毫秒）
     */
    private final long timeout;

    /**
     * 事务标签
     */
    private final String label;

    /**
     * 父事务ID（用于嵌套事务）
     */
    private final String parentTransactionId;

    /**
     * 事务创建时间
     */
    private final LocalDateTime createdAt;

    /**
     * 事务中执行的操作列表
     */
    private final List<String> operations;

    /**
     * 事务是否已开始（MULTI命令是否已执行）
     */
    private boolean transactionStarted;

    /**
     * 事务是否已提交
     */
    private boolean committed;

    /**
     * 事务是否已回滚
     */
    private boolean rolledBack;

    /**
     * 是否为嵌套事务
     */
    private boolean nested;

    /**
     * 事务开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 事务结束时间
     */
    private LocalDateTime completedAt;

    /**
     * 构造函数 - 创建新事务上下文
     *
     * @param transactionId  事务ID
     * @param newTransaction 是否为新事务
     * @param readOnly       是否只读
     * @param timeout        超时时间
     * @param label          事务标签
     */
    public TransactionContext(String transactionId, boolean newTransaction, boolean readOnly,
            long timeout, String label) {
        this(transactionId, newTransaction, readOnly, timeout, label, null);
    }

    /**
     * 构造函数 - 创建事务上下文（支持父事务）
     *
     * @param transactionId       事务ID
     * @param newTransaction      是否为新事务
     * @param readOnly            是否只读
     * @param timeout             超时时间
     * @param label               事务标签
     * @param parentTransactionId 父事务ID
     */
    public TransactionContext(String transactionId, boolean newTransaction, boolean readOnly,
            long timeout, String label, String parentTransactionId) {
        this.transactionId = transactionId;
        this.newTransaction = newTransaction;
        this.readOnly = readOnly;
        this.timeout = timeout;
        this.label = label;
        this.parentTransactionId = parentTransactionId;
        this.createdAt = LocalDateTime.now();
        this.operations = new ArrayList<>();
        this.transactionStarted = false;
        this.committed = false;
        this.rolledBack = false;
        this.nested = false;
    }

    /**
     * 添加操作记录
     *
     * @param operation 操作描述
     */
    public void addOperation(String operation) {
        if (operation != null && !operation.trim().isEmpty()) {
            this.operations.add(operation);
        }
    }

    /**
     * 获取操作列表的不可变副本
     *
     * @return 操作列表
     */
    public List<String> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    /**
     * 标记事务开始
     */
    public void markTransactionStarted() {
        this.transactionStarted = true;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 标记事务完成
     */
    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 检查事务是否超时
     *
     * @return 如果事务超时返回true
     */
    public boolean isTimeout() {
        if (timeout <= 0 || startedAt == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long elapsedMillis = java.time.Duration.between(startedAt, now).toMillis();
        return elapsedMillis > timeout;
    }

    /**
     * 获取事务执行时长（毫秒）
     *
     * @return 执行时长，如果事务未开始则返回0
     */
    public long getExecutionTimeMillis() {
        if (startedAt == null) {
            return 0;
        }

        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).toMillis();
    }

    /**
     * 检查事务是否已完成（提交或回滚）
     *
     * @return 如果事务已完成返回true
     */
    public boolean isCompleted() {
        return committed || rolledBack;
    }

    /**
     * 检查事务是否活跃
     *
     * @return 如果事务活跃返回true
     */
    public boolean isActive() {
        return transactionStarted && !isCompleted();
    }

    /**
     * 获取事务状态描述
     *
     * @return 状态描述
     */
    public String getStatus() {
        if (rolledBack) {
            return "ROLLED_BACK";
        } else if (committed) {
            return "COMMITTED";
        } else if (transactionStarted) {
            return "ACTIVE";
        } else {
            return "CREATED";
        }
    }

    /**
     * 检查是否为根事务（没有父事务）
     *
     * @return 如果是根事务返回true
     */
    public boolean isRootTransaction() {
        return parentTransactionId == null;
    }

    /**
     * 获取事务层级（根事务为0，嵌套事务递增）
     *
     * @return 事务层级
     */
    public int getTransactionLevel() {
        // 这里简化处理，实际应该通过事务栈计算
        return parentTransactionId == null ? 0 : 1;
    }

    /**
     * 创建事务摘要信息
     *
     * @return 事务摘要
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction[")
                .append("id=").append(transactionId)
                .append(", status=").append(getStatus())
                .append(", operations=").append(operations.size())
                .append(", duration=").append(getExecutionTimeMillis()).append("ms");

        if (readOnly) {
            sb.append(", readOnly");
        }

        if (nested) {
            sb.append(", nested");
        }

        if (parentTransactionId != null) {
            sb.append(", parent=").append(parentTransactionId);
        }

        if (label != null && !label.trim().isEmpty()) {
            sb.append(", label=").append(label);
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TransactionContext that = (TransactionContext) obj;
        return transactionId.equals(that.transactionId);
    }

    @Override
    public int hashCode() {
        return transactionId.hashCode();
    }
}