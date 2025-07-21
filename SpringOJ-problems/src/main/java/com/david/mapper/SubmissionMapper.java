package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.Submission;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  提交记录Mapper 接口
 * </p>
 *
 * @author david
 * @since 2025-07-22
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

}
