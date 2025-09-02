package com.david.commons.redis.cache.aspect.chain;

import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存切面执行上下文
 *
 * <p>封装责任链处理过程中的所有必要信息，包括缓存上下文、元数据、执行结果等
 *
 * @author David
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AspectContext {
    /** 缓存操作上下文 */
    private CacheContext context;

    /** 当前处理的缓存元数据 */
    private CacheMetadata metadata;

    /** AOP连接点 */
    private ProceedingJoinPoint joinPoint;

    /** 缓存命中的值（如果有） */
    private Object cacheValue;

    /** 方法执行结果 */
    private Object methodResult;

    /** 是否缓存命中 */
    @Builder.Default private Boolean cacheHit = false;

    /** 是否已执行原方法 */
    @Builder.Default private Boolean methodInvoked = false;

    /** 是否结束责任链处理 */
    @Builder.Default private Boolean isEnd = false;

    public Boolean isEnd() {
        return isEnd != null && isEnd;
    }

    /** 标记缓存命中并设置缓存值 */
    public void setCacheHit(Object value) {
        this.cacheValue = value;
        this.cacheHit = true;
        this.isEnd = true; // 缓存命中时结束处理链
    }

    /** 标记方法已执行并设置结果 */
    public void setMethodExecuted(Object result) {
        this.methodResult = result;
        this.methodInvoked = true;
        // 设置到缓存上下文中，便于后续缓存写入时使用
        if (this.context != null) {
            this.context.setResult(result);
        }
    }

    /** 获取最终返回值（缓存值或方法执行结果） */
    public Object getFinalResult() {
        return cacheHit ? cacheValue : methodResult;
    }
}
