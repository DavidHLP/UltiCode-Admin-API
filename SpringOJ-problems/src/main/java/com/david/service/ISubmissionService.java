package com.david.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.Submission;
import com.david.vo.SubmissionVo;

/**
 * <p>
 *  提交记录服务类
 * </p>
 *
 * @author david
 * @since 2025-07-22
 */
public interface ISubmissionService extends IService<Submission> {

    List<SubmissionVo> getSubmissionsByProblemId(Long problemId);
}
