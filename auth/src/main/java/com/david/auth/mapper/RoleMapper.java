package com.david.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
