package com.david.log.commons;

import com.david.log.commons.core.enums.LogLevel;
import com.david.log.commons.core.operations.*;
import com.david.log.commons.core.utils.BusinessLogBuilder;

/**
 * 日志工具类门面
 * 
 * <p>
 * 提供统一的日志API入口，支持标准日志级别和业务日志级别
 * </p>
 * <p>
 * 使用门面模式屏蔽内部实现复杂性，提供简洁易用的API
 * </p>
 * 
 * <h3>使用示例：</h3>
 * 
 * <pre>{@code
 * // 标准日志调用（兼容@Slf4j格式）
 * LogUtils.debug("调试信息");
 * LogUtils.info("处理完成，耗时：{}ms", 100);
 * LogUtils.warn("警告信息：{}", warningMsg);
 * LogUtils.error("错误信息：{}", e.getMessage());
 * 
 * // 业务日志调用（链式调用）
 * LogUtils.business()
 *         .className("UserService")
 *         .methodName("login")
 *         .message("用户登录成功")
 *         .info();
 * 
 * // 自动获取类名和方法名
 * LogUtils.business()
 *         .auto()
 *         .message("业务操作完成")
 *         .info();
 * }</pre>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class LogUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private LogUtils() {
        throw new UnsupportedOperationException("LogUtils is a utility class and cannot be instantiated");
    }

    // ==================== 标准日志级别 API ====================

    /**
     * 记录DEBUG级别日志
     * 
     * @param message 日志消息
     */
    public static void debug(String message) {
        DebugLogger.debug(message);
    }

    /**
     * 记录DEBUG级别日志（支持参数格式化）
     * 
     * @param message 日志消息，支持{}占位符
     * @param args    参数数组
     */
    public static void debug(String message, Object... args) {
        DebugLogger.debug(message, args);
    }

    /**
     * 记录INFO级别日志
     * 
     * @param message 日志消息
     */
    public static void info(String message) {
        InfoLogger.info(message);
    }

    /**
     * 记录INFO级别日志（支持参数格式化）
     * 
     * @param message 日志消息，支持{}占位符
     * @param args    参数数组
     */
    public static void info(String message, Object... args) {
        InfoLogger.info(message, args);
    }

    /**
     * 记录WARN级别日志
     * 
     * @param message 日志消息
     */
    public static void warn(String message) {
        WarnLogger.warn(message);
    }

    /**
     * 记录WARN级别日志（支持参数格式化）
     * 
     * @param message 日志消息，支持{}占位符
     * @param args    参数数组
     */
    public static void warn(String message, Object... args) {
        WarnLogger.warn(message, args);
    }

    /**
     * 记录ERROR级别日志
     * 
     * @param message 日志消息
     */
    public static void error(String message) {
        ErrorLogger.error(message);
    }

    /**
     * 记录ERROR级别日志（支持参数格式化）
     * 
     * @param message 日志消息，支持{}占位符
     * @param args    参数数组
     */
    public static void error(String message, Object... args) {
        ErrorLogger.error(message, args);
    }

    /**
     * 记录ERROR级别日志（带异常信息）
     * 
     * @param message   日志消息
     * @param throwable 异常对象
     */
    public static void error(String message, Throwable throwable) {
        ErrorLogger.error(message, throwable);
    }

    /**
     * 记录ERROR级别日志（带异常信息和参数格式化）
     * 
     * @param message   日志消息，支持{}占位符
     * @param throwable 异常对象
     * @param args      参数数组
     */
    public static void error(String message, Throwable throwable, Object... args) {
        ErrorLogger.error(message, throwable, args);
    }

    // ==================== 业务日志级别 API ====================

    /**
     * 创建业务日志构建器
     * 
     * <p>
     * 支持链式调用构建业务日志，自动追加类名和方法名信息
     * </p>
     * 
     * @return BusinessLogBuilder实例
     */
    public static BusinessLogBuilder business() {
        return new BusinessLogBuilder();
    }

    /**
     * 直接记录业务日志（INFO级别）
     * 
     * @param message 日志消息
     * @param args    参数数组
     */
    public static void business(String message, Object... args) {
        BusinessLogger.business(message, args);
    }

    // ==================== 便捷方法 ====================

    /**
     * 记录方法进入日志
     * 
     * @param className  类名
     * @param methodName 方法名
     * @param args       方法参数
     */
    public static void enter(String className, String methodName, Object... args) {
        business()
                .className(className)
                .methodName(methodName)
                .message("方法进入，参数：{}", formatArgs(args))
                .debug();
    }

    /**
     * 记录方法进入日志（自动获取类名和方法名）
     * 
     * @param args 方法参数
     */
    public static void enter(Object... args) {
        business()
                .auto()
                .message("方法进入，参数：{}", formatArgs(args))
                .debug();
    }

    /**
     * 记录方法退出日志
     * 
     * @param className  类名
     * @param methodName 方法名
     * @param result     返回结果
     */
    public static void exit(String className, String methodName, Object result) {
        business()
                .className(className)
                .methodName(methodName)
                .message("方法退出，返回值：{}", result)
                .debug();
    }

    /**
     * 记录方法退出日志（自动获取类名和方法名）
     * 
     * @param result 返回结果
     */
    public static void exit(Object result) {
        business()
                .auto()
                .message("方法退出，返回值：{}", result)
                .debug();
    }

    /**
     * 记录性能日志
     * 
     * @param className  类名
     * @param methodName 方法名
     * @param duration   执行耗时（毫秒）
     */
    public static void performance(String className, String methodName, long duration) {
        if (duration > 1000) {
            // 超过1秒的操作记录为警告
            business()
                    .className(className)
                    .methodName(methodName)
                    .message("性能警告：方法执行耗时 {}ms", duration)
                    .warn();
        } else if (duration > 100) {
            // 超过100ms的操作记录为信息
            business()
                    .className(className)
                    .methodName(methodName)
                    .message("性能统计：方法执行耗时 {}ms", duration)
                    .info();
        }
    }

    /**
     * 记录性能日志（自动获取类名和方法名）
     * 
     * @param duration 执行耗时（毫秒）
     */
    public static void performance(long duration) {
        if (duration > 1000) {
            business()
                    .auto()
                    .message("性能警告：方法执行耗时 {}ms", duration)
                    .warn();
        } else if (duration > 100) {
            business()
                    .auto()
                    .message("性能统计：方法执行耗时 {}ms", duration)
                    .info();
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 格式化参数数组为字符串
     * 
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    private static String formatArgs(Object... args) {
        if (args == null || args.length == 0) {
            return "无参数";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    /**
     * 检查是否启用调试模式
     * 
     * @return 如果启用调试模式则返回true
     */
    public static boolean isDebugEnabled() {
        return DebugLogger.getInstance().getLevel().isEnabled(LogLevel.DEBUG);
    }

    /**
     * 获取版本信息
     * 
     * @return 版本信息字符串
     */
    public static String getVersion() {
        return "LogUtils v1.0 - Enterprise Logging Framework";
    }

    /**
     * 打印系统信息（用于调试）
     */
    public static void printSystemInfo() {
        info("=== LogUtils 系统信息 ===");
        info("版本: {}", getVersion());
        info("Java版本: {}", System.getProperty("java.version"));
        info("操作系统: {}", System.getProperty("os.name"));
        info("当前线程: {}", Thread.currentThread().getName());
        info("========================");
    }
}
