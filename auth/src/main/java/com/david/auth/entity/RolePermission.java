package com.david.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("role_permissions")
public class RolePermission {

    @TableField("role_id")
    private Long roleId;

    @TableField("perm_id")
    private Long permissionId;
}
