package com.david.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.pojo.SolutionComments;
import com.david.vo.SolutionCommentVo;

public interface ISolutionCommentService extends IService<SolutionComments> {
	List<SolutionCommentVo> getCommentsBySolutionId(Long solutionId);
}
