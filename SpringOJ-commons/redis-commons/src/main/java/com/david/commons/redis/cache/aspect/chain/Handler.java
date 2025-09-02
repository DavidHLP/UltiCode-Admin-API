package com.david.commons.redis.cache.aspect.chain;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 责任链抽象处理器基类。
 *
 * <p>每个具体处理器实现 {@link #handleRequest(AspectContext)} 完成一个环节，并可将处理权交给 {@code nextHandler} 继续执行。
 */
@Setter
@Slf4j
public abstract class Handler {
    /** 下一个处理器，构成责任链 */
    protected Handler nextHandler;

    /**
     * 判断是否存在下一个处理器
     *
     * @param context 判断上下文
     * @return 是否存在下一个处理器
     */
    protected Boolean hasNextHandler(AspectContext context) {
        return nextHandler != null && !context.isEnd();
    }

    /**
     * 处理评测请求。
     *
     * @param context 评测上下文
     */
    public abstract void handleRequest(AspectContext context);

    /**
     * 执行处理逻辑的方法
     *
     * @param context 处理上下文
     */
    protected void executeHandle(AspectContext context) {
        if (!hasNextHandler(context)) {
            return;
        }
        nextHandler.handleRequest(context);
    }

    protected void actionMethodInvoked(AspectContext context) {
        // 如果方法已经执行过，不需要重复执行
        if (context.getMethodInvoked()) {
            log.debug("方法已经执行过，跳过重复执行");
            executeHandle(context);
            return;
        }

        try {
            log.debug("开始执行原方法：{}", context.getContext().getMethodName());

            // 执行原方法
            Object result = context.getJoinPoint().proceed();

            log.debug(
                    "方法执行完成：{}，返回值类型：{}",
                    context.getContext().getMethodName(),
                    result != null ? result.getClass().getSimpleName() : "null");

            // 标记方法已执行并设置结果
            context.setMethodExecuted(result);

        } catch (Throwable e) {
            log.error("执行方法时发生异常：{}", context.getContext().getMethodName(), e);
            // 将异常重新抛出，让上层处理
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("方法执行异常", e);
        }
    }
}
