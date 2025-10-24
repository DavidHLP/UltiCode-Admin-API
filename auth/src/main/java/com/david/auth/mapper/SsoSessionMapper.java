package com.david.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.auth.entity.SsoSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SsoSessionMapper extends BaseMapper<SsoSession> {}
