package com.david.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.admin.dto.AuditLogView;
import com.david.admin.dto.PageResult;
import com.david.admin.entity.SecurityAuditLog;
import com.david.admin.mapper.SecurityAuditLogMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditLogQueryService {

    private final SecurityAuditLogMapper securityAuditLogMapper;

    public AuditLogQueryService(SecurityAuditLogMapper securityAuditLogMapper) {
        this.securityAuditLogMapper = securityAuditLogMapper;
    }

    public PageResult<AuditLogView> pageAuditLogs(
            int page, int size, String action, String keyword) {
        Page<SecurityAuditLog> pager = new Page<>(page, size);
        LambdaQueryWrapper<SecurityAuditLog> query = Wrappers.lambdaQuery(SecurityAuditLog.class);
        if (action != null && !action.isBlank()) {
            query.eq(SecurityAuditLog::getAction, action);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(
                    wrapper ->
                            wrapper.like(SecurityAuditLog::getObjectId, trimmed)
                                    .or()
                                    .like(SecurityAuditLog::getDescription, trimmed)
                                    .or()
                                    .like(SecurityAuditLog::getActorUsername, trimmed));
        }
        query.orderByDesc(SecurityAuditLog::getCreatedAt);
        Page<SecurityAuditLog> result = securityAuditLogMapper.selectPage(pager, query);
        List<SecurityAuditLog> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(
                    List.of(), result.getTotal(), result.getCurrent(), result.getSize());
        }
        List<AuditLogView> items = records.stream().map(this::toView).toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private AuditLogView toView(SecurityAuditLog log) {
        return new AuditLogView(
                log.getId(),
                log.getActorId(),
                log.getActorUsername(),
                log.getAction(),
                log.getObjectType(),
                log.getObjectId(),
                log.getDescription(),
                log.getIpAddress(),
                log.getCreatedAt());
    }
}
