package com.david.log.commons.core.utils;

import com.david.log.commons.core.operations.BusinessLogger;

/**
 * 业务日志构建器
 *
 * <p>支持链式调用构建业务日志，自动追加类名和方法名信息
 *
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class BusinessLogBuilder {

    /** 类名 */
    private String className;

    /** 方法名 */
    private String methodName;

    /** 消息内容 */
    private String message;

    /** 参数数组 */
    private Object[] args;

    /** 公有构造函数，支持外部实例化 */
    public BusinessLogBuilder() {
        // 默认构造函数
    }

    /**
     * 设置类名
     *
     * @param className 类名
     * @return 当前构建器实例，支持链式调用
     */
    public BusinessLogBuilder className(String className) {
        this.className = className != null ? className.trim() : "";
        return this;
    }

    /**
     * 设置方法名
     *
     * @param methodName 方法名
     * @return 当前构建器实例，支持链式调用
     */
    public BusinessLogBuilder methodName(String methodName) {
        this.methodName = methodName != null ? methodName.trim() : "";
        return this;
    }

    /**
     * 设置消息内容
     *
     * @param message 消息内容
     * @param args 参数数组
     * @return 当前构建器实例，支持链式调用
     */
    public BusinessLogBuilder message(String message, Object... args) {
        this.message = message != null ? message : "";
        this.args = args;
        return this;
    }

    /**
     * 自动从当前调用栈中获取类名和方法名
     *
     * @return 当前构建器实例，支持链式调用
     */
    public BusinessLogBuilder auto() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 跳过当前方法和LogUtils调用，找到真正的调用者
        for (int i = 3; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String fullClassName = element.getClassName();

            // 跳过日志相关的类
            if (fullClassName.startsWith("com.david.log.commons")
                    || fullClassName.startsWith("org.slf4j")
                    || fullClassName.startsWith("ch.qos.logback")) {
                continue;
            }

            // 提取简单类名
            this.className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            this.methodName = element.getMethodName();
            break;
        }

        return this;
    }

    /** 构建并输出INFO级别的业务日志 */
    public void info() {
        buildAndLog(BusinessLogger.LogMethod.INFO);
    }

    /** 构建并输出WARN级别的业务日志 */
    public void warn() {
        buildAndLog(BusinessLogger.LogMethod.WARN);
    }

    /** 构建并输出ERROR级别的业务日志 */
    public void error() {
        buildAndLog(BusinessLogger.LogMethod.ERROR);
    }

    /** 构建并输出DEBUG级别的业务日志 */
    public void debug() {
        buildAndLog(BusinessLogger.LogMethod.DEBUG);
    }

    /**
     * 构建完整的业务日志消息
     *
     * @return 格式化的业务日志消息
     */
    private String buildBusinessMessage() {
        StringBuilder sb = new StringBuilder();

        // 添加业务日志标识
        sb.append("[BUSINESS]");

        // 添加类名和方法名
        if (isNotEmpty(className) && isNotEmpty(methodName)) {
            sb.append("[").append(className).append(".").append(methodName).append("]");
        } else if (isNotEmpty(className)) {
            sb.append("[").append(className).append("]");
        } else if (isNotEmpty(methodName)) {
            sb.append("[").append(methodName).append("]");
        }

        // 添加消息内容
        if (isNotEmpty(message)) {
            sb.append(" ").append(message);
        }

        return sb.toString();
    }

    /**
     * 构建并记录业务日志
     *
     * @param logMethod 日志输出方法
     */
    private void buildAndLog(BusinessLogger.LogMethod logMethod) {
        String businessMessage = buildBusinessMessage();
        BusinessLogger.getInstance().log(businessMessage, logMethod, args);
    }

    /**
     * 检查字符串是否非空
     *
     * @param str 待检查的字符串
     * @return 如果字符串非空则返回true
     */
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 重置构建器状态
     *
     * @return 重置后的构建器实例
     */
    public BusinessLogBuilder reset() {
        this.className = null;
        this.methodName = null;
        this.message = null;
        this.args = null;
        return this;
    }

    /**
     * 获取当前构建器状态的字符串表示
     *
     * @return 构建器状态字符串
     */
    @Override
    public String toString() {
        return String.format(
                "BusinessLogBuilder{className='%s', methodName='%s', message='%s'}",
                className, methodName, message);
    }

    /**
     * 验证必要参数是否已设置
     *
     * @return 如果参数有效则返回true
     */
    public boolean isValid() {
        return isNotEmpty(message) || isNotEmpty(className) || isNotEmpty(methodName);
    }
}
