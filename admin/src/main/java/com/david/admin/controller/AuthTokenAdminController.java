package com.david.admin.controller;

import com.david.admin.dto.AuthTokenView;
import com.david.admin.service.AuthTokenManagementService;
import com.david.admin.service.SensitiveOperationGuard;
import com.david.common.forward.CurrentForwardedUser;
import com.david.common.forward.ForwardedUser;
import com.david.common.http.ApiResponse;
import java.util.List;
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
    public ApiResponse<List<AuthTokenView>> listTokens(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) Boolean revoked) {
        List<AuthTokenView> tokens = authTokenManagementService.listTokens(userId, kind, revoked);
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
}
