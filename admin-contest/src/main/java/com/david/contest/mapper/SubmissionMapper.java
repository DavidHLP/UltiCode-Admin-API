package com.david.contest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.contest.entity.Submission;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {}
