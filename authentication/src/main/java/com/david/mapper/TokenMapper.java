package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.entity.token.Token;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TokenMapper extends BaseMapper<Token> {
    Token findValidToken(@Param("userId") Long userId, @Param("token") String token);
}