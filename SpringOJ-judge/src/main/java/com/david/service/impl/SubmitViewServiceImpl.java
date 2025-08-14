package com.david.service.impl;

import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.service.ISubmitViewService;
import com.david.submission.dto.SubmitCodeRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * 判题服务实现类 - 优雅重构版本，使用责任链模式处理判题流程
 * 
 * @author David
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitViewServiceImpl implements ISubmitViewService {

	private final SubmissionServiceFeignClient submissionServiceFeignClient;

	@Override
	public Long submitAndJudge(SubmitCodeRequest request, Long userId) {
		return null;
	}
}
