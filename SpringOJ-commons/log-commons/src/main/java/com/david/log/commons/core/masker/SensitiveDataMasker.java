package com.david.log.commons.core.masker;

/**
 * 敏感数据脱敏器接口
 * 
 * <p>
 * 定义敏感信息脱敏的统一接口，支持自定义脱敏规则和策略。
 * 用于在日志输出前对敏感信息进行处理，保护用户隐私和系统安全。
 * 
 * @author David
 */
public interface SensitiveDataMasker {

    /**
     * 对数据进行脱敏处理
     * 
     * @param data 原始数据
     * @return 脱敏后的数据
     */
    String mask(Object data);

    /**
     * 判断数据是否包含敏感信息
     * 
     * @param data 待检查的数据
     * @return 是否为敏感数据
     */
    boolean isSensitive(Object data);

    /**
     * 添加自定义敏感词规则
     * 
     * @param keyword 敏感词
     */
    void addSensitiveKeyword(String keyword);

    /**
     * 添加自定义敏感数据正则表达式
     * 
     * @param pattern 正则表达式模式
     */
    void addSensitivePattern(String pattern);
}
