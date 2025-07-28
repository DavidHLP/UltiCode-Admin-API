package com.david.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.dto.SubmissionDto;
import com.david.judge.Submission;

/**
 * <p>
 *  提交记录服务类
 * </p>
 *
 * @author david
 * @since 2025-07-22
 */
public interface ISubmissionService extends IService<Submission> {

    List<SubmissionDto> getSubmissionsByProblemId(Long problemId);
}
