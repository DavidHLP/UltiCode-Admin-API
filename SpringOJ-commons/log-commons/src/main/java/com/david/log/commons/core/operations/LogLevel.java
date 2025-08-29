package com.david.log.commons.core.operations;

/**
 * 日志级别枚举
 * 
 * <p>
 * 定义系统支持的日志级别，按照严重程度从低到高排序。
 * 
 * @author David
 */
public enum LogLevel {

    /**
     * 调试级别 - 详细的调试信息
     */
    DEBUG(0, "DEBUG"),

    /**
     * 信息级别 - 一般信息记录
     */
    INFO(1, "INFO"),

    /**
     * 警告级别 - 潜在问题提醒
     */
    WARN(2, "WARN"),

    /**
     * 错误级别 - 错误信息记录
     */
    ERROR(3, "ERROR");

    private final int level;
    private final String name;

    LogLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }

    /**
     * 获取级别数值
     * 
     * @return 级别数值
     */
    public int getLevel() {
        return level;
    }

    /**
     * 获取级别名称
     * 
     * @return 级别名称
     */
    public String getName() {
        return name;
    }

    /**
     * 判断当前级别是否启用指定级别
     * 
     * @param targetLevel 目标级别
     * @return 是否启用
     */
    public boolean isEnabled(LogLevel targetLevel) {
        return this.level <= targetLevel.level;
    }

    @Override
    public String toString() {
        return name;
    }
}
