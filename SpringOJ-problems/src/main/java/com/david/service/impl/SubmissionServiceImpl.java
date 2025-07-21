package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.Submission;
import com.david.mapper.SubmissionMapper;
import com.david.service.ISubmissionService;
import org.springframework.stereotype.Service;

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

}
