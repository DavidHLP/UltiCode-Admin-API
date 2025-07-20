package com.david.domain.enums;

public enum SubmissionStatus {
    PENDING("待处理"),
    JUDGING("判题中"),
    ACCEPTED("答案正确"),
    WRONG_ANSWER("答案错误"),
    TIME_LIMIT_EXCEEDED("超出时间限制"),
    MEMORY_LIMIT_EXCEEDED("超出内存限制"),
    RUNTIME_ERROR("运行时错误"),
    COMPILE_ERROR("编译错误"),
    SYSTEM_ERROR("系统错误");

    private final String status;

    SubmissionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
