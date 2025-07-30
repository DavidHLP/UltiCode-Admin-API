package com.david.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.Submission;
import com.david.mapper.SubmissionMapper;
import com.david.service.ISubmissionService;
import com.david.vo.SubmissionVo;

/**
 * <p>
 *  提交记录服务实现类
 * </p>
 *
 * @author david
 * @since 2025-07-22
 */
@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements ISubmissionService {

    @Override
    public List<SubmissionVo> getSubmissionsByProblemId(Long problemId) {
        return this.lambdaQuery()
                .eq(Submission::getProblemId, problemId)
                .list()
                .stream()
                .map(submission -> {
                    SubmissionVo dto = new SubmissionVo();
                    org.springframework.beans.BeanUtils.copyProperties(submission, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
