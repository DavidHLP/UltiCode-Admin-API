package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.solution.Solution;
import com.david.solution.vo.SolutionCardVo;
import com.david.solution.vo.SolutionManagementCardVo;
import com.david.solution.enums.SolutionStatus;

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

    Solution selectApprovedById(@Param("id") Long id);

    Boolean updateViews(@Param("id") Long id);
    Page<SolutionCardVo> pageSolutionCardVosByUserId( Page<SolutionCardVo> page, @Param("userId") Long userId);

    Page<SolutionManagementCardVo> pageSolutionManagementCardVos(
            Page<SolutionManagementCardVo> page,
            @Param("problemId") Long problemId,
            @Param("keyword") String keyword,
            @Param("userId") Long userId,
            @Param("status") SolutionStatus status);
    Long getViews(@Param("id") Long id);
}
