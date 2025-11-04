package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.admin.dto.AuthTokenView;
import com.david.admin.dto.PageResult;
import com.david.admin.entity.AuthToken;
import com.david.core.exception.BusinessException;
import com.david.admin.mapper.AuthTokenMapper;
import com.david.core.forward.ForwardedUser;
import com.david.core.security.AuditAction;
import com.david.core.security.SecurityAuditRecord;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthTokenManagementService {

    private final AuthTokenMapper authTokenMapper;
    private final AuditTrailService auditTrailService;

    public AuthTokenManagementService(AuthTokenMapper authTokenMapper, AuditTrailService auditTrailService) {
        this.authTokenMapper = authTokenMapper;
        this.auditTrailService = auditTrailService;
    }

    public PageResult<AuthTokenView> listTokens(
            int page,
            int size,
            String keyword,
            Long userId,
            String kind,
            Boolean revoked,
            LocalDate createdAtStart,
            LocalDate createdAtEnd,
            LocalDate expiresAtStart,
            LocalDate expiresAtEnd) {
        Page<AuthToken> pager = new Page<>(page, size);
        LambdaQueryWrapper<AuthToken> query = Wrappers.lambdaQuery(AuthToken.class);
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(
                    wrapper -> {
                        wrapper.like(AuthToken::getKind, trimmed);
                        try {
                            long numeric = Long.parseLong(trimmed);
                            wrapper.or().eq(AuthToken::getId, numeric).or().eq(AuthToken::getUserId, numeric);
                        } catch (NumberFormatException ignored) {
                            // 非数字关键字，仅匹配 kind
                        }
                    });
        }
        if (userId != null) {
            query.eq(AuthToken::getUserId, userId);
        }
        if (kind != null && !kind.isBlank()) {
            query.eq(AuthToken::getKind, kind);
        }
        if (revoked != null) {
            query.eq(AuthToken::getRevoked, revoked);
        }
        if (createdAtStart != null) {
            query.ge(AuthToken::getCreatedAt, createdAtStart.atStartOfDay());
        }
        if (createdAtEnd != null) {
            query.lt(AuthToken::getCreatedAt, createdAtEnd.plusDays(1).atStartOfDay());
        }
        if (expiresAtStart != null) {
            query.ge(AuthToken::getExpiresAt, expiresAtStart.atStartOfDay());
        }
        if (expiresAtEnd != null) {
            query.lt(AuthToken::getExpiresAt, expiresAtEnd.plusDays(1).atStartOfDay());
        }
        query.orderByDesc(AuthToken::getCreatedAt);
        Page<AuthToken> result = authTokenMapper.selectPage(pager, query);
        List<AuthToken> tokens = result.getRecords();
        List<AuthTokenView> items =
                tokens == null || tokens.isEmpty()
                        ? List.of()
                        : tokens.stream().map(this::toView).toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public void revokeToken(ForwardedUser principal, Long tokenId) {
        AuthToken token = authTokenMapper.selectById(tokenId);
        if (token == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "令牌不存在");
        }
        LambdaUpdateWrapper<AuthToken> update = Wrappers.lambdaUpdate(AuthToken.class)
                .eq(AuthToken::getId, tokenId)
                .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
        if (principal != null) {
            auditTrailService.record(
                    SecurityAuditRecord.builder()
                            .actorId(principal.id())
                            .actorUsername(principal.username())
                            .action(AuditAction.TOKEN_REVOKED)
                            .objectType("auth_token")
                            .objectId(String.valueOf(tokenId))
                            .description("平台管理员撤销令牌")
                            .build());
        }
    }

    private AuthTokenView toView(AuthToken token) {
        return new AuthTokenView(
                token.getId(),
                token.getUserId(),
                token.getKind(),
                Boolean.TRUE.equals(token.getRevoked()),
                token.getCreatedAt(),
                token.getExpiresAt());
    }
}
