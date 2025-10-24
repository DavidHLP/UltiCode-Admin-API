package com.david.auth.service;

import com.david.auth.entity.SecurityAuditLog;
import com.david.auth.mapper.SecurityAuditLogMapper;
import com.david.common.security.AuditAction;
import com.david.common.security.SecurityAuditRecord;
import com.david.common.security.SensitiveDataMasker;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final SecurityAuditLogMapper auditLogMapper;
    private final Clock clock;

    public void record(SecurityAuditRecord record) {
        if (record == null) {
            return;
        }
        SecurityAuditLog entity = new SecurityAuditLog();
        entity.setActorId(record.getActorId());
        entity.setActorUsername(record.getActorUsername());
        entity.setAction(record.getAction().name());
        entity.setObjectType(record.getObjectType());
        entity.setObjectId(record.getObjectId());
        entity.setDescription(record.getDescription());
        if (record.getDiff() != null && !record.getDiff().isEmpty()) {
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

    public void record(
            Long actorId,
            String actorUsername,
            AuditAction action,
            String objectType,
            String objectId,
            String description,
            String ipAddress) {
        SecurityAuditRecord auditRecord =
                SecurityAuditRecord.builder()
                        .actorId(actorId)
                        .actorUsername(actorUsername)
                        .action(action)
                        .objectType(objectType)
                        .objectId(objectId)
                        .description(description)
                        .ipAddress(ipAddress)
                        .timestamp(LocalDateTime.now(clock))
                        .build();
        record(auditRecord);
    }
}
