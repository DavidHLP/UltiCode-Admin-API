package com.david.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.entity.role.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@TableName("user")
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    private String username;
    private String avatar;
    @Default
    private String introduction = "用户未填写";
    private String email;
    private Integer status;
    @Default
    private String address = "用户未填写";
    private String lastLoginIp;
    private LocalDateTime lastLogin;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;
    private String password;
    @TableField(exist = false)
    private List<Role> roles;
}