package com.david.service;

import com.david.dto.JudgeResult;
import com.david.dto.SubmitCodeRequest;

/**
 * 判题服务接口
 */
public interface IJudgeService {
    /**
     * 执行判题
     */
    void judge(Long submissionId);
    
    /**
     * 异步判题
     */
    void judgeAsync(SubmitCodeRequest request, Long submissionId);
}
