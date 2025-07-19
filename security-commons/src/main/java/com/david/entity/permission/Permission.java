package com.david.entity.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体类
 * @author david
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Permission implements Serializable {
    private Long id;
    private Boolean status;
    private String remark;
    private String permission;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}