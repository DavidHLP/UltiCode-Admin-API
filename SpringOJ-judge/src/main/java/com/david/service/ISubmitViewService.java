package com.david.service;

import com.david.submission.dto.SubmitCodeRequest;

/**
 * 判题服务接口 - 简化设计，直接提供业务方法
 */
public interface ISubmitViewService {

    /**
     * 提交代码并执行判题
     * @param request 提交请求
     * @param userId 用户ID
     * @return 提交记录ID
     */
    Long submitAndJudge(SubmitCodeRequest request, Long userId);
}
