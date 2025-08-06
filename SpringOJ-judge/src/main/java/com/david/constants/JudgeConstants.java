package com.david.constants;

/**
 * 判题系统常量定义 - 统一管理所有判题相关的常量
 * 
 * @author David
 */
public final class JudgeConstants {
    
    private JudgeConstants() {
        // 工具类，禁止实例化
    }
    
    /**
     * 响应状态码
     */
    public static final class ResponseCode {
        public static final int SUCCESS = 200;
        public static final int ERROR = 500;
        
        private ResponseCode() {}
    }
    
    /**
     * 默认配置值
     */
    public static final class DefaultConfig {
        /** 默认时间限制（毫秒） */
        public static final int TIME_LIMIT_MS = 5000;
        
        /** 默认内存限制（MB） */
        public static final int MEMORY_LIMIT_MB = 256;
        
        /** 默认分数 */
        public static final int DEFAULT_SCORE = 10;
        
        /** 默认CPU核心数 */
        public static final long CPU_COUNT = 1L;
        
        /** 默认进程数限制 */
        public static final long PID_LIMIT = 64L;
        
        private DefaultConfig() {}
    }
    
    /**
     * 文件相关常量
     */
    public static final class FileConfig {
        /** Java源文件后缀 */
        public static final String JAVA_SUFFIX = ".java";
        
        /** 解决方案文件名前缀 */
        public static final String SOLUTION_PREFIX = "Solution";
        
        /** 主类文件名前缀 */
        public static final String MAIN_PREFIX = "Main";
        
        /** 临时目录前缀 */
        public static final String TEMP_DIR_PREFIX = "submission_";
        
        /** 编译输出目录 */
        public static final String COMPILE_OUTPUT_DIR = "/tmp/classes";
        
        private FileConfig() {}
    }
    
    /**
     * Docker相关常量
     */
    public static final class DockerConfig {
        /** Java Docker镜像 */
        public static final String JAVA_IMAGE = "openjdk:17-alpine";
        
        /** Ubuntu Docker镜像 */
        public static final String UBUNTU_IMAGE = "ubuntu:20.04";
        
        /** 容器内应用目录 */
        public static final String CONTAINER_APP_DIR = "/app";
        
        /** 内存监控脚本容器路径 */
        public static final String MEM_SCRIPT_CONTAINER_PATH = "/script/mem.sh";
        
        /** 网络模式 */
        public static final String NETWORK_MODE = "none";
        
        private DockerConfig() {}
    }
    
    /**
     * Java语言特定常量
     */
    public static final class JavaConfig {
        /** 默认导入语句 */
        public static final String DEFAULT_IMPORTS = """
                import java.util.*;
                import java.io.*;
                import java.math.*;
                
                """;
        
        /** 编译命令模板 */
        public static final String COMPILE_COMMAND = "mkdir -p /tmp/classes && javac -d /tmp/classes /app/*.java";
        
        /** 执行命令前缀 */
        public static final String EXECUTE_COMMAND_PREFIX = "java";
        
        /** JVM内存参数模板 */
        public static final String JVM_MEMORY_PARAM = "-Xmx%dm";
        
        /** 类路径参数 */
        public static final String CLASSPATH_PARAM = "-cp";
        
        /** 主类名 */
        public static final String MAIN_CLASS = "Main";
        
        private JavaConfig() {}
    }
    
    /**
     * 错误消息模板
     */
    public static final class ErrorMessages {
        public static final String UNSUPPORTED_LANGUAGE = "不支持的编程语言: %s";
        public static final String SUBMISSION_NOT_FOUND = "提交记录不存在: %d";
        public static final String PROBLEM_NOT_FOUND = "题目不存在: %d";
        public static final String TEST_CASES_NOT_FOUND = "题目缺少测试用例: %d";
        public static final String CODE_TEMPLATE_NOT_FOUND = "题目缺少代码模板: %d";
        public static final String CREATE_SUBMISSION_FAILED = "创建提交记录失败";
        public static final String COMPILATION_FAILED = "编译失败: %s";
        public static final String EXECUTION_TIMEOUT = "执行超时: 超过 %d ms";
        public static final String MEMORY_EXCEEDED = "内存超限: 超过 %d MB";
        public static final String OUTPUT_MISMATCH = "输出不匹配. 期望: %s, 实际: %s";
        public static final String CONTAINER_CLEANUP_FAILED = "清理容器失败: %s";
        public static final String TEMP_DIR_CLEANUP_FAILED = "清理临时目录失败: %s";
        public static final String MEM_SCRIPT_NOT_FOUND = "无法找到内存监控脚本: %s";
        
        private ErrorMessages() {}
    }
    
    /**
     * 日志消息模板
     */
    public static final class LogMessages {
        public static final String JUDGE_REQUEST_SENT = "判题请求已发送到沙箱: submissionId={}, problemId={}, language={}";
        public static final String JUDGE_FAILED = "判题失败: submissionId={}";
        public static final String TEMP_DIR_CREATED = "创建临时目录: {}";
        public static final String SOURCE_CODE_WRITTEN = "写入用户源代码文件: {}";
        public static final String MAIN_WRAPPER_WRITTEN = "写入Main包装文件: {}";
        public static final String CONTAINER_CREATED = "创建容器成功: {}";
        public static final String CONTAINER_STARTED = "启动容器成功: {}";
        public static final String COMPILATION_SUCCESS = "编译成功: {}";
        public static final String EXECUTION_COMPLETED = "执行完成: containerId={}, timeUsed={}ms";
        public static final String CONTAINER_CLEANED = "清理容器成功: {}";
        public static final String TEMP_DIR_CLEANED = "清理临时目录成功: {}";
        public static final String MEMORY_SCRIPT_PATH = "内存监控脚本路径: {}";
        
        private LogMessages() {}
    }
}
