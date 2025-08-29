package com.david.log.commons.core.operations;

/**
 * 性能日志操作接口
 * 
 * <p>
 * 专门用于性能监控的日志记录，包括执行时间统计、
 * 资源使用监控、QPS统计等功能。
 * 
 * @author David
 */
public interface PerformanceLogOperations extends LogOperations {

    /**
     * 记录方法执行时间
     * 
     * @param methodName 方法名称
     * @param duration   执行时长(毫秒)
     * @param args       方法参数
     */
    void timing(String methodName, long duration, Object... args);

    /**
     * 记录SQL执行性能
     * 
     * @param sql      SQL语句
     * @param duration 执行时长(毫秒)
     * @param rowCount 影响行数
     */
    void sql(String sql, long duration, int rowCount);

    /**
     * 记录HTTP请求性能
     * 
     * @param method   HTTP方法
     * @param url      请求URL
     * @param status   响应状态码
     * @param duration 执行时长(毫秒)
     */
    void http(String method, String url, int status, long duration);

    /**
     * 记录内存使用情况
     * 
     * @param component   组件名称
     * @param usedMemory  已使用内存(MB)
     * @param totalMemory 总内存(MB)
     */
    void memory(String component, long usedMemory, long totalMemory);

    /**
     * 记录QPS统计信息
     * 
     * @param endpoint        接口端点
     * @param qps             每秒请求数
     * @param avgResponseTime 平均响应时间(毫秒)
     */
    void qps(String endpoint, double qps, double avgResponseTime);
}
