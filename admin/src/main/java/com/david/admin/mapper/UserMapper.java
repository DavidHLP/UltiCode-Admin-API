package com.david.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.admin.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}

