package com.david.exception;

import com.david.judge.enums.JudgeStatus;
import lombok.Getter;

/**
 * 判题专用异常类 - 提供统一的异常处理机制
 * 
 * @author David
 */
@Getter
public class JudgeException extends RuntimeException {
    
    private final JudgeStatus status;
    private final Long submissionId;
    private final String errorCode;
    
    public JudgeException(String message) {
        this(message, JudgeStatus.SYSTEM_ERROR, null, null);
    }
    
    public JudgeException(String message, JudgeStatus status) {
        this(message, status, null, null);
    }
    
    public JudgeException(String message, Long submissionId) {
        this(message, JudgeStatus.SYSTEM_ERROR, submissionId, null);
    }
    
    public JudgeException(String message, JudgeStatus status, Long submissionId) {
        this(message, status, submissionId, null);
    }
    
    public JudgeException(String message, JudgeStatus status, Long submissionId, String errorCode) {
        super(message);
        this.status = status;
        this.submissionId = submissionId;
        this.errorCode = errorCode;
    }
    
    public JudgeException(String message, Throwable cause) {
        this(message, cause, JudgeStatus.SYSTEM_ERROR, null, null);
    }
    
    public JudgeException(String message, Throwable cause, JudgeStatus status, Long submissionId, String errorCode) {
        super(message, cause);
        this.status = status;
        this.submissionId = submissionId;
        this.errorCode = errorCode;
    }
    
    /**
     * 创建语言不支持异常
     */
    public static JudgeException unsupportedLanguage(String language, Long submissionId) {
        return new JudgeException(
            String.format("不支持的编程语言: %s", language),
            JudgeStatus.SYSTEM_ERROR,
            submissionId,
            "UNSUPPORTED_LANGUAGE"
        );
    }
    
    /**
     * 创建数据获取失败异常
     */
    public static JudgeException dataNotFound(String dataType, Long id) {
        return new JudgeException(
            String.format("%s不存在: %d", dataType, id),
            JudgeStatus.SYSTEM_ERROR,
            null,
            "DATA_NOT_FOUND"
        );
    }
    
    /**
     * 创建编译失败异常
     */
    public static JudgeException compilationFailed(String message, Long submissionId) {
        return new JudgeException(
            String.format("编译失败: %s", message),
            JudgeStatus.COMPILE_ERROR,
            submissionId,
            "COMPILATION_FAILED"
        );
    }
    
    /**
     * 创建执行超时异常
     */
    public static JudgeException executionTimeout(Long submissionId, int timeLimit) {
        return new JudgeException(
            String.format("执行超时: 超过 %d ms", timeLimit),
            JudgeStatus.TIME_LIMIT_EXCEEDED,
            submissionId,
            "EXECUTION_TIMEOUT"
        );
    }
    
    /**
     * 创建内存超限异常
     */
    public static JudgeException memoryLimitExceeded(Long submissionId, int memoryLimit) {
        return new JudgeException(
            String.format("内存超限: 超过 %d MB", memoryLimit),
            JudgeStatus.MEMORY_LIMIT_EXCEEDED,
            submissionId,
            "MEMORY_LIMIT_EXCEEDED"
        );
    }
}
