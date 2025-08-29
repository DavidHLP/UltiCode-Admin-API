package com.david.log.commons.core.context;

import lombok.Getter;

/**
 * 日志类型枚举
 * 
 * <p>
 * 定义系统支持的日志分类，用于区分不同场景的日志记录。
 * 
 * @author David
 */
@Getter
public enum LogType {

    /**
     * 业务日志 - 用户操作、业务流程记录
     */
    BUSINESS("BUSINESS", "业务日志"),

    /**
     * 性能日志 - 执行时间、资源使用监控
     */
    PERFORMANCE("PERFORMANCE", "性能日志"),

    /**
     * 安全日志 - 认证授权、安全事件记录
     */
    SECURITY("SECURITY", "安全日志"),

    /**
     * 异常日志 - 异常信息、错误处理记录
     */
    EXCEPTION("EXCEPTION", "异常日志");

    /**
     * -- GETTER --
     * 获取类型代码
     *
     * @return 类型代码
     */
    private final String code;
    /**
     * -- GETTER --
     * 获取类型描述
     *
     * @return 类型描述
     */
    private final String description;

    LogType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code;
    }
}
