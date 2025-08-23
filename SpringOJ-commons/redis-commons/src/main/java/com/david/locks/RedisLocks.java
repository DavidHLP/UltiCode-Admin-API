package com.david.locks;

public enum RedisLocks {
    LOGIN("auth:login:lock:"),
    LOGOUT("auth:logout:lock:"),
    REGISTER("auth:register:lock:");

    private final String lockKey;

    RedisLocks(String lockKey) {
        this.lockKey = lockKey;
    }
    @Override
    public String toString() {
        return lockKey;
    }
}
