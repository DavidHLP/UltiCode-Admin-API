package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.UserContentViewMapper;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.service.IUserContentViewService;
import com.david.usercontent.UserContentView;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class UserContentViewServiceImpl extends ServiceImpl<UserContentViewMapper, UserContentView>
        implements IUserContentViewService {
    private final UserContentViewMapper userContentViewMapper;

    @Override
    @RedisCacheable(
            key = "'userContentView:userHasViewedContent:' + #userId + ':' + #contentId",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = Boolean.class)
    public Boolean userHasViewedContent(Long userId, Long contentId) {
        return userContentViewMapper.userHasViewedContent(userId, contentId);
    }

    @Override
    @RedisEvict(
            keys = {
                "'userContentView:userHasViewedContent:' + #entity.getUserId() + ':' + #entity.getContentId()"
            },
            keyPrefix = "springoj:cache:")
    public boolean save(UserContentView entity) {
        return userContentViewMapper.insert(entity) > 0;
    }
}
