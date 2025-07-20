package com.david.service;

public interface JudgeService {
    /**
     * 执行判题
     * @param submissionId 提交记录ID
     */
    void doJudge(long submissionId);
}
