package com.david.redis.commons.aspect.chain;

import com.david.log.commons.LogUtils;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 抽象切面处理器基类
 *
 * <p>
 * 提供通用的处理器功能实现。
 *
 * @author David
 */
@RequiredArgsConstructor
public abstract class AbstractAspectHandler implements AspectHandler {

    protected final LogUtils logUtils;

    @Override
    public boolean canHandle(AspectContext context) {
        // 默认实现：检查切面类型是否匹配
        return getSupportedAspectTypes().contains(context.getAspectType());
    }

    @Override
    public int getOrder() {
        return 100; // 默认顺序
    }

    @Override
    public boolean isAsync() {
        return false; // 默认同步处理
    }

    @Override
    public boolean supports(AspectType aspectType) {
        return getSupportedAspectTypes().contains(aspectType);
    }

    /**
     * 获取支持的切面类型
     *
     * @return 支持的切面类型集合
     */
    protected abstract Set<AspectType> getSupportedAspectTypes();

    /**
     * 记录处理器执行日志（简洁版本）
     *
     * @param context 切面上下文
     * @param action  执行动作
     * @param message 简洁消息
     */
    protected void logExecution(AspectContext context, String action, String message) {
        logUtils.business().trace("cache_handler", action, message);
    }

    /**
     * 记录处理器异常（简洁版本）
     *
     * @param context   切面上下文
     * @param action    执行动作
     * @param throwable 异常
     * @param keyInfo   关键信息（如缓存key）
     */
    protected void logException(AspectContext context, String action, Throwable throwable, String keyInfo) {
        logUtils.exception().business(
                action + "_failed",
                throwable,
                "缓存操作失败",
                keyInfo);
    }
}
