package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.UserContentViewMapper;
import com.david.service.IUserContentViewService;
import com.david.usercontent.UserContentView;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserContentViewServiceImpl extends ServiceImpl<UserContentViewMapper, UserContentView>
        implements IUserContentViewService {
    private final UserContentViewMapper userContentViewMapper;

    @Override
    public Boolean userHasViewedContent(Long userId, Long contentId) {
        return userContentViewMapper.userHasViewedContent(userId, contentId);
    }
}
