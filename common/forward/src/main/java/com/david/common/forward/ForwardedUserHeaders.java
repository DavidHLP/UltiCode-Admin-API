package com.david.common.forward;

/**
 * Shared header names for user details propagated from the gateway
 * to downstream services once a token has been validated.
 */
public final class ForwardedUserHeaders {

    private ForwardedUserHeaders() {}

    public static final String USER_ID = "X-User-Id";
    public static final String USER_NAME = "X-User-Name";
    public static final String USER_ROLES = "X-User-Roles";

    /**
     * Field separator used when roles are forwarded as plain strings.
     */
    public static final String ROLE_DELIMITER = ",";
}
