package com.david.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user_security_profiles")
public class UserSecurityProfile {

    @TableId
    @TableField("user_id")
    private Long userId;

    @TableField("mfa_secret")
    private String mfaSecret;

    @TableField("mfa_enabled")
    private Boolean mfaEnabled;

    @TableField("sso_binding")
    private String ssoBinding;

    @TableField("last_mfa_verified_at")
    private LocalDateTime lastMfaVerifiedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
