package com.david.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.auth.entity.SecurityAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecurityAuditLogMapper extends BaseMapper<SecurityAuditLog> {}
