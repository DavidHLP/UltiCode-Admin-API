package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.solution.SolutionComments;
import com.david.vo.SolutionCommentVo;

import java.util.List;

public interface ISolutionCommentService extends IService<SolutionComments> {
	List<SolutionCommentVo> getSolutionCommentVos(Long solutionId);
}
