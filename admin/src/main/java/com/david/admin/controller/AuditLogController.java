package com.david.admin.controller;

import com.david.admin.dto.AuditLogView;
import com.david.admin.dto.PageResult;
import com.david.admin.service.AuditLogQueryService;
import com.david.core.http.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('platform_admin')")
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    @GetMapping
    public ApiResponse<PageResult<AuditLogView>> listAuditLogs(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String objectId,
            @RequestParam(required = false) String createdAtStart,
            @RequestParam(required = false) String createdAtEnd) {
        PageResult<AuditLogView> result =
                auditLogQueryService.pageAuditLogs(
                        page,
                        size,
                        action,
                        keyword,
                        actorId,
                        actorUsername,
                        objectType,
                        objectId,
                        parseDate(createdAtStart),
                        parseDate(createdAtEnd));
        return ApiResponse.success(result);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("日期格式不正确: " + value, ex);
        }
    }
}
