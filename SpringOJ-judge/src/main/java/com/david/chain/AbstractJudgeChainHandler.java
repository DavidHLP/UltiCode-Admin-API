package com.david.chain;

import com.david.dto.JudgeContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象判题责任链处理器 - 提供责任链的基础实现
 * 
 * @author David
 */
@Slf4j
public abstract class AbstractJudgeChainHandler implements JudgeChainHandler {
    
    private JudgeChainHandler nextHandler;
    
    @Override
    public final void handle(JudgeContext context) {
        log.debug("开始执行处理器: {}", getHandlerName());
        
        try {
            // 执行当前处理器的具体逻辑
            doHandle(context);
            
            // 如果处理成功且需要继续，则调用下一个处理器
            if (context.shouldContinueProcessing() && nextHandler != null) {
                nextHandler.handle(context);
            }
            
        } catch (Exception e) {
            log.error("处理器 {} 执行失败", getHandlerName(), e);
            context.markFailed(e.getMessage());
            throw e;
        }
        
        log.debug("处理器 {} 执行完成", getHandlerName());
    }
    
    @Override
    public JudgeChainHandler setNext(JudgeChainHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }
    
    /**
     * 子类实现的具体处理逻辑
     * 
     * @param context 判题上下文
     */
    protected abstract void doHandle(JudgeContext context);
}
