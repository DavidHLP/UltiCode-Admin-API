package com.david.redis.commons.aspect.chain;

/**
 * 切面处理器接口
 *
 * <p>基于责任链模式的处理器抽象，每个处理器负责特定的处理逻辑。
 * 
 * @author David
 */
public interface AspectHandler {

    /**
     * 判断当前处理器是否能处理给定的上下文
     *
     * @param context 切面上下文
     * @return true 如果能处理，false 否则
     */
    boolean canHandle(AspectContext context);

    /**
     * 处理切面逻辑
     *
     * @param context 切面上下文
     * @param chain 处理器链
     * @return 处理结果
     * @throws Throwable 处理异常
     */
    Object handle(AspectContext context, AspectChain chain) throws Throwable;

    /**
     * 获取处理器执行顺序
     *
     * @return 执行顺序，数字越小越早执行
     */
    int getOrder();

    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 处理器是否为异步执行
     *
     * @return true 如果是异步处理器
     */
    default boolean isAsync() {
        return false;
    }

    /**
     * 判断处理器是否支持指定的切面类型
     *
     * @param aspectType 切面类型
     * @return true 如果支持该切面类型
     */
    boolean supports(AspectType aspectType);
}
