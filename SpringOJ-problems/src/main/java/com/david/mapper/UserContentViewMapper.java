package com.david.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.solution.UserContentView;

@Mapper
public interface UserContentViewMapper extends BaseMapper<UserContentView> {
	UserContentView getUserContentViews(@Param("userId") Long userId, @Param("contentId") Long contentId);
}
