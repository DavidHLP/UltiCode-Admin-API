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
    List<SolutionCommentVo> getSolutionCommentVos(
            @NotNull(message = "题解ID不能为空")
            @Min(value = 1, message = "题解ID必须为正数") Long solutionId);
}
