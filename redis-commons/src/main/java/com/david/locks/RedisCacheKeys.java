package com.david.locks;

public enum RedisCacheKeys {
    AUTH_USERNAME_KEY("auth:username:"),
    VERIFICATION_CODE_KEY_PREFIX("verification:code:");

    private final String cacheKey;

    RedisCacheKeys(String cacheKey) {
        this.cacheKey = cacheKey;
    }
    @Override
    public String toString() {
        return cacheKey;
    }
}
