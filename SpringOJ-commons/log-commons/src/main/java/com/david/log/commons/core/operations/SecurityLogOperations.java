package com.david.log.commons.core.operations;

/**
 * 安全日志操作接口
 * 
 * <p>
 * 专门用于安全相关的日志记录，包括认证授权、
 * 安全事件记录、威胁检测等功能。
 * 
 * @author David
 */
public interface SecurityLogOperations extends LogOperations {

    /**
     * 记录用户登录日志
     * 
     * @param userId    用户ID
     * @param loginType 登录类型
     * @param success   是否成功
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    void login(String userId, String loginType, boolean success, String ipAddress, String userAgent);

    /**
     * 记录用户登出日志
     * 
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param reason    登出原因
     */
    void logout(String userId, String sessionId, String reason);

    /**
     * 记录权限检查日志
     * 
     * @param userId    用户ID
     * @param resource  资源名称
     * @param operation 操作类型
     * @param granted   是否授权
     */
    void permission(String userId, String resource, String operation, boolean granted);

    /**
     * 记录安全威胁日志
     * 
     * @param threatType  威胁类型
     * @param severity    严重程度
     * @param description 威胁描述
     * @param source      威胁来源
     */
    void threat(String threatType, String severity, String description, String source);

    /**
     * 记录数据访问日志
     * 
     * @param userId      用户ID
     * @param dataType    数据类型
     * @param action      操作动作
     * @param recordCount 记录数量
     */
    void dataAccess(String userId, String dataType, String action, int recordCount);
}
