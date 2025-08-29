package com.david.log.commons.core.operations;

/**
 * 业务日志操作接口
 * 
 * <p>
 * 专门用于业务流程的日志记录，包括用户操作审计、
 * 业务流程追踪、关键业务事件记录等功能。
 * 
 * @author David
 */
public interface BusinessLogOperations extends LogOperations {

    /**
     * 记录用户操作审计日志
     * 
     * @param userId    用户ID
     * @param operation 操作名称
     * @param result    操作结果
     * @param details   操作详情
     */
    void audit(String userId, String operation, String result, Object... details);

    /**
     * 记录业务流程追踪日志
     * 
     * @param processId 流程ID
     * @param step      流程步骤
     * @param status    步骤状态
     * @param data      相关数据
     */
    void trace(String processId, String step, String status, Object... data);

    /**
     * 记录业务事件日志
     * 
     * @param eventType 事件类型
     * @param eventData 事件数据
     */
    void event(String eventType, Object... eventData);

    /**
     * 记录业务指标日志
     * 
     * @param metric 指标名称
     * @param value  指标值
     * @param tags   标签信息
     */
    void metric(String metric, Object value, Object... tags);
}
