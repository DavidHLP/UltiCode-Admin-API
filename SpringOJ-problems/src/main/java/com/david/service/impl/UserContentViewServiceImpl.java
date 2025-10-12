package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.asyncs.AsyncUserContentViewService;
import com.david.mapper.SolutionMapper;
import com.david.mapper.UserContentViewMapper;
import com.david.service.IUserContentViewService;
import com.david.solution.enums.ContentType;
import com.david.usercontent.UserContentView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class UserContentViewServiceImpl extends ServiceImpl<UserContentViewMapper, UserContentView>
        implements IUserContentViewService {

    private final UserContentViewMapper userContentViewMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final SolutionMapper solutionMapper;
    private final AsyncUserContentViewService asyncUserContentViewService;

    private String buildUserContentViewKey(ContentType contentType, Long contentId) {
        return "UserContentView:" + contentType + ":" + contentId;
    }

    private String buildContentViewCountKey(ContentType contentType) {
        return "UserContentView:" + contentType;
    }

    @Override
    public Long saveOrPassAndGetViewsNumber(Long userId, Long contentId, ContentType contentType) {
        log.info("开始处理用户 {} 对 {} 类型内容 {} 的浏览请求", userId, contentType, contentId);

        String userViewKey = buildUserContentViewKey(contentType, contentId);
        String viewCountKey = buildContentViewCountKey(contentType);

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

        // 使用 HSETNX 保证原子去重，避免并发下重复计数
        boolean firstView =
                Optional.ofNullable(hashOps.putIfAbsent(userViewKey, userId.toString(), true))
                        .orElse(false);

        // 设置滑动过期，防止去重哈希无限增长（30天）
        redisTemplate.expire(userViewKey, Duration.ofDays(30));

        if (firstView) {
            log.info("检测到用户 {} 首次浏览 {} 类型内容 {}", userId, contentType, contentId);
            // 确保在首次递增前完成基线初始化，避免 HINCRBY 抢先创建字段导致丢失 DB 初始值
            Long dbViews = Optional.ofNullable(solutionMapper.getViews(contentId)).orElse(0L);
            hashOps.putIfAbsent(viewCountKey, contentId.toString(), dbViews);

            Long after = hashOps.increment(viewCountKey, contentId.toString(), 1);
            log.info("更新 {} 类型内容 {} 的浏览次数为 {}", contentType, contentId, after);

            // 持久化异步入库（表有唯一约束兜底）
            asyncUserContentViewService.save(
                    UserContentView.builder()
                            .userId(userId)
                            .contentId(contentId)
                            .contentType(contentType)
                            .build());
            log.info("异步保存用户 {} 对 {} 类型内容 {} 的浏览记录", userId, contentType, contentId);
            return after;
        }

        // 非首次浏览仅返回当前计数；若计数字段缺失则延迟初始化为 DB 值
        Object rawCurrent = hashOps.get(viewCountKey, contentId.toString());
        Long current = null;
        if (rawCurrent instanceof Number) {
            current = ((Number) rawCurrent).longValue();
        } else if (rawCurrent != null) {
            try {
                current = Long.parseLong(rawCurrent.toString());
            } catch (NumberFormatException e) {
                log.warn(
                        "Failed to parse number from redis for key {} field {}",
                        viewCountKey,
		                contentId);
            }
        }

        if (current == null) {
            Long dbViews = Optional.ofNullable(solutionMapper.getViews(contentId)).orElse(0L);
            hashOps.putIfAbsent(viewCountKey, contentId.toString(), dbViews);
            log.info("初始化 {} 类型内容 {} 的浏览次数为 {}", contentType, contentId, dbViews);
            return dbViews;
        }
        log.info("用户 {} 再次浏览 {} 类型内容 {}，当前浏览次数为 {}", userId, contentType, contentId, current);
        return current;
    }

    @Override
    public Long getViewsNumber(Long userId, Long contentId, ContentType contentType) {
        String viewCountKey = buildContentViewCountKey(contentType);
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        Object rawCount = hashOps.get(viewCountKey, contentId.toString());
        Long count = null;
        if (rawCount instanceof Number) {
            count = ((Number) rawCount).longValue();
        } else if (rawCount != null) {
            try {
                count = Long.parseLong(rawCount.toString());
            } catch (NumberFormatException e) {
                log.warn(
                        "Failed to parse number from redis for key {} field {}",
                        viewCountKey,
		                contentId);
            }
        }

        if (count == null) {
            count = Optional.ofNullable(solutionMapper.getViews(contentId)).orElse(0L);
            hashOps.putIfAbsent(viewCountKey, contentId.toString(), count);
        }
        return count;
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void SyncUserContentViewCountTask() {
        log.info("开始执行用户浏览记录同步任务");
        for (ContentType contentType : ContentType.values()) {
            log.info("处理 {} 类型内容的浏览记录同步", contentType);
            String pattern = "UserContentView:" + contentType + ":*";

            Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern, 1000);
            HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

            for (String key : keys) {
                try {
                    String[] keyParts = key.split(":");
                    Long contentId = Long.parseLong(keyParts[keyParts.length - 1]);
                    log.info("处理内容 {} 的浏览记录同步", contentId);

                    hashOps.entries(key)
                            .forEach(
                                    (userIdObj, isViewedObj) -> {
                                        String userId = userIdObj.toString();
                                        boolean isViewed = false;
                                        if (isViewedObj instanceof Boolean) {
                                            isViewed = (Boolean) isViewedObj;
                                        } else if (isViewedObj != null) {
                                            isViewed = Boolean.parseBoolean(isViewedObj.toString());
                                        }

                                        if (isViewed
                                                && !userContentViewMapper.userHasViewedContent(
                                                        Long.parseLong(userId), contentId)) {
                                            log.info("同步用户 {} 对内容 {} 的浏览记录", userId, contentId);
                                            userContentViewMapper.insert(
                                                    UserContentView.builder()
                                                            .userId(Long.parseLong(userId))
                                                            .contentId(contentId)
                                                            .contentType(contentType)
                                                            .build());
                                            solutionMapper.updateViews(contentId);
                                        }
                                    });
                } catch (Exception e) {
                    log.warn("Sync view task processing key {} failed", key, e);
                }
                redisTemplate.delete(key);
            }

            // 将缓存中的计数刷新为数据库真实值，避免长期漂移
            log.info("刷新 {} 类型内容的浏览次数缓存", contentType);
            String viewCountKey = buildContentViewCountKey(contentType);
            hashOps.entries(viewCountKey)
                    .forEach(
                            (contentIdObj, countObj) -> {
                                String contentId = contentIdObj.toString();
                                Long count = null;
                                if (countObj instanceof Number) {
                                    count = ((Number) countObj).longValue();
                                } else if (countObj != null) {
                                    try {
                                        count = Long.parseLong(countObj.toString());
                                    } catch (NumberFormatException e) {
                                        log.warn(
                                                "Could not parse count for contentId {}",
                                                contentId,
                                                e);
                                        return;
                                    }
                                }

                                Long dbViews =
                                        Optional.ofNullable(
                                                        solutionMapper.getViews(
                                                                Long.parseLong(contentId)))
                                                .orElse(0L);
                                log.info("更新内容 {} 的浏览次数缓存，从 {} 更新为 {}", contentId, count, dbViews);
                                hashOps.put(viewCountKey, contentId, dbViews);
                            });
        }
        log.info("用户浏览记录同步任务执行完成");
    }
}
