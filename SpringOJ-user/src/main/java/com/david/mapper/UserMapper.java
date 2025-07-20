package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.entity.role.Role;
import com.david.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author david
 * @since 2025-07-20
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<Role> findRolesByUserId(@Param("userId") Long userId);
}