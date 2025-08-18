package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.solution.Solution;
import com.david.solution.vo.SolutionCardVo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 题解Mapper 接口
 *
 * @author david
 * @since 2025-07-28
 */
@Mapper
public interface SolutionMapper extends BaseMapper<Solution> {
    Page<SolutionCardVo> pageSolutionsCardVos(
            Page<SolutionCardVo> page,
            @Param("problemId") Long problemId,
            @Param("keyword") String keyword);

    Boolean updateViews(@Param("id") Long id);
}
