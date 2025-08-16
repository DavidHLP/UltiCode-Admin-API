package com.david.enums.interfaces;

/**
 * 资源限制类型接口。
 * <p>定义获取时间与内存限制的统一契约（秒、MB），并提供便捷的单位换算默认方法。</p>
 */
public interface LimitType {
    /**
     * 获取时间限制（秒）
     * @return 时间限制（秒），必须 > 0
     */
    int getTimeLimitSeconds();

    /**
     * 获取内存限制（MB）
     * @return 内存限制（MB），必须 > 0
     */
    int getMemoryLimitMB();

    /**
     * 获取时间限制（毫秒）
     * @return 时间限制（毫秒）
     */
    default long getTimeLimitMillis() {
        // 使用 long 避免溢出
        return (long) getTimeLimitSeconds() * 1000L;
    }

    /**
     * 获取内存限制（字节）
     * @return 内存限制（字节）
     */
    default long getMemoryLimitBytes() {
        return (long) getMemoryLimitMB() * 1024L * 1024L;
    }
}
