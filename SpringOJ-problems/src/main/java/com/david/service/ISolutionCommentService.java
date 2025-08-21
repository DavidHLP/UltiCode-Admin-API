package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.solution.SolutionComments;
import com.david.solution.vo.SolutionCommentVo;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
public interface ISolutionCommentService extends IService<SolutionComments> {
    List<SolutionCommentVo> getSolutionCommentVos(@NotNull @Min(1) Long solutionId);
}
