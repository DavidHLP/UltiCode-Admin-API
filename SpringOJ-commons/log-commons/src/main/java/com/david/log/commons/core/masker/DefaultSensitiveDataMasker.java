package com.david.log.commons.core.masker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 默认敏感数据脱敏器实现
 * 
 * <p>
 * 提供常见敏感信息的自动检测和脱敏处理，支持自定义规则扩展。
 * 内置对密码、手机号、邮箱、身份证等敏感信息的处理能力。
 * 
 * @author David
 */
@Slf4j
@Component
public class DefaultSensitiveDataMasker implements SensitiveDataMasker {

    private static final String MASK_PATTERN = "***";
    private static final int MAX_DATA_LENGTH = 1000;

    // 敏感关键词集合 - 线程安全
    private final Set<String> sensitiveKeywords = ConcurrentHashMap.newKeySet();

    // 敏感数据正则表达式模式 - 线程安全
    private final Set<Pattern> sensitivePatterns = ConcurrentHashMap.newKeySet();

    public DefaultSensitiveDataMasker() {
        initDefaultKeywords();
        initDefaultPatterns();
    }

    /**
     * 初始化默认敏感关键词
     */
    private void initDefaultKeywords() {
        // 密码相关
        sensitiveKeywords.add("password");
        sensitiveKeywords.add("pwd");
        sensitiveKeywords.add("passwd");

        // 令牌相关
        sensitiveKeywords.add("token");
        sensitiveKeywords.add("accesstoken");
        sensitiveKeywords.add("refreshtoken");

        // 密钥相关
        sensitiveKeywords.add("secret");
        sensitiveKeywords.add("secretkey");
        sensitiveKeywords.add("privatekey");
        sensitiveKeywords.add("apikey");

        // 其他敏感信息
        sensitiveKeywords.add("credit");
        sensitiveKeywords.add("card");
        sensitiveKeywords.add("bank");
    }

    /**
     * 初始化默认正则表达式模式
     */
    private void initDefaultPatterns() {
        // 手机号码 (11位数字，1开头)
        sensitivePatterns.add(Pattern.compile("1[3-9]\\d{9}"));

        // 邮箱地址
        sensitivePatterns.add(Pattern.compile("[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}"));

        // 身份证号码 (18位)
        sensitivePatterns.add(Pattern.compile("\\d{17}[\\dXx]"));

        // 银行卡号 (13-19位数字)
        sensitivePatterns.add(Pattern.compile("\\d{13,19}"));

        // IP地址
        sensitivePatterns.add(Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"));

        // 可能的密钥 (32位以上的字母数字组合)
        sensitivePatterns.add(Pattern.compile("[a-zA-Z0-9]{32,}"));
    }

    @Override
    public String mask(Object data) {
        if (data == null) {
            return "null";
        }

        String dataStr = data.toString();

        // 长度保护
        if (dataStr.length() > MAX_DATA_LENGTH) {
            return dataStr.substring(0, 50) + "..." + MASK_PATTERN + " (truncated)";
        }

        // 如果不是敏感数据，直接返回
        if (!isSensitive(data)) {
            return dataStr;
        }

        // 执行脱敏处理
        return doMask(dataStr);
    }

    @Override
    public boolean isSensitive(Object data) {
        if (data == null) {
            return false;
        }

        String dataStr = data.toString().toLowerCase();

        // 检查关键词匹配
        if (containsSensitiveKeyword(dataStr)) {
            return true;
        }

        // 检查正则表达式匹配
        return matchesSensitivePattern(dataStr);
    }

    @Override
    public void addSensitiveKeyword(String keyword) {
        if (StringUtils.hasText(keyword)) {
            sensitiveKeywords.add(keyword.toLowerCase());
            log.debug("添加敏感关键词: {}", keyword);
        }
    }

    @Override
    public void addSensitivePattern(String pattern) {
        if (StringUtils.hasText(pattern)) {
            try {
                sensitivePatterns.add(Pattern.compile(pattern));
                log.debug("添加敏感数据模式: {}", pattern);
            } catch (Exception e) {
                log.warn("添加敏感数据模式失败: {}, 错误: {}", pattern, e.getMessage());
            }
        }
    }

    /**
     * 检查是否包含敏感关键词
     */
    private boolean containsSensitiveKeyword(String dataStr) {
        return sensitiveKeywords.stream()
                .anyMatch(dataStr::contains);
    }

    /**
     * 检查是否匹配敏感数据模式
     */
    private boolean matchesSensitivePattern(String dataStr) {
        return sensitivePatterns.stream()
                .anyMatch(pattern -> pattern.matcher(dataStr).find());
    }

    /**
     * 执行脱敏处理
     */
    private String doMask(String dataStr) {
        // 完全脱敏策略
        if (dataStr.length() <= 3) {
            return MASK_PATTERN;
        }

        // 部分脱敏策略 - 保留前后字符
        if (dataStr.length() <= 10) {
            return dataStr.charAt(0) + MASK_PATTERN + dataStr.charAt(dataStr.length() - 1);
        }

        // 长字符串脱敏策略 - 保留前2后2
        return dataStr.substring(0, 2) + MASK_PATTERN +
                dataStr.substring(dataStr.length() - 2);
    }
}
