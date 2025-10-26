package com.david.admin.service;

import com.david.admin.entity.SecurityAuditLog;
import com.david.admin.mapper.SecurityAuditLogMapper;
import com.david.core.security.AuditAction;
import com.david.core.security.SecurityAuditRecord;
import com.david.core.security.SensitiveDataMasker;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditTrailService {

    private final SecurityAuditLogMapper auditLogMapper;
    private final Clock clock;

    public void record(SecurityAuditRecord record) {
        if (record == null || record.getAction() == null) {
            return;
        }
        SecurityAuditLog entity = new SecurityAuditLog();
        entity.setActorId(record.getActorId());
        entity.setActorUsername(record.getActorUsername());
        entity.setAction(record.getAction().name());
        entity.setObjectType(record.getObjectType());
        entity.setObjectId(record.getObjectId());
        entity.setDescription(record.getDescription());
        if (record.getDiff() != null) {
            entity.setDiffSnapshot(record.getDiff().toString());
        }
        entity.setIpAddress(SensitiveDataMasker.maskIp(record.getIpAddress()));
        entity.setCreatedAt(record.getTimestamp() == null ? LocalDateTime.now(clock) : record.getTimestamp());
        auditLogMapper.insert(entity);
        log.info(
                "记录审计日志: action={}, objectType={}, objectId={}",
                record.getAction(),
                record.getObjectType(),
                record.getObjectId());
    }

    public void recordRoleChange(Long actorId, String actorUsername, Long roleId, String description) {
        SecurityAuditRecord record =
                SecurityAuditRecord.builder()
                        .actorId(actorId)
                        .actorUsername(actorUsername)
                        .action(AuditAction.ROLE_PERMISSION_UPDATED)
                        .objectType("role")
                        .objectId(String.valueOf(roleId))
                        .description(description)
                        .timestamp(LocalDateTime.now(clock))
                        .build();
        record(record);
    }
}
