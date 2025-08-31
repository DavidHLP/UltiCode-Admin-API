package com.david.commons.redis.serialization;

/**
 * Redis 序列化类型枚举
 *
 * @author David
 */
public enum SerializationType {

    /**
     * JSON 序列化 - 跨语言兼容，调试友好
     */
    JSON("json", "application/json"),

    /**
     * Kryo 序列化 - 高性能，体积小
     */
    KRYO("kryo", "application/x-kryo"),

    /**
     * JDK 序列化 - Java 原生支持
     */
    JDK("jdk", "application/x-java-serialized-object"),

    /**
     * Protobuf 序列化 - 跨语言，体积最小
     */
    PROTOBUF("protobuf", "application/x-protobuf");

    private final String code;
    private final String mediaType;

    SerializationType(String code, String mediaType) {
        this.code = code;
        this.mediaType = mediaType;
    }

    public String getCode() {
        return code;
    }

    public String getMediaType() {
        return mediaType;
    }

    /**
     * 根据代码获取序列化类型
     */
    public static SerializationType fromCode(String code) {
        for (SerializationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown serialization type: " + code);
    }
}