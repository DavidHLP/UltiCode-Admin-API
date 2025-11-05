package com.david.interaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.interaction.dto.ModerationTaskQuery;
import com.david.interaction.entity.ModerationTask;
import com.david.interaction.mapper.CommentMapper;
import com.david.interaction.mapper.ModerationTaskMapper;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModerationAdminServiceTest {

    @Mock
    private ModerationTaskMapper moderationTaskMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentAdminService commentAdminService;

    @Mock
    private ModerationWorkflowService moderationWorkflowService;

    @InjectMocks
    private ModerationAdminService moderationAdminService;

    @BeforeEach
    void setUp() {
        when(moderationTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> new Page<ModerationTask>(1, 10));
    }

    @Test
    void listTasks_appliesKeywordFilterToWrapper() {
        ModerationTaskQuery query =
                new ModerationTaskQuery(1, 10, null, null, null, null, null, "note");

        moderationAdminService.listTasks(query);

        ArgumentCaptor<LambdaQueryWrapper<ModerationTask>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(moderationTaskMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        LambdaQueryWrapper<ModerationTask> wrapper = wrapperCaptor.getValue();
        Map<String, Object> params = wrapper.getParamNameValuePairs();
        assertThat(params.values())
                .as("关键字应作用于备注筛选")
                .anyMatch(value -> value instanceof String && value.toString().contains("note"));
    }
}
