package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.solution.Solution;
import com.david.solution.enums.SolutionStatus;
import com.david.solution.vo.SolutionCardVo;
import com.david.solution.vo.SolutionDetailVo;
import com.david.solution.vo.SolutionManagementCardVo;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * 题解服务类
 * </p>
 *
 * @author david
 * @since 2025-07-28
 */
@Validated
public interface ISolutionService extends IService<Solution> {
    SolutionDetailVo getSolutionDetailVoBy(@NotNull @Min(1) Long solutionId , @NotNull @Min(1) Long userId);

    Page<SolutionCardVo> pageSolutionCardVos(@NotNull Page<SolutionCardVo> p,
                                             @NotNull @Min(1) Long problemId,
                                             @Size(max = 100) String keyword);
    Page<SolutionCardVo> pageSolutionCardVosByUserId(@NotNull Page<SolutionCardVo> p,
                                                     @NotNull @Min(1) Long userId);

    Page<SolutionManagementCardVo> pageSolutionManagementCardVo(@NotNull Page<SolutionManagementCardVo> page,
                                                                @Min(1) Long problemId,
                                                                @Size(max = 100) String keyword,
                                                                @Min(1) Long userId,
                                                                SolutionStatus status);
}
