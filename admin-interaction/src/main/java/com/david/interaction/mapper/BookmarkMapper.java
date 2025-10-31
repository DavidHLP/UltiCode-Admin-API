package com.david.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.interaction.entity.Bookmark;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookmarkMapper extends BaseMapper<Bookmark> {}

