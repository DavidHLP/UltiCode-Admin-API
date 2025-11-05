package com.david.interaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.interaction.dto.BookmarkQuery;
import com.david.interaction.entity.Bookmark;
import com.david.interaction.mapper.BookmarkMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

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
class BookmarkAdminServiceTest {

    @Mock
    private BookmarkMapper bookmarkMapper;

    @Mock
    private SensitiveWordAdminService sensitiveWordAdminService;

    @InjectMocks
    private BookmarkAdminService bookmarkAdminService;

    @BeforeEach
    void setUp() {
        bookmarkAdminService =
                new BookmarkAdminService(bookmarkMapper, new ObjectMapper(), sensitiveWordAdminService);
        when(sensitiveWordAdminService.loadActiveWords()).thenReturn(List.of());
        when(bookmarkMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> new Page<Bookmark>(1, 10));
    }

    @Test
    void listBookmarks_appliesKeywordFilterToWrapper() {
        BookmarkQuery query = new BookmarkQuery(1, 10, null, null, null, null, null, "demo");

        bookmarkAdminService.listBookmarks(query);

        ArgumentCaptor<LambdaQueryWrapper<Bookmark>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(bookmarkMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        LambdaQueryWrapper<Bookmark> wrapper = wrapperCaptor.getValue();
        Map<String, Object> params = wrapper.getParamNameValuePairs();
        assertThat(params.values())
                .as("关键字应被用于模糊匹配")
                .anyMatch(value -> value instanceof String && value.toString().contains("demo"));
    }
}
