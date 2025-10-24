package com.david.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.auth.entity.BlacklistedIp;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlacklistedIpMapper extends BaseMapper<BlacklistedIp> {}
