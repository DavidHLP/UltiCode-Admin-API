package com.david.common.forward;

/**
 * 令牌验证通过后，网关向下游服务传播的用户详细信息共享标头名称。
 */
public final class ForwardedUserHeaders {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_NAME = "X-User-Name";
    public static final String USER_ROLES = "X-User-Roles";
    /**
     * Field separator used when roles are forwarded as plain strings.
     */
    public static final String ROLE_DELIMITER = ",";

    private ForwardedUserHeaders() {}
}
