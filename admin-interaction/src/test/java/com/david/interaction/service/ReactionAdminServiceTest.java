package com.david.interaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.interaction.dto.ReactionQuery;
import com.david.interaction.entity.Reaction;
import com.david.interaction.mapper.ReactionMapper;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReactionAdminServiceTest {

    @Mock
    private ReactionMapper reactionMapper;

    @Mock
    private SensitiveWordAdminService sensitiveWordAdminService;

    @InjectMocks
    private ReactionAdminService reactionAdminService;

    @BeforeEach
    void setUp() {
        when(sensitiveWordAdminService.loadActiveWords()).thenReturn(List.of());
        when(reactionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> new Page<Reaction>(1, 10));
    }

    @Test
    void listReactions_appliesKeywordFilterToWrapper() {
        ReactionQuery query = new ReactionQuery(1, 10, null, null, null, null, null, "flag");

        reactionAdminService.listReactions(query);

        ArgumentCaptor<LambdaQueryWrapper<Reaction>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(reactionMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        LambdaQueryWrapper<Reaction> wrapper = wrapperCaptor.getValue();
        Map<String, Object> params = wrapper.getParamNameValuePairs();
        assertThat(params.values())
                .as("关键字应参与模糊查询")
                .anyMatch(value -> value instanceof String && value.toString().contains("flag"));
    }
}
