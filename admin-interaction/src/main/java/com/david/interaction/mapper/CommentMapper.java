package com.david.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.interaction.entity.Comment;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {}

