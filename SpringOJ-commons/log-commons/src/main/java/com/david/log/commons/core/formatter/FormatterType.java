package com.david.log.commons.core.formatter;

import lombok.Getter;

/**
 * 格式化器类型枚举
 * 
 * <p>
 * 定义日志格式化器的类型，用于区分不同的格式化策略。
 * 
 * @author David
 */
@Getter
public enum FormatterType {

    /**
     * 默认格式化器 - 简单文本格式
     */
    DEFAULT("DEFAULT", "默认格式化器"),

    /**
     * JSON格式化器 - JSON结构化输出
     */
    JSON("JSON", "JSON格式化器"),

    /**
     * 模板格式化器 - 自定义模板格式
     */
    TEMPLATE("TEMPLATE", "模板格式化器");

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

    FormatterType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code;
    }
}
