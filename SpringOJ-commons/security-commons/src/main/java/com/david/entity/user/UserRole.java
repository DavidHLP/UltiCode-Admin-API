package com.david.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_role")
public class UserRole implements Serializable {
    private Long userId;
    private Long roleId;
}
