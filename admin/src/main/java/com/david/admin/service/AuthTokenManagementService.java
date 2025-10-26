package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.admin.dto.AuthTokenView;
import com.david.admin.entity.AuthToken;
import com.david.core.exception.BusinessException;
import com.david.admin.mapper.AuthTokenMapper;
import com.david.core.forward.ForwardedUser;
import com.david.core.security.AuditAction;
import com.david.core.security.SecurityAuditRecord;
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

    public List<AuthTokenView> listTokens(Long userId, String kind, Boolean revoked) {
        LambdaQueryWrapper<AuthToken> query = Wrappers.lambdaQuery(AuthToken.class);
        if (userId != null) {
            query.eq(AuthToken::getUserId, userId);
        }
        if (kind != null && !kind.isBlank()) {
            query.eq(AuthToken::getKind, kind);
        }
        if (revoked != null) {
            query.eq(AuthToken::getRevoked, revoked);
        }
        query.orderByDesc(AuthToken::getCreatedAt);
        List<AuthToken> tokens = authTokenMapper.selectList(query);
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }
        return tokens.stream().map(this::toView).toList();
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
