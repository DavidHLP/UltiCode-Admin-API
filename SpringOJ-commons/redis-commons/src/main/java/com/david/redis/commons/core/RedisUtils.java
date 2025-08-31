package com.david.redis.commons.core;

import com.david.redis.commons.core.operations.*;
import com.david.redis.commons.core.operations.interfaces.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis核心工具类 - 门面模式
 *
 * <p>提供统一的Redis操作接口，支持String、Hash、List、Set、ZSet等数据类型操作， 以及事务支持。 所有操作都委托给对应的专门操作类，保持向后兼容性。
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

    @Getter private final RedisTemplate<String, Object> redisTemplate;

    // 各个操作模块
    private final RedisStringOperations stringOperations;
    // 分布式锁管理器
    private final RedisLockOperations lockOperations;


    /** 获取 String 类型操作入口 */
    public RedisStringOperations strings() {
        return this.stringOperations;
    }

    /** 获取分布式锁入口 */
    public RedisLockOperations locks() {
        return this.lockOperations;
    }
}
