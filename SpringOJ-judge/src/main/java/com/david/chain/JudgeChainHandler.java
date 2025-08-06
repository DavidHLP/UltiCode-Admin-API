package com.david.chain;

import com.david.dto.JudgeContext;

/**
 * 判题责任链处理器接口 - 定义判题流程中每个处理步骤的标准接口
 * 
 * @author David
 */
public interface JudgeChainHandler {
    
    /**
     * 处理判题请求
     * 
     * @param context 判题上下文，包含所有必要地判题信息
     * @throws com.david.exception.JudgeException 当处理失败时
     */
    void handle(JudgeContext context);
    
    /**
     * 设置下一个处理器
     * 
     * @param nextHandler 下一个处理器
     * @return 当前处理器，支持链式调用
     */
    JudgeChainHandler setNext(JudgeChainHandler nextHandler);
    
    /**
     * 获取处理器名称，用于日志和调试
     * 
     * @return 处理器名称
     */
    default String getHandlerName() {
        return this.getClass().getSimpleName();
    }
}
