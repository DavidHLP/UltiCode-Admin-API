package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.enums.CategoryType;
import com.david.enums.LanguageType;
import com.david.problem.Problem;
import com.david.problem.enums.ProblemDifficulty;
import com.david.problem.vo.ProblemCardVo;
import com.david.problem.vo.ProblemDetailVo;
import com.david.submission.dto.CompareDescription;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Validated
public interface IProblemService extends IService<Problem> {

    Page<Problem> pageProblems(@NotNull Page<Problem> page,
                               @Size(max = 100) String keyword,
                               ProblemDifficulty difficulty,
                               CategoryType category,
                               Boolean isVisible,
                               @Size(max = 20) String sort);

    Page<ProblemCardVo> pageProblemVos(@NotNull Page<Problem> page,
                                       @Size(max = 100) String keyword,
                                       ProblemDifficulty difficulty,
                                       CategoryType category,
                                       @Size(max = 20) String sort);

    ProblemDetailVo getProblemDetailVoById(@NotNull @Min(1) Long id);

    String getCodeTemplate(@NotNull @Min(1) Long problemId, @NotNull LanguageType language);

    CompareDescription getCompareDescription(@NotNull @Min(1) Long id);
}
