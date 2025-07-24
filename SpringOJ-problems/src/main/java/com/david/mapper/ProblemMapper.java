package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.QuestionBankItemDto;
import com.david.dto.QuestionBankQueryDto;
import com.david.judge.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {
    Page<QuestionBankItemDto> findProblemsForQuestionBank(Page<QuestionBankItemDto> page, @Param("query") QuestionBankQueryDto query, @Param("userId") Long userId);
}
