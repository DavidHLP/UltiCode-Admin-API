package com.david.commons.redis.operations;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作接口
 *
 * @author David
 */
public interface RedisCommonOperations {

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 批量判断键是否存在
     *
     * @param keys 键集合
     * @return 存在的键数量
     */
    Long countExistingKeys(Collection<String> keys);

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否删除成功
     */
    Boolean delete(String key);

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    Long delete(Collection<String> keys);

    /**
     * 设置键的过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 设置键的过期时间
     *
     * @param key      键
     * @param duration 过期时间
     * @return 是否设置成功
     */
    Boolean expire(String key, Duration duration);

    /**
     * 设置键在指定时间过期
     *
     * @param key  键
     * @param date 过期时间
     * @return 是否设置成功
     */
    Boolean expireAt(String key, Date date);

    /**
     * 移除键的过期时间
     *
     * @param key 键
     * @return 是否移除成功
     */
    Boolean persist(String key);

    /**
     * 获取键的剩余过期时间
     *
     * @param key  键
     * @param unit 时间单位
     * @return 剩余过期时间
     */
    Long getExpire(String key, TimeUnit unit);

    /**
     * 获取键的剩余过期时间
     *
     * @param key 键
     * @return 剩余过期时间（秒）
     */
    Long getExpire(String key);

    /**
     * 重命名键
     *
     * @param oldKey 旧键名
     * @param newKey 新键名
     */
    void rename(String oldKey, String newKey);

    /**
     * 仅当新键不存在时重命名
     *
     * @param oldKey 旧键名
     * @param newKey 新键名
     * @return 是否重命名成功
     */
    Boolean renameIfAbsent(String oldKey, String newKey);

    /**
     * 获取键的数据类型
     *
     * @param key 键
     * @return 数据类型
     */
    DataType type(String key);

    /**
     * 根据模式查找键
     *
     * @param pattern 模式
     * @return 匹配的键集合
     */
    Set<String> keys(String pattern);

    /**
     * 随机获取一个键
     *
     * @return 随机键
     */
    String randomKey();

    /**
     * 将键移动到指定数据库
     *
     * @param key     键
     * @param dbIndex 数据库索引
     * @return 是否移动成功
     */
    Boolean move(String key, int dbIndex);

    /**
     * 清空当前数据库
     */
    void flushDb();

    /**
     * 清空所有数据库
     */
    void flushAll();

    /**
     * 获取数据库大小
     *
     * @return 键数量
     */
    Long dbSize();

    /**
     * Redis 数据类型枚举
     */
    enum DataType {
        NONE("none"),
        STRING("string"),
        LIST("list"),
        SET("set"),
        ZSET("zset"),
        HASH("hash"),
        STREAM("stream");

        private final String code;

        DataType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static DataType fromCode(String code) {
            for (DataType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return NONE;
        }
    }
}