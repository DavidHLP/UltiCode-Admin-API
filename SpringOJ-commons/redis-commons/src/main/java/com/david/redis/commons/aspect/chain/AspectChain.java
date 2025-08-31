package com.david.redis.commons.aspect.chain;

import com.david.log.commons.LogUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 切面处理器链
 *
 * <p>
 * 管理和执行处理器链，支持同步和异步处理器。
 * 
 * @author David
 */
@RequiredArgsConstructor
public class AspectChain {

    private final List<AspectHandler> handlers;
    private final LogUtils logUtils;
    /**
     * -- GETTER --
     * 获取当前处理器索引
     *
     */
    @Getter
    private int currentIndex = 0;

    /**
     * 创建处理器链
     *
     * @param handlers 处理器列表
     * @param logUtils 日志工具
     * @return 处理器链
     */
    public static AspectChain create(List<AspectHandler> handlers, LogUtils logUtils) {
        // 按 order 排序处理器
        List<AspectHandler> sortedHandlers = new ArrayList<>(handlers);
        sortedHandlers.sort(Comparator.comparingInt(AspectHandler::getOrder));

        return new AspectChain(sortedHandlers, logUtils);
    }

    /**
     * 执行处理器链
     *
     * @param context 切面上下文
     * @return 处理结果
     * @throws Throwable 处理异常
     */
    public Object proceed(AspectContext context) throws Throwable {
        if (currentIndex >= handlers.size()) {
            // 所有处理器都已执行完毕，返回结果
            return context.getResult();
        }

        AspectHandler handler = handlers.get(currentIndex++);

        // 检查处理器是否能处理当前上下文
        if (!handler.canHandle(context)) {
            logUtils.business()
                    .trace(
                            "aspect_chain",
                            "handler_skip",
                            "condition_not_met",
                            "handler: " + handler.getName(),
                            "aspect: " + context.getAspectType());
            return proceed(context);
        }

        logUtils.business()
                .trace(
                        "aspect_chain",
                        "handler_execute",
                        "start",
                        "handler: " + handler.getName(),
                        "order: " + handler.getOrder(),
                        "aspect: " + context.getAspectType());

        long startTime = System.currentTimeMillis();
        try {
            Object result;
            if (handler.isAsync()) {
                // 异步处理器
                result = handleAsync(handler, context);
            } else {
                // 同步处理器
                result = handler.handle(context, this);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logUtils.business()
                    .trace(
                            "aspect_chain",
                            "handler_execute",
                            "success",
                            "handler: " + handler.getName(),
                            "executionTime: " + executionTime + "ms");

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            logUtils.exception()
                    .business(
                            "aspect_chain_handler_failed",
                            throwable,
                            "处理器执行失败",
                            "handler: " + handler.getName(),
                            "executionTime: " + executionTime + "ms");
            throw throwable;
        }
    }

    /**
     * 处理异步处理器
     *
     * @param handler 异步处理器
     * @param context 切面上下文
     * @return 处理结果
     * @throws Throwable 处理异常
     */
    private Object handleAsync(AspectHandler handler, AspectContext context) throws Throwable {
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                return handler.handle(context, this);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });

        try {
            return future.get();
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException &&
                    e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            }
            throw e;
        }
    }
}
