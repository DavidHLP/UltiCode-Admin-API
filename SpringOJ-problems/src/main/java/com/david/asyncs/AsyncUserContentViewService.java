package com.david.asyncs;

import com.david.mapper.SolutionMapper;
import com.david.mapper.UserContentViewMapper;
import com.david.usercontent.UserContentView;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsyncUserContentViewService {

    private final UserContentViewMapper userContentViewMapper;
    private final SolutionMapper solutionMapper;

    @Async
    @Transactional
    public void save(UserContentView entity) {
        userContentViewMapper.insert(entity);
        solutionMapper.updateViews(entity.getContentId());
    }
}