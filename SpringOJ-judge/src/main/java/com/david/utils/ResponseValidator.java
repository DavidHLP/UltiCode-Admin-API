package com.david.utils;

import com.david.constants.JudgeConstants;
import com.david.exception.JudgeException;
import lombok.extern.slf4j.Slf4j;

/**
 * 响应验证工具类 - 统一处理服务调用响应的验证逻辑
 * 
 * @author David
 */
@Slf4j
public final class ResponseValidator {
    
    private ResponseValidator() {
        // 工具类，禁止实例化
    }
    
    /**
     * 验证响应是否有效
     * 
     * @param response 响应对象
     * @return true 如果响应有效，false 如果响应无效
     */
    public static boolean isValid(ResponseResult<?> response) {
        return response != null 
            && response.getCode() == JudgeConstants.ResponseCode.SUCCESS;
    }
    
    /**
     * 验证响应是否有效且数据不为空
     * 
     * @param response 响应对象
     * @return true 如果响应有效且数据不为空
     */
    public static boolean isValidWithData(ResponseResult<?> response) {
        return isValid(response) && response.getData() != null;
    }
    
    /**
     * 验证响应并在无效时抛出异常（仅验证状态码）
     * 
     * @param response 响应对象
     * @param errorMessage 错误消息
     * @throws JudgeException 当响应无效时
     */
    public static void validateOrThrow(ResponseResult<?> response, String errorMessage) {
        if (!isValid(response)) {
            log.error("响应验证失败: {}, response: {}", errorMessage, response);
            throw new JudgeException(errorMessage);
        }
    }
    
    /**
     * 验证响应并在无效或数据为空时抛出异常
     * 
     * @param response 响应对象
     * @param errorMessage 错误消息
     * @throws JudgeException 当响应无效或数据为空时
     */
    public static void validateWithDataOrThrow(ResponseResult<?> response, String errorMessage) {
        if (!isValidWithData(response)) {
            log.error("响应验证失败: {}, response: {}", errorMessage, response);
            throw new JudgeException(errorMessage);
        }
    }
    
    /**
     * 验证响应并在无效时抛出异常（带提交ID，仅验证状态码）
     * 
     * @param response 响应对象
     * @param errorMessage 错误消息
     * @param submissionId 提交ID
     * @throws JudgeException 当响应无效时
     */
    public static void validateOrThrow(ResponseResult<?> response, String errorMessage, Long submissionId) {
        if (!isValid(response)) {
            log.error("响应验证失败: {}, submissionId: {}, response: {}", errorMessage, submissionId, response);
            throw new JudgeException(errorMessage, submissionId);
        }
    }
    
    /**
     * 验证响应并在无效或数据为空时抛出异常（带提交ID）
     * 
     * @param response 响应对象
     * @param errorMessage 错误消息
     * @param submissionId 提交ID
     * @throws JudgeException 当响应无效或数据为空时
     */
    public static void validateWithDataOrThrow(ResponseResult<?> response, String errorMessage, Long submissionId) {
        if (!isValidWithData(response)) {
            log.error("响应验证失败: {}, submissionId: {}, response: {}", errorMessage, submissionId, response);
            throw new JudgeException(errorMessage, submissionId);
        }
    }
    
    /**
     * 获取验证后的响应数据
     * 
     * @param response 响应对象
     * @param errorMessage 错误消息
     * @param <T> 数据类型
     * @return 响应数据
     * @throws JudgeException 当响应无效或数据为空时
     */
    public static <T> T getValidatedData(ResponseResult<T> response, String errorMessage) {
        validateWithDataOrThrow(response, errorMessage);
        return response.getData();
    }
    
    /**
     * 获取验证后的响应数据（带提交ID）
     * 
     * @param response 响应对象
     * @param errorMessage 错误消息
     * @param submissionId 提交ID
     * @param <T> 数据类型
     * @return 响应数据
     * @throws JudgeException 当响应无效或数据为空时
     */
    public static <T> T getValidatedData(ResponseResult<T> response, String errorMessage, Long submissionId) {
        validateWithDataOrThrow(response, errorMessage, submissionId);
        return response.getData();
    }
    
    /**
     * 验证列表响应是否有效且非空
     * 
     * @param response 响应对象
     * @param errorMessage 错误消息
     * @throws JudgeException 当响应无效或数据为空时
     */
    public static void validateListNotEmpty(ResponseResult<?> response, String errorMessage) {
        validateWithDataOrThrow(response, errorMessage);
        
        if (response.getData() instanceof java.util.Collection<?> collection && collection.isEmpty()) {
            log.error("列表响应为空: {}", errorMessage);
            throw new JudgeException(errorMessage);
        }
    }
}
