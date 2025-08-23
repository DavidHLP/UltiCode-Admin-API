package com.david.cache.support;

/**
 * 用于表示缓存中的空值占位，防止缓存穿透。
 * 使用枚举单例，序列化体积小且线程安全。
 */
public enum NullValue {
    INSTANCE;
}
