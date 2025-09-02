package com.david.commons.redis.serialization;

import lombok.Getter;

/**
 * Redis 序列化类型枚举 - 简化为仅支持 JSON
 *
 * @author David
 */
@Getter
public enum SerializationType {

    /** JSON 序列化 - 基于 Jackson，支持通过 Class 进行反序列化 */
    JSON("json", "application/json");

    private final String code;
    private final String mediaType;

    SerializationType(String code, String mediaType) {
        this.code = code;
        this.mediaType = mediaType;
    }
}
