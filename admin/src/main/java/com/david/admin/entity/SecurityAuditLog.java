package com.david.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("security_audit_logs")
public class SecurityAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("actor_id")
    private Long actorId;

    @TableField("actor_username")
    private String actorUsername;

    private String action;

    @TableField("object_type")
    private String objectType;

    @TableField("object_id")
    private String objectId;

    private String description;

    @TableField("diff_snapshot")
    private String diffSnapshot;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
