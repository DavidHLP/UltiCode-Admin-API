package com.david.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对应数据库中的 role 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
    private Long id;         // BIGINT 主键
    private String roleName; // 角色名称
    private Integer status;  // 状态
    private String remark;   // 备注信息
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
