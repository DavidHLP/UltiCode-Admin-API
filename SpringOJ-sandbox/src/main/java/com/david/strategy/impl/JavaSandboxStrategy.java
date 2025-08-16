package com.david.strategy.impl;

import com.david.chain.handler.Java.JavaCompareHandler;
import com.david.chain.handler.Java.JavaCompileHandler;
import com.david.chain.handler.Java.JavaFormatCodeHandler;
import com.david.chain.handler.Java.JavaRunHandler;
import com.david.chain.utils.JudgmentContext;
import com.david.strategy.Strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JavaSandboxStrategy implements Strategy {
    private final JavaFormatCodeHandler formatHandler;

    private final JavaCompileHandler compileHandler;

    private final JavaRunHandler runHandler;

    private final JavaCompareHandler compareHandler;

    /**
     * 在 Bean 初始化完成后装配责任链：格式化 -> 编译 -> 运行 -> 对比。
     * 避免在并发执行时重复修改处理器的 nextHandler 引用，提升线程安全性与性能。
     */
    @PostConstruct
    public void initChain() {
        formatHandler.setNextHandler(compileHandler);
        compileHandler.setNextHandler(runHandler);
        runHandler.setNextHandler(compareHandler);
        compareHandler.setNextHandler(null);
    }

    /**
     * 执行 Java 评测流程（策略 + 责任链）：
     * 1) JavaFormatCodeHandler：根据测试用例与函数签名生成可运行的 Main + Solution 源码，写入到 {@link JudgmentContext#setRunCode(String)}
     * 2) JavaCompileHandler：编译生成的源码，编译诊断写入 {@link JudgmentContext#setCompileInfo(String)}
     * 3) JavaRunHandler：运行 Main，记录 {@link JudgmentContext#setTimeUsed(String)}、{@link JudgmentContext#setMemoryUsed(String)}，并在 {@link JudgmentContext#setJudgeInfo(String)} 中写入“是否通过=...”
     * 4) JavaCompareHandler：综合时间/内存限制与输出结果给出最终对比结论，更新 {@link JudgmentContext#setJudgeInfo(String)}
     *
     * @param judgmentContext 评测上下文，需包含题目、提交、语言、函数名、测试用例等信息
     * @return 是否评测通过（所有环节成功且满足限制）
     */
    @Override
    public Boolean execute(JudgmentContext judgmentContext) {
        // 为空直接判失败，避免 NPE
        if (judgmentContext == null) {
            return false;
        }

        try {
            // 启动责任链
            return formatHandler.handleRequest(judgmentContext);
        } catch (Exception e) {
            // 兜底：捕获链路异常并在上下文中记录
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            judgmentContext.setJudgeInfo("评测流程异常: " + msg);
            return false;
        }
    }
}
