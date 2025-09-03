package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.asyncs.AsyncUserContentViewService;
import com.david.commons.redis.RedisUtils;
import com.david.mapper.SolutionMapper;
import com.david.mapper.UserContentViewMapper;
import com.david.service.IUserContentViewService;
import com.david.solution.enums.ContentType;
import com.david.usercontent.UserContentView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final RedisUtils redisUtils;
    private final SolutionMapper solutionMapper;
    private final AsyncUserContentViewService asyncUserContentViewService;

    private String buildUserContentViewKey(ContentType contentType, Long contentId) {
        return redisUtils.buildKey("UserContentView:" + contentType + ":" + contentId);
    }

    private String buildContentViewCountKey(ContentType contentType) {
        return redisUtils.buildKey("UserContentView:" + contentType);
    }

    // 仅用于 RedisCommonOperations（其内部会自动添加前缀）
    private String buildUserContentViewBizKey(ContentType contentType, Long contentId) {
        return "UserContentView:" + contentType + ":" + contentId;
    }

    @Override
    public Long saveOrPassAndGetViewsNumber(Long userId, Long contentId, ContentType contentType) {
        String userViewKey = buildUserContentViewKey(contentType, contentId);
        String viewCountKey = buildContentViewCountKey(contentType);

        var hashOps = redisUtils.hash();

        // 使用 HSETNX 保证原子去重，避免并发下重复计数
        boolean firstView =
                Optional.ofNullable(hashOps.hSetIfAbsent(userViewKey, userId.toString(), true))
                        .orElse(false);

        // 设置滑动过期，防止去重哈希无限增长（30天）
        redisUtils.common().expire(buildUserContentViewBizKey(contentType, contentId), Duration.ofDays(30));

        if (firstView) {
            // 确保在首次递增前完成基线初始化，避免 HINCRBY 抢先创建字段导致丢失 DB 初始值
            Long dbViews = Optional.ofNullable(solutionMapper.getViews(contentId)).orElse(0L);
            hashOps.hSetIfAbsent(viewCountKey, contentId.toString(), dbViews);

            Long after = hashOps.hIncrBy(viewCountKey, contentId.toString(), 1);
            // 持久化异步入库（表有唯一约束兜底）
            asyncUserContentViewService.save(
                    UserContentView.builder()
                            .userId(userId)
                            .contentId(contentId)
                            .contentType(contentType)
                            .build());
            return after;
        }

        // 非首次浏览仅返回当前计数；若计数字段缺失则延迟初始化为 DB 值
        Long current = hashOps.hGet(viewCountKey, contentId.toString(), Long.class);
        if (current == null) {
            Long dbViews = Optional.ofNullable(solutionMapper.getViews(contentId)).orElse(0L);
            hashOps.hSetIfAbsent(viewCountKey, contentId.toString(), dbViews);
            return dbViews;
        }
        return current;
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void SyncUserContentViewCountTask() {
        for (ContentType contentType : ContentType.values()) {
            String pattern = "UserContentView:" + contentType + ":*";
            redisUtils
                    .common()
                    .scan(
                            pattern,
                            1000,
                            key -> {
                                try {
                                    String[] keyParts = key.split(":");
                                    Long contentId = Long.parseLong(keyParts[keyParts.length - 1]);
                                    // scan 回调返回的是去前缀的业务键，这里需要还原完整键
                                    String fullKey = redisUtils.buildKey(key);
                                    redisUtils
                                            .hash()
                                            .hGetAll(fullKey, Boolean.class)
                                            .forEach(
                                                    (userId, isViewed) -> {
                                                        if (Boolean.TRUE.equals(isViewed)
                                                                && !userContentViewMapper
                                                                        .userHasViewedContent(
                                                                                Long.parseLong(
                                                                                        userId),
                                                                                contentId)) {
                                                            userContentViewMapper.insert(
                                                                    UserContentView.builder()
                                                                            .userId(
                                                                                    Long.parseLong(
                                                                                            userId))
                                                                            .contentId(contentId)
                                                                            .contentType(contentType)
                                                                            .build());
                                                            solutionMapper.updateViews(contentId);
                                                        }
                                                    });
                                } catch (Exception e) {
                                    log.warn("Sync view task processing key {} failed", key, e);
                                }
                            });

            // 将缓存中的计数刷新为数据库真实值，避免长期漂移
            redisUtils
                    .hash()
                    .hGetAll(buildContentViewCountKey(contentType), Long.class)
                    .forEach(
                            (contentId, count) ->
                                    redisUtils
                                            .hash()
                                            .hSet(
                                                    buildContentViewCountKey(contentType),
                                                    contentId,
                                                    solutionMapper.getViews(
                                                            Long.parseLong(contentId))));
        }
    }
}
