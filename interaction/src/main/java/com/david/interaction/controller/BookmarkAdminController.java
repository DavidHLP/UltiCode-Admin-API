package com.david.interaction.controller;

import com.david.common.http.ApiResponse;
import com.david.interaction.dto.BookmarkDeleteRequest;
import com.david.interaction.dto.BookmarkQuery;
import com.david.interaction.dto.BookmarkView;
import com.david.interaction.dto.PageResult;
import com.david.interaction.service.BookmarkAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/interaction/bookmarks")
public class BookmarkAdminController {

    private final BookmarkAdminService bookmarkAdminService;

    @GetMapping
    public ApiResponse<PageResult<BookmarkView>> listBookmarks(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String source) {
        BookmarkQuery query =
                new BookmarkQuery(page, size, userId, entityType, entityId, visibility, source);
        log.info("查询收藏列表: {}", query);
        PageResult<BookmarkView> result = bookmarkAdminService.listBookmarks(query);
        return ApiResponse.success(result);
    }

    @DeleteMapping
    public ApiResponse<Void> deleteBookmark(@Valid @RequestBody BookmarkDeleteRequest request) {
        log.info(
                "删除收藏 userId={}, entityType={}, entityId={}",
                request.userId(),
                request.entityType(),
                request.entityId());
        bookmarkAdminService.deleteBookmark(request);
        return ApiResponse.success(null);
    }
}

