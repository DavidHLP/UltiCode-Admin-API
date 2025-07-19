package com.david.locks;

public enum RedisLocks {
    AUTH_INSERT_REDIS_LOCK("auth:insert:redis:lock:"),
    AUTH_UPDATE_REDIS_LOCK("auth:update:redis:lock:"),
    AUTH_DELETE_REDIS_LOCK("auth:delete:redis:lock:"),
    AUTH_INSERT_TOKEN_LOCK("auth:insert:token:lock:"),
    LOGIN("auth:login:lock:"),
    REGISTER("auth:register:lock:"),;

    private final String lockKey;

    RedisLocks(String lockKey) {
        this.lockKey = lockKey;
    }
    @Override
    public String toString() {
        return lockKey;
    }
}
