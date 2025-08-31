package com.david.log.commons.core.enums;

import lombok.Getter;

/**
 * 日志级别枚举
 * 
 * <p>
 * 定义了系统支持的所有日志级别，包括标准级别和业务级别
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
@Getter
public enum LogLevel {

    /**
     * 调试级别 - 用于开发调试信息
     */
    DEBUG("DEBUG", 0, "\u001B[36m"), // 青色

    /**
     * 信息级别 - 用于一般信息输出
     */
    INFO("INFO", 1, "\u001B[32m"), // 绿色

    /**
     * 警告级别 - 用于警告信息
     */
    WARN("WARN", 2, "\u001B[33m"), // 黄色

    /**
     * 错误级别 - 用于错误信息
     */
    ERROR("ERROR", 3, "\u001B[31m"), // 红色

    /**
     * 业务级别 - 用于业务日志记录
     */
    BUSINESS("BUSINESS", 4, "\u001B[35m"); // 紫色

    /**
     * ANSI颜色重置代码
     */
    public static final String RESET = "\u001B[0m";

    /**
     * 级别名称
     * -- GETTER --
     * 获取级别名称
     *
     * 
     */
    private final String name;

    /**
     * 级别优先级（数值越大优先级越高）
     * -- GETTER --
     * 获取优先级
     *
     * 
     */
    private final int priority;

    /**
     * ANSI颜色代码
     * -- GETTER --
     * 获取ANSI颜色代码
     *
     */
    private final String colorCode;

    /**
     * 构造函数
     * 
     * @param name      级别名称
     * @param priority  优先级
     * @param colorCode ANSI颜色代码
     */
    LogLevel(String name, int priority, String colorCode) {
        this.name = name;
        this.priority = priority;
        this.colorCode = colorCode;
    }

    /**
     * 判断当前级别是否启用（基于优先级比较）
     *
     * @param threshold 阈值级别
     * @return 如果当前级别优先级大于等于阈值级别则返回true
     */
    public boolean isEnabled(LogLevel threshold) {
        return this.priority >= threshold.priority;
    }

    @Override
    public String toString() {
        return name;
    }
}
