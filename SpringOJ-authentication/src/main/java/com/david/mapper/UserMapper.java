package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.entity.user.AuthUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<AuthUser> {
    AuthUser loadUserByUsername(String username);
}