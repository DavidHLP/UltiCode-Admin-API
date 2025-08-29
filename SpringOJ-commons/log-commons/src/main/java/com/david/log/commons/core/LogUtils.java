package com.david.log.commons.core;

import com.david.log.commons.core.operations.BusinessLogOperations;
import com.david.log.commons.core.operations.ExceptionLogOperations;
import com.david.log.commons.core.operations.PerformanceLogOperations;
import com.david.log.commons.core.operations.SecurityLogOperations;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/// 统一日志操作工具类 - 门面入口
///
/// 提供统一的日志操作接口，支持业务、性能、安全、异常四类日志记录。
/// 采用门面模式设计，简化日志操作API，提供良好的用户体验。
///
/// 使用示例：
/// ```java
/// // 业务日志
/// LogUtils.business().audit("user123", "login", "success", "192.168.1.1");
///
/// // 性能日志
/// LogUtils.performance().timing("getUserInfo", 150, userId);
///
/// // 安全日志
/// LogUtils.security().login("user123", "password", true, "192.168.1.1", userAgent);
///
/// // 异常日志
/// LogUtils.exception().business("createUser", exception, userData);
/// ```
///
/// @author David
@Slf4j
@Component
public class LogUtils {

    private static BusinessLogOperations businessLogOperations;
    private static PerformanceLogOperations performanceLogOperations;
    private static SecurityLogOperations securityLogOperations;
    private static ExceptionLogOperations exceptionLogOperations;

    /** 构造函数 - Spring依赖注入 */
    public LogUtils(
            BusinessLogOperations businessLogOperations,
            PerformanceLogOperations performanceLogOperations,
            SecurityLogOperations securityLogOperations,
            ExceptionLogOperations exceptionLogOperations) {

        LogUtils.businessLogOperations = businessLogOperations;
        LogUtils.performanceLogOperations = performanceLogOperations;
        LogUtils.securityLogOperations = securityLogOperations;
        LogUtils.exceptionLogOperations = exceptionLogOperations;

        log.info("LogUtils统一日志组件已初始化");
    }

    /// 获取业务日志操作接口
    ///
    /// 用于记录用户操作审计、业务流程追踪、业务事件等。
    ///
    /// @return 业务日志操作接口
    public BusinessLogOperations business() {
        checkInitialization();
        return businessLogOperations;
    }

    /// 获取性能日志操作接口
    ///
    /// 用于记录方法执行时间、SQL性能、HTTP请求性能等。
    ///
    /// @return 性能日志操作接口
    public PerformanceLogOperations performance() {
        checkInitialization();
        return performanceLogOperations;
    }

    /// 获取安全日志操作接口
    ///
    /// 用于记录用户登录登出、权限检查、安全威胁等。
    ///
    /// @return 安全日志操作接口
    public SecurityLogOperations security() {
        checkInitialization();
        return securityLogOperations;
    }

    /// 获取异常日志操作接口
    ///
    /// 用于记录业务异常、系统异常、网络异常等。
    ///
    /// @return 异常日志操作接口
    public ExceptionLogOperations exception() {
        checkInitialization();
        return exceptionLogOperations;
    }

    /// 检查组件是否已正确初始化
    private void checkInitialization() {
        if (businessLogOperations == null
                || performanceLogOperations == null
                || securityLogOperations == null
                || exceptionLogOperations == null) {

            String errorMsg = "LogUtils组件未正确初始化，请确保在Spring容器中正确配置";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }
}
