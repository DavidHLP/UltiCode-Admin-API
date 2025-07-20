package com.david.entity.role;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("role")
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;         // BIGINT 主键
    private String roleName; // 角色名称
    private Integer status;  // 状态
    private String remark;   // 备注信息
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
