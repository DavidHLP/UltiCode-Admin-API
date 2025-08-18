package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.usercontent.UserContentView;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserContentViewMapper extends BaseMapper<UserContentView> {
    Boolean userHasViewedContent(@Param("userId") Long userId, @Param("contentId") Long contentId);
}
