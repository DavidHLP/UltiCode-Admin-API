package com.david.chain;

import com.david.chain.utils.JudgmentContext;

import lombok.Setter;

/**
 * 责任链抽象处理器基类。
 * <p>
 * 每个具体处理器实现 {@link #handleRequest(JudgmentContext)} 完成一个环节，并可将处理
 * 权交给 {@code nextHandler} 继续执行。
 */
@Setter
public abstract class Handler {
    protected Handler nextHandler;

    /**
     * 处理评测请求。
     *
     * @param judgmentContext 评测上下文
     * @return 当前环节是否成功；当返回 true 且存在下一处理器时，通常会继续调用下一处理器
     */
    public abstract Boolean handleRequest(JudgmentContext judgmentContext);
}
