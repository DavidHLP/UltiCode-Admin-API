package com.david.admin.controller;

import com.david.admin.dto.AuthTokenView;
import com.david.admin.dto.PageResult;
import com.david.admin.service.AuthTokenManagementService;
import com.david.admin.service.SensitiveOperationGuard;
import com.david.core.security.CurrentForwardedUser;
import com.david.core.forward.ForwardedUser;
import com.david.core.http.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('platform_admin')")
@RequestMapping("/api/admin/auth-tokens")
public class AuthTokenAdminController {

    private final AuthTokenManagementService authTokenManagementService;
    private final SensitiveOperationGuard sensitiveOperationGuard;

    @GetMapping
    public ApiResponse<PageResult<AuthTokenView>> listTokens(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) Boolean revoked,
            @RequestParam(required = false) String createdAtStart,
            @RequestParam(required = false) String createdAtEnd,
            @RequestParam(required = false) String expiresAtStart,
            @RequestParam(required = false) String expiresAtEnd) {
        PageResult<AuthTokenView> tokens =
                authTokenManagementService.listTokens(
                        page,
                        size,
                        keyword,
                        userId,
                        kind,
                        revoked,
                        parseDate(createdAtStart),
                        parseDate(createdAtEnd),
                        parseDate(expiresAtStart),
                        parseDate(expiresAtEnd));
        return ApiResponse.success(tokens);
    }

    @DeleteMapping("/{tokenId}")
    public ApiResponse<Void> revokeToken(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @PathVariable Long tokenId) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        authTokenManagementService.revokeToken(principal, tokenId);
        log.info("撤销令牌成功，tokenId={} by user {}", tokenId, principal.username());
        return ApiResponse.success(null);
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
