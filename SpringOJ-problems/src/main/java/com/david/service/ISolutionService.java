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
    SolutionDetailVo getSolutionDetailVoBy(@NotNull(message = "题解ID不能为空") @Min(value = 1, message = "题解ID必须>=1") Long solutionId,
                                           @Min(value = 1, message = "用户ID必须>=1") Long userId);

    Page<SolutionCardVo> pageSolutionCardVos(@NotNull(message = "分页对象不能为空") Page<SolutionCardVo> p,
                                             @NotNull(message = "题目ID不能为空") @Min(value = 1, message = "题目ID必须>=1") Long problemId,
                                             @Size(max = 100, message = "关键词长度不能超过100字符") String keyword);
    Page<SolutionCardVo> pageSolutionCardVosByUserId(@NotNull(message = "分页对象不能为空") Page<SolutionCardVo> p,
                                                     @NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须>=1") Long userId);

    Page<SolutionManagementCardVo> pageSolutionManagementCardVo(@NotNull(message = "分页对象不能为空") Page<SolutionManagementCardVo> page,
                                                                @Min(value = 1, message = "题目ID必须>=1") Long problemId,
                                                                @Size(max = 100, message = "关键词长度不能超过100字符") String keyword,
                                                                @Min(value = 1, message = "用户ID必须>=1") Long userId,
                                                                SolutionStatus status);
}
