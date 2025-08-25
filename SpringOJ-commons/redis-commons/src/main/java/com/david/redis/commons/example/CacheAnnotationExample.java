package com.david.redis.commons.example;

import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存注解使用示例
 * 演示如何使用@RedisCacheable和@RedisEvict注解
 *
 * @author David
 */
@Slf4j
@Service
public class CacheAnnotationExample {

    // 模拟数据存储
    private final Map<Long, User> userDatabase = new HashMap<>();

    public CacheAnnotationExample() {
        // 初始化一些测试数据
        userDatabase.put(1L, new User(1L, "张三", "zhangsan@example.com"));
        userDatabase.put(2L, new User(2L, "李四", "lisi@example.com"));
        userDatabase.put(3L, new User(3L, "王五", "wangwu@example.com"));
    }

    /**
     * 根据用户ID获取用户信息（带缓存）
     * 缓存键格式：user:1, user:2, etc.
     * 缓存时间：30分钟
     */
    @RedisCacheable(key = "'user:' + #userId", ttl = 1800, // 30分钟
            condition = "#userId != null && #userId > 0", type = User.class)
    public User getUserById(Long userId) {
        log.info("从数据库查询用户: {}", userId);
        // 模拟数据库查询延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return userDatabase.get(userId);
    }

    /**
     * 根据用户名获取用户信息（带缓存）
     * 缓存键格式：user:name:张三
     * 只有当结果不为null时才缓存
     */
    @RedisCacheable(key = "'user:name:' + #username", ttl = 600, // 10分钟
            condition = "#result != null", type = User.class, cacheNullValues = false)
    public User getUserByName(String username) {
        log.info("从数据库根据用户名查询用户: {}", username);
        return userDatabase.values().stream()
                .filter(user -> user.getName().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取用户详细信息（复合缓存键）
     * 缓存键格式：user:detail:1:profile
     */
    @RedisCacheable(key = "'user:detail:' + #userId + ':' + #type", ttl = 3600, // 1小时
            keyPrefix = "app:cache:", type = UserDetail.class)
    public UserDetail getUserDetail(Long userId, String type) {
        log.info("从数据库查询用户详细信息: userId={}, type={}", userId, type);
        User user = userDatabase.get(userId);
        if (user == null) {
            return null;
        }

        return new UserDetail(user, type, "详细信息内容");
    }

    /**
     * 更新用户信息（清除相关缓存）
     * 清除用户ID缓存和用户名缓存
     */
    @RedisEvict(keys = {
            "'user:' + #user.id",
            "'user:name:' + #user.name"
    }, condition = "#user != null")
    public User updateUser(User user) {
        log.info("更新用户信息: {}", user);
        if (user != null && user.getId() != null) {
            userDatabase.put(user.getId(), user);
        }
        return user;
    }

    /**
     * 删除用户（清除所有相关缓存）
     * 清除该用户的所有缓存条目
     */
    @RedisEvict(keys = {
            "'user:' + #userId",
            "'user:name:' + #T.getUserName(#userId)"
    }, keyPrefix = "app:cache:", condition = "#userId != null && #userId > 0")
    public boolean deleteUser(Long userId) {
        log.info("删除用户: {}", userId);
        User removed = userDatabase.remove(userId);
        return removed != null;
    }

    /**
     * 清除所有用户缓存
     */
    @RedisEvict(allEntries = true, keyPrefix = "user:", beforeInvocation = true)
    public void clearAllUserCache() {
        log.info("清除所有用户缓存");
    }

    /**
     * 批量获取用户（演示复杂的缓存键生成）
     */
    @RedisCacheable(key = "'users:batch:' + #T.hash(#userIds)", ttl = 300, // 5分钟
            condition = "#userIds != null && #userIds.size() > 0", type = Map.class)
    public Map<Long, User> getUsersByIds(java.util.List<Long> userIds) {
        log.info("批量查询用户: {}", userIds);
        Map<Long, User> result = new HashMap<>();
        for (Long userId : userIds) {
            User user = userDatabase.get(userId);
            if (user != null) {
                result.put(userId, user);
            }
        }
        return result;
    }

    // 内部类：用户实体
    public static class User {
        private Long id;
        private String name;
        private String email;

        public User() {
        }

        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }

    // 内部类：用户详细信息
    public static class UserDetail {
        private User user;
        private String type;
        private String content;

        public UserDetail() {
        }

        public UserDetail(User user, String type, String content) {
            this.user = user;
            this.type = type;
            this.content = content;
        }

        // Getters and Setters
        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "UserDetail{user=" + user + ", type='" + type + "', content='" + content + "'}";
        }
    }
}