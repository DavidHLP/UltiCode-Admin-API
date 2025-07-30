package com.david.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.ProblemBankQueryDto;
import com.david.judge.Problem;
import com.david.vo.ProblemBankItemVo;

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
    Page<ProblemBankItemVo> findProblemsForQuestionBank(Page<ProblemBankItemVo> page, @Param("query") ProblemBankQueryDto query, @Param("userId") Long userId);
}
