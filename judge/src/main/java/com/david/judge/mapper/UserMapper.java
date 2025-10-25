package com.david.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
