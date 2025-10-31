package com.david.contest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.contest.entity.User;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
