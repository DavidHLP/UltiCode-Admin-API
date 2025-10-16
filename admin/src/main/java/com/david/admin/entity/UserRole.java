package com.david.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user_roles")
public class UserRole {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    @TableField("role_id")
    private Long roleId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

