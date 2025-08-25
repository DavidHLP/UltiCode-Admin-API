package com.david.redis.commons.example;

import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.core.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Redis事务注解使用示例
 *
 * <p>
 * 展示如何使用@RedisTransactional注解来管理Redis事务，
 * 包括基本用法、异常回滚、嵌套事务等场景。
 * </p>
 *
 * @author David
 */
@Slf4j
@Service
public class TransactionAnnotationExample {

    private final RedisUtils redisUtils;

    public TransactionAnnotationExample(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * 基本事务示例
     * 所有Redis操作将在一个事务中执行
     */
    @RedisTransactional
    public void basicTransactionExample(String userId, String userName, String email) {
        log.info("执行基本事务示例 - 用户ID: {}", userId);

        // 这些操作将在同一个Redis事务中执行
        redisUtils.set("user:" + userId + ":name", userName);
        redisUtils.set("user:" + userId + ":email", email);
        redisUtils.hSet("user:index", userId, userName);

        log.info("基本事务示例完成");
    }

    /**
     * 带超时的事务示例
     * 设置30秒超时时间
     */
    @RedisTransactional(timeout = 30000, label = "用户数据更新")
    public void transactionWithTimeoutExample(String userId, UserData userData) {
        log.info("执行带超时的事务示例 - 用户ID: {}", userId);

        redisUtils.set("user:" + userId + ":profile", userData);
        redisUtils.hSet("user:stats", userId + ":lastUpdate", System.currentTimeMillis());

        // 模拟一些处理时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("带超时的事务示例完成");
    }

    /**
     * 指定回滚异常的事务示例
     * 只有BusinessException会触发回滚
     */
    @RedisTransactional(rollbackFor = { BusinessException.class })
    public void transactionWithSpecificRollbackExample(String orderId, OrderData orderData) {
        log.info("执行指定回滚异常的事务示例 - 订单ID: {}", orderId);

        redisUtils.set("order:" + orderId, orderData);
        redisUtils.hSet("order:index", orderId, orderData.getStatus());

        // 业务逻辑检查
        if (orderData.getAmount() < 0) {
            throw new BusinessException("订单金额不能为负数");
        }

        log.info("指定回滚异常的事务示例完成");
    }

    /**
     * 只读事务示例
     * 不会执行MULTI/EXEC，仅用于标识和监控
     */
    @RedisTransactional(readOnly = true, label = "数据查询")
    public UserData readOnlyTransactionExample(String userId) {
        log.info("执行只读事务示例 - 用户ID: {}", userId);

        String name = redisUtils.getString("user:" + userId + ":name");
        String email = redisUtils.getString("user:" + userId + ":email");

        UserData userData = new UserData();
        userData.setName(name);
        userData.setEmail(email);

        log.info("只读事务示例完成");
        return userData;
    }

    /**
     * 嵌套事务示例
     * 演示事务传播行为
     */
    @RedisTransactional(propagation = RedisTransactional.Propagation.REQUIRED)
    public void nestedTransactionExample(String userId, String sessionId) {
        log.info("执行嵌套事务示例 - 用户ID: {}", userId);

        // 外层事务操作
        redisUtils.set("user:" + userId + ":session", sessionId);

        // 调用另一个事务方法
        updateUserActivity(userId);

        log.info("嵌套事务示例完成");
    }

    /**
     * 内层事务方法
     * 使用REQUIRES_NEW传播行为创建新事务
     */
    @RedisTransactional(propagation = RedisTransactional.Propagation.REQUIRES_NEW)
    public void updateUserActivity(String userId) {
        log.info("更新用户活动 - 用户ID: {}", userId);

        redisUtils.hSet("user:activity", userId + ":lastSeen", System.currentTimeMillis());
        redisUtils.hIncrBy("user:activity", userId + ":loginCount", 1);

        log.info("用户活动更新完成");
    }

    /**
     * 异常处理示例
     * 演示事务回滚机制
     */
    @RedisTransactional
    public void exceptionHandlingExample(String userId, boolean shouldFail) {
        log.info("执行异常处理示例 - 用户ID: {}, 是否失败: {}", userId, shouldFail);

        redisUtils.set("user:" + userId + ":temp", "临时数据");
        redisUtils.hSet("temp:data", userId, System.currentTimeMillis());

        if (shouldFail) {
            throw new RuntimeException("模拟业务异常");
        }

        log.info("异常处理示例完成");
    }

    /**
     * 复杂业务场景示例
     * 包含多种Redis操作类型
     */
    @RedisTransactional(timeout = 60000, label = "复杂业务处理")
    public void complexBusinessExample(String userId, String productId, int quantity) {
        log.info("执行复杂业务示例 - 用户: {}, 产品: {}, 数量: {}", userId, productId, quantity);

        // 检查库存
        Long stock = redisUtils.hGet("product:stock", productId, Long.class);
        if (stock == null || stock < quantity) {
            throw new BusinessException("库存不足");
        }

        // 扣减库存
        redisUtils.hIncrBy("product:stock", productId, -quantity);

        // 创建订单
        String orderId = "order:" + System.currentTimeMillis();
        OrderData order = new OrderData();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setStatus("CREATED");

        redisUtils.set(orderId, order);

        // 更新用户订单列表
        redisUtils.lPush("user:" + userId + ":orders", orderId);

        // 记录操作日志
        redisUtils.lPush("operation:log",
                String.format("用户%s购买产品%s数量%d", userId, productId, quantity));

        log.info("复杂业务示例完成 - 订单ID: {}", orderId);
    }

    // 示例数据类
    public static class UserData {
        private String name;
        private String email;

        // getters and setters
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
    }

    public static class OrderData {
        private String userId;
        private String productId;
        private int quantity;
        private String status;
        private double amount;

        // getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    // 示例异常类
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}