package com.david.log.commons.core.processor;

import lombok.Getter;

/**
 * 处理器类型枚举
 *
 * <p>定义日志处理器的类型，用于区分不同的处理策略。
 *
 * @author David
 */
@Getter
public enum ProcessorType {

    /** 同步处理器 - 直接写入日志 */
    SYNC("SYNC", "同步处理器"),

    /** 异步处理器 - 缓冲后批量写入 */
    ASYNC("ASYNC", "异步处理器");

    /** -- GETTER -- 获取类型代码 */
    private final String code;

    /** -- GETTER -- 获取类型描述 */
    private final String description;

    ProcessorType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code;
    }
}
