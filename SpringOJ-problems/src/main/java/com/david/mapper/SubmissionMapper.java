package com.david.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.Submission;
import com.david.vo.SubmissionCalendarVo;

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

    List<SubmissionCalendarVo> getSubmissionCalendar(@Param("userId") Long userId);

}
